package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutComponent {
	private PDFLayoutContent content;
	private PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;
	private PDFLayoutPlacement placement;

	public PDFLayoutComponent()
	{}

	public PDFLayoutComponent(PDFLayoutContent content, PDFLayoutObjectFit fit, PDFLayoutHorizontalAlignment horizontal, PDFLayoutVerticalAlignment vertical)
	{ this(content, fit, new PDFLayoutAlignmentPlacement(horizontal, vertical)); }

	public PDFLayoutComponent(PDFLayoutContent content, PDFLayoutObjectFit fit, double x, double y)
	{ this(content, fit, new PDFLayoutCoordinatePlacement(x, y)); }
	
	public PDFLayoutComponent(PDFLayoutContent content, PDFLayoutObjectFit fit, PDFLayoutPlacement placement)
	{
		this.content = content; 
		if (fit != null)
			this.objectFit = fit; 
		
		this.placement = placement; 
	}
	
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
