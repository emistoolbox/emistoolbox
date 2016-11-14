package com.emistoolbox.common.renderer.pdfreport.layout;

import java.io.Serializable;

import com.emistoolbox.common.ChartColor;

public class BorderStyle implements Serializable
{
	public static BorderStyle DEFAULT_BORDER = new BorderStyle(1, ChartColor.BLACK.copy());
	
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
	
	public BorderStyle copy()
	{ 
		BorderStyle result = new BorderStyle(); 
		result.color = color == null ? null : color.copy(); 
		result.width = width; 
		
		return result; 
	}
}
