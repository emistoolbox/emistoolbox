package com.emistoolbox.server.util;

public abstract interface TableWriter
{
    public abstract void nextCell(String paramString);

    public abstract void nextRow();

    public abstract void close();
}
