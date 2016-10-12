package com.emistoolbox.lib.pdf.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.lib.pdf.layout.PDFLayout;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFont;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFontStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutFrameElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutHorizontalAlignment;
import com.emistoolbox.lib.pdf.layout.PDFLayoutLineStyle;
import com.emistoolbox.lib.pdf.layout.PDFLayoutPlacement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutSides;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableElement;
import com.emistoolbox.lib.pdf.layout.PDFLayoutTableFormat;
import com.emistoolbox.lib.pdf.layout.PDFLayoutVerticalAlignment;

public class TablePDFLayoutTest {
	public List<PDFLayout> getLayout () {
		List<PDFLayout> layouts = new ArrayList<PDFLayout> ();

		// test formats and borders
		PDFLayout layout = new PDFLayout ();
		PDFLayoutFrameElement outerFrame = new PDFLayoutFrameElement (500,500);
		PDFLayoutTableElement table = new PDFLayoutTableElement ();
		table.setDimensions (4,3);
		
		PDFLayoutFont defaultFont = new PDFLayoutFont ("Courier",10,PDFLayoutFontStyle.BOLD);
		PDFLayoutFont columnFont = new PDFLayoutFont ("Helvetica",12,PDFLayoutFontStyle.ITALIC); 
		PDFLayoutFont rowFont = new PDFLayoutFont ("Helvetica",14,PDFLayoutFontStyle.BOLD_ITALIC); 
		PDFLayoutFont cellFont = new PDFLayoutFont ("Times",20,PDFLayoutFontStyle.PLAIN);

		PDFLayoutTableFormat defaultFormat = new PDFLayoutTableFormat ();
		defaultFormat.setFont (defaultFont);
		defaultFormat.setBackgroundColor (Color.LIGHT_GRAY);
		defaultFormat.setPlacement (PDFLayoutPlacement.CENTER);
		table.setDefaultFormat (defaultFormat);
		
		PDFLayoutTableFormat columnFormat = new PDFLayoutTableFormat ();
		columnFormat.setFont (columnFont);
		columnFormat.setBackgroundColor (Color.GREEN);
		columnFormat.setPadding (new PDFLayoutSides<Double> (2.));
		columnFormat.setPlacement (new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.LEFT,PDFLayoutVerticalAlignment.BOTTOM));
		table.setColFormat (1,columnFormat);
		
		PDFLayoutTableFormat rowFormat = new PDFLayoutTableFormat ();
		rowFormat.setFont (rowFont);
		rowFormat.setBackgroundColor (Color.RED);
		rowFormat.setPlacement (new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.TOP));
		table.setRowFormat (2,rowFormat);
		
		PDFLayoutTableFormat cellFormat = new PDFLayoutTableFormat ();
		cellFormat.setFont (cellFont);	
		cellFormat.setBackgroundColor (Color.YELLOW);
		cellFormat.setPlacement (new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.RIGHT,PDFLayoutVerticalAlignment.CENTER));
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
		
		// test row and column spanning
		layout = new PDFLayout ();
		outerFrame = new PDFLayoutFrameElement (500,500);
		outerFrame.pad (50);
		PDFLayoutTableElement [] tables = new PDFLayoutTableElement [2];
		
		PDFLayoutLineStyle borderStyle = new PDFLayoutLineStyle (1.,Color.BLACK);

		defaultFormat = new PDFLayoutTableFormat ();
		defaultFormat.setFont (cellFont);
		defaultFormat.setPadding (new PDFLayoutSides<Double> (3.));
		defaultFormat.setPlacement (PDFLayoutPlacement.CENTER);
		
		for (int i = 0;i < 2;i++) {
			tables [i] = new PDFLayoutTableElement ();
			tables [i].setDefaultFormat (defaultFormat);
			tables [i].setDimensions (5,5);
		
			row = 0;
			for (String stanza : "We hold these truths to;be self-evident, that all cells;are not created equal, that;they are endowed by their;Creator with certain unalienable sizes".split (";")) {
				int col = 0;
				for (String word : stanza.split (" ")) {
					tables [i].setText (row,col,word);
					col++;
				}
				row++;
			}

			for (int j = 0;j <= 5;j++) {
				tables [i].setHorizontalBorderStyle (j,borderStyle);
				tables [i].setVerticalBorderStyle (j,borderStyle);
			}
		}

		tables [1].setCellSpan (1,1,3,1);
		tables [1].setCellSpan (2,3,2,1);
		
		PDFLayoutTableFormat spanFormat = new PDFLayoutTableFormat (defaultFormat);
		spanFormat.setBackgroundColor (Color.red);

		tables [1].setElement (1,1,new PDFLayoutFrameElement (100,100).color (Color.LIGHT_GRAY));
		tables [1].setCellFormat (1,1,spanFormat);

		tables [1].setElement (2,3,new PDFLayoutFrameElement (100,100).color (Color.LIGHT_GRAY));
		tables [1].setCellFormat (2,3,spanFormat);

		outerFrame.addElement (tables [0].align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.TOP));
		outerFrame.addElement (tables [1].align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.BOTTOM));
		
		layout.setOuterFrame (outerFrame);
		layouts.add (layout);
		
		return layouts;
	}
}
