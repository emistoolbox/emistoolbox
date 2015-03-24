package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.mapping.impl.UnflattenDbColumn;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbQuery;
import com.emistoolbox.common.model.mapping.impl.UnflattenDbRow;

public class DbResultSetUnflattenDb extends DbResultSetBase implements DbResultSet
{
    private UnflattenDbQuery config;
    private DbResultSet sourceRS;

    private Map<String, String> currentRow = null;
    private Map<String, String> currentSourceRow = null;
    private int currentRowsIndex = -1;

    public DbResultSetUnflattenDb(DbResultSet rs, UnflattenDbQuery config) 
    {
        super(null); 
        this.sourceRS = rs;
        this.config = config;
        setAccessColumns(new HashSet<String>(config.getFieldNames()));
    }
    
    public boolean next() throws IOException
    {
        if (currentSourceRow == null)
            fetchNext();

        if (currentSourceRow == null)
            return false;

        currentRowsIndex++;
        if (currentRowsIndex >= config.getRows().size())
        {
            currentSourceRow = null;
            return next();
        }

        currentRow = config.getAllValues(currentSourceRow, currentRowsIndex);
        return true;
    }
    
    public Map<String, String> getAllValues() throws IOException
    { return currentRow; }

    private void fetchNext() throws IOException
    {
        currentSourceRow = null;
        if (!sourceRS.next())
            return;

        currentSourceRow = sourceRS.getAllValues();
        currentRowsIndex = -1;
    }

    public String get(String paramString) throws IOException
    {
        if (currentSourceRow == null)
            next();

        if (currentSourceRow == null)
            return null;

        return currentSourceRow.get(paramString);
    }

    public void close()
    {
        currentRow = null;
        currentSourceRow = null;
        currentRowsIndex = -1;

        sourceRS.close();
    }

    public void setAccessColumns(Set<String> columns)
    {
        super.setAccessColumns(columns); 
        
        // Access columns passed to source RS depend on our configuration. 
        Set<String> sourceColumns = new HashSet<String>(); 
        for (UnflattenDbColumn col : config.getColumns())
        {
            if (columns.contains(col.getName()) && col.getColumn() != null)
                sourceColumns.add(col.getColumn()); 
        }
        
        for (UnflattenDbRow row : config.getRows())
        {
            for (UnflattenDbColumn col : row.getColumns()) 
            {
                if (columns.contains(col.getName()) && col.getColumn() != null)
                    sourceColumns.add(col.getColumn());
            }
        }
        
        sourceRS.setAccessColumns(sourceColumns); 
    }


}
