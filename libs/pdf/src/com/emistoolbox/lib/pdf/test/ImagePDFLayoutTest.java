package com.emistoolbox.lib.pdf.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutImageElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutObjectFit;

import es.jbauer.lib.io.impl.IOFileInput;

public class ImagePDFLayoutTest {
	public List<PDFLayout> getLayout (File file) {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);

		String contentType = null;
		for (String [] imageType : new String [] [] {{"png","png"},{"jpg","jpeg"},{"jpeg","jpeg"}})
			if (file.getName ().toLowerCase ().endsWith ('.' + imageType [0]))
				contentType = "image/" + imageType [1];

		PDFLayoutImageElement image = new PDFLayoutImageElement (new IOFileInput (file,contentType,null));
		image.setObjectFit (PDFLayoutObjectFit.CONTAIN);
		outerFrame.addElement (image);

		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		return layouts;
	}
}
