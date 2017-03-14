package com.emistoolbox.lib.pdf.layout;

// only elliptical shadows implemented; others would be a lot more effort
public class PDFLayoutShadowStyle {
	// shift in pixels
	double shiftX;
	double shiftY;
	// shadow strength at darkest point, in [0,1]
	double strength;

	public PDFLayoutShadowStyle (double shiftX,double shiftY,double strength) {
		this.shiftX = shiftX;
		this.shiftY = shiftY;
		this.strength = strength;
	}
	
	public double getShiftX () {
		return shiftX;
	}
	
	public void setShiftX (double shiftX) {
		this.shiftX = shiftX;
	}
	
	public double getShiftY () {
		return shiftY;
	}
	
	public void setShiftY (double shiftY) {
		this.shiftY = shiftY;
	}
	
	public double getStrength () {
		return strength;
	}
	
	public void setStrength (double strength) {
		this.strength = strength;
	}
}
