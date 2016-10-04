package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutElementLink extends PDFLayoutLink {
	PDFLayoutElement targetElement;
	boolean zooming;

	public PDFLayoutElementLink (PDFLayoutElement targetElement,boolean zooming) {
		this.targetElement = targetElement;
		this.zooming = zooming;
	}

	public PDFLayoutElement getTargetElement () {
		return targetElement;
	}

	public void setTargetElement (PDFLayoutElement targetElement) {
		this.targetElement = targetElement;
	}

	public boolean isZooming () {
		return zooming;
	}

	public void setZooming (boolean zooming) {
		this.zooming = zooming;
	}
}
