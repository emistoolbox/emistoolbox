package com.emistoolbox.common.util.impl;

import com.emistoolbox.common.util.Named;

import java.io.Serializable;

public class NamedImpl implements Named, Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    
    protected String normalize(String name)
    {
    	if (name == null)
    		return null; 
    	
    	int startPos = name.indexOf("'");
    	int endPos = name.indexOf("'", startPos + 1); 
    	while (startPos != -1 || endPos != -1)
    	{
    		if (endPos == -1)
    			return name; 
    		
    		String tmp = name.substring(0, startPos);
    		if (endPos + 1 != name.length())
    			tmp += name.substring(endPos + 1); 

    		name = tmp; 
    		
    		startPos = name.indexOf("'"); 
    		endPos = name.indexOf("'", startPos + 1); 
    	}
    	
    	return name.trim(); 
    }
}
