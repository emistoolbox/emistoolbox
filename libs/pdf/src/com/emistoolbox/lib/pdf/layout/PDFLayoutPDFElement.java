package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

import es.jbauer.lib.io.IOInput;

public class PDFLayoutPDFElement extends PDFLayoutElement {
	private IOInput input;

	public PDFLayoutPDFElement (IOInput input) {
		this.input = input;
	}

	public IOInput getInput () {
		return input;
	}

	public void setInput (IOInput input) {
		this.input = input;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}
}
