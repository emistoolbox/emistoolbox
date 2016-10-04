package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;

public class PDFLayoutLink {
	private double borderWidth = 1;
	private double borderRadius = 0;
	private Color borderColor = Color.BLACK;

	public double getBorderWidth () {
		return borderWidth;
	}

	public void setBorderWidth (double borderWidth) {
		this.borderWidth = borderWidth;
	}

	public double getBorderRadius () {
		return borderRadius;
	}

	public void setBorderRadius (double borderRadius) {
		this.borderRadius = borderRadius;
	}

	public Color getBorderColor () {
		return borderColor;
	}

	public void setBorderColor (Color borderColor) {
		this.borderColor = borderColor;
	}
}
