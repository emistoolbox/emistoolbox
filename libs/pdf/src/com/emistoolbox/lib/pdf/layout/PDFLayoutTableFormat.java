package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.Serializable;

public class PDFLayoutTableFormat implements Serializable {
	private PDFLayoutFont font;
	private Color backgroundColor;
	private PDFLayoutPlacement placement;
	private PDFLayoutSides<Double> padding;
	private PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;

	public PDFLayoutTableFormat () {}

	public PDFLayoutTableFormat (PDFLayoutTableFormat format) {
		this.font = format.font;
		this.backgroundColor = format.backgroundColor;
		this.placement = format.placement;
		this.padding = format.padding;
		this.objectFit = format.objectFit;
	}

	public PDFLayoutFont getFont () {
		return font;
	}

	public void setFont (PDFLayoutFont font) {
		this.font = font;
	}

	public Color getBackgroundColor () {
		return backgroundColor;
	}

	public void setBackgroundColor (Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public PDFLayoutPlacement getPlacement () {
		return placement;
	}

	public void setPlacement (PDFLayoutPlacement placement) {
		this.placement = placement;
	}

	public PDFLayoutObjectFit getObjectFit () {
		return objectFit;
	}

	public void setObjectFit (PDFLayoutObjectFit objectFit) {
		this.objectFit = objectFit;
	}

	public PDFLayoutSides<Double> getPadding () {
		return padding;
	}

	public void setPadding (PDFLayoutSides<Double> padding) {
		this.padding = padding;
	}
}
