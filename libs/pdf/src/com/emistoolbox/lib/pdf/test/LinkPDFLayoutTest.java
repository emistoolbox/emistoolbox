package com.emistoolbox.lib.pdf.test;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutElementLink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPageLink;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;

public class LinkPDFLayoutTest {
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();

		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);
		PDFLayoutFrameElement innerFrame = new PDFLayoutFrameElement (300,300);
		innerFrame.position (100,100);
		PDFLayoutFont font = new PDFLayoutFont ("Courier",14,PDFLayoutFontStyle.ITALIC);
		PDFLayoutTextElement pageLinkElement = new PDFLayoutTextElement ("page link",font);
		PDFLayoutTextElement textLinkElement = new PDFLayoutTextElement ("text link",font);
		PDFLayoutTextElement zoomLinkElement = new PDFLayoutTextElement ("a rather long link text for the zoom link so we can see it wrapped",font);
		innerFrame.addElement (pageLinkElement.pad (5).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
		innerFrame.addElement (textLinkElement.pad (5).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
		innerFrame.addElement (zoomLinkElement.pad (5).align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BELOW));
		outerFrame.addElement (innerFrame);
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);

		layout = new PDFLayout ();
		outerFrame = new PDFLayoutFrameElement (500,500);
		PDFLayoutTextElement linkTarget = new PDFLayoutTextElement ("link target",font);
		PDFLayoutTableElement tableElement = new PDFLayoutTableElement ();
		tableElement.setRowCount (1);
		tableElement.setColCount (1);
		tableElement.setElement (0,0,linkTarget);
		outerFrame.addElement (tableElement);
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		
		pageLinkElement.setLink (new PDFLayoutPageLink (layout));
		textLinkElement.setLink (new PDFLayoutElementLink (linkTarget,false));
		zoomLinkElement.setLink (new PDFLayoutElementLink (linkTarget,true));
		
		return layouts;
	}
}
