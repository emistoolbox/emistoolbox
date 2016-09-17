package com.emistoolbox.lib.pdf.specification;

import es.jbauer.lib.io.IOInput;

public class PDFLayoutPDFContent extends PDFLayoutContent {
	private IOInput input;

	public IOInput getInput () {
		return input;
	}

	public void setInput (IOInput input) {
		this.input = input;
	}
}
