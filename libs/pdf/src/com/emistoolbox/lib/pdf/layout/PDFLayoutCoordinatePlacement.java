package com.emistoolbox.lib.pdf.layout;

import java.io.Serializable;

public class PDFLayoutCoordinatePlacement implements PDFLayoutHorizontalPlacement, PDFLayoutVerticalPlacement, Serializable {
	private double x;
	
	public PDFLayoutCoordinatePlacement (double x) {
		this.x = x;
	}

	public double getX () {
		return x;
	}

	public void setX (double x) {
		this.x = x;
	}
	
	public String toString () {
		return String.valueOf (x);
	}
}
