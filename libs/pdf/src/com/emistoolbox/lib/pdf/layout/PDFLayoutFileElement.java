package com.emistoolbox.lib.pdf.layout;

import es.jbauer.lib.io.IOInput;

public abstract class PDFLayoutFileElement extends PDFLayoutElement {
	private IOInput input;

	public PDFLayoutFileElement (IOInput input) {
		this.input = input;
	}

	public IOInput getInput () {
		return input;
	}

	public void setInput (IOInput input) {
		this.input = input;
	}
}
