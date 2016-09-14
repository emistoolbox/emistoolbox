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
import java.util.ArrayList;
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
    private Map<String, EmisEnumSet> enumFilters;
    private Map<String, EmisEnumSet> dateEnumFilters = new HashMap<String, EmisEnumSet>();
    private Map<String, byte[]> entityFilters = new HashMap<String, byte[]>();

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
    { return this.enumFilters == null ? null : (EmisEnumSet) this.enumFilters.get(name); }

    public Map<String, EmisEnumSet> getEnumFilters()
    { return this.enumFilters; }

    @Override
	public EmisEnumSet getDateEnumFilter(String dateEnumName) 
    { return dateEnumFilters.get(dateEnumName); }

	@Override
	public void addDateEnumFilter(EmisEnumSet values) 
	{ dateEnumFilters.put(values.getEnum().getName(), values); }

	@Override
	public Map<String, EmisEnumSet> getDateEnumFilters() 
	{ return dateEnumFilters; }

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
        if (this.enumFilters == null)
            this.enumFilters = new HashMap();
        this.enumFilters.put(values.getEnum().getName(), values);
    }

    public void setEnumFilters(Map<String, EmisEnumSet> enums)
    { this.enumFilters = enums; }

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
                result.add(key.substring(entity.getName().length() + 1));
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
    
    public EmisContext createCopy()
    {
		Context result = new Context(); 
		result.dateIndex = dateIndex; 
		result.dateType = dateType; 
		if (dates != null)
		{
			result.dates = new ArrayList<EmisEnumTupleValue>(); 
			for (EmisEnumTupleValue date : dates)
				result.dates.add(date.createCopy()); 
		}
		
		if (entities != null)
		{
			result.entities = new ArrayList<EmisEntity>(); 
			result.entities.addAll(entities); 
		}
		
		result.entityType = entityType; 
		
		if (entityFilters != null)
		{
			result.entityFilters = new HashMap<String, byte[]>();
			for (Map.Entry<String, byte[]> filter : entityFilters.entrySet())
			{
				byte[] oldValues = filter.getValue(); 
				byte[] values = new byte[oldValues.length];
				for (int i = 0; i < values.length; i++) 
					values[i] = oldValues[i]; 
				
				result.entityFilters.put(filter.getKey(), values); 
			}
		}
		
		if (dateEnumFilters != null)
		{
			result.dateEnumFilters = new HashMap<String, EmisEnumSet>(); 
			for (Map.Entry<String, EmisEnumSet> dateFilter : dateEnumFilters.entrySet())
				result.dateEnumFilters.put(dateFilter.getKey(), dateFilter.getValue().createCopy()); 
		}
		
		if (enumFilters != null)
		{
			result.enumFilters = new HashMap<String, EmisEnumSet>(); 
			for (Map.Entry<String, EmisEnumSet> enumFilter : enumFilters.entrySet())
				result.enumFilters.put(enumFilter.getKey(), enumFilter.getValue().createCopy()); 
		}

		return result; 
    }
}
