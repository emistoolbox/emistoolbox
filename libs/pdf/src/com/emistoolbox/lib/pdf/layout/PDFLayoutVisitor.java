package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;

public interface PDFLayoutVisitor<T> {
	T visit (PDFLayoutFrameElement frameElement) throws IOException;
	T visit (PDFLayoutPDFElement pdfElement) throws IOException;
	T visit (PDFLayoutTextElement textElement);
}
