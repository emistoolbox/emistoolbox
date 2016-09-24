package com.emistoolbox.lib.pdf.specification;

import java.io.IOException;

public interface PDFLayoutVisitor<T> {
	T visit (PDFLayoutFrame frame) throws IOException;
	T visit (PDFLayoutPDFContent pdfContent) throws IOException;
	T visit (PDFLayoutTextContent textContent);
}
