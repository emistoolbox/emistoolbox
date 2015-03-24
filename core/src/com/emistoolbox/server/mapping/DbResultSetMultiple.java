package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DbResultSetMultiple implements DbResultSet
{
    private List<DbDataSource> sources;
    private DbResultSet currentRs = null;
    private int nextIndex = 0;
    private String query;
    private Set<String> columns = new HashSet();
    private Map<String, String> lastRow = null;

    public DbResultSetMultiple(List<DbDataSource> sources, String query) {
        this.sources = sources;
        this.query = query;
    }

    public void addAccessColumn(String column)
    {
        this.columns.add(column);
    }

    public void close()
    {
        if (this.currentRs != null)
            this.currentRs.close();
    }

    public String get(String key) throws IOException
    {
        if (this.lastRow != null)
        {
            return null;
        }
        return (String) this.lastRow.get(key);
    }

    public Set<String> getAccessColumns()
    {
        return this.columns;
    }

    public Map<String, String> getAllValues() throws IOException
    {
        return this.lastRow;
    }

    public boolean next() throws IOException
    {
        if (this.currentRs != null)
        {
            if (this.currentRs.next())
            {
                this.lastRow = this.currentRs.getAllValues();
                return true;
            }

            this.currentRs.close();
        }

        if (this.nextIndex < this.sources.size())
        {
            DbDataSource source = (DbDataSource) this.sources.get(this.nextIndex);
            this.currentRs = source.query(this.query);
            this.currentRs.setAccessColumns(this.columns);
            this.nextIndex += 1;
            return next();
        }

        return false;
    }

    public void setAccessColumns(Set<String> columns)
    {
        this.columns = columns;
        if (this.currentRs != null)
            this.currentRs.setAccessColumns(columns);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.mapping.DbResultSetMultiple JD-Core
 * Version: 0.6.0
 */