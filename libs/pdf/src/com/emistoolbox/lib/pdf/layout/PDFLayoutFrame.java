package com.emistoolbox.lib.pdf.layout;

import info.joriki.graphics.Rectangle;

import java.io.IOException;
import java.util.List;

public class PDFLayoutFrame extends PDFLayoutContent {
	private List<PDFLayoutComponent> components;
	private Rectangle rectangle;

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

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}
}
