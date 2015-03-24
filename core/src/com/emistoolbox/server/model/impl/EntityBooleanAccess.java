package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.BitSet;
import java.io.Serializable;

public class EntityBooleanAccess extends EntityDataAccessBase implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static Object getArray(int entries)
    {
        return new BitSet(entries);
    }

    public boolean get(Object[] masterArray, int index)
    {
        return get((BitSet) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.BOOLEAN.ordinal()], index);
    }

    public boolean get(BitSet dataArray, int index)
    {
        return dataArray.get(getInternalIndex(index));
    }

    public void set(Object[] masterArray, int index, boolean value)
    {
        set((BitSet) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.BOOLEAN.ordinal()], index, value);
    }

    public void set(BitSet dataArray, int index, boolean value)
    {
        dataArray.set(getInternalIndex(index), value);
    }

    public String getAsString(Object[] masterArray, int index)
    {
        return "" + get(masterArray, index);
    }

    public int getAsInt(Object[] masterArray, int index)
    {
        return get(masterArray, index) ? 1 : 0;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityBooleanAccess JD-Core
 * Version: 0.6.0
 */