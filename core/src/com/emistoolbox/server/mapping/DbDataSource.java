package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DbDataSource
{
    public Map<String, List<String>> getDataInfo() throws IOException;

    public List<String> getTableNames() throws IOException;
    public List<String> getFieldNames(String table) throws IOException;

    public DbResultSet query(String query) throws IOException;
    
    public void setDataset(String dataset); 
    public String getDataset(); 
}
