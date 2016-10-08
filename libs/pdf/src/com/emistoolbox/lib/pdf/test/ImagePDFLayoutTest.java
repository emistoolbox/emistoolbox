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
	private String testDir; 
	
	public ImagePDFLayoutTest(String testDir)
	{ this.testDir = testDir; } 
	
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();
		layouts.add (getLayout ("toolbox.png"));
		layouts.add (getLayout ("toolbox.jpg"));
		return layouts;
	}
	
	private PDFLayout getLayout (String filename) {
		File file = new File (testDir,filename);
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
		return layout;
	}
}
