package com.emistoolbox.lib.pdf.specification;

import info.joriki.graphics.Rectangle;

import java.awt.Color;
import java.util.List;

public class PDFLayoutFrame extends PDFLayoutContent {
	private List<PDFLayoutComponent> components;
	private Rectangle rectangle;
	private Double borderRadius;
	private PDFLayoutSides<Double>lineWidths;
	private PDFLayoutSides<Double> margins;
	private PDFLayoutSides<Color> colors;

	public List<PDFLayoutComponent> getComponents () {
		return components;
	}

	public void setComponents (List<PDFLayoutComponent> components) {
		this.components = components;
	}

	public Rectangle getRectangle () {
		return rectangle;
	}

	public void setRectangle (Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public Double getBorderRadius () {
		return borderRadius;
	}

	public void setBorderRadius (Double borderRadius) {
		this.borderRadius = borderRadius;
	}

	public PDFLayoutSides<Double> getLineWidths () {
		return lineWidths;
	}

	public void setLineWidths (PDFLayoutSides<Double> lineWidths) {
		this.lineWidths = lineWidths;
	}

	public PDFLayoutSides<Double> getMargins () {
		return margins;
	}

	public void setMargins (PDFLayoutSides<Double> margins) {
		this.margins = margins;
	}

	public PDFLayoutSides<Color> getColors () {
		return colors;
	}

	public void setColors (PDFLayoutSides<Color> colors) {
		this.colors = colors;
	}

	public Rectangle getBoundingBox () {
		return rectangle;
	}
}
