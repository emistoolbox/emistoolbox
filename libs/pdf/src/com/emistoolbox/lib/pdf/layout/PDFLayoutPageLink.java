package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutPageLink extends PDFLayoutLink {
	PDFLayout targetPage;

	public PDFLayoutPageLink (PDFLayout targetPage) {
		this.targetPage = targetPage;
	}

	public PDFLayout getTargetPage () {
		return targetPage;
	}

	public void setTargetPage (PDFLayout targetPage) {
		this.targetPage = targetPage;
	}
}
