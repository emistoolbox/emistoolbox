package com.emistoolbox.lib.pdf.layout;

public enum PDFLayoutVerticalAlignment implements PDFLayoutVerticalPlacement {
	TOP,
	CENTER,
	BOTTOM,
	PREVIOUS_TOP,    //    top-aligned with the previous element
	PREVIOUS_BOTTOM, // bottom-aligned with the previous element
	ABOVE,                         // above the previous element
	BELOW  						   // below the previous element
}
