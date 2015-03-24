package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContextAdaptor implements EmisContext
{
    private EmisContext context;

    public ContextAdaptor(EmisContext context) {
        this.context = context;
    }

    protected EmisContext getAdaptedContext()
    {
        return this.context;
    }

    public void addBooleanEntityFilter(EmisMetaData field, Boolean selectTrue)
    {
        this.context.addBooleanEntityFilter(field, selectTrue);
    }

    public void addEnumEntityFilter(EmisMetaData field, EmisEnumSet values)
    {
        this.context.addEnumEntityFilter(field, values);
    }

    public void addEnumFilter(EmisEnumSet values)
    {
        this.context.addEnumFilter(values);
    }

    public boolean allowEntityWithValue(EmisMetaData field, int value)
    {
        return this.context.allowEntityWithValue(field, value);
    }

    public EmisMetaDateEnum getDateType()
    {
        return this.context.getDateType();
    }

    public List<EmisEnumTupleValue> getDates()
    {
        return this.context.getDates();
    }

    public List<EmisEnumTupleValue> getDates(EmisMetaDateEnum dateEnum)
    {
        return this.context.getDates(dateEnum);
    }

    public List<EmisEntity> getEntities()
    {
        return this.context.getEntities();
    }

    public Set<String> getEntityFilterNames(EmisMetaEntity entity)
    {
        return this.context.getEntityFilterNames(entity);
    }

    public byte[] getEntityFilterValues(EmisMetaData field)
    {
        return this.context.getEntityFilterValues(field);
    }

    public EmisMetaEntity getEntityType()
    {
        return this.context.getEntityType();
    }

    public EmisEnumSet getEnumFilter(String name)
    {
        return this.context.getEnumFilter(name);
    }

    public Map<String, EmisEnumSet> getEnumFilters()
    {
        return this.context.getEnumFilters();
    }

    public int getHierarchyDateIndex()
    {
        return this.context.getHierarchyDateIndex();
    }

    public void setDateType(EmisMetaDateEnum dateType)
    {
        this.context.setDateType(dateType);
    }

    public void setDates(List<EmisEnumTupleValue> dates)
    {
        this.context.setDates(dates);
    }

    public void setEntities(List<EmisEntity> entities)
    {
        this.context.setEntities(entities);
    }

    public void setEntityType(EmisMetaEntity entityType)
    {
        this.context.setEntityType(entityType);
    }

    public void setEnumFilters(Map<String, EmisEnumSet> filters)
    {
        this.context.setEnumFilters(filters);
    }

    public void setHierarchyDateIndex(int dateIndex)
    {
        this.context.setHierarchyDateIndex(dateIndex);
    }
}
