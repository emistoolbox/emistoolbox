package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class DbDataSourceBase implements DbDataSource
{
    private Map<String, List<String>> info;
    private String dataset; 
    
    public void setDataset(String dataset)
    { this.dataset = dataset; }
    
    public String getDataset()
    { return dataset; } 
    
    public List<String> getFieldNames(String query) throws IOException
    {
        if (this.info == null)
            this.info = getDataInfo();

        return (List) this.info.get(query);
    }

    public List<String> getTableNames() throws IOException
    {
        if (this.info == null)
        {
            this.info = getDataInfo();
        }
        List result = new ArrayList();
        result.addAll(this.info.keySet());

        return result;
    }

    public static IOException getIOException(String message, Throwable err)
    {
        if ((err instanceof IOException))
            return (IOException) err;

        IOException result = new IOException(message + (err == null ? "" : " - " + err.toString())); 
        if (err != null)
            result.initCause(err); 

        return result; 
    }

}
