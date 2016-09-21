package com.emistoolbox.lib.pdf.specification;

import java.io.IOException;

import info.joriki.graphics.Rectangle;
import info.joriki.io.SeekableByteArray;
import info.joriki.io.Util;
import info.joriki.pdf.PDFDictionary;
import info.joriki.pdf.PDFDocument;
import info.joriki.pdf.PDFFile;
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

	private PDFDocument document;
	private PDFDocument getDocument () throws IOException {
		if (document == null)
			document = new PDFFile (new SeekableByteArray (Util.toByteArray (input.getInputStream ()))).getDocument ();
		return document;
	}

	public PDFDictionary getPage () throws IOException {
		return getDocument ().getPage (1);
	}

	public Rectangle getBoundingBox () throws IOException {
		return getPage ().getMediaBox ().toRectangle ();
	}
}
