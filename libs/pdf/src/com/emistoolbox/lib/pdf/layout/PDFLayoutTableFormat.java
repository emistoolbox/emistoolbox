package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.Serializable;

public class PDFLayoutTableFormat implements Serializable {
	PDFLayoutFont font;
	Color backgroundColor;
	PDFLayoutPlacement placement;
	PDFLayoutSides<Double> padding;
	PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;

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
