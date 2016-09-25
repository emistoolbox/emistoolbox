package com.emistoolbox.lib.pdf.layout;

import info.joriki.graphics.Rectangle;

public interface PDFLayoutBoundingBoxVisitor {
	Rectangle visit (PDFLayoutFrameElement frameElement);
	Rectangle visit (PDFLayoutPDFElement pdfElement);
	Rectangle visit (PDFLayoutTextElement textElement);
}
