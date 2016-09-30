package com.emistoolbox.lib.pdf.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFLayoutFrameElement extends PDFLayoutElement {
	private double width;
	private double height;
	private List<PDFLayoutElement> elements = new ArrayList<PDFLayoutElement>();

	public PDFLayoutFrameElement () {
	}
	
	public PDFLayoutFrameElement (double width,double height) {
		this.width = width;
		this.height = height;
	}

	public PDFLayoutFrameElement (double width,double height,List<PDFLayoutElement> elements) {
		this.width = width;
		this.height = height;
		this.elements = elements;
	}

	public double getWidth () {
		return width;
	}

	public void setWidth (double width) {
		this.width = width;
	}

	public double getHeight () {
		return height;
	}

	public void setHeight (double height) {
		this.height = height;
	}

	public <T> T accept (PDFLayoutVisitor<T> visitor) throws IOException {
		return visitor.visit (this);
	}

	public List<PDFLayoutElement> getElements () {
		return elements;
	}

	public void setElements (List<PDFLayoutElement> elements) {
		this.elements = elements;
	}
	
	public void addElement(PDFLayoutElement element) {
		if (element == null)
			return; 
		
		if (elements == null)
			elements = new ArrayList<PDFLayoutElement>(); 
		
		elements.add(element); 
	}
}
