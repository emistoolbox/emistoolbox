package com.emistoolbox.lib.pdf.test;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPDFElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;

import es.jbauer.lib.io.impl.IOFileInput;

public class CropPDFLayoutTest {
	private String testDir; 
	
	public CropPDFLayoutTest(String testDir)
	{ this.testDir = testDir; } 
	
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);
		PDFLayoutPDFElement uncroppedElement = new PDFLayoutPDFElement (new IOFileInput (new File (testDir,"chart_uncropped.pdf")));
		PDFLayoutPDFElement   croppedElement = new PDFLayoutPDFElement (new IOFileInput (new File (testDir,"chart_uncropped.pdf")));
		croppedElement.setCropping (true);
		PDFLayoutTableElement tableElement = new PDFLayoutTableElement ();
		tableElement.setRowCount (2);
		tableElement.setColCount (1);
		tableElement.setAllBorderStyles (new PDFLayoutLineStyle (1,Color.black));
		tableElement.setElement (0,0,uncroppedElement.wrap (400,200));
		tableElement.setElement (1,0,  croppedElement.wrap (400,200));
		outerFrame.addElement (tableElement);
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		return layouts;
	}
}
