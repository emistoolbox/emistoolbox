package com.emistoolbox.lib.pdf.specification;

import info.joriki.graphics.Rectangle;

public interface PDFLayoutBoundingBoxVisitor {
	Rectangle visit (PDFLayoutFrame frame);
	Rectangle visit (PDFLayoutPDFContent pdfContent);
	Rectangle visit (PDFLayoutTextContent textContent);
}
