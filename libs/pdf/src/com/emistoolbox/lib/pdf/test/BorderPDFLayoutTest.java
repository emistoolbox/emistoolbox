package com.emistoolbox.lib.pdf.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;

public class BorderPDFLayoutTest {
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);
		PDFLayoutFrameElement innerFrame = new PDFLayoutFrameElement (300,300);
		innerFrame.position (100,100);
		innerFrame.setBorderStyle (new PDFLayoutBorderStyle (new PDFLayoutSides<PDFLayoutLineStyle> (new PDFLayoutLineStyle [] {new PDFLayoutLineStyle (3.,Color.GREEN),new PDFLayoutLineStyle (5.,Color.GREEN),new PDFLayoutLineStyle (8.,Color.GREEN),new PDFLayoutLineStyle (12.,Color.GREEN)}),5.));
		innerFrame.setBackgroundColor (new Color (0x7fff0000,true));
		innerFrame.addElement (new PDFLayoutTextElement ("Hello World!",new PDFLayoutFont ("Times",14,PDFLayoutFontStyle.BOLD_ITALIC)));
		outerFrame.addElement (innerFrame);
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		return layouts;
	}
}
