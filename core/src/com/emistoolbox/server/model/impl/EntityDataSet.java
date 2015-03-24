package com.emistoolbox.server.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EntityDataSet implements EmisEntityDataSet, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaDateEnum dateEnum;
    private EmisMetaEntity entity;
    private Map<Integer, EmisEntityData>[] data;
    private int[] dataCount = new int[EmisMetaData.EmisDataType.values().length];
    private Map<String, EntityDataAccess> access = new HashMap();

    public void init(EmisMetaEntity entity, EmisMetaDateEnum dateEnum)
    {
        this.entity = entity;
        this.dateEnum = dateEnum;
        this.data = new HashMap[dateEnum.getCombinations()];

        for (EmisMetaData data : entity.getData())
        {
            if (NamedUtil.sameName(data.getDateType(), dateEnum))
                addEntry(data);
        }
    }

    public Set<Integer> getAllIds(int dateIndex)
    {
        Map map = this.data[dateIndex];
        return map.keySet();
    }

    public Map<Integer, String> getAllValues(int dateIndex, String field)
    {
        Map<Integer, String> result = new HashMap<Integer, String>();

        EntityDataAccess valueAccess = (EntityDataAccess) this.access.get(field);
        Map<Integer, EmisEntityData> map = this.data[dateIndex];
        if (map != null)
        {
            for (Map.Entry<Integer, EmisEntityData> entry : map.entrySet())
            {
                result.put(entry.getKey(), valueAccess.getAsString(entry.getValue().getMasterArray()));
            }
        }
        return result;
    }

    public Map<Integer, String> getAllValues(int dateIndex, String field, int[] ids)
    {
        Map result = new HashMap();
        if (ids == null)
        {
            return result;
        }
        EntityDataAccess valueAccess = (EntityDataAccess) this.access.get(field);
        Map map = this.data[dateIndex];
        if (map == null)
        {
            return result;
        }
        for (int id : ids)
        {
            EmisEntityData entityData = (EmisEntityData) map.get(Integer.valueOf(id));
            if (entityData != null)
            {
                result.put(Integer.valueOf(id), valueAccess.getAsString(entityData.getMasterArray()));
            }
        }
        return result;
    }

    private void addEntry(EmisMetaData data)
    {
        int index = data.getType().ordinal();
        int offset = this.dataCount[index];

        EmisMetaEnumTuple dimensions = data.getArrayDimensions();
        int size = dimensions == null ? 1 : dimensions.getCombinations();

        this.dataCount[index] += size;
        this.access.put(data.getName(), EntityDataAccessBase.getEntityAccess(data.getType(), offset, size, data.getEnumType()));
    }

    public EmisEntityData getData(String[] values, int entityId)
    {
        return getData(this.dateEnum.getIndex(values), entityId);
    }

    public EmisEntityData getData(byte[] values, int entityId)
    {
        return getData(this.dateEnum.getIndex(values), entityId);
    }

    public EmisEntityData getData(int index, int entityId)
    {
        if (index == -1)
        {
            return null;
        }
        Map tmp = this.data[index];
        if (tmp == null)
        {
            return null;
        }
        return (EmisEntityData) tmp.get(Integer.valueOf(entityId));
    }

    public EmisEntityData getWithCreate(int dateIndex, int entityId)
    {
        if (this.data[dateIndex] == null)
            this.data[dateIndex] = new HashMap();
        Map tmp = this.data[dateIndex];

        EmisEntityData result = (EmisEntityData) tmp.get(Integer.valueOf(entityId));
        if (result == null)
        {
            result = new EntityDataImpl();
            Object[] master = result.getMasterArray();
            for (EmisMetaData.EmisDataType type : EmisMetaData.EmisDataType.values())
            {
                master[type.ordinal()] = EntityDataAccessBase.getArray(type, this.dataCount[type.ordinal()]);
            }
            result.setId(entityId);
            tmp.put(Integer.valueOf(entityId), result);
        }

        return result;
    }

    public Set<String> getFields()
    {
        return this.access.keySet();
    }

    public EntityDataAccess getDataAccess(String field)
    {
        return (EntityDataAccess) this.access.get(field);
    }

    public EmisMetaDateEnum getDateEnum()
    {
        return this.dateEnum;
    }

    public EmisMetaEntity getEntity()
    {
        return this.entity;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.model.impl.EntityDataSet JD-Core
 * Version: 0.6.0
 */