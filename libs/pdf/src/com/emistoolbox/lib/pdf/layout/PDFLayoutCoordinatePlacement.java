package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutCoordinatePlacement extends PDFLayoutPlacement {
	private double x,y;
	
	public PDFLayoutCoordinatePlacement (double x,double y) {
		this.x = x;
		this.y = y;
	}

	public double getX () {
		return x;
	}

	public void setX (double x) {
		this.x = x;
	}

	public double getY () {
		return y;
	}

	public void setY (double y) {
		this.y = y;
	}
	
	public String toString()
	{ return "(" + x + ", " + y + ")"; }
}
