package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class DbResultSetJdbc implements DbResultSet
{
    private ResultSetResult rsr;
    private Set<String> columns = new HashSet<String>();
    private Map<String, String> lastRow = null;
    private String contextName;
    private String contextValue;

    public DbResultSetJdbc(ResultSetResult rs, String contextName, String contextValue) {
        this.rsr = rs;
        this.contextName = contextName;
        this.contextValue = contextValue;
    }

    public void close()
    { rsr.close(); }

    public String get(String key) throws IOException
    { return (String) this.lastRow.get(key); }

    public Set<String> getAccessColumns()
    { return this.columns; }

    public Map<String, String> getAllValues() throws IOException
    {
        return this.lastRow;
    }

    private Map<String, String> fetchRow() throws IOException, SQLException
    {
        Map result = new HashMap();
        for (String key : this.columns)
            result.put(key, this.rsr.getResultSet().getString(key));

        if ((this.contextName != null) && (this.contextValue != null))
            result.put("[" + this.contextName + "]", this.contextValue);

        return result;
    }

    public boolean next() throws IOException
    {
        try
        {
            boolean result = this.rsr.getResultSet().next();
            if (result)
                this.lastRow = fetchRow();
            else
                this.lastRow = null;
            return result;
        }
        catch (SQLException ex)
        {
            throw DbDataSourceJdbc.getIOException("Failed to get next data row.", ex);
        }
    }

    public void addAccessColumn(String column)
    {
        this.columns.add(column);
    }

    public void setAccessColumns(Set<String> columns)
    {
        this.columns = columns;
    }
}
