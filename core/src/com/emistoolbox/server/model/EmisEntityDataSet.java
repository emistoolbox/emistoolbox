package com.emistoolbox.server.model;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.server.model.impl.EntityDataAccess;
import java.util.Map;
import java.util.Set;

public abstract interface EmisEntityDataSet
{
    public abstract void init(EmisMetaEntity paramEmisMetaEntity, EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisMetaDateEnum getDateEnum();

    public abstract EmisMetaEntity getEntity();

    public abstract Set<String> getFields();

    public abstract EntityDataAccess getDataAccess(String paramString);

    public abstract EmisEntityData getData(String[] paramArrayOfString, int paramInt);

    public abstract EmisEntityData getData(byte[] paramArrayOfByte, int paramInt);

    public abstract EmisEntityData getData(int dateIndex, int entityId);

    public abstract Set<Integer> getAllIds(int paramInt);

    public abstract Map<Integer, String> getAllValues(int paramInt, String paramString);

    public abstract Map<Integer, String> getAllValues(int paramInt, String paramString, int[] paramArrayOfInt);

    public abstract EmisEntityData getWithCreate(int dateIndex, int entityId);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.EmisEntityDataSet JD-Core
 * Version: 0.6.0
 */