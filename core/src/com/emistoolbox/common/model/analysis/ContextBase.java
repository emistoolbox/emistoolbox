package com.emistoolbox.common.model.analysis;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;

    public List<EmisEnumTupleValue> getDates(EmisMetaDateEnum targetDateEnum)
    {
        EmisMetaDateEnum dateType = getDateType();

        Map<Integer, EmisEnumTupleValue> result = new HashMap<Integer, EmisEnumTupleValue>();
        List<EmisEnumTupleValue> dates = getDates();
        if (dates == null)
            dates = new ArrayList<EmisEnumTupleValue>();

        for (EmisEnumTupleValue value : dates)
        {
            EmisEnumTupleValue newValue = value.get(targetDateEnum);
            if (newValue == null)
                throw new IllegalArgumentException("Context defines enums of '" + dateType.getName() + "'. Cannot get '" + targetDateEnum.getName() + "' from those.");

            result.put(Integer.valueOf(targetDateEnum.getIndex(newValue.getIndex())), newValue);
        }

        List<EmisEnumTupleValue> resultList = new ArrayList<EmisEnumTupleValue>();
        resultList.addAll(result.values());

        return resultList;
    }

    public void setHierarchyDateIndex(int dateIndex)
    {
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
        return null;
    }

    public EmisMetaEntity getEntityType()
    {
        return null;
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

    public void setDates(List<EmisEnumTupleValue> dates)
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

    @Override
	public EmisEnumSet getDateEnumFilter(String dateEnumName) 
    { return null; }

	@Override
	public void addDateEnumFilter(EmisEnumSet values) 
	{}

	@Override
	public Map<String, EmisEnumSet> getDateEnumFilters() 
	{ return new HashMap(); }

	public void addBooleanEntityFilter(EmisMetaData field, Boolean selectTrue)
    {}

    public void addEnumEntityFilter(EmisMetaData field, EmisEnumSet values)
    {}

    public boolean allowEntityWithValue(EmisMetaData field, int value)
    {
        return true;
    }

    public int getHierarchyDateIndex()
    {
        return -1;
    }

    public Set<String> getEntityFilterNames(EmisMetaEntity entity)
    {
        return new HashSet();
    }

    public byte[] getEntityFilterValues(EmisMetaData field)
    {
        return null;
    }
}
