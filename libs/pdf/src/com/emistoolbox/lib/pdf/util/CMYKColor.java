package com.emistoolbox.lib.pdf.util;

import java.awt.Color;

public class CMYKColor extends Color {
	public CMYKColor (float ... components) {
		super (CMYKColorSpace.CMYK,components,1);
	}
}
