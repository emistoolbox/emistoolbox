package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.Map;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

public class DbRowByColumnIndexAccess extends DbRowAccessBase implements Serializable
{
    private DbRowByColumnAccess valueAccess; 
    private EmisMetaEnum enumType; 

    public DbRowByColumnIndexAccess()
    {}
    
    public DbRowByColumnIndexAccess(EmisMetaEnum enumType)
    { this.enumType = enumType; }
    
    public DbRowByColumnIndexAccess(DbRowByColumnAccess valueAccess, EmisMetaEnum enumType)
    {
        this.enumType = enumType; 
        this.valueAccess = valueAccess; 
    }
    
    public void setEnumType(EmisMetaEnum enumType)
    { this.enumType = enumType; } 
    
    public void setByColumnAccess(DbRowByColumnAccess valueAccess)
    { this.valueAccess = valueAccess; } 
    
    public int getValuesPerRow()
    { return valueAccess.getValuesPerRow(); }

    public String getValue(int index, Map<String, String> row)
    { return "" + getValueAsInt(index, -1, row); }

    public int getValueAsInt(int valueIndex, int defaultValue, Map<String, String> row)
    {
        ColumnConstValues values = valueAccess.getColumn(valueIndex); 
        String value = values.get(enumType.getName()); 
        if (value == null)
            return defaultValue; 
        
        int result = enumType.getIndex(value);
        if (result == -1)
            return defaultValue; 
        
        return result; 
    }
    
    public EmisMetaEnum getEnumType()
    { return enumType; } 
    
    public String getInfo()
    { return "byColumnIndex"; }

    public static DbRowByColumnIndexAccess getByColumnIndexAccess(DbRowAccess access)
    {
    	if (access instanceof DbRowByColumnIndexAccess)
    		return (DbRowByColumnIndexAccess) access; 
    	
    	if (access instanceof DbRowEnumAccess)
    		return getByColumnIndexAccess(((DbRowEnumAccess) access).getAccess()); 
    	
    	return null; 
    }
}
