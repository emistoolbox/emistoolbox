package com.emistoolbox.lib.pdf.specification;

public class PDFLayoutSides<T> {
	private T left;
	private T right;
	private T top;
	private T bottom;

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
