package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContextConstEntity extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisEntity entity;
    private List<EmisEntity> entityList = new ArrayList();
    private int dateIndex = -1;

    public ContextConstEntity() {
    }

    public ContextConstEntity(EmisEntity entity, int dateIndex) {
        this.entity = entity;
        this.dateIndex = dateIndex;

        this.entityList.add(entity);
    }

    public int getHierarchyDateIndex()
    {
        return this.dateIndex;
    }

    public void setHierarchyDateIndex(int dateIndex)
    {
        this.dateIndex = dateIndex;
    }

    public EmisMetaDateEnum getDateType()
    {
        return null;
    }

    public List<EmisEnumTupleValue> getDates()
    {
        return null;
    }

    public List<EmisEntity> getEntities()
    {
        return this.entityList;
    }

    public EmisMetaEntity getEntityType()
    {
        return this.entity.getEntityType();
    }

    public EmisEnumSet getEnumFilter(String name)
    {
        return null;
    }

    public Map<String, EmisEnumSet> getEnumFilters()
    {
        return null;
    }

    public void setDateType(EmisMetaDateEnum dateType)
    {
    }

    public void setDates(Collection<EmisEnumTupleValue> dates)
    {
    }

    public void setEntities(List<EmisEntity> entities)
    {
    }

    public void setEntityType(EmisMetaEntity entityType)
    {
    }

    public void addEnumFilter(EmisEnumSet values)
    {
    }

    public void setEnumFilters(Map<String, EmisEnumSet> filters)
    {
    }

    public Boolean getBooleanEntityFilter(String key)
    {
        return null;
    }

    public Set<String> getBooleanEntityFilterNames(String entityName)
    {
        return new HashSet();
    }

    public EmisEnumSet getEnumEntityFilter(String key)
    {
        return null;
    }

    public Set<String> getEnumEntityFilterNames(String entityName)
    {
        return new HashSet();
    }

    public void setBooleanEntityFilter(String key, Boolean selectTrue)
    {
    }

    public void setEnumEntityFilter(String key, EmisEnumSet values)
    {
    }
}
