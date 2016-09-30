package com.emistoolbox.common;

import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.util.LayoutSides;

public class TableCellFormat 
{
	public enum VerticalAlign { TOP, CENTER, BOTTOM }; 
	public enum HorizontalAlign { LEFT, CENTER, RIGHT }; 
	private ChartFont font; 
	private ChartColor backgroundColor; 

	private LayoutSides<Double> padding = new LayoutSides<Double>(); 
	
	private VerticalAlign vAlign; 
	private HorizontalAlign hAlign;
	
	public ChartFont getFont() 
	{ return font; }

	public void setFont(ChartFont font) 
	{ this.font = font; }

	public ChartColor getBackgroundColor() 
	{ return backgroundColor; }
	
	public void setBackgroundColor(ChartColor backgroundColor) 
	{ this.backgroundColor = backgroundColor; }
	
	public LayoutSides<Double> getPadding() 
	{ return padding; }
	
	public void setPadding(LayoutSides<Double> padding) 
	{ this.padding = padding; }
	
	public VerticalAlign getVerticalAlign() 
	{ return vAlign; }
	
	public void setVerticalAlign(VerticalAlign vAlign) 
	{ this.vAlign = vAlign; }
	
	public HorizontalAlign getHorizontalAlign() 
	{ return hAlign; }
	
	public void setHorizontalAlign(HorizontalAlign hAlign) 
	{ this.hAlign = hAlign; } 	
	
	public TableCellFormat clone()
	{
		TableCellFormat result = new TableCellFormat();
		result.setBackgroundColor(getBackgroundColor());
		result.setFont(getFont());
		result.setHorizontalAlign(getHorizontalAlign());
		result.setVerticalAlign(getVerticalAlign());
		result.setPadding(getPadding().clone());
		
		return result; 
	}
}
