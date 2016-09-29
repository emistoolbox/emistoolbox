package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public class PDFLayoutTextElement extends PDFLayoutElement 
{
	private String text;
	private PDFLayoutFont font;

	public PDFLayoutTextElement()
	{}

	public PDFLayoutTextElement(String text, PDFLayoutFont font)
	{
		this.text = text; 
		this.font = font; 
	}
	
	public String getText () {
		return text;
	}

	public void setText (String text) {
		this.text = text;
	}

	public PDFLayoutFont getFont() {
		return font;
	}

	public void setFont (PDFLayoutFont font) {
		this.font = font;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}
}
