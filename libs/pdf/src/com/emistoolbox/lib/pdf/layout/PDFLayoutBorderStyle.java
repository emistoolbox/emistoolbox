package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.Serializable;

public class PDFLayoutBorderStyle implements Serializable {
	PDFLayoutSides<PDFLayoutLineStyle> lineStyles;
	double borderRadius;

	public PDFLayoutBorderStyle()
	{}
	
	public PDFLayoutBorderStyle (PDFLayoutSides<PDFLayoutLineStyle> lineStyles,double borderRadius) {
		this.lineStyles = lineStyles;
		this.borderRadius = borderRadius;
	}

	public PDFLayoutBorderStyle (double width) {
		this (new PDFLayoutSides<PDFLayoutLineStyle> (new PDFLayoutLineStyle (width,Color.BLACK)),0);
	}

	public PDFLayoutSides<PDFLayoutLineStyle> getLineStyles () {
		return lineStyles;
	}

	public void setLineStyles (PDFLayoutSides<PDFLayoutLineStyle> lineStyles) {
		this.lineStyles = lineStyles;
	}

	public double getBorderRadius () {
		return borderRadius; 
	}

	public void setBorderRadius (Double borderRadius) {
		this.borderRadius = borderRadius;
	}
}
