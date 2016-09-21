package com.emistoolbox.lib.pdf.test;

import info.joriki.io.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.specification.PDFLayout;

import es.jbauer.lib.io.IOInput;

public class SpecificationTest {
	public static void main (String [] args) throws IOException {
		String testDir = "/Users/joriki/work/Jörg/code/emistoolbox/libs/pdf/test/content";
		List<PDFLayout> layout = new BasicPdfLayoutTest (testDir).getLayout ();
		IOInput input = new PDFLayoutRenderer ().render (layout);
		Util.copy (input.getInputStream (),new File (testDir,"test.pdf"));
	}
}
