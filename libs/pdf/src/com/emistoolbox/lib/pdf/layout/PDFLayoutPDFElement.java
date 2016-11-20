package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

import es.jbauer.lib.io.IOInput;

public class PDFLayoutPDFElement extends PDFLayoutFileElement {
	private boolean cropping;

	public boolean isCropping () {
		return cropping;
	}

	public void setCropping (boolean cropping) {
		this.cropping = cropping;
	}

	public PDFLayoutPDFElement(IOInput file)
	{ super(file); } 
	
	@Override
	public <T> T accept(PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit(this); 
	}
}
