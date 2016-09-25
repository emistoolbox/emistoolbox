package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;

public class PDFLayoutBorderStyle {
	PDFLayoutSides<PDFLayoutLineStyle> lineStyles;
	Double borderRadius;

	public PDFLayoutBorderStyle (PDFLayoutSides<PDFLayoutLineStyle> lineStyles,Double borderRadius) {
		this.lineStyles = lineStyles;
		this.borderRadius = borderRadius;
	}

	public PDFLayoutBorderStyle (Double width) {
		this (new PDFLayoutSides<PDFLayoutLineStyle> (new PDFLayoutLineStyle (2.,Color.BLACK)),null);
	}

	public PDFLayoutSides<PDFLayoutLineStyle> getLineStyles () {
		return lineStyles;
	}

	public void setLineStyles (PDFLayoutSides<PDFLayoutLineStyle> lineStyles) {
		this.lineStyles = lineStyles;
	}

	public Double getBorderRadius () {
		return borderRadius;
	}

	public void setBorderRadius (Double borderRadius) {
		this.borderRadius = borderRadius;
	}
}
