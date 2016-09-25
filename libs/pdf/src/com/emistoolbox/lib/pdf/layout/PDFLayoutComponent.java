package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutComponent {
	private PDFLayoutContent content;
	private PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;
	private PDFLayoutPlacement placement;
	private PDFLayoutSides<Double> padding;
	PDFLayoutBorderStyle borderStyle;

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

	public PDFLayoutBorderStyle getBorderStyle () {
		return borderStyle;
	}

	public void setBorderStyle (PDFLayoutBorderStyle borderStyle) {
		this.borderStyle = borderStyle;
	}

	public PDFLayoutSides<Double> getPadding () {
		return padding;
	}

	public void setPadding (PDFLayoutSides<Double> padding) {
		this.padding = padding;
	}
}
