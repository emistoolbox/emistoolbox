package com.emistoolbox.server.model.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaEnum;

public class EntityEnumSetAccess extends EntityDataAccessBase implements EntityDataAccess, Serializable 
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum enumType;

    public EntityEnumSetAccess(EmisMetaEnum enumType) 
    { this.enumType = enumType; }

    public EmisMetaEnum getEnumType()
    { return enumType; }

    public static Object getArray(int entries)
    { return new int[entries]; }
    
	@Override
	public String getAsString(Object[] masterArray, int index) 
	{ return enumType.getSetValues(getAsInt(masterArray, index)); }

    private int getEnumIndexes(Object[] masterArray, int index)
    { return getEnumIndexes((int[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.ENUM_SET.ordinal()], index); }

    private int getEnumIndexes(int[] dataArray, int index)
    { return dataArray[getInternalIndex(index)]; }

    public void set(Object[] masterArray, int index, String values)
    {set(masterArray, index, enumType.getSetIndexes(values)); }

    public void set(Object[] masterArray, int index, int bits)
    {
        set((int[]) masterArray[com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType.ENUM_SET.ordinal()], index, bits);
    }

    public void set(int[] dataArray, int index, int value)
    { dataArray[getInternalIndex(index)] = value; }

    @Override
	public int getAsInt(Object[] masterArray, int index) 
	{ return getEnumIndexes(masterArray, index); }
}
