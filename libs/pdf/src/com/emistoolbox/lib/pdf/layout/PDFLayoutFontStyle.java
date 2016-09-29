package com.emistoolbox.lib.pdf.layout;

import java.io.Serializable;

public class PDFLayoutFontStyle implements Serializable {
	private final static int italicFlag = 1 << 0;
	private final static int boldFlag   = 1 << 1;

	public final static PDFLayoutFontStyle PLAIN       = new PDFLayoutFontStyle (0); 
	public final static PDFLayoutFontStyle BOLD        = new PDFLayoutFontStyle (boldFlag); 
	public final static PDFLayoutFontStyle ITALIC      = new PDFLayoutFontStyle (italicFlag); 
	public final static PDFLayoutFontStyle BOLD_ITALIC = new PDFLayoutFontStyle (boldFlag | italicFlag); 

	private int attributes;

	private PDFLayoutFontStyle (int attributes) {
		this.attributes = attributes;
	}

	public boolean isItalic () {
		return (attributes & italicFlag) != 0;
	}

	public boolean isBold () {
		return (attributes & boldFlag) != 0;
	}

	public String toString () {
		if (attributes == 0)
			return "plain";
		String string = "";
		if (isBold ())
			string += "Bold";
		if (isItalic ())
			string += "Italic";
		return string;
	}
}
