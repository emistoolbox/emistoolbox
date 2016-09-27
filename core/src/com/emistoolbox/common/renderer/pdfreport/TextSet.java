package com.emistoolbox.common.renderer.pdfreport;

import java.util.Set;

import com.emistoolbox.common.ChartFont;

public interface TextSet 
{
	public ChartFont getDefaultFont(); 
	public void setDefaultFont(ChartFont font); 
	
	public void putText(String key, String value); 
	public void putText(String key, String value, ChartFont font); 
	public ChartFont getFont(String key); 

	public String getText(String key); 
	public Set<String> getTextKeys(); 
}
