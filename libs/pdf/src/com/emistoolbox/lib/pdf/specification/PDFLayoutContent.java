package com.emistoolbox.lib.pdf.specification;

import java.io.IOException;
import java.util.Collections;

import info.joriki.graphics.Rectangle;

abstract public class PDFLayoutContent {
	abstract public Rectangle getBoundingBox () throws IOException;

	public PDFLayoutFrame wrap (double width,double height) {
		PDFLayoutFrame frame = new PDFLayoutFrame ();
		frame.setRectangle (new Rectangle (0,0,width,height));
		frame.setComponents (Collections.singletonList (new PDFLayoutComponent (this,PDFLayoutObjectFit.CONTAIN,new PDFLayoutAlignmentPlacement (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.CENTER))));
		return frame;
	}

	public PDFLayoutComponent position (double x,double y) {
		return new PDFLayoutComponent (this,PDFLayoutObjectFit.NONE,new PDFLayoutCoordinatePlacement (x,y));
	}
}
