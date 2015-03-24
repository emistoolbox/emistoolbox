package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.server.model.EmisEntityData;
import java.io.Serializable;

public class EntityDataImpl implements EmisEntityData, Serializable
{
    private static final long serialVersionUID = 1L;
    private int entityId;
    private Object[] masterArray = new Object[EmisDataType.values().length];

    public int getId()
    {
        return this.entityId;
    }

    public void setId(int entityId)
    {
        this.entityId = entityId;
    }

    public Object[] getMasterArray()
    {
        return this.masterArray;
    }

    public void setMasterArray(Object[] masterArray)
    {
        this.masterArray = masterArray;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityDataImpl JD-Core
 * Version: 0.6.0
 */