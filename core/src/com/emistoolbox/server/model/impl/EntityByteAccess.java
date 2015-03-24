package com.emistoolbox.server.model.impl;

import java.io.Serializable;

public class EntityByteAccess extends EntityDataAccessBase implements EntityDataAccess, Serializable
{
    private static final long serialVersionUID = 1L;

    public static Object getArray(int entries)
    {
        return new byte[entries];
    }

    public byte get(Object[] masterArray, int index)
    {
        return get((byte[]) (byte[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.BYTE.ordinal()], index);
    }

    public byte get(byte[] dataArray, int index)
    {
        return dataArray[getInternalIndex(index)];
    }

    public void set(Object[] masterArray, int index, byte value)
    {
        set((byte[]) (byte[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.BYTE.ordinal()], index, value);
    }

    public void set(byte[] dataArray, int index, byte value)
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
 * Qualified Name: com.emistoolbox.server.model.impl.EntityByteAccess JD-Core
 * Version: 0.6.0
 */