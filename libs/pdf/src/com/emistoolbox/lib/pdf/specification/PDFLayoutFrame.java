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
}
