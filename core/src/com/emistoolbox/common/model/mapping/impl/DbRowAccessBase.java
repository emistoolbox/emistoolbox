package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public abstract class DbRowAccessBase implements DbRowAccess, Serializable
{
    private static final long serialVersionUID = 1L;

    public int getValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
        String value = getValue(valueIndex, row);
        if (value != null)
            try
            {
                return (int) Math.round(Double.parseDouble(value));
            }
            catch (NumberFormatException ex)
            {
            }
        return defaultValue;
    }

    public void addColumns(Set<String> columns)
    {}

    public void map(DbRowAccessFn callback)
    { callback.fn(this); }
    
    public static void map(DbRowAccessFn callback, DbRowAccess[] accesses)
    {
        for (DbRowAccess a : accesses)
            DbRowAccessBase.map(callback, a); 
    }

    public static void map(DbRowAccessFn callback, DbRowAccess access)
    {
        if (access != null)
            access.map(callback);  
    }

    public static void updateDimensions(DbRowAccess access)
    {
    	DbRowAccessFn fn = new DbRowAccessFn() 
    	{
			@Override
			public void fn(DbRowAccess access) 
			{
		    	if (access instanceof DbRowArrayAccessImpl)
		    	{
		    		DbRowArrayAccessImpl arrayAccess = (DbRowArrayAccessImpl) access; 
		    		DbRowAccess indexes[] = arrayAccess.getIndexAccess(); 
		    		int[] dimensions = arrayAccess.getDimensions();  
		    		for (int i = 0; i < indexes.length; i++)
		    		{
		    			if (i < dimensions.length)
		    				dimensions[i] = getEnumSize(indexes[i], dimensions[i]); 
		    		}
		    		
		    		arrayAccess.updateDimensions(dimensions);
		    	}
			}
    	};
    	
    	map(fn, access); 
    }
    
	private static int getEnumSize(DbRowAccess access, int defaultSize)
	{
		if (access instanceof DbRowEnumAccess)
		{
			EmisMetaEnum enumType = ((DbRowEnumAccess) access).getEnumType();
			if (enumType != null)
				return enumType.getSize(); 
		}
		
		return defaultSize; 
	}
}
