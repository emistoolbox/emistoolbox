package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.impl.Hierarchy;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.EmisGisEntityDataSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DataSetImpl implements EmisDataSet, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMeta metaDataSet;
    private int initialEntityCount;
    private int initialDateEnumCount;
    private EmisGisEntityDataSet[] gisData;
    private EmisEntityDataSet[][] data;
    private Map<String, EmisHierarchy> hierarchies = new HashMap<String, EmisHierarchy>();

    public EmisEntityDataSet getEntityDataSet(EmisMetaEntity entity, EmisMetaDateEnum dateEnum)
    {
        return getEntityDataSet(this.metaDataSet.getEntities().getIndex(entity), this.metaDataSet.getDateEnums().getIndex(dateEnum));
    }

    public EmisEntityDataSet getEntityDataSetWithCreate(EmisMetaEntity entity, EmisMetaDateEnum dateEnum)
    {
        return getEntityDataSetWithCreate(this.metaDataSet.getEntities().getIndex(entity), this.metaDataSet.getDateEnums().getIndex(dateEnum));
    }

    public EmisEntityDataSet getEntityDataSetWithCreate(int entityIndex, int dateEnumIndex)
    {
        if (this.data[entityIndex][dateEnumIndex] == null)
        {
            EmisEntityDataSet dataset = new EntityDataSet();
            dataset.init((EmisMetaEntity) this.metaDataSet.getEntities().get(entityIndex), (EmisMetaDateEnum) this.metaDataSet.getDateEnums().get(dateEnumIndex));
            this.data[entityIndex][dateEnumIndex] = dataset;
        }

        return this.data[entityIndex][dateEnumIndex];
    }

    public EmisEntityDataSet getEntityDataSet(int entityIndex, int dateEnumIndex)
    {
        if (this.metaDataSet.getEntities().size() != this.initialEntityCount)
            throw new IllegalStateException("metaDataSet.entities has changed in size");
        if (this.metaDataSet.getDateEnums().size() != this.initialDateEnumCount)
        {
            throw new IllegalStateException("metaDataSet.dateEnums has changed in size");
        }
        return this.data[entityIndex][dateEnumIndex];
    }

    public EmisMeta getMetaDataSet()
    {
        return this.metaDataSet;
    }

    public void setMetaDataSet(EmisMeta metaDataSet)
    {
        if (this.metaDataSet != null)
        {
            throw new IllegalStateException("Cannot redefine dimension of DateSetImpl");
        }
        this.metaDataSet = metaDataSet;
        this.initialDateEnumCount = metaDataSet.getDateEnums().size();
        this.initialEntityCount = metaDataSet.getEntities().size();

        this.data = new EmisEntityDataSet[this.initialEntityCount][this.initialDateEnumCount];
        this.gisData = new EmisGisEntityDataSet[this.initialEntityCount];
    }

    public EmisEntityData getEntityData(EmisMetaEntity entity, int entityId, EmisMetaDateEnum dateEnum, int dateIndex)
    {
        EmisEntityDataSet dataset = getEntityDataSet(entity, dateEnum);
        if (dataset == null)
        {
            return null;
        }
        return dataset.getData(dateIndex, entityId);
    }

    public EmisEntityData getEntityData(EmisMetaEntity entity, int entityId, EmisMetaDateEnum dateEnum, byte[] dateIndexes)
    {
        EmisEntityDataSet dataset = getEntityDataSet(entity, dateEnum);
        if (dataset == null)
            return null;
        return dataset.getData(dateIndexes, entityId);
    }

    public EmisEntityData getEntityData(EmisMetaEntity entity, int entityId, EmisMetaDateEnum dateEnum, String[] dateIndexes)
    {
        EmisEntityDataSet dataset = getEntityDataSet(entity, dateEnum);
        if (dataset == null)
            return null;
        return dataset.getData(dateIndexes, entityId);
    }

    public EmisEntityData getEntityData(int entityIndex, int entityId, int dateEnumIndex, int dateIndex)
    {
        EmisEntityDataSet entityData = getEntityDataSet(entityIndex, dateEnumIndex);
        return entityData.getData(dateIndex, entityId);
    }

    public EmisEntityData getWithCreate(int entityIndex, int entityId, int dateEnumIndex, int dateIndex)
    {
        return getEntityDataSetWithCreate(entityIndex, dateEnumIndex).getWithCreate(dateIndex, entityId);
    }

    public EmisHierarchy getHierarchy(String name)
    {
        return (EmisHierarchy) this.hierarchies.get(name);
    }

    public EmisHierarchy getHierarchyWithCreate(String name)
    {
        EmisMetaHierarchy metaHierarchy = (EmisMetaHierarchy) NamedUtil.find(name, this.metaDataSet.getHierarchies());
        if (metaHierarchy == null)
        {
            throw new IllegalArgumentException("Cannot find a hierarchy with name '" + name + "'");
        }
        return getHierarchyWithCreate(metaHierarchy);
    }

    public EmisHierarchy getHierarchyWithCreate(EmisMetaHierarchy metaHierarchy)
    {
        EmisHierarchy result = (EmisHierarchy) this.hierarchies.get(metaHierarchy.getName());
        if (result == null)
        {
            result = new Hierarchy(this.metaDataSet.getDefaultDateType(), metaHierarchy);
            this.hierarchies.put(metaHierarchy.getName(), result);
        }

        return result;
    }

    public void putHierarchy(EmisHierarchy hierarchy)
    {
        this.hierarchies.put(hierarchy.getMetaHierarchy().getName(), hierarchy);
    }

    public double[] getGisData(EmisMetaEntity entity, int entityId)
    {
        int index = this.metaDataSet.getEntities().getIndex(entity);
        if (this.gisData[index] == null)
        {
            return null;
        }
        return this.gisData[index].getGisData(entityId);
    }

    public EmisGisEntityDataSet getGisEntityDataSet(EmisMetaEntity entity)
    {
        return this.gisData[this.metaDataSet.getEntities().getIndex(entity)];
    }

    public EmisGisEntityDataSet getGisEntityDataSetWithCreate(EmisMetaEntity entity)
    {
        int index = this.metaDataSet.getEntities().getIndex(entity);
        if (this.gisData[index] == null)
        {
            this.gisData[index] = new GisEntityDataSet();
            this.gisData[index].setMetaEntity(entity);
        }

        return this.gisData[index];
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.DataSetImpl JD-Core
 * Version: 0.6.0
 */