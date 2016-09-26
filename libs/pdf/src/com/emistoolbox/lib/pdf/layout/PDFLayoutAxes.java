package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutAxes<T> {
	T [] axes;
	
	public PDFLayoutAxes (T ... axes) {
		if (axes.length != 2)
			throw new IllegalArgumentException ();
		this.axes = axes;
	}
	
	public T getHorizontal () {
		return axes [0];
	}

	public void setHorizontal (T t) {
		axes [0] = t;
	}
	
	public T getVertical () {
		return axes [1];
	}
	
	public void setVertical (T t) {
		axes [1] = t;
	}
	
	public T [] getAxes () {
		return axes;
	}
	
	public void setAxes (T [] axes) {
		this.axes = axes;
	}
}
