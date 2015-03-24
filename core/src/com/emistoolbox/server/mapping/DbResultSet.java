package com.emistoolbox.server.mapping;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public abstract interface DbResultSet
{
    public abstract void setAccessColumns(Set<String> paramSet);

    public abstract Set<String> getAccessColumns();

    public abstract void addAccessColumn(String paramString);

    public abstract boolean next() throws IOException;

    public abstract String get(String paramString) throws IOException;

    public abstract Map<String, String> getAllValues() throws IOException;

    public abstract void close();
}

