package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutFont {
	private String fontName;
	private double fontSize;
	private PDFLayoutFontStyle fontStyle;

	public String getFontName () {
		return fontName;
	}

	public void setFontName (String fontName) {
		this.fontName = fontName;
	}

	public double getFontSize () {
		return fontSize;
	}

	public void setFontSize (double fontSize) {
		this.fontSize = fontSize;
	}

	public PDFLayoutFontStyle getFontStyle () {
		return fontStyle;
	}

	public void setFontStyle (PDFLayoutFontStyle fontStyle) {
		this.fontStyle = fontStyle;
	}
}
