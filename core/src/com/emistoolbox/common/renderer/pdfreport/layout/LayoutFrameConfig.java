package com.emistoolbox.common.renderer.pdfreport.layout;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.util.LayoutSides;
import com.emistoolbox.common.util.Rectangle;

public interface LayoutFrameConfig extends TextSet
{
	public static final String[] TEXT_KEYS = new String[] {PdfText.TEXT_TITLE, PdfText.TEXT_SUBTITLE, PdfText.TEXT_FOOTER}; 

	public Rectangle getPosition(); 
	public void setPosition(Rectangle dimension); 
	
	public LayoutSides<BorderStyle> getBorders(); 
	public void setBorders(LayoutSides<BorderStyle> borders); 
	
	public int getBorderRadius(); 
	public void setBorderRadius(int radius); 

	public ChartColor getBackgroundColour(); 
	public void setBackgroundColour(ChartColor color); 

	public String getBackgroundImagePath(); 
	public void setBackgroundImagePath(String path); 
	
	public PdfContentConfig getContentConfig(); 
	public void setContentConfig(PdfContentConfig content); 
	
	public LayoutSides<Double> getPadding(); 
	public void setPadding(LayoutSides<Double> padding); 
}
