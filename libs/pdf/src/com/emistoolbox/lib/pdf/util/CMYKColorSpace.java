package com.emistoolbox.lib.pdf.util;

import java.awt.color.ColorSpace;

public class CMYKColorSpace extends ColorSpace {
	public final static CMYKColorSpace CMYK = new CMYKColorSpace ();
	
	public CMYKColorSpace () {
		super (ColorSpace.TYPE_CMYK,4);
	}

	// This is currently only used to hold CMYK components, but these should be reasonable conversion functions
	public float [] toRGB (float [] cmyk) {
		return new float [] {1 - cmyk [0] - cmyk [3],1 - cmyk [1] - cmyk [3],1 - cmyk [2] - cmyk [3]};
	}

	public float [] fromRGB (float [] rgb) {
		return new float [] {1 - rgb [0],1 - rgb [1],1 - rgb [2],0};
	}

	public float [] toCIEXYZ (float [] cmyk) {
		return ColorSpace.getInstance (CS_sRGB).toCIEXYZ (toRGB (cmyk));
	}

	public float [] fromCIEXYZ (float [] xyz) {
		return fromRGB (ColorSpace.getInstance (CS_sRGB).toRGB (xyz));
	}
}
