package com.emistoolbox.lib.pdf.layout;

import info.joriki.graphics.Rectangle;

public interface PDFLayoutBoundingBoxVisitor {
	Rectangle visit (PDFLayoutFrame frame);
	Rectangle visit (PDFLayoutPDFContent pdfContent);
	Rectangle visit (PDFLayoutTextContent textContent);
}
