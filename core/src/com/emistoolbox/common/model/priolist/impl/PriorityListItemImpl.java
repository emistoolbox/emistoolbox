package com.emistoolbox.common.model.priolist.impl;

import com.emistoolbox.common.model.priolist.PriorityListItem;

public class PriorityListItemImpl implements PriorityListItem
{
    private int id;
    private String name;
    private String[] entityValues; 
    private double[] values;

    public int getId()
    { return this.id; }

    public String getName()
    { return this.name; }

    public double[] getValues()
    { return this.values; }

    public String[] getEntityValues()
    { return this.entityValues; } 
    
    public void setId(int id)
    { this.id = id; }

    public void setName(String name)
    { this.name = name; }

    public void setValues(double[] values)
    { this.values = values; }
    
    public void setEntityValues(String[] values)
    { this.entityValues = values; }
    
    public boolean isEmpty()
    {
    	for (double value : values)
    	{
    		if (!Double.isNaN(value) && value >= 0.0)
    			return false; 
    	}
    	
    	return true; 
    }
}
