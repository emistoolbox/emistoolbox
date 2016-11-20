package com.emistoolbox.lib.pdf.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.PDFLayoutRenderer;
import com.emistoolbox.lib.pdf.layout.PDFLayout;

import es.jbauer.lib.io.impl.IOFileOutput;

public class LayoutTest {
	public static void main (String [] args) throws IOException {
		String testDir = args[0];
		List<PDFLayout> layout = new ArrayList<PDFLayout>(); 
		layout.addAll(new BasicPdfLayoutTest (testDir).getLayout ());
		layout.addAll(new BorderPDFLayoutTest().getLayout ()); 
		layout.addAll(new TablePDFLayoutTest().getLayout ()); 
		layout.addAll(new LinkPDFLayoutTest().getLayout ());
		layout.addAll(new ImagePDFLayoutTest (testDir).getLayout ());
		layout.addAll(new CropPDFLayoutTest (testDir).getLayout ());
		
		
		new PDFLayoutRenderer (true).render (layout,new IOFileOutput (new File (testDir,"test.pdf"),"application/pdf",null));
	}
}
