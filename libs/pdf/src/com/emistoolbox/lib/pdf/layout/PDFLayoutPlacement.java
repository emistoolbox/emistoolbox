package com.emistoolbox.lib.pdf.layout;

import java.io.Serializable;

public class PDFLayoutPlacement implements Serializable {
	public final static PDFLayoutPlacement CENTER = new PDFLayoutPlacement (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.CENTER);

	PDFLayoutHorizontalPlacement horizontalPlacement;
	PDFLayoutVerticalPlacement verticalPlacement;
	
	public PDFLayoutPlacement (PDFLayoutHorizontalPlacement horizontalPlacement,PDFLayoutVerticalPlacement verticalPlacement) {
		this.horizontalPlacement = horizontalPlacement;
		this.verticalPlacement = verticalPlacement;
	}

	public PDFLayoutHorizontalPlacement getHorizontalPlacement () {
		return horizontalPlacement;
	}

	public void setHorizontalPlacement (PDFLayoutHorizontalPlacement horizontalPlacement) {
		this.horizontalPlacement = horizontalPlacement;
	}

	public PDFLayoutVerticalPlacement getVerticalPlacement () {
		return verticalPlacement;
	}

	public void setVerticalPlacement (PDFLayoutVerticalPlacement verticalPlacement) {
		this.verticalPlacement = verticalPlacement;
	}

	public String toString () {
		return horizontalPlacement + ", " + verticalPlacement;
	}
}
