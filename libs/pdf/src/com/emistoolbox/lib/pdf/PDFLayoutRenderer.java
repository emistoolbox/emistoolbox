package com.emistoolbox.lib.pdf;

import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.pdf.ConstructiblePDFDocument;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFStream;
import info.joriki.pdf.PDFWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.emistoolbox.lib.pdf.specification.PDFLayout;
import com.emistoolbox.lib.pdf.specification.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutComponent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutCoordinatePlacement;
import com.emistoolbox.lib.pdf.specification.PDFLayoutFrame;
import com.emistoolbox.lib.pdf.specification.PDFLayoutObjectFit;
import com.emistoolbox.lib.pdf.specification.PDFLayoutPDFContent;
import com.emistoolbox.lib.pdf.specification.PDFLayoutPlacement;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.impl.IOInputStreamInput;

public class PDFLayoutRenderer {
	public IOInput render (List<PDFLayout> layouts) throws IOException {
		ConstructiblePDFDocument document = new ConstructiblePDFDocument ();
		
		// TODO: test document with more than one page
		for (PDFLayout layout : layouts)
			render (layout,document);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		PDFWriter writer = new PDFWriter (baos);
		writer.write (document);
		writer.close ();
		baos.close ();
		
		return new IOInputStreamInput (new ByteArrayInputStream (baos.toByteArray ()));
	}

	private void render (PDFLayout layout,ConstructiblePDFDocument document) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		PrintStream ps = new PrintStream (baos);
		ResourceRenamer renamer = new ResourceRenamer ();
		PDFLayoutFrame outerFrame = layout.getOuterFrame ();
		render (outerFrame,ps,renamer);
		ps.close ();
		PDFDictionary page = new PDFDictionary ("Page");
		page.put ("MediaBox",new PDFArray (outerFrame.getBoundingBox ()));
		page.putIndirect ("Contents",new PDFStream (baos.toByteArray ()));
		page.putIndirect ("Resources",renamer.getResources ());
		document.addPage (page);
	}

	// take margins into account
	private void render (PDFLayoutContent content,PrintStream ps,ResourceRenamer resourceRenamer) throws IOException {
		if (content instanceof PDFLayoutPDFContent)
			ps.write (resourceRenamer.rename (((PDFLayoutPDFContent) content).getPage ()));
		else if (content instanceof PDFLayoutFrame) {
			PDFLayoutFrame frame = (PDFLayoutFrame) content;
			Rectangle frameBox = frame.getRectangle ();
			for (PDFLayoutComponent component : frame.getComponents ()) {
				ps.print ("q\n");
				PDFLayoutContent componentContent = component.getContent ();
				PDFLayoutPlacement placement = component.getPlacement ();
				PDFLayoutObjectFit objectFit = component.getObjectFit ();
				Rectangle componentBox = componentContent.getBoundingBox ();
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
					transform (ps,componentBox,newComponentBox);
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
					case CENTER:
						x = frameBox.xmin + (frameBox.width () - componentBox.width ()) / 2;
						break;
					default:
						throw new Error ("horizontal placement " + alignmentPlacement.getHorizontalAlignment () + " not implemented");
					}
					switch (alignmentPlacement.getVerticalAlignment ()) {
					case CENTER:
						y = frameBox.ymin + (frameBox.height () - componentBox.height ()) / 2;
						break;
					default:
						throw new Error ("vertical placement " + alignmentPlacement.getVerticalAlignment () + " not implemented");
					}
				}
				else
					throw new Error ("placement " + placement.getClass () + " not implemented");
				ps.print ("1 0 0 1 " + toString (x - componentBox.xmin) + " " + toString (y - componentBox.ymin) + " cm\n");
				render (componentContent,ps,resourceRenamer);
				ps.print ("Q\n");
			}
		}
	}
	
	private void transform (PrintStream ps,Rectangle from,Rectangle to) {
		double [] m = Transformation.matchBoxes (from,to).matrix;
		for (double v : m) {
			ps.print (v);
			ps.print (' ');
		}
		ps.print ("cm\n");
	}
	
	private String toString (double x) {
		return String.valueOf (x);
	}
}
