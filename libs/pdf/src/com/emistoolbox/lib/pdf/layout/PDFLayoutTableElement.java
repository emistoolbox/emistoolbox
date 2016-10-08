package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;
import java.util.Arrays;

public class PDFLayoutTableElement extends PDFLayoutElement {
	private PDFLayoutElement [] [] elements = new PDFLayoutElement [0] [0];
	private String [] [] texts = new String [0] [0];
	private Integer [] [] rowSpans = new Integer [0] [0]; 
	private Integer [] [] colSpans = new Integer [0] [0]; 
	private PDFLayoutTableFormat defaultFormat;
	private PDFLayoutTableFormat [] rowFormats = new PDFLayoutTableFormat [0];
	private PDFLayoutTableFormat [] colFormats = new PDFLayoutTableFormat [0];
	private PDFLayoutTableFormat [] [] cellFormats = new PDFLayoutTableFormat [0] [0];
	private PDFLayoutLineStyle [] [] horizontalLines = new PDFLayoutLineStyle [1] [0];
	private PDFLayoutLineStyle [] [] verticalLines = new PDFLayoutLineStyle [0] [1];
	private int rows;
	private int cols;

	public int getRowCount () {
		return rows;
	}

	public void setRowCount (int rows) {
		setDimensions (rows,cols);
	}

	public int getColCount () {
		return cols;
	}
	
	public void setColCount (int cols) {
		setDimensions (rows,cols);
	}

	public void setDimensions (int rows,int cols) {
		if (rows == this.rows && cols == this.cols)
			return;

		if (rows != this.rows)
			rowFormats = Arrays.copyOf (rowFormats,rows);
		if (cols != this.cols)
			colFormats = Arrays.copyOf (colFormats,cols);

		elements = copy (elements,new PDFLayoutElement [rows] [cols]);
		texts = copy (texts,new String [rows] [cols]);
		rowSpans = copy (rowSpans,new Integer [rows] [cols]);
		colSpans = copy (colSpans,new Integer [rows] [cols]);
		cellFormats = copy (cellFormats,new PDFLayoutTableFormat [rows] [cols]);
		horizontalLines = copy (horizontalLines,new PDFLayoutLineStyle [rows + 1] [cols]);
		verticalLines = copy (verticalLines,new PDFLayoutLineStyle [rows] [cols + 1]);
		
		this.rows = rows;
		this.cols = cols;
	}

	public void setCellSpan (int row,int col,int rowSpan,int colSpan) {
		rowSpans [row] [col] = rowSpan;
		colSpans [row] [col] = colSpan;
	}
	
	public int getRowSpan (int row,int col) {
		return rowSpans [row] [col] != null ? rowSpans [row] [col] : 1;
	}
	
	public int getColSpan (int row,int col) {
		return colSpans [row] [col] != null ? colSpans [row] [col] : 1;
	}
	
	// set line style of the external borders of the table
	public void setTableBorderStyle (PDFLayoutLineStyle style) {
		for (int col = 0;col < cols;col++) {
			setHorizontalBorderStyle (0,col,style);
			setHorizontalBorderStyle (rows,col,style);
		}

		for (int row = 0;row < rows;row++) {
			setVerticalBorderStyle (row,0,style);
			setVerticalBorderStyle (row,cols,style);
		}
	}

	// set line style of a single horizontal border segment (row <= rows, col < cols)
	public void setHorizontalBorderStyle (int row,int col,PDFLayoutLineStyle style) {
		horizontalLines [row] [col] = style;
	}

	// set line style of a single vertical border segment (row < rows, col <= cols)
	public void setVerticalBorderStyle (int row,int col,PDFLayoutLineStyle style) {
		verticalLines [row] [col] = style;
	}

	// set line style of an entire horizontal border row (row <= rows)
	public void setHorizontalBorderStyle (int row,PDFLayoutLineStyle style) {
		for (int col = 0;col < cols;col++)
			setHorizontalBorderStyle (row,col,style);
	}

	// set line style of an entire vertical border column (col <= cols)
	public void setVerticalBorderStyle (int col,PDFLayoutLineStyle style) {
		for (int row = 0;row < rows;row++)
			setVerticalBorderStyle (row,col,style);
	}

	public void setText (int row,int col,String text) {
		texts [row] [col] = text;
	}

	public void setElement (int row,int col,PDFLayoutElement element) {
		elements [row] [col] = element;
	}

	public PDFLayoutElement getElement (int row,int col) {
		PDFLayoutElement element = elements [row] [col];
		if (element != null)
			return element;

		String text = texts [row] [col];
		if (text == null)
			return null;

		PDFLayoutTextElement textElement = new PDFLayoutTextElement ();
		textElement.setText (text);
		PDFLayoutTableFormat format = getFormat (row,col);
		if (format != null) {
			textElement.setFont (format.getFont ());
			textElement.setPlacement (format.getPlacement ());
			textElement.setObjectFit (format.getObjectFit ());
			textElement.setPadding (format.getPadding ());
		}
		return textElement;
	}

	public void setDefaultFormat (PDFLayoutTableFormat format) {
		defaultFormat = format;
	}

	public void setRowFormat (int row,PDFLayoutTableFormat format) {
		rowFormats [row] = format;
	}

	public void setColFormat (int col,PDFLayoutTableFormat format) {
		colFormats [col] = format;
	}

	public void setCellFormat (int row,int col,PDFLayoutTableFormat format) {
		cellFormats [row] [col] = format;
	}

	public PDFLayoutTableFormat getFormat (int row,int col) {
		return
				cellFormats [row] [col] != null ? cellFormats [row] [col] :
				rowFormats [row] != null ? rowFormats [row] :
				colFormats [col] != null ? colFormats [col] :
				defaultFormat;
	}
	
	public PDFLayoutLineStyle getHorizontalLineStyle (int row,int col) {
		return horizontalLines [row] [col];
	}

	public PDFLayoutLineStyle getVerticalLineStyle (int row,int col) {
		return verticalLines [row] [col];
	}

	private <T> T [] [] copy (T [] [] from,T [] [] to) {
		for (int row = 0;row < from.length && row < to.length;row++)
			for (int col = 0;col < from [row].length && col < to [row].length;col++)
				to [row] [col] = from [row] [col];
		return to;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}
}
