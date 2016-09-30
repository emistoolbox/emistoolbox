package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.Serializable;

public class PDFLayoutFont implements Serializable 
{
	public static final String FONT_TIMES = "Times"; 
	public static final String FONT_HELVETICA = "Helvetica"; 
	public static final String FONT_COURIER= "Courier"; 
	
	private String fontName;
	private double fontSize;

	private double lineSpacing = 1;
	private PDFLayoutFontStyle fontStyle;
	private Color color = Color.BLACK;

	public PDFLayoutFont()
	{}
	
	public PDFLayoutFont(String name, double size, PDFLayoutFontStyle style)
	{
		this.fontName = name; 
		this.fontSize = size; 
		this.fontStyle = style; 
	}
	
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

	public Color getColor () {
		return color;
	}

	public void setColor (Color color) {
		this.color = color;
	}

	public double getLineSpacing () {
		return lineSpacing;
	}

	public void setLineSpacing (double lineSpacing) {
		this.lineSpacing = lineSpacing;
	}
}
