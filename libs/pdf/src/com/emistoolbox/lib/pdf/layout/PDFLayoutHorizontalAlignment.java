package com.emistoolbox.lib.pdf.layout;

public enum PDFLayoutHorizontalAlignment implements PDFLayoutHorizontalPlacement {
	LEFT,
	CENTER,
	RIGHT,
	PREVIOUS_LEFT,  //  left-aligned with the previous element
	PREVIOUS_RIGHT, // right-aligned with the previous element
	BEFORE,    // before (to the left of) the previous element
	AFTER      // after (to the right of) the previous element
}
