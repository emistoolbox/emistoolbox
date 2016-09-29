package com.emistoolbox.common.util;

import java.io.Serializable;

public class Rectangle implements Serializable
{
	private double left; 
	private double top; 
	private double right; 
	private double bottom;
	
	public Rectangle()
	{}

	public Rectangle(double ... values)
	{
		left = values[0]; 
		top = values[1]; 
		right = values[2]; 
		bottom = values[3];
	}
	
	public double[] toDoubleArray()
	{ 
		double[] result = new double[4]; 
		result[0] = left; 
		result[1] = top; 
		result[2] = right; 
		result[3] = bottom; 
		
		return result; 
	}

	public double getLeft() 
	{ return left; }

	public void setLeft(double left) 
	{ this.left = left; }

	public double getTop() 
	{ return top; }

	public void setTop(double top) 
	{ this.top = top; }

	public double getRight() 
	{ return right; }

	public void setRight(double right) 
	{ this.right = right; }

	public double getBottom() 
	{ return bottom; }

	public void setBottom(double bottom) 
	{ this.bottom = bottom; }
	
	public double getWidth()
	{ return right - left; } 
	
	public double getHeight()
	{ return bottom - top; } 
}
