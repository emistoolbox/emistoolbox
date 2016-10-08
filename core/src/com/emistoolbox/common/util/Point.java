package com.emistoolbox.common.util;

import java.io.Serializable;

public class Point implements Serializable 
{
	public double x; 
	public double y; 
	
	public Point(double x, double y)
	{
		this.x = x; 
		this.y = y; 
	} 
	
	public int getIntX()
	{ return (int) Math.round(x); } 
	
	public int getIntY()
	{ return (int) Math.round(y); }
}
