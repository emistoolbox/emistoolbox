package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbDataSourceConfig;

import java.util.HashMap;
import java.util.Map;

public abstract class DbDataSourceConfigBase implements DbDataSourceConfig
{
    private Map<String, String> queries = new HashMap<String, String>();
    private boolean hasQueries;

    private boolean allowUnflattenXmlPath = false;
    private String unflattenXmlPath;

    public DbDataSourceConfigBase(boolean hasQueries, boolean allowUnflattenXmlPath) 
    {
        this.hasQueries = hasQueries;
        this.allowUnflattenXmlPath = allowUnflattenXmlPath;
    }

    public Map<String, String> getQueries()
    { return this.queries; }

    public boolean hasQueries()
    { return this.hasQueries; }

    public void setQueries(Map<String, String> queries)
    { this.queries = queries; }

    public String getQuery(String id)
    { return (String) this.queries.get(id); }
    
    public void setQuery(String key, String query)
    { queries.put(key,  query); }

    public void addQuery(String id, String query)
    { this.queries.put(id, query); }

    public boolean hasUnflattenXml()
    { return allowUnflattenXmlPath; }

    public String getUnflattenXmlPath()
    { return unflattenXmlPath; }

    public void setUnflattenXmlPath(String unflattenXmlPath)
    { this.unflattenXmlPath = unflattenXmlPath; }
}
