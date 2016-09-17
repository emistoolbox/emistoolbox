package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutTextContent extends PDFLayoutContent {
	private String text;
	private PDFLayoutFont font;

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
}
