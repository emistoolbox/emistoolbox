package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SingleTableDataSource implements DbDataSource
{
    protected String tableName;
    protected List<String> fieldNames;
    private String dataset; 

    protected SingleTableDataSource(String tableName, List<String> fieldNames) 
    {
        this.tableName = tableName;
        this.fieldNames = fieldNames;
    }

    public Map<String, List<String>> getDataInfo() throws IOException
    {
        Map<String, List<String>> dataInfo = new HashMap<String, List<String>>();
        dataInfo.put(this.tableName, this.fieldNames);
        return dataInfo;
    }

    public List<String> getTableNames() throws IOException
    { return Arrays.asList(new String[] { this.tableName }); }

    public List<String> getFieldNames(String query) throws IOException
    { return this.fieldNames; }
    
    public void setDataset(String dataset)
    { this.dataset = dataset; } 
    
    public String getDataset()
    { return dataset; } 
}
