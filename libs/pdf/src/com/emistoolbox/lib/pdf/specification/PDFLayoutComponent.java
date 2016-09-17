package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutComponent {
	private PDFLayoutContent content;
	private PDFLayoutObjectFit objectFit;
	private PDFLayoutPlacement placement;

	public PDFLayoutContent getContent () {
		return content;
	}

	public void setContent (PDFLayoutContent content) {
		this.content = content;
	}

	public PDFLayoutObjectFit getObjectFit () {
		return objectFit;
	}

	public void setObjectFit (PDFLayoutObjectFit objectFit) {
		this.objectFit = objectFit;
	}

	public PDFLayoutPlacement getPlacement () {
		return placement;
	}

	public void setPlacement (PDFLayoutPlacement placement) {
		this.placement = placement;
	}
}
