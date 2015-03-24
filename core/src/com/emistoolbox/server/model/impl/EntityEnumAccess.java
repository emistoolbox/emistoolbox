package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;

public class EntityEnumAccess extends EntityDataAccessBase implements EntityDataAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum enumType;

    public EntityEnumAccess(EmisMetaEnum enumType) 
    { this.enumType = enumType; }

    public EmisMetaEnum getEnumType()
    { return this.enumType; }

    public static Object getArray(int entries)
    {
        return new byte[entries];
    }

    public String get(Object[] masterArray, int index)
    {
        byte enumIndex = getIndex(masterArray, index);
        if ((enumIndex < 0) || (enumIndex >= this.enumType.getSize()))
            return "";

        return this.enumType.getValue(enumIndex);
    }

    public byte getIndex(Object[] masterArray, int index)
    {
        return getIndex((byte[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.ENUM.ordinal()], index);
    }

    public byte getIndex(byte[] dataArray, int index)
    { return dataArray[getInternalIndex(index)]; }

    public void set(Object[] masterArray, int index, String value)
    { set(masterArray, index, this.enumType.getIndex(value)); }

    public void set(Object[] masterArray, int index, byte value)
    { set((byte[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.ENUM.ordinal()], index, value); }

    public void set(byte[] dataArray, int index, byte value)
    {
        dataArray[getInternalIndex(index)] = value;
    }

    public String getAsString(Object[] masterArray, int index)
    {
        return get(masterArray, index);
    }

    public int getAsInt(Object[] masterArray, int index)
    {
        return getIndex(masterArray, index);
    }
}
