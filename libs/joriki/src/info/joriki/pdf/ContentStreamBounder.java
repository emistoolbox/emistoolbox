package info.joriki.pdf;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.io.Util;
import info.joriki.util.Handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

public class ContentStreamBounder extends StateHandler {
	// test: add bounding boxes to all PDFs in a directory
	public static void main (String [] args) throws IOException {
		File inputDirectory = new File (args [0]);
		File outputDirectory = new File (args [1]);
		for (String filename : inputDirectory.list ())
			if (filename.endsWith (".pdf"))
				addBoundingBox (new File (inputDirectory,filename),new File (outputDirectory,filename));
	}
	
	// test: add a red bounding box to all pages in a PDF
	static void addBoundingBox (File from,File to) throws IOException {
		PDFDocument document = new PDFFile (from).getDocument ();
	    document.traversePageObjects (new Handler<PDFDictionary> () {
	      public void handle (PDFDictionary pageObject) {
	        try {
	        	Rectangle boundingBox = new ContentStreamBounder ().getBoundingBox (pageObject);
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream ();
	        	PrintStream ps = new PrintStream (baos);
	        	ps.println ("q");
	        	Util.copy (pageObject.getContentStream (),baos);
	        	ps.println ();
	        	ps.println ("Q");
	        	ps.println ("1 0 0 RG");
	        	ps.println (boundingBox.xmin + " " + boundingBox.ymin + " " + boundingBox.width () + " " + boundingBox.height () + " re s");
	        	ps.close ();
	        	PDFStream contentStream = new PDFStream (baos.toByteArray ());
	        	pageObject.putIndirect ("Contents",contentStream);
	        } catch (IOException e) {
	          e.printStackTrace ();
	          throw new Error ("couldn't parse content stream");
	        }
	      }
	    });
	    PDFWriter writer = new PDFWriter (to);
		writer.write (document);
		writer.close ();
	}

	Rectangle boundingBox;
	
	public Rectangle getBoundingBox (PDFDictionary pageObject) throws IOException {
		boundingBox = new Rectangle ();
		new ContentStreamParser (this,new ResourceResolver ()).parse (pageObject.getContentStream (),(PDFDictionary) pageObject.getInherited ("Resources"),ContentStreamTypes.ROOT);
		return boundingBox;
	}
	
	private final static Rectangle unitSquare = new Rectangle (0,0,1,1);
	
	public void drawImage (PDFImage image) throws IOException {
		super.drawImage (image);
		boundingBox.add (unitSquare.transformedBy (graphicsState.ctm));
	}
	
	public void drawForm (PDFStream formStream) {
		super.drawForm (formStream);
		throw new Error ("bounding forms not implemented");
	}
	
	public void shade (PDFShading shading) {
		super.shade (shading);
		if (shading.boundingBox != null)
			throw new Error ("bounding shading with bounding box not implemented");
		if (!(shading instanceof LinearShading))
			throw new Error ("bounding non-linear shading not implemented");
		LinearShading linearShading = (LinearShading) shading;
		if (!(linearShading.extend [0] && linearShading.extend [1]))
			throw new Error ("bounding non-extended shading not implemented");
		boundingBox.add (clipBoundingBox);
	}
	
	public void moveTo (double x,double y) {
		super.moveTo (x,y);
		addPathPoint (x,y);
	}
	
	public void lineTo (double x,double y) {
		super.lineTo (x,y);
		addPathPoint (x,y);
	}

	public void curveTo (double [] coors) {
		super.curveTo (coors);
		for (int i = 0;i < coors.length;i += 2)
			addPathPoint (coors [i],coors [i + 1]);
	}
	
	Rectangle pathBoundingBox = new Rectangle ();

	private void addPathPoint (double x,double y) {
		pathBoundingBox.add (new Point (x,y).transformedBy (graphicsState.ctm));
	}

	Rectangle clipBoundingBox = Rectangle.getInfinitePlane ();
	Stack<Rectangle> clipBoundingBoxStack = new Stack<Rectangle> ();  
	
	public void gsave () {
		super.gsave ();
		clipBoundingBoxStack.push (clipBoundingBox);
	}
	
	public void grestore () {
		super.grestore ();
		clipBoundingBox = clipBoundingBoxStack.pop ();
	}
	
	public void usePath (boolean stroke,int fillRule,int clipRule) {
		super.usePath (stroke,fillRule,clipRule);
		add (pathBoundingBox,fillRule != NOFILL,stroke);
		if (clipRule != NOFILL)
			// path bounding box was clipped by add ()
			clipBoundingBox = new Rectangle (pathBoundingBox);
		pathBoundingBox = new Rectangle ();
	}
	
	private void add (Rectangle box,boolean fill,boolean stroke) {
		if (stroke) {
			// TODO: not taking into account miter line joins
			double [] m = graphicsState.ctm.matrix;
			double xwidth = Math.sqrt (m [0] * m [0] + m [2] * m [2]) * graphicsState.lineWidth / 2;
			double ywidth = Math.sqrt (m [1] * m [1] + m [3] * m [3]) * graphicsState.lineWidth / 2;
			box.xmin -= xwidth;
			box.xmax += xwidth;
			box.ymin -= ywidth;
			box.ymax += ywidth;
		}
		box.intersectWith (clipBoundingBox);
		if (stroke || fill)
			boundingBox.add (box);
	}

	public void show (List text) {
		startShow ();
		for (Object o : text)
			if (o instanceof byte [])
				add ((byte []) o);
			else if (o instanceof PDFNumber)
				graphicsState.textState.adjustBy ((PDFNumber) o);
			else
				throw new Error ("unknown text operator argument type " + o.getClass ());
		endShow ();
	}
	
	public void show (byte [] text) {
		startShow ();
		add (text);
		endShow ();
	}

	Rectangle textBoundingBox;

	private void startShow () {
		textBoundingBox = new Rectangle ();
	}

	private void endShow () {
		TextRenderingMode mode = graphicsState.textState.renderingMode;
		add (textBoundingBox,mode.fills (),mode.strokes ());

		if (mode.clips ())
			throw new Error ("bounding clipping text not implemented");
		if (graphicsState.textState.font instanceof Type3Font)
			throw new Error ("bounding Type 3 fonts not implemented");
	}

	private void add (byte [] text) {
		boundText ();
		graphicsState.textState.advanceBy (text);
		boundText ();
	}
	
	private void boundText () {
		PDFFont font = graphicsState.textState.font;
		if (font.vertical)
			throw new Error ("text bounding for vertical fonts not tested");
		Transformation transform = new Transformation (graphicsState.textState.getTotalTextMatrix (),graphicsState.ctm);
		textBoundingBox.add (new Point (0,font.getDescent ()).transformedBy (transform));
		textBoundingBox.add (new Point (0,font.getAscent  ()).transformedBy (transform));
	}
}
