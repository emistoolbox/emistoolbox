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

	private int asInt(double value)
	{ return (int) Math.round(value); }
	
	public double getLeft() 
	{ return left; }
	
	public int getIntLeft()
	{ return asInt(left); } 

	public void setLeft(double left) 
	{ this.left = left; }

	public double getTop() 
	{ return top; }

	public int getIntTop()
	{ return asInt(top); } 

	public void setTop(double top) 
	{ this.top = top; }

	public double getRight() 
	{ return right; }

	public int getIntRight()
	{ return asInt(right); } 

	public void setRight(double right) 
	{ this.right = right; }

	public double getBottom() 
	{ return bottom; }

	public int getIntBottom()
	{ return asInt(bottom); } 

	public void setBottom(double bottom) 
	{ this.bottom = bottom; }
	
	public double getWidth()
	{ return right - left; } 
	
	public int getIntWidth()
	{ return asInt(getWidth()); } 
	
	public double getHeight()
	{ return bottom - top; }
	
	public int getIntHeight()
	{ return asInt(getHeight()); } 
}
