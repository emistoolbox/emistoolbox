package com.emistoolbox.server.model;

import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;

public abstract interface EmisDataSet
{
    public abstract EmisMeta getMetaDataSet();

    public abstract EmisEntityDataSet getEntityDataSet(EmisMetaEntity paramEmisMetaEntity, EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisEntityDataSet getEntityDataSet(int entityIndex, int dateIndex);

    public abstract EmisEntityData getEntityData(EmisMetaEntity paramEmisMetaEntity, int paramInt, EmisMetaDateEnum paramEmisMetaDateEnum, String[] paramArrayOfString);

    public abstract EmisEntityData getEntityData(EmisMetaEntity paramEmisMetaEntity, int paramInt, EmisMetaDateEnum paramEmisMetaDateEnum, byte[] paramArrayOfByte);

    public abstract EmisEntityData getEntityData(EmisMetaEntity paramEmisMetaEntity, int paramInt1, EmisMetaDateEnum paramEmisMetaDateEnum, int paramInt2);

    public abstract EmisEntityData getEntityData(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract EmisEntityDataSet getEntityDataSetWithCreate(EmisMetaEntity paramEmisMetaEntity, EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisEntityDataSet getEntityDataSetWithCreate(int paramInt1, int paramInt2);

    public abstract EmisEntityData getWithCreate(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract double[] getGisData(EmisMetaEntity paramEmisMetaEntity, int paramInt);

    public abstract EmisGisEntityDataSet getGisEntityDataSet(EmisMetaEntity paramEmisMetaEntity);

    public abstract EmisGisEntityDataSet getGisEntityDataSetWithCreate(EmisMetaEntity paramEmisMetaEntity);

    public abstract EmisHierarchy getHierarchy(String paramString);

    public abstract EmisHierarchy getHierarchyWithCreate(EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract void putHierarchy(EmisHierarchy paramEmisHierarchy);
}
