package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

public class DbRowByColumnAccess extends DbRowAccessBase implements Serializable
{
    /** List of columns from which we get values and their associated constant values for other enums. */
    private List<ColumnConstValues> columns = new ArrayList<ColumnConstValues>();
    private Map<String, EmisMetaEnum> enums = new HashMap<String, EmisMetaEnum>();
    
    public int getValuesPerRow()
    { return columns.size(); }
    
    public DbRowByColumnAccess()
    {}

    public String getValue(int index, Map<String, String> dbRow)
    {
        String fieldName = getColumn(index).getColumnName(); 
        return dbRow.get(fieldName); 
    }
    
    public void addEnum(EmisMetaEnum enumType)
    { enums.put(enumType.getName(), enumType); } 
    
    public void removeEnum(String enumName)
    {
        enums.remove(enumName);
        for (ColumnConstValues col : columns)
            col.remove(enumName); 
    }
    
    public List<String> getColumnNames()
    {
        List<String> result = new ArrayList<String>(); 
        for (ColumnConstValues col : columns)
            result.add(col.getColumnName()); 

        Collections.sort(result);
        return result; 
    }

    public List<String> getEnumNames()
    {
        List<String> result = new ArrayList<String>(enums.keySet()); 
        Collections.sort(result); 
        return result; 
    } 
    
    public EmisMetaEnum getEnumType(String enumName)
    { return enums.get(enumName); } 

    public String getValue(String colName, String enumName)
    {
        ColumnConstValues col = findColumn(colName, false);
        if (col == null)
            return null; 
        
        return col.get(enumName); 
    }
    
    public void setValue(String colName, String enumName, String value)
    {
        if (value != null && getEnumType(enumName).getIndex(value) == -1)
            return; 
        
        findColumn(colName, true).put(enumName,  value); 
    }
    
    public ColumnConstValues getColumn(int index)
    { return columns.get(index); }
    
    public ColumnConstValues findColumn(String colName, boolean add)
    {
        for (ColumnConstValues col : columns)
        {
            if (col.getColumnName().equals(colName))
                return col; 
        }
        
        if (add)
        {
            ColumnConstValues col = new ColumnConstValues(colName); 
            columns.add(col); 
            return col; 
        }
        
        return null; 
    }
    
    public DbRowAccess getIndexAccess(EmisMetaEnum enumType)
    { return new DbRowByColumnIndexAccess(this, enumType); }

    public void addColumns(Set<String> columns)
    {
        for (ColumnConstValues col : this.columns)
            columns.add(col.getColumnName()); 
    }
    
    public String getInfo()
    { 
    	StringBuffer result = new StringBuffer("byColumn("); 
    	String delim = ""; 
    	for (ColumnConstValues values : columns)
    	{
    		result.append(delim); 
    		result.append(values.getColumnName()); 
    		delim = ","; 
    	}
    	result.append(")"); 
    	
    	return result.toString(); 
    }

    public static DbRowByColumnAccess getByColumnAccess(DbRowAccess access)
    {
    	if (access instanceof DbRowByColumnAccess)
    		return (DbRowByColumnAccess) access; 
    	
    	if (access instanceof DbRowEnumAccess)
    		return getByColumnAccess(((DbRowEnumAccess) access).getAccess()); 
    	
    	return null; 
    }
}
