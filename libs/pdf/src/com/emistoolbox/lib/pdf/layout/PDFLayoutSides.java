package com.emistoolbox.lib.pdf.layout;

public class PDFLayoutSides<T> {
	private T left;
	private T right;
	private T top;
	private T bottom;

	public PDFLayoutSides()
	{}
	
	public PDFLayoutSides(T value)
	{
		left = value; 
		right = value; 
		top = value; 
		bottom = value; 
	}
	
	public PDFLayoutSides(T[] values)
	{ set(values); } 
	
	public void set(T[] values)
	{
		if (values == null || values.length != 4)
			throw new IllegalArgumentException("Expected array of size == 4."); 
		
		left = values[0]; 
		top = values[1]; 
		right = values[2]; 
		bottom = values[3]; 
	}
	public T getLeft () {
		return left;
	}

	public void setLeft (T left) {
		this.left = left;
	}

	public T getRight () {
		return right;
	}

	public void setRight (T right) {
		this.right = right;
	}

	public T getTop () {
		return top;
	}

	public void setTop (T top) {
		this.top = top;
	}

	public T getBottom () {
		return bottom;
	}

	public void setBottom (T bottom) {
		this.bottom = bottom;
	}
}
