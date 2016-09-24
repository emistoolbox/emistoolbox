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

import com.emistoolbox.lib.pdf.specification.PDFLayout;
import com.emistoolbox.lib.pdf.specification.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutFont;
import com.emistoolbox.lib.pdf.specification.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.specification.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.specification.PDFLayoutPDFContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutTextContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutVisitor;

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
		flip (outerFrame.getBoundingBox ());
		outerFrame.accept (this);
		ps.close ();
		PDFDictionary page = new PDFDictionary ("Page");
		page.put ("MediaBox",new PDFArray (outerFrame.getBoundingBox ()));
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

	private double getWidth (String text,PDFLayoutFont layoutFont) {
		TextState textState = new TextState ();
		textState.setTextFont (getPDFFont (layoutFont),layoutFont.getFontSize ());
		return textState.getAdvance (text.getBytes ()); // TODO: proper encoding
	}

	private void transform (Rectangle from,Rectangle to) {
		transform (Transformation.matchBoxes (from,to).matrix);
	}

	private void flip (Rectangle r) {
		transform (1,0,0,-1,0,r.ymin + r.ymax);
	}
	
	private void transform (double ... m) {
		for (double v : m) {
			ps.print (toString (v));
			ps.print (' ');
		}
		ps.print ("cm\n");
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
			if (objectFit != PDFLayoutObjectFit.NONE) {
				Rectangle newComponentBox = new Rectangle (0,0,width,height);
				transform (componentBox,newComponentBox);
				componentBox = newComponentBox;
			}
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
					x = frameBox.xmin + (frameBox.width () - componentBox.width ()) / 2;
					break;
				default:
					throw new Error ("horizontal placement " + alignmentPlacement.getHorizontalAlignment () + " not implemented");
				}
				switch (alignmentPlacement.getVerticalAlignment ()) {
				case TOP:
					y = frameBox.ymin;
					break;
				case CENTER:
					y = frameBox.ymin + (frameBox.height () - componentBox.height ()) / 2;
					break;
				default:
					throw new Error ("vertical placement " + alignmentPlacement.getVerticalAlignment () + " not implemented");
				}
			}
			else
				throw new Error ("placement " + placement.getClass () + " not implemented");
			translate (x - componentBox.xmin,y - componentBox.ymin);
			if (!(componentContent instanceof PDFLayoutFrame))
				flip (getBoundingBox (componentContent));
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
		ps.print ("BT /" + getFontLabel (textContent.getFont ()) + " " + textContent.getFont ().getFontSize () + " Tf (" + textContent.getText () + ") Tj ET\n");
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
				return new Rectangle (0,0,getWidth (textContent.getText (),textContent.getFont ()),30);
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
