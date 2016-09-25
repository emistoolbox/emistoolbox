package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public interface PDFLayoutVisitor<T> {
	T visit (PDFLayout page) throws IOException; 
	T visit (PDFLayoutComponent component) throws IOException;
	T visit (PDFLayoutFrame frame) throws IOException;
	T visit (PDFLayoutPDFContent pdfContent) throws IOException;
	T visit (PDFLayoutTextContent textContent);
}
