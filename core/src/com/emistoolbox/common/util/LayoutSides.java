package com.emistoolbox.common.util;

import java.io.Serializable;

public class LayoutSides<T> implements Serializable
{
	private T left; 
	private T top; 
	private T right; 
	private T bottom;
	
	public T getLeft() 
	{ return left; }

	public void setLeft(T left) 
	{ this.left = left; }
	
	public T getTop() 
	{ return top; }
	
	public void setTop(T top) 
	{ this.top = top; }
	
	public T getRight() 
	{ return right; }
	
	public void setRight(T right) 
	{ this.right = right; }
	
	public T getBottom() 
	{ return bottom; }
	
	public void setBottom(T bottom) 
	{ this.bottom = bottom; } 
}
