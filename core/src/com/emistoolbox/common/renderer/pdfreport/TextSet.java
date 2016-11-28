package com.emistoolbox.common.renderer.pdfreport;

import java.util.Set;
import com.emistoolbox.common.ChartFont;

public interface TextSet 
{
	public void putText(String key, String value); 
	public void putText(String key, String value, ChartFont font); 
	public void putText(String key, String value, ChartFont font, String align); 
	
	public ChartFont getFont(String key); 
	public String getAlignment(String key); 
	public void putAlignment(String key, String align); 
	
	public String getText(String key); 
	public Set<String> getTextKeys(); 
}
