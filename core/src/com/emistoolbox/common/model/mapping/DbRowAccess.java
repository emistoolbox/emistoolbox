package com.emistoolbox.common.model.mapping;

import java.util.Map;
import java.util.Set;

public abstract interface DbRowAccess
{
    public abstract int getValuesPerRow();

    public abstract String getValue(int paramInt, Map<String, String> paramMap);

    public abstract int getValueAsInt(int paramInt1, int paramInt2, Map<String, String> paramMap);

    public abstract void addColumns(Set<String> paramSet);
    
    public abstract void map(DbRowAccessFn callback); 
    
    public abstract String getInfo(); 
}
