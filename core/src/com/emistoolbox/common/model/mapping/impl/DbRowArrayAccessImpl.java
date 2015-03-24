package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.FlatArrayUtil;
import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.mapping.DbRowArrayAccess;
import com.emistoolbox.common.model.mapping.EmisAccessException;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class DbRowArrayAccessImpl extends DbRowAccessBase implements DbRowArrayAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private int[] dimensions;
    private DbRowAccess[] indexAccess;
    private DbRowAccess valueAccess;

    public int getIndex(int valueIndex, Map<String, String> row)
    	throws EmisAccessException
    {
        int[] indexes = new int[this.dimensions.length];
        for (int dim = 0; dim < this.dimensions.length; dim++)
        {
            indexes[dim] = this.indexAccess[dim].getValueAsInt(valueIndex, 0, row);
            if (indexes[dim] == -1)
            	throw new EmisAccessException(indexAccess[dim], "Value not found."); 

            if (indexes[dim] >= dimensions[dim])
            	throw new EmisAccessException(indexAccess[dim], "Index (" + indexes[dim] + ") too big for enum (" + dimensions[dim] + ").");
        }
        
        return FlatArrayUtil.getFlatIndex(indexes, this.dimensions);
    }

    public DbRowAccess[] getIndexAccess()
    { return this.indexAccess; }

    public DbRowAccess getValueAccess()
    { return this.valueAccess; }

    public void setIndexAccess(DbRowAccess[] access)
    { this.indexAccess = access; }

    public void setValueAccess(DbRowAccess access)
    { this.valueAccess = access; }

    public String getValue(int valueIndex, Map<String, String> row)
    { return this.valueAccess.getValue(valueIndex, row); }

    public int getValuesPerRow()
    { return this.valueAccess.getValuesPerRow(); }

    public void setDimensions(int[] dimensions)
    {
        this.dimensions = dimensions;
        this.indexAccess = new DbRowAccess[dimensions.length];
    }
    
    public void updateDimensions(int[] dimensions)
    {
    	if (indexAccess == null || dimensions.length != indexAccess.length)
    		throw new IllegalArgumentException("Dimension length needs to match indexes"); 
    	
    	this.dimensions = dimensions; 
    } 

    public int[] getDimensions()
    { return this.dimensions; }

    public void addColumns(Set<String> columns)
    {
        if (this.valueAccess != null)
            this.valueAccess.addColumns(columns);

        for (DbRowAccess access : this.indexAccess)
        {
            if (access != null)
                access.addColumns(columns);
        }
    }

    @Override
    public void map(DbRowAccessFn callback)
    {
        super.map(callback);
        DbRowAccessBase.map(callback, indexAccess); 
        DbRowAccessBase.map(callback, valueAccess); 
    }
    
    public String getInfo()
    {
    	StringBuffer result = new StringBuffer("array("); 
    	String delim = ""; 
    	for (DbRowAccess access : indexAccess)
    	{
    		result.append(delim); 
    		result.append(access == null ? "(none)" : access.getInfo()); 
    		delim = ", "; 
    	}
    	
    	result.append(")="); 
    	result.append(valueAccess == null ? "(none)" : valueAccess.getInfo());
    	
    	return result.toString(); 
    }
}
