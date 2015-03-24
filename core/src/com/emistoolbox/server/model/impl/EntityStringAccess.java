package com.emistoolbox.server.model.impl;

import java.io.Serializable;

public class EntityStringAccess extends EntityDataAccessBase implements EntityDataAccess, Serializable
{
    private static final long serialVersionUID = 1L;

    public static Object getArray(int entries)
    {
        return new String[entries];
    }

    public String get(Object[] masterArray, int index)
    {
        return get((String[]) (String[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.STRING.ordinal()], index);
    }

    public String get(String[] dataArray, int index)
    {
        return dataArray[getInternalIndex(index)];
    }

    public void set(Object[] masterArray, int index, String value)
    {
        set((String[]) (String[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.STRING.ordinal()], index, value);
    }

    public void set(String[] dataArray, int index, String value)
    {
        dataArray[getInternalIndex(index)] = value;
    }

    public String getAsString(Object[] masterArray, int index)
    {
        return get(masterArray, index);
    }

    public int getAsInt(Object[] masterArray, int index)
    {
        return -1;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityStringAccess JD-Core
 * Version: 0.6.0
 */