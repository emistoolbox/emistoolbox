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
		if (stroke)
			pathBoundingBox.growBy (graphicsState.lineWidth);
		pathBoundingBox.intersectWith (clipBoundingBox);
		if (stroke || fillRule != NOFILL)
			boundingBox.add (pathBoundingBox);
		if (clipRule != NOFILL)
			clipBoundingBox = new Rectangle (pathBoundingBox);
		pathBoundingBox = new Rectangle ();
	}
	
	public void show (List text) {
		for (Object o : text)
			if (o instanceof byte [])
				show ((byte []) o);
			else if (o instanceof PDFNumber)
				graphicsState.textState.adjustBy ((PDFNumber) o);
			else
				throw new Error ("unknown text operator argument type " + o.getClass ());
	}
	
	public void show (byte [] text) {
		boundText ();
		graphicsState.textState.advanceBy (text);
		boundText ();
	}
	
	private void boundText () {
		if (graphicsState.textState.renderingMode.strokes () ||
			graphicsState.textState.renderingMode.clips () ||
			!graphicsState.textState.renderingMode.fills ())
			throw new Error ("non-fill text bounding not implemented");
		
		PDFFont font = graphicsState.textState.font;
		if (font.vertical)
			throw new Error ("text bounding for vertical fonts not tested");
		Transformation transform = new Transformation (graphicsState.textState.getTotalTextMatrix (),graphicsState.ctm);
		boundingBox.add (new Point (0,font.getDescent ()).transformedBy (transform));
		boundingBox.add (new Point (0,font.getAscent  ()).transformedBy (transform));
	}
}
