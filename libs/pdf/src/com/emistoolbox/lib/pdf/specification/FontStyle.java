package com.emistoolbox.lib.pdf.specification;

public class FontStyle {
	final static int italicFlag = 1 << 0;
	final static int boldFlag   = 1 << 1;

	public final static FontStyle PLAIN       = new FontStyle (0); 
	public final static FontStyle BOLD        = new FontStyle (boldFlag); 
	public final static FontStyle ITALIC      = new FontStyle (italicFlag); 
	public final static FontStyle BOLD_ITALIC = new FontStyle (boldFlag | italicFlag); 
	
	int attributes;

	private FontStyle (int attributes) {
		this.attributes = attributes;
	}
	
	public boolean isItalic () {
		return (attributes & italicFlag) != 0;
	}

	public boolean isBold () {
		return (attributes & boldFlag) != 0;
	}
}
