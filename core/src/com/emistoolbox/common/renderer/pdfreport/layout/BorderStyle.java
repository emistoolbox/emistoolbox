package com.emistoolbox.common.renderer.pdfreport.layout;

import java.io.Serializable;

import com.emistoolbox.common.ChartColor;

public class BorderStyle implements Serializable
{
	private ChartColor color; 
	private int width;

	public BorderStyle()
	{}
	
	public BorderStyle(int width, ChartColor color)
	{
		this.color = color; 
		this.width = width; 
	}
	
	public ChartColor getColour() 
	{ return color; }
	
	public void setColor(ChartColor color) 
	{ this.color = color; }
	
	public int getWidth() 
	{ return width; }
	
	public void setWidth(int width) 
	{ this.width = width; } 
}
