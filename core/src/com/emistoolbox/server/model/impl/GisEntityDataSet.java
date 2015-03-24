package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.server.model.EmisGisEntityDataSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GisEntityDataSet implements EmisGisEntityDataSet, Serializable
{
    private EmisMetaEntity entityType;
    private Map<Integer, double[]> data = new HashMap();

    public double[] getGisData(int entityId)
    {
        return (double[]) this.data.get(Integer.valueOf(entityId));
    }

    public EmisMetaEntity getMetaEntity()
    {
        return this.entityType;
    }

    public void setGisData(int entityId, double[] gisdata)
    {
        this.data.put(Integer.valueOf(entityId), gisdata);
    }

    public void setMetaEntity(EmisMetaEntity entity)
    {
        this.entityType = entity;
    }

    public Set<Integer> getAllIds()
    {
        return this.data.keySet();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.GisEntityDataSet JD-Core
 * Version: 0.6.0
 */