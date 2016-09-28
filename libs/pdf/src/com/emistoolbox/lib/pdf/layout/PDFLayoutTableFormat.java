package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;

public class PDFLayoutTableFormat {
	PDFLayoutFont font;
	Color backgroundColor;
	PDFLayoutPlacement placement;
	PDFLayoutObjectFit objectFit;

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
}
