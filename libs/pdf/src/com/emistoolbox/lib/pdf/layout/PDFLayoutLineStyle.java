package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;

public class PDFLayoutLineStyle {
	Double width;
	Color color;

	public PDFLayoutLineStyle (Double width,Color color) {
		this.width = width;
		this.color = color;
	}

	public Double getWidth () {
		return width;
	}

	public void setWidth (Double width) {
		this.width = width;
	}

	public Color getColor () {
		return color;
	}

	public void setColor (Color color) {
		this.color = color;
	}
}
