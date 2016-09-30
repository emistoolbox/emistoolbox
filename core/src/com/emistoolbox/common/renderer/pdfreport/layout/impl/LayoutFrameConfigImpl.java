package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.io.Serializable;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Rectangle;

public class LayoutFrameConfigImpl extends TextSetImpl implements LayoutFrameConfig, Serializable
{
	private Rectangle position = new Rectangle(); 
	private LayoutSides<BorderStyle> borders = new LayoutSides<BorderStyle>(new BorderStyle(0, null)); 
	private int borderRadius = 0; 
	
	private String backgroundImage; 
	private ChartColor backgroundColour; 
	private int backgroundTransparency = 0;

	private LayoutSides<Double> padding = new LayoutSides<Double>(0.0); 
	
	private PdfContentConfig content; 
	
	public LayoutFrameConfigImpl()
	{ super(TEXT_KEYS); } 
	
	@Override
	public Rectangle getPosition() 
	{ return position; } 

	@Override
	public void setPosition(Rectangle position) 
	{ this.position = position; } 

	@Override
	public LayoutSides<BorderStyle> getBorders() 
	{ return this.borders; } 

	@Override
	public void setBorders(LayoutSides<BorderStyle> borders) 
	{ this.borders = borders; } 

	@Override
	public int getBorderRadius() 
	{ return borderRadius; } 

	@Override
	public void setBorderRadius(int radius) 
	{ this.borderRadius = radius; }

	@Override
	public ChartColor getBackgroundColour() 
	{ return backgroundColour; } 

	@Override
	public void setBackgroundColour(ChartColor color) 
	{ this.backgroundColour = color; } 

	@Override
	public String getBackgroundImagePath() 
	{ return this.backgroundImage; }

	@Override
	public void setBackgroundImagePath(String image) 
	{ this.backgroundImage = image; } 

	@Override
	public int getBackgroundTransparency() 
	{ return backgroundTransparency; } 

	@Override
	public void setBackgroundTransparency(int transparency) 
	{ this.backgroundTransparency = transparency; } 

	@Override
	public PdfContentConfig getContentConfig() 
	{ return content; }

	@Override
	public void setContentConfig(PdfContentConfig content) 
	{ this.content = content; }

	public LayoutSides<Double> getPadding() 
	{ return padding; }

	public void setPadding(LayoutSides<Double> padding) 
	{ this.padding = padding; }
}
