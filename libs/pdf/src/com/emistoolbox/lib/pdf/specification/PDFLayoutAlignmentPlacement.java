package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutAlignmentPlacement extends PDFLayoutPlacement {
	private PDFLayoutHorizontalAlignment horizontalAlignment;
	private PDFLayoutVerticalAlignment verticalAlignment;

	public PDFLayoutHorizontalAlignment getHorizontalAlignment () {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment (PDFLayoutHorizontalAlignment horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public PDFLayoutVerticalAlignment getVerticalAlignment () {
		return verticalAlignment;
	}

	public void setVerticalAlignment (PDFLayoutVerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}
}
