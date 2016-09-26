package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

import es.jbauer.lib.io.IOInput;

public class PDFLayoutImageElement extends PDFLayoutFileElement {

	public PDFLayoutImageElement(IOInput file)
	{ super(file); }

	@Override
	public <T> T accept(PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit(this);
	} 
	
	
}
