package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbDataSourceMultiple implements DbDataSource
{
    private List<DbDataSource> sources = new ArrayList<DbDataSource>();

    public void addDataSource(DbDataSource source)
    { this.sources.add(source); }

    public Map<String, List<String>> getDataInfo() throws IOException
    { return getDefaultDataSource().getDataInfo(); }

    public List<String> getFieldNames(String query) throws IOException
    { return getDefaultDataSource().getFieldNames(query); }

    public List<String> getTableNames() throws IOException
    { return getDefaultDataSource().getTableNames(); }

    public DbResultSet query(String query) throws IOException
    { return new DbResultSetMultiple(sources, query); }
    
    public List<DbDataSource> getDataSources()
    { return sources; }

    private DbDataSource getDefaultDataSource()
    { return sources.get(0); }

    @Override
	public void setDataset(String dataset) 
    { getDefaultDataSource().setDataset(dataset); } 
    
	@Override
	public String getDataset() 
	{ return getDefaultDataSource().getDataset(); } 
}
