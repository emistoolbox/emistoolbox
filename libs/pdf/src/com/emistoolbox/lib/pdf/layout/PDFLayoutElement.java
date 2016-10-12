package com.emistoolbox.lib.pdf.layout;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

abstract public class PDFLayoutElement implements Serializable {
	private PDFLayoutObjectFit objectFit = PDFLayoutObjectFit.NONE;
	private PDFLayoutPlacement placement = PDFLayoutPlacement.CENTER;
	private PDFLayoutSides<Double> padding;
	private PDFLayoutBorderStyle borderStyle;
	private PDFLayoutAxes<Boolean> displacement = new PDFLayoutAxes<Boolean> (false,false);
	private Color backgroundColor;
	private PDFLayoutLink link;
	private int rotation; // in multiples of 90 degrees

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

	public PDFLayoutLink getLink () {
		return link;
	}

	public void setLink (PDFLayoutLink link) {
		this.link = link;
	}

	public int getRotation () {
		return rotation;
	}

	public void setRotation (int rotation) {
		this.rotation = rotation;
	}

	public PDFLayoutElement pad (double padding)
	{ return pad (padding, padding); }
	
	public PDFLayoutElement pad (double xpad, double ypad)
	{ return pad (xpad, ypad, xpad, ypad); }
	
	public PDFLayoutElement pad (double left, double top, double right, double bottom)
	{
		this.setPadding(new PDFLayoutSides<Double>(new Double[] { left, top, right, bottom })); 
		return this; 
	}
	
	public PDFLayoutFrameElement wrap (double width,double height) {
		align (PDFLayoutHorizontalAlignment.CENTER,PDFLayoutVerticalAlignment.CENTER);
		setObjectFit (PDFLayoutObjectFit.CONTAIN);
		return new PDFLayoutFrameElement (width,height,Collections.singletonList (this));
	}

	public PDFLayoutElement position (double x,double y) {
		setPlacement (new PDFLayoutPlacement (new PDFLayoutCoordinatePlacement(x), new PDFLayoutCoordinatePlacement (y)));
		return this;
	}
	
	public PDFLayoutElement rotate (int rightAngles) {
		setRotation (rightAngles);
		return this;
	}
	
	public PDFLayoutElement color (Color color) {
		setBackgroundColor (color);
		return this;
	}

	public PDFLayoutElement align (PDFLayoutHorizontalPlacement horizontalPlacement,PDFLayoutVerticalPlacement verticalPlacement) {
		setPlacement (new PDFLayoutPlacement (horizontalPlacement,verticalPlacement));
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
	
	public PDFLayoutElement border (double width) {
		setBorderStyle (new PDFLayoutBorderStyle (width));
		return this;
	}
	
	public PDFLayoutElement link (PDFLayoutLink link) {
		setLink (link);
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
