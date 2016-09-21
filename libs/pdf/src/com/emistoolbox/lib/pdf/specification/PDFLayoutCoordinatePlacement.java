package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutCoordinatePlacement extends PDFLayoutPlacement {
	private double x,y;
	private double width,height;
	
	public PDFLayoutCoordinatePlacement (double x,double y,double width,double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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

	public double getWidth () {
		return width;
	}

	public void setWidth (double width) {
		this.width = width;
	}

	public double getHeight () {
		return height;
	}

	public void setHeight (double height) {
		this.height = height;
	}
}
