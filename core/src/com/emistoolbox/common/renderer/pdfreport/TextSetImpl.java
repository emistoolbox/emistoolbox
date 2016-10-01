package com.emistoolbox.common.renderer.pdfreport;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.ChartFont;

public class TextSetImpl implements TextSet, Serializable
{
	private Set<String> keys = new HashSet<String>(); 
	private Map<String, String> texts = new HashMap<String, String>(); 
	private Map<String, ChartFont> fonts= new HashMap<String, ChartFont>(); 
	
	public TextSetImpl(String[] keys)
	{ Collections.addAll(this.keys, keys); }
	
	public void putText(String key, String value)
	{
		if (!keys.contains(key))
			return; 
		
		texts.put(key, value); 
	}
	
	public String getText(String key)
	{ return texts.get(key); } 
	
	public Set<String> getTextKeys()
	{ return keys; }

	@Override
	public void putText(String key, String value, ChartFont font) 
	{
		putText(key, value); 
		fonts.put(key, font); 
	}

	@Override
	public ChartFont getFont(String key) 
	{
		ChartFont result = fonts.get(key); 
		if (result == null)
			return null; 
		
		return result;
	} 

}
