package com.emistoolbox.lib.pdf;

import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.Util;
import info.joriki.pdf.ConstructiblePDFDocument;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFFile;
import info.joriki.pdf.PDFFont;
import info.joriki.pdf.PDFStream;
import info.joriki.pdf.PDFWriter;
import info.joriki.pdf.TextState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVisitor;

import es.jbauer.lib.io.IOOutput;

public class PDFLayoutRenderer implements PDFLayoutVisitor<Void> {
	ResourceRenamer resourceRenamer;
	PrintStream ps;

	public void render (List<PDFLayout> layouts,IOOutput output) throws IOException {
		ConstructiblePDFDocument document = new ConstructiblePDFDocument ();
		
		// TODO: test document with more than one page
		for (PDFLayout layout : layouts)
			render (layout,document);
		
		PDFWriter writer = new PDFWriter (output.getOutputStream ());
		writer.write (document);
		writer.close ();
	}

	private void render (PDFLayout layout,ConstructiblePDFDocument document) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		ps = new PrintStream (baos);
		resourceRenamer = new ResourceRenamer ("R");
		PDFLayoutFrame outerFrame = layout.getOuterFrame ();
		flip (outerFrame.getRectangle ());
		outerFrame.accept (this);
		ps.close ();
		PDFDictionary page = new PDFDictionary ("Page");
		page.put ("MediaBox",new PDFArray (outerFrame.getRectangle ()));
		page.putIndirect ("Contents",new PDFStream (baos.toByteArray ()));
		PDFDictionary resources = resourceRenamer.getResources ();
		PDFDictionary fonts = resources.getOrCreateDictionary ("Font");
		for (Entry<PDFLayoutFont,String> entry : fontLabels.entrySet ())
			fonts.putIndirect (entry.getValue (),getFontDictionary (entry.getKey ()));
		page.putIndirect ("Resources",resources);
		document.addPage (page);
	}

	final static Set<String> standardFontNames = new HashSet<String> ();
	static {
		standardFontNames.add (PDFLayoutFont.FONT_TIMES);
		standardFontNames.add (PDFLayoutFont.FONT_HELVETICA);
		standardFontNames.add (PDFLayoutFont.FONT_COURIER);
	}

	private PDFDictionary getFontDictionary (PDFLayoutFont layoutFont) {
		if (!standardFontNames.contains (layoutFont.getFontName ()))
			throw new Error ("only standard fonts implemented");

		PDFDictionary dictionary = new PDFDictionary ("Font");
		dictionary.put ("Subtype","Type1");

		String baseFont = layoutFont.getFontName ();
		boolean isBold = layoutFont.getFontStyle ().isBold ();
		boolean isItalic = layoutFont.getFontStyle ().isItalic ();
		boolean isTimes = baseFont.equals ("Times");
		if (isBold || isItalic) {
			baseFont += '-';
			if (isBold)
				baseFont += "Bold";
			if (isItalic)
				baseFont += isTimes ? "Italic" : "Oblique";
		}
		else if (isTimes)
			baseFont += "-Roman";

		dictionary.put ("BaseFont",baseFont);
		// TODO: using standard encoding for now -- do proper encoding

		return dictionary;
	}

	private PDFFont getPDFFont (PDFLayoutFont layoutFont) {
		return PDFFont.getInstance (getFontDictionary (layoutFont),null);
	}

	private void drawRectangle (Rectangle r) {
		outputRectangle (r);
		ps.print ("s\n");
	}

	private void outputRectangle (Rectangle r) {
		coordinateCommand ("re",r.toDoubleArray ());
	}

	private void flip (Rectangle r) {
		transform (1,0,0,-1,0,r.ymin + r.ymax);
	}

	private void transform (Rectangle from,Rectangle to) {
		transform (Transformation.matchBoxes (from,to).matrix);
	}

	private void transform (double ... m) {
		coordinateCommand ("cm",m);
	}

	private void coordinateCommand (String command,double ... m) {
		for (double v : m) {
			ps.print (toString (v));
			ps.print (' ');
		}
		ps.print (command);
		ps.print ('\n');
	}

	private void translate (double dx,double dy) {
		transform (1,0,0,1,dx,dy);
	}

	private void pushGraphicsState () {
		ps.print ("q\n");
	}

	private void popGraphicsState () {
		ps.print ("Q\n");
	}

	private String toString (double x) {
		return String.valueOf (x);
	}

	public Void visit (PDFLayoutFrame frame) throws IOException {
		Rectangle frameBox = frame.getRectangle ();
		for (PDFLayoutComponent component : frame.getComponents ()) {
			pushGraphicsState ();
			PDFLayoutContent componentContent = component.getContent ();
			PDFLayoutPlacement placement = component.getPlacement ();
			PDFLayoutObjectFit objectFit = component.getObjectFit ();
			Rectangle componentBox = getBoundingBox (componentContent);
			double width = 0;
			double height = 0;
			switch (objectFit) {
			case CONTAIN:
				double scale = Math.min (frameBox.width () / componentBox.width (),frameBox.height () / componentBox.height ()); 
				width = scale * componentBox.width ();
				height = scale * componentBox.height ();
				break;
			case NONE:
				break;
				default:
					throw new Error ("object fit " + objectFit + " not implemented");
			}
			Rectangle newComponentBox = objectFit == PDFLayoutObjectFit.NONE ? componentBox : new Rectangle (0,0,width,height);
			double x;
			double y;
			if (placement instanceof PDFLayoutCoordinatePlacement) {
				PDFLayoutCoordinatePlacement coordinatePlacement = (PDFLayoutCoordinatePlacement) placement;
				x = coordinatePlacement.getX ();
				y = coordinatePlacement.getY ();
			}
			else if (placement instanceof PDFLayoutAlignmentPlacement) {
				PDFLayoutAlignmentPlacement alignmentPlacement = (PDFLayoutAlignmentPlacement) placement;
				switch (alignmentPlacement.getHorizontalAlignment ()) {
				case LEFT:
					x = frameBox.xmin;
					break;
				case CENTER:
					x = frameBox.xmin + (frameBox.width () - newComponentBox.width ()) / 2;
					break;
				default:
					throw new Error ("horizontal placement " + alignmentPlacement.getHorizontalAlignment () + " not implemented");
				}
				switch (alignmentPlacement.getVerticalAlignment ()) {
				case TOP:
					y = frameBox.ymin;
					break;
				case CENTER:
					y = frameBox.ymin + (frameBox.height () - newComponentBox.height ()) / 2;
					break;
				default:
					throw new Error ("vertical placement " + alignmentPlacement.getVerticalAlignment () + " not implemented");
				}
			}
			else
				throw new Error ("placement " + placement.getClass () + " not implemented");
			translate (x - newComponentBox.xmin,y - newComponentBox.ymin);
			if (!newComponentBox.equals (componentBox))
				transform (componentBox,newComponentBox);
			drawRectangle (componentBox);
			// This could be done more elegantly by always flipping here and always flipping frames instead of just the outer one.
			// All frame flips except the outer one would cancel, but we'd have a lot of unnecessary cancelling transformations
			// in the output (unless we cancel them explicitly, which would defeat the purpose of elegance)
			if (!(componentContent instanceof PDFLayoutFrame))
				flip (componentBox);
			componentContent.accept (this);
			popGraphicsState ();
		}
		return null;
	}

	public Void visit (PDFLayoutPDFContent pdfContent) throws IOException {
		ps.write (resourceRenamer.rename (getPage (pdfContent)));
		return null;
	}

	public Void visit (PDFLayoutTextContent textContent) {
		PDFLayoutFont layoutFont = textContent.getFont ();
		double fontSize = layoutFont.getFontSize ();
		ps.print ("BT /" + getFontLabel (layoutFont) + " " + fontSize + " Tf 0 " + -fontSize * getPDFFont (layoutFont).getDescent () + " Td (" + textContent.getText () + ") Tj ET\n");
		return null;
	}

	private int fontIndex;
	private Map<PDFLayoutFont,String> fontLabels = new HashMap<PDFLayoutFont,String> ();

	private String getFontLabel (PDFLayoutFont layoutFont) {
		String fontLabel = fontLabels.get (layoutFont);
		if (fontLabel == null) {
			fontLabel = "F" + ++fontIndex;
			fontLabels.put (layoutFont,fontLabel);
		}
		return fontLabel;
	}

	private Rectangle getBoundingBox (PDFLayoutContent content) throws IOException {
		return content.accept (new PDFLayoutVisitor<Rectangle> () {
			public Rectangle visit (PDFLayoutFrame frame) throws IOException {
				return frame.getRectangle ();
			}

			public Rectangle visit (PDFLayoutPDFContent pdfContent) throws IOException {
				return getPage (pdfContent).getMediaBox ().toRectangle ();
			}

			public Rectangle visit (PDFLayoutTextContent textContent) {
				PDFLayoutFont layoutFont = textContent.getFont ();
				PDFFont pdfFont = getPDFFont (layoutFont);
				TextState textState = new TextState ();
				double fontSize = layoutFont.getFontSize ();
				textState.setTextFont (pdfFont,fontSize);
				double advance = textState.getAdvance (textContent.getText ().getBytes ()); // TODO: proper encoding
				double descent = fontSize * pdfFont.getDescent ();
				double ascent = fontSize * pdfFont.getAscent ();
				return new Rectangle (0,descent,advance,ascent - descent);
			}
		});
	}

	private Map<PDFLayoutPDFContent,PDFDictionary> pageMap = new HashMap<PDFLayoutPDFContent,PDFDictionary> ();

	private PDFDictionary getPage (PDFLayoutPDFContent content) throws IOException {
		PDFDictionary page = pageMap.get (content);
		if (page== null) {
			page = new PDFFile (new SeekableByteArray (Util.toByteArray (content.getInput ().getInputStream ()))).getDocument ().getPage (1);
			pageMap.put (content,page);
		}
		return page;
	}
}
