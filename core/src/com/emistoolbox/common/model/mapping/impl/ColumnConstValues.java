package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnConstValues implements Serializable
{
    private String colName; 
    private Map<String, String> enumValues = new HashMap<String, String>(); 
    
    // Constructor for serialization. 
    public ColumnConstValues()
    {} 
    
    public ColumnConstValues(String colName)
    { this.colName = colName; } 
    
    public String getColumnName()
    { return colName; } 
    
    public void setColumn(String col)
    { this.colName = col; } 
    
    public void put(String field, String value)
    { enumValues.put(field, value); }

    public void remove(String enumName)
    { enumValues.remove(enumName); } 
    
    public String get(String enumName)
    { return enumValues.get(enumName); } 
    
    public List<String> getEnumNames()
    {
        List<String> result = new ArrayList<String>(enumValues.keySet()); 
        Collections.sort(result); 
        return result; 
    }
}
