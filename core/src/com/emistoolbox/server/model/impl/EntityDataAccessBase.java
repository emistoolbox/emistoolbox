package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;

public abstract class EntityDataAccessBase implements EntityDataAccess, Serializable
{
    private static final long serialVersionUID = 1L;
    private int offset;
    private int size;

    public static EntityDataAccess getEntityAccess(EmisDataType type, int offset, int size, EmisMetaEnum enumType)
    {
        EntityDataAccessBase result = null;
        if (type == EmisDataType.BOOLEAN)
            result = new EntityBooleanAccess();
        else if (type == EmisDataType.BYTE)
            result = new EntityByteAccess();
        else if (type == EmisDataType.INTEGER)
            result = new EntityIntegerAccess();
        else if (type == EmisDataType.STRING)
            result = new EntityStringAccess();
        else if (type == EmisDataType.ENUM)
            result = new EntityEnumAccess(enumType);
        else if (type == EmisDataType.ENUM_SET)
        	result = new EntityEnumSetAccess(enumType); 
        
        if (result != null)
        {
            result.init(offset, size);
        }
        return result;
    }

    public static Object getArray(EmisDataType type, int size)
    {
        if (type == EmisDataType.BOOLEAN)
            return EntityBooleanAccess.getArray(size);
        if (type == EmisDataType.BYTE)
            return EntityByteAccess.getArray(size);
        if (type == EmisDataType.INTEGER)
            return EntityIntegerAccess.getArray(size);
        if (type == EmisDataType.STRING)
            return EntityStringAccess.getArray(size);
        if (type == EmisDataType.ENUM)
            return EntityEnumAccess.getArray(size);
        if (type == EmisDataType.ENUM_SET)
        	return EntityEnumSetAccess.getArray(size); 

        return null;
    }

    public void init(int offset, int size)
    {
        this.offset = offset;
        this.size = size;
    }

    protected int getInternalIndex(int index)
    {
        if ((index < 0) || (index >= this.size))
        {
            throw new IllegalArgumentException("Index of " + index + " out of range 0 <= index < " + this.size);
        }
        return index + this.offset;
    }

    public int getOffset()
    {
        return this.offset;
    }

    public int getSize()
    {
        return this.size;
    }

    public String getAsString(Object[] masterArray)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < this.size; i++)
        {
            if (i > 0)
                result.append(",");
            result.append(getAsString(masterArray, i));
        }

        return result.toString();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityDataAccessBase
 * JD-Core Version: 0.6.0
 */