package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DbResultSetBase implements DbResultSet
{
    private Set<String> accessColumns;

    /** If columns == null then delay the setting of access columns. */ 
    protected DbResultSetBase(Collection<String> columns) 
    {
        if (columns != null)
            setAccessColumns(new HashSet<String>(columns));
    }

    public void setAccessColumns(Set<String> columns)
    {
        this.accessColumns = columns;
    }

    public Set<String> getAccessColumns()
    {
        return new HashSet(this.accessColumns);
    }

    public void addAccessColumn(String column)
    {
        this.accessColumns.add(column);
    }

    public Map<String, String> getAllValues() throws IOException
    {
        HashMap allValues = new HashMap();
        for (String accessColumn : this.accessColumns)
            allValues.put(accessColumn, get(accessColumn));
        return allValues;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.mapping.DbResultSetBase JD-Core
 * Version: 0.6.0
 */