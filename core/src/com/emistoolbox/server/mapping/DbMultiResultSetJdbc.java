package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbMultiResultSetJdbc implements DbResultSet
{
    private String[] queries = null;
    private DbDataSourceJdbc dbSource =  null; 
    private String contextVariable = null; 
    private String contextValue = null; 

    private DbResultSetJdbc currentResultSet = null; 
    private int currentIndex = -1; 
    
    private Set<String> accessColumns = new HashSet<String>(); 

    public DbMultiResultSetJdbc(DbDataSourceJdbc dbSource, String[] queries, String contextVariable, String contextValue) 
        throws IOException
    {
        this.dbSource = dbSource;
        this.queries = queries; 
        this.contextVariable = contextVariable; 
        this.contextValue = contextValue; 
        
        loadNext(false); 
    }

    private synchronized boolean loadNext(boolean loadRow)
        throws IOException
    {
        close(); 

        currentIndex++; 
        if (currentIndex < queries.length)
        {
            long time = System.currentTimeMillis(); 
            try { 
                currentResultSet = new DbResultSetJdbc(dbSource.getResultSet(queries[currentIndex]), contextVariable, contextValue);
                currentResultSet.setAccessColumns(accessColumns); 
                
                if (loadRow)
                    return currentResultSet.next();

                return true; 
            }
            finally 
            { System.out.println("Query took " + (System.currentTimeMillis() - time) + " ms: " + queries[currentIndex]); }
        }
        else
            return false; 
    }

    @Override
    public boolean next() 
        throws IOException
    {
        if (currentResultSet == null)
            return false; 
        
        boolean result = currentResultSet.next();
        if (!result)
            return loadNext(true); 
        
        return result; 
    }

    @Override
    public String get(String paramString) 
        throws IOException
    { return currentResultSet.get(paramString); } 

    @Override
    public void close()
    { 
        if (currentResultSet != null)
        {
            currentResultSet.close(); 
            currentResultSet = null; 
        }
    }

    @Override
    public void setAccessColumns(Set<String> accessColumns)
    {
        this.accessColumns = accessColumns; 
        if (currentResultSet != null)
            currentResultSet.setAccessColumns(accessColumns); 
    }

    @Override
    public Set<String> getAccessColumns()
    { 
        if (currentResultSet != null)
            return currentResultSet.getAccessColumns(); 
        
        return null; 
    }

    @Override
    public void addAccessColumn(String accessColumn)
    {
        accessColumns.add(accessColumn); 
        if (currentResultSet != null)
            currentResultSet.addAccessColumn(accessColumn); 
    }

    @Override
    public Map<String, String> getAllValues() 
        throws IOException
    {
        if (currentResultSet != null)
            return currentResultSet.getAllValues(); 

        return null;
    }
}
