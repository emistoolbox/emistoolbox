package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract interface EmisContext
{
    public abstract int getHierarchyDateIndex();

    public abstract void setHierarchyDateIndex(int paramInt);

    public abstract EmisMetaEntity getEntityType();

    public abstract void setEntityType(EmisMetaEntity paramEmisMetaEntity);

    public abstract List<EmisEntity> getEntities();

    public abstract void setEntities(List<EmisEntity> paramList);

    public abstract EmisMetaDateEnum getDateType();

    public abstract void setDateType(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract List<EmisEnumTupleValue> getDates();

    public abstract void setDates(List<EmisEnumTupleValue> paramList);

    public abstract List<EmisEnumTupleValue> getDates(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisEnumSet getEnumFilter(String paramString);

    public abstract void addEnumFilter(EmisEnumSet paramEmisEnumSet);

    public abstract Map<String, EmisEnumSet> getEnumFilters();

    public abstract EmisEnumSet getDateEnumFilter(String dateEnumName); 
    
    public abstract void addDateEnumFilter(EmisEnumSet values); 
    
    public abstract Map<String, EmisEnumSet> getDateEnumFilters(); 
    
    public abstract void setEnumFilters(Map<String, EmisEnumSet> paramMap);

    public abstract Set<String> getEntityFilterNames(EmisMetaEntity paramEmisMetaEntity);

    public abstract boolean allowEntityWithValue(EmisMetaData paramEmisMetaData, int paramInt);

    public abstract void addBooleanEntityFilter(EmisMetaData paramEmisMetaData, Boolean paramBoolean);

    public abstract void addEnumEntityFilter(EmisMetaData paramEmisMetaData, EmisEnumSet paramEmisEnumSet);

    public abstract byte[] getEntityFilterValues(EmisMetaData paramEmisMetaData);
}
