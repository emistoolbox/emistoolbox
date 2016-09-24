package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

import es.jbauer.lib.io.IOInput;

public class PDFLayoutPDFContent extends PDFLayoutContent {
	private IOInput input;

	public PDFLayoutPDFContent (IOInput input) {
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
