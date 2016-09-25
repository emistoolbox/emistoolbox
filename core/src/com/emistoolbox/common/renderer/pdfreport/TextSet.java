package com.emistoolbox.common.renderer.pdfreport;

import java.util.Set;

public interface TextSet 
{
	public void putText(String key, String value); 
	public String getText(String key); 
	public Set<String> getTextKeys(); 
}
