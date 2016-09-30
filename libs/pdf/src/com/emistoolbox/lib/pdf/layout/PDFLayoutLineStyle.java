package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.Serializable;

public class PDFLayoutLineStyle implements Serializable {
	double width;
	Color color;

	public PDFLayoutLineStyle()
	{}
	
	public PDFLayoutLineStyle (double width, Color color) {
		this.width = width;
		this.color = color;
	}

	public double getWidth () 
	{ return width; }

	public void setWidth (double width) 
	{ this.width = width; }

	public Color getColor () 
	{ return color; }

	public void setColor (Color color) 
	{ this.color = color; }
	
	public String toString () 
	{ return "w=" + width + ", " + color; }
}
