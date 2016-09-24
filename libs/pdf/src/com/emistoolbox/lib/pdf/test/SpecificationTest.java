package com.emistoolbox.lib.pdf.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.specification.PDFLayout;

import es.jbauer.lib.io.impl.IOFileOutput;

public class SpecificationTest {
	public static void main (String [] args) throws IOException {
		String testDir = "/Users/joriki/work/Jörg/code/emistoolbox/libs/pdf/test/content";
		List<PDFLayout> layout = new BasicPdfLayoutTest (testDir).getLayout ();
		new PDFLayoutRenderer ().render (layout,new IOFileOutput (new File (testDir,"test.pdf"),"application/pdf",null));
	}
}
