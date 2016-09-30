package com.emistoolbox.lib.pdf.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutAlignmentPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutBorderStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableFormat;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTextElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;

public class TablePDFLayoutTest {
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);
//		PDFLayoutFrameElement innerFrame = new PDFLayoutFrameElement (300,300);
//		innerFrame.position (100,100);
//		innerFrame.setBorderStyle (new PDFLayoutBorderStyle (new PDFLayoutSides<PDFLayoutLineStyle> (new PDFLayoutLineStyle [] {new PDFLayoutLineStyle (3.,Color.GREEN),new PDFLayoutLineStyle (5.,Color.GREEN),new PDFLayoutLineStyle (8.,Color.GREEN),new PDFLayoutLineStyle (12.,Color.GREEN)}),5.));
//		innerFrame.setBackgroundColor (new Color (0x7fff0000,true));
//		innerFrame.addElement (new PDFLayoutTextElement ("Hello World!",new PDFLayoutFont ("Times",14,PDFLayoutFontStyle.BOLD_ITALIC)));
//		outerFrame.addElement (innerFrame);
		PDFLayoutTableElement table = new PDFLayoutTableElement ();
		table.setDimensions (4,3);
		
		PDFLayoutFont defaultFont = new PDFLayoutFont ("Courier",10,PDFLayoutFontStyle.BOLD);
		PDFLayoutFont columnFont = new PDFLayoutFont ("Helvetica",12,PDFLayoutFontStyle.ITALIC); 
		PDFLayoutFont rowFont = new PDFLayoutFont ("Helvetica",14,PDFLayoutFontStyle.BOLD_ITALIC); 
		PDFLayoutFont cellFont = new PDFLayoutFont ("Times",20,PDFLayoutFontStyle.PLAIN);

		PDFLayoutTableFormat defaultFormat = new PDFLayoutTableFormat ();
		defaultFormat.setFont (defaultFont);
		defaultFormat.setBackgroundColor (Color.LIGHT_GRAY);
		defaultFormat.setPlacement (PDFLayoutAlignmentPlacement.CENTER);
		table.setDefaultFormat (defaultFormat);
		
		PDFLayoutTableFormat columnFormat = new PDFLayoutTableFormat ();
		columnFormat.setFont (columnFont);
		columnFormat.setBackgroundColor (Color.GREEN);
		columnFormat.setPadding (new PDFLayoutSides<Double> (2.));
		columnFormat.setPlacement (new PDFLayoutAlignmentPlacement (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BOTTOM));
		table.setColFormat (1,columnFormat);
		
		PDFLayoutTableFormat rowFormat = new PDFLayoutTableFormat ();
		rowFormat.setFont (rowFont);
		rowFormat.setBackgroundColor (Color.RED);
		rowFormat.setPlacement (new PDFLayoutAlignmentPlacement (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.TOP));
		table.setRowFormat (2,rowFormat);
		
		PDFLayoutTableFormat cellFormat = new PDFLayoutTableFormat ();
		cellFormat.setFont (cellFont);	
		cellFormat.setBackgroundColor (Color.YELLOW);
		cellFormat.setPlacement (new PDFLayoutAlignmentPlacement (PDFLayoutHorizontalAlignment.RIGHT,PDFLayoutVerticalAlignment.CENTER));
		table.setCellFormat (2,2,cellFormat);
		
		int row = 0;
		for (String stanza : "The quick brown;fox jumps over;the lazy dog;thus defying gravity".split (";")) {
			int col = 0;
			for (String word : stanza.split (" ")) {
				table.setText (row,col,word);
				col++;
			}
			row++;
		}
		
		table.setTableBorderStyle (new PDFLayoutLineStyle (2.,Color.red));
		table.setHorizontalBorderStyle (2,new PDFLayoutLineStyle (3.,Color.BLUE.brighter ()));
		table.setVerticalBorderStyle (2,new PDFLayoutLineStyle (3.,Color.ORANGE.brighter ()));

		outerFrame.addElement (table);
		
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		return layouts;
	}
}
