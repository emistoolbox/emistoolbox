package com.emistoolbox.pets;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;

import es.jbauer.lib.io.impl.IOFileInput;
import es.jbauer.lib.io.impl.IOFileOutput;

public class PdfPosterTest {
	private static final int pageHeight = 842;
	private static final int pageWidth  = 595;
	private static final int horizontalMargin = 36;
	private static final int verticalMargin   = 36;

	public static void main (String [] args) throws IOException {
		List<PetPosterDetail> details = new ArrayList<PetPosterDetail> ();
		details.add (new PetPosterDetail ("name","NAME","Violet",1));
		details.add (new PetPosterDetail ("breed","BREED","Russian Blue domestic long hair cat",2));
		details.add (new PetPosterDetail ("color","COLOR","Blue/Gray",1));
		details.add (new PetPosterDetail ("weight","WEIGHT","10 lbs",1));
		
		String directory = args [0];
		PdfPosterCreator creator = new PdfPosterCreator ();
		creator.setLogo (new IOFileInput (new File (directory,"logo.pdf")));
//		creator.setTableBorderStyle (new PDFLayoutLineStyle (2.,java.awt.Color.LIGHT_GRAY));
		for (int design = 0;design <= 3;design++)
			creator.render (new IOFileOutput (new File (directory,"poster-" + design + ".pdf")),new IOFileInput (new File (directory,"pet.png")),design,details,new Point (pageWidth,pageHeight),new Rectangle (horizontalMargin,verticalMargin,horizontalMargin,verticalMargin));
	}
}
