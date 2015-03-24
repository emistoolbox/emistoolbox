package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DbResultSetLoop implements DbResultSet
{
    private String fullVariable;
    private String[] values;
    private int valueIndex = -1;
    private DbResultSet rs;

    public DbResultSetLoop(String variable, String[] values, DbResultSet rs) {
        this.fullVariable = ("[" + variable + "]");
        this.values = values;
        this.rs = rs;
    }

    public void addAccessColumn(String column)
    {
        if (!column.equals(this.fullVariable))
            this.rs.addAccessColumn(column);
    }

    public void close()
    {
        this.rs.close();
    }

    public String get(String key) throws IOException
    {
        if (this.fullVariable.equals(key))
        {
            if ((this.valueIndex == -1) || (this.valueIndex >= this.values.length))
            {
                throw new IllegalArgumentException("DbResultSet hasn't had next() called, yet.");
            }
            return this.values[this.valueIndex];
        }

        return this.rs.get(key);
    }

    public Set<String> getAccessColumns()
    {
        Set result = new HashSet();
        result.addAll(this.rs.getAccessColumns());
        result.add(this.fullVariable);

        return result;
    }

    public Map<String, String> getAllValues() throws IOException
    {
        Map result = this.rs.getAllValues();
        if ((this.valueIndex < -1) || (this.valueIndex >= this.values.length))
        	throw new IllegalArgumentException("Date loop - no values defined for index = " + valueIndex);

        result.put(this.fullVariable, this.values[this.valueIndex]);
        return result;
    }

    public boolean next() throws IOException
    {
        if ((this.valueIndex < 0) || (this.valueIndex + 1 >= this.values.length))
        {
            if (!this.rs.next())
            {
                return false;
            }
            this.valueIndex = 0;
        }
        else
        {
            this.valueIndex += 1;
        }
        return true;
    }

    public void setAccessColumns(Set<String> columns)
    {
        columns.remove(this.fullVariable);
        this.rs.setAccessColumns(columns);
    }
}
