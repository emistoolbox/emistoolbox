package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.DbRowAccessFn;
import com.emistoolbox.common.model.mapping.DbRowMultipleAccess;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class DbRowMultipleAccessImpl extends DbRowAccessBase implements DbRowMultipleAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private String[] indexes;
    private DbRowAccess[] accesses;

    public String getValue(int valueIndex, Map<String, String> row)
    {
        return this.accesses[valueIndex].getValue(0, row);
    }

    public int getValuesPerRow()
    {
        return this.accesses.length;
    }

    public String[] getIndexes()
    {
        return this.indexes;
    }

    public DbRowAccess[] getAccesses()
    {
        return this.accesses;
    }

    public void setIndexes(String[] indexes)
    {
        this.indexes = indexes;
        this.accesses = new DbRowAccess[indexes.length];
    }

    public void setAccesses(DbRowAccess[] accesses)
    {
        this.accesses = accesses;
    }

    public DbRowAccess getIndexAccess()
    {
        DbRowAccessMultipleAccessIndex result = new DbRowAccessMultipleAccessIndex();
        result.setSize((byte) this.indexes.length);
        return result;
    }

    public void addColumns(Set<String> columns)
    {
        for (DbRowAccess access : this.accesses)
            access.addColumns(columns);
    }

    public void map(DbRowAccessFn callback)
    {
        super.map(callback);
        DbRowAccessBase.map(callback, accesses); 
    }

	@Override
	public String getInfo() 
	{
		StringBuffer result = new StringBuffer("multiple(");
		String delim = ""; 
		for (int i = 0; i < indexes.length; i++)
		{
			result.append(delim); 
			result.append(indexes[i]); 
			result.append("="); 
			result.append(accesses[i].getInfo()); 
			delim = ","; 
		}
		
		return result.toString(); 
	}
}
