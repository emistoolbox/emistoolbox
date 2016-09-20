package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutAlignmentPlacement extends PDFLayoutPlacement {
	private PDFLayoutHorizontalAlignment horizontalAlignment;
	private PDFLayoutVerticalAlignment verticalAlignment;

	public PDFLayoutAlignmentPlacement()
	{}
	
	public PDFLayoutAlignmentPlacement(PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical)
	{
		this.horizontalAlignment = horizontal; 
		this.verticalAlignment = vertical; 
	}
	
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
