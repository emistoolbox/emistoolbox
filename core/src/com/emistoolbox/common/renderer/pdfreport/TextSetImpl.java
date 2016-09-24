package com.emistoolbox.common.renderer.pdfreport;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextSetImpl implements TextSet
{
	private Set<String> keys = new HashSet<String>(); 
	private Map<String, String> texts = new HashMap<String, String>(); 
	
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
}
