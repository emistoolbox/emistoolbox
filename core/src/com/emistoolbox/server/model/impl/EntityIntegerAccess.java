package com.emistoolbox.server.model.impl;

import java.io.Serializable;

public class EntityIntegerAccess extends EntityDataAccessBase implements EntityDataAccess, Serializable
{
    private static final long serialVersionUID = 1L;

    public static Object getArray(int entries)
    {
        return new int[entries];
    }

    public int get(Object[] masterArray, int index)
    {
        return get((int[]) (int[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.INTEGER.ordinal()], index);
    }

    public int get(int[] dataArray, int index)
    {
        return dataArray[getInternalIndex(index)];
    }

    public void set(Object[] masterArray, int index, int value)
    {
        set((int[]) (int[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.INTEGER.ordinal()], index, value);
    }

    public void set(int[] dataArray, int index, int value)
    {
        dataArray[getInternalIndex(index)] = value;
    }

    public String getAsString(Object[] masterArray, int index)
    {
        return "" + get(masterArray, index);
    }

    public int getAsInt(Object[] masterArray, int index)
    {
        return get(masterArray, index);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityIntegerAccess JD-Core
 * Version: 0.6.0
 */