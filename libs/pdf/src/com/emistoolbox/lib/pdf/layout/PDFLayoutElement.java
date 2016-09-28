package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

abstract public class PDFLayoutElement implements Serializable {
	private PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;
	private PDFLayoutPlacement placement;
	private PDFLayoutSides<Double> padding;
	private PDFLayoutBorderStyle borderStyle;
	private PDFLayoutAxes<Boolean> displacement = new PDFLayoutAxes<Boolean> (false,false);
	private Color backgroundColor;

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

	public PDFLayoutSides<Double> getPadding () {
		return padding;
	}

	public void setPadding (PDFLayoutSides<Double> padding) {
		this.padding = padding;
	}

	public PDFLayoutBorderStyle getBorderStyle () {
		return borderStyle;
	}

	public void setBorderStyle (PDFLayoutBorderStyle borderStyle) {
		this.borderStyle = borderStyle;
	}

	public PDFLayoutAxes<Boolean> getDisplacement () {
		return displacement;
	}

	public void setDisplacement (PDFLayoutAxes<Boolean> displacement) {
		this.displacement = displacement;
	}

	public PDFLayoutFrameElement wrap (double width,double height) {
		align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.CENTER);
		setObjectFit (PDFLayoutObjectFit.CONTAIN);
		return new PDFLayoutFrameElement (width,height,Collections.singletonList (this));
	}

	public PDFLayoutElement position (double x,double y) {
		setPlacement (new PDFLayoutCoordinatePlacement (x,y));
		return this;
	}

	public PDFLayoutElement align (PDFLayoutHorizontalAlignment horizontalAlignment,PDFLayoutVerticalAlignment verticalAlignment) {
		setPlacement (new PDFLayoutAlignmentPlacement (horizontalAlignment,verticalAlignment));
		return this;
	}

	public PDFLayoutElement displace (boolean horizontally,boolean vertically) {
		setDisplacement (new PDFLayoutAxes<Boolean> (horizontally,vertically));
		return this;
	}

	public PDFLayoutElement fit (PDFLayoutObjectFit objectFit) {
		setObjectFit (objectFit);
		return this;
	}

	public Color getBackgroundColor () {
		return backgroundColor;
	}

	public void setBackgroundColor (Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	abstract public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException;
}
