package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Context extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private int dateIndex = -1;
    private EmisMetaDateEnum dateType;
    private List<EmisEnumTupleValue> dates;
    private List<EmisEntity> entities;
    private EmisMetaEntity entityType;
    private Map<String, EmisEnumSet> enums;
    private Map<String, byte[]> entityFilters = new HashMap();

    public int getHierarchyDateIndex()
    { return this.dateIndex; }

    public void setHierarchyDateIndex(int dateIndex)
    { this.dateIndex = dateIndex; }

    public EmisMetaDateEnum getDateType()
    { return this.dateType; }

    public List<EmisEnumTupleValue> getDates()
    { return this.dates; }

    public List<EmisEntity> getEntities()
    { return this.entities; }

    public EmisMetaEntity getEntityType()
    { return this.entityType; }

    public EmisEnumSet getEnumFilter(String name)
    { return this.enums == null ? null : (EmisEnumSet) this.enums.get(name); }

    public Map<String, EmisEnumSet> getEnumFilters()
    { return this.enums; }

    public void setDateType(EmisMetaDateEnum dateType)
    { this.dateType = dateType; }

    public void setDates(List<EmisEnumTupleValue> newDates)
    { this.dates = newDates; }

    public void setEntities(List<EmisEntity> entities)
    { this.entities = entities; }

    public void setEntityType(EmisMetaEntity entityType)
    { this.entityType = entityType; }

    public void addEnumFilter(EmisEnumSet values)
    {
        if (this.enums == null)
            this.enums = new HashMap();
        this.enums.put(values.getEnum().getName(), values);
    }

    public void setEnumFilters(Map<String, EmisEnumSet> enums)
    { this.enums = enums; }

    public void addBooleanEntityFilter(EmisMetaData field, Boolean selectTrue)
    { this.entityFilters.put(getKey(field), new byte[] { (byte) (selectTrue.booleanValue() ? 1 : 0) }); }

    public void addEnumEntityFilter(EmisMetaData field, EmisEnumSet values)
    {
        String key = getKey(field);
        this.entityFilters.put(key, getBytes(values.getAllIndexes()));
    }

    public void addEntityFilter(String key, byte[] values)
    { this.entityFilters.put(key, values); }

    public boolean allowEntityWithValue(EmisMetaData field, int value)
    {
        byte[] values = (byte[]) this.entityFilters.get(getKey(field));
        if (values == null)
        {
            return true;
        }
        for (int i = 0; i < values.length; i++)
            if (values[i] == value)
                return true;
        return false;
    }

    public Set<String> getEntityFilterNames(EmisMetaEntity entity)
    {
        Set result = new HashSet();
        for (String key : this.entityFilters.keySet())
        {
            if (key.startsWith(entity.getName() + "."))
            {
                result.add(key.substring(entity.getName().length() + 1));
            }
        }
        return result;
    }

    public byte[] getEntityFilterValues(EmisMetaData field)
    { return (byte[]) this.entityFilters.get(getKey(field)); }

    public Map<String, byte[]> getEntityFilters()
    { return this.entityFilters; }

    private byte[] getBytes(Set<Byte> values)
    {
        byte[] result = new byte[values.size()];
        int index = 0;
        for (Byte value : values)
        {
            result[index] = value.byteValue();
            index++;
        }

        return result;
    }

    public String getKey(EmisMetaData field)
    { return field.getEntity().getName() + "." + field.getName(); }
}
