package com.emistoolbox.common.model.mapping;

import java.io.Serializable;
import java.util.Map;

public abstract interface DbDataSourceConfig extends Serializable
{
    public static final String GLOBAL_QUERY = "GLOBAL"; 
    
    public abstract String getName();

    public abstract boolean hasConnected();

    public abstract void setHasConnected(boolean paramBoolean);

    public abstract boolean hasQueries();

    public abstract String getQuery(String paramString);
    
    public abstract void setQuery(String key, String sql); 

    public abstract void addQuery(String paramString1, String paramString2);

    public abstract Map<String, String> getQueries();

    public abstract void setQueries(Map<String, String> paramMap);
}
