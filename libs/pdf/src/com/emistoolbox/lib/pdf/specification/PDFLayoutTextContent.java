package com.emistoolbox.lib.pdf.specification;

import java.io.IOException;

public class PDFLayoutTextContent extends PDFLayoutContent {
	private String text;
	private PDFLayoutFont font;

	public PDFLayoutTextContent()
	{}
	
	public PDFLayoutTextContent(String text, PDFLayoutFont font)
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

	public PDFLayoutFont getFont () {
		return font;
	}

	public void setFont (PDFLayoutFont font) {
		this.font = font;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}
}
