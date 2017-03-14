package com.emistoolbox.lib.pdf.test;

import java.util.Collections;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;

public class RotatedPDFLayoutTest {
	public List<PDFLayout> getLayout()
	{
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (842,595);
		PDFLayoutFrameElement frame = new PDFLayoutFrameElement (200,300);
		frame.border (1);
		frame.addElement (new PDFLayoutTextElement ("Hello world!",new PDFLayoutFont (PDFLayoutFont.FONT_HELVETICA,12,PDFLayoutFontStyle.PLAIN)));
		frame.rotate (1);
		outerFrame.addElement (frame);
		layout.setOuterFrame (outerFrame);
		return Collections.singletonList (layout); 
	}
}
