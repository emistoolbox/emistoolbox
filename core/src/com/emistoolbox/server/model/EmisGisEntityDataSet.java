package com.emistoolbox.server.model;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.util.Set;

public abstract interface EmisGisEntityDataSet
{
    public abstract EmisMetaEntity getMetaEntity();

    public abstract void setMetaEntity(EmisMetaEntity paramEmisMetaEntity);

    public abstract Set<Integer> getAllIds();

    public abstract double[] getGisData(int paramInt);

    public abstract void setGisData(int paramInt, double[] paramArrayOfDouble);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.EmisGisEntityDataSet JD-Core
 * Version: 0.6.0
 */