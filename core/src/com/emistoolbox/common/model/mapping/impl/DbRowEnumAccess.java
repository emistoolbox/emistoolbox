package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class DbRowEnumAccess implements DbRowAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private int offset = 0;
    private boolean asValue = false;
    private boolean valueAsSet = false; 
    private Map<String, String> map = null;
    private DbRowAccess access;
    private EmisMetaEnum enumType;

    public DbRowEnumAccess() 
    {}

    public DbRowEnumAccess(DbRowAccess access, EmisMetaEnum enumType) 
    {
        this.enumType = enumType;
        setAccess(access);
    }

    public void setValueAsSet(boolean asSet)
    { this.valueAsSet = asSet; }
       
    public boolean isValueAsSet()
    { return valueAsSet; } 
    
    public int getOffset()
    { return this.offset; }

    public void setOffset(int offset)
    { this.offset = offset; }

    public boolean getAsValue()
    { return this.asValue; }

    public void setAsValue(boolean asValue)
    { this.asValue = asValue; }

    public String getValue(int valueIndex, Map<String, String> row)
    {
        int index = getValueAsInt(valueIndex, -1, row);
        if (index == -1)
        	return null; 
        
        if (valueAsSet)
        	return enumType.getSetValues(index); 
        else
            return enumType.getValue((byte) index);
    }

    public void addColumns(Set<String> columns)
    {
        if (this.access != null)
            this.access.addColumns(columns);
    }

    public int getValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
    	if (valueAsSet)
    		return getSetValueAsInt(valueIndex, defaultValue, row); 
    	else
    		return getSingleValueAsInt(valueIndex, defaultValue, row); 
    }
    
    private int getSetValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
    	if (!this.asValue)
    		return this.access.getValueAsInt(valueIndex, -1, row);
    	
    	String values = this.access.getValue(valueIndex, row); 
    	if (values == null || values.equals(""))
    		return defaultValue; 
    	
    	int result = 0; 
    	for (String value : values.split(","))
    	{
    		int index = getIndex(value, -1);
    		if (index != -1)
    			result |= 1 << index;     
    	}
    	
    	return result; 
    }
    
    private int getSingleValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
    	int index = -1;
        if (!this.asValue)
        {
            index = this.access.getValueAsInt(valueIndex, -1, row);
            if (index != -1)
                index -= this.offset;
        }

        if ((index < 0) || (index >= this.enumType.getSize()))
            return getIndex(access.getValue(valueIndex, row), defaultValue); 

        return index;
    }
    
    private int getIndex(String value, int defaultValue)
    {
    	if (value == null)
    		return defaultValue; 
    	
    	if (map != null && map.get(value) != null)
    		value = map.get(value);
    	
    	int result = enumType.getIndex(value);
    	if (result == -1)
    		result = defaultValue;
    		
    	return result; 
    }

    public int getValuesPerRow()
    { return this.access.getValuesPerRow(); }

    public Map<String, String> getMap()
    { return this.map; }

    public void setMap(Map<String, String> map)
    { this.map = map; }

    public void setAccess(DbRowAccess access)
    {
        this.access = access;
        if (access instanceof DbRowByColumnIndexAccess)
        {
            ((DbRowByColumnIndexAccess) access).setEnumType(enumType); 
            asValue = false; 
            offset = 0; 
        }
    }

    public DbRowAccess getAccess()
    { return this.access; }

    public EmisMetaEnum getEnumType()
    { return this.enumType; }

    public void map(DbRowAccessFn callback)
    {
        callback.fn(this);
        DbRowAccessBase.map(callback, access); 
    }

	@Override
	public String getInfo() 
	{ return "enum:" + enumType.getName() + "(" + access.getInfo() + ")"; }
}
