package com.emistoolbox.lib.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import info.joriki.graphics.Point;
import info.joriki.graphics.Rectangle;
import info.joriki.graphics.Transformation;
import info.joriki.io.Util;
import info.joriki.pdf.ConstructiblePDFDocument;
import info.joriki.pdf.PDFArray;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFDocument;
import info.joriki.pdf.PDFFile;
import info.joriki.pdf.PDFStream;
import info.joriki.pdf.PDFWriter;

public class PlaceCharts {
	static Rectangle [] boxes = {
			new Rectangle (new Point (200,200),400,200),
			new Rectangle (new Point (700,200),400,200),
			new Rectangle (new Point (200,500),400,200)
	};
	
	public static void main (String [] args) throws IOException {
	    Rectangle mediaBox = new Rectangle ();
		ConstructiblePDFDocument newDocument = new ConstructiblePDFDocument ();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		PrintStream ps = new PrintStream (baos);
		
		ResourceRenamer renamer = new ResourceRenamer ();

//		PDFDictionary resources = null;
		
		for (int i = 0;i < args.length;i++) {
			PDFDocument document = new PDFFile (args [i]).getDocument ();
			PDFDictionary page = document.getPage (1);
	        mediaBox.add (page.getMediaBox ().toRectangle ());
			if (i == 0)
;//			    document.setMediaBox (new PDFArray (mediaBox));
			double [] m = Transformation.matchBoxes (mediaBox,boxes [i]).matrix;
			ps.print ("q ");
			for (double v : m) {
				ps.print (v);
				ps.print (' ');
			}
			ps.print ("cm\n");
			baos.write (renamer.rename (page));
//			baos.write (Util.toByteArray (page.getContentStream ()));
//			resources = (PDFDictionary) page.get ("Resources");
			ps.print ("\nQ\n");
		}
		ps.close ();
		baos.close ();
		PDFDictionary page = new PDFDictionary ("Page");
		page.putIndirect ("Contents",new PDFStream (baos.toByteArray ()));
		page.putIndirect ("Resources",renamer.getResources ());
//		page.putIndirect ("Resources",resources);
		newDocument.addPage (page);
		
	    newDocument.setMediaBox (new PDFArray (mediaBox));
	    PDFWriter writer = new PDFWriter ("/Users/joriki/charts.pdf");
		writer.write (newDocument);
		writer.close ();
	}
}
