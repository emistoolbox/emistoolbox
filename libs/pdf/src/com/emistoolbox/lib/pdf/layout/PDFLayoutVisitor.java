package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public interface PDFLayoutVisitor<T> {
	T visit (PDFLayout page) throws IOException; 
	T visit (PDFLayoutFrameElement frameElement) throws IOException;
	T visit (PDFLayoutHighchartElement pdfElement) throws IOException;
	T visit (PDFLayoutImageElement pdfElement) throws IOException;
	T visit (PDFLayoutPDFElement pdfElement) throws IOException;
	T visit (PDFLayoutTableElement tableElement) throws IOException;
	T visit (PDFLayoutTextElement textElement);
}
