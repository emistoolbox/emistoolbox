package com.emistoolbox.server.util;

import java.util.HashMap;

import java.util.Map;

import org.stringtemplate.v4.ST;

public class TemplateEngine 
{
	private Map<String, Object> context = new HashMap<String, Object>(); 
	
	public void put(String key, Object value)
	{ context.put(key,  value); }
	
	public Object get(String key)
	{ return context.get(key); } 
	
	public void remove(String key)
	{ context.remove(key); }
	
	public void remove(String[] keys)
	{
		for (String key : keys)
			remove(key); 
	}
	
	public void clear()
	{ context.clear(); } 
	
	public String render(String template)
	{
		if (template == null)
			return null; 
		
		ST st = new ST(template, '{', '}'); 
		for (Map.Entry<String, Object> entry : context.entrySet())
		{
			if (entry.getValue() != null)
				st.add(entry.getKey(), entry.getValue()); 
		}
		
		return st.render(); 
	}
}
