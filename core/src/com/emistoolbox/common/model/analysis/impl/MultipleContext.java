package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultipleContext extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private List<EmisContext> contexts = new ArrayList();
    private EmisMetaDateEnum targetDateType; 
    
    public MultipleContext() 
    {}

    public MultipleContext(EmisContext[] contexts, EmisMetaDateEnum targetDateType) 
    {
    	this.targetDateType = targetDateType; 
    	this.contexts.addAll(Arrays.asList(contexts)); 
    }

    public void add(EmisContext c)
    { this.contexts.add(c); }

    public EmisMetaDateEnum getDateType()
    { return targetDateType; }
    
/* TODO remove?
 *     	EmisMetaDateEnum result = null; 
        for (EmisContext c : this.contexts)
        {
            if (c.getDateType() == null)
            	continue; 
            
            if (result == null || result.getDimensions() < c.getDateType().getDimensions())
            	result = c.getDateType(); 
        }

        return result;
    }
*/ 
    public List<EmisEnumTupleValue> getDates()
    {
    	List<EmisEnumTupleValue> results = new ArrayList<EmisEnumTupleValue>(); 
    	List<EmisEnumTupleValue> overrides = new ArrayList<EmisEnumTupleValue>(); 
    	
        for (EmisContext c : this.contexts)
        {
        	List<EmisEnumTupleValue> newDates = c.getDates(); 
        	if (newDates == null)
        		continue; 
        	
        	for (EmisEnumTupleValue date : newDates)
        	{
        		if (isMask(date))
        			overrides.add(date);
        		else
        			results.add(date); 
        	}
        }

        if (results.size() == 0)
        	results.add(getDefaultDate()); 
        
        for (EmisEnumTupleValue result : results)
        {
        	for (EmisEnumTupleValue override : overrides)
        		overrideDate(result, override); 
        }

        return results;
    }
    
    private EmisEnumTupleValue getDefaultDate()
    {
    	EmisEnumTupleValue value = new EnumTupleValueImpl();
    	value.setEnumTuple(targetDateType);
    	    	
    	byte[] indexes = new byte[targetDateType.getDimensions()]; 
    	for (int i = 0; i < indexes.length; i++) 
    		indexes[i] = 0; 
    	value.setIndex(indexes);

    	return value; 
    }
    
    private void overrideDate(EmisEnumTupleValue value, EmisEnumTupleValue override)
    {
    	EmisMetaEnum[] valueEnums = value.getEnumTuple().getEnums(); 
    	byte[] valueIndexes = value.getIndex(); 

    	EmisMetaEnum[] overrideEnums = override.getEnumTuple().getEnums(); 
    	byte[] overrideIndexes = override.getIndex(); 
    	
    	for (int i = 0; i < Math.min(valueIndexes.length, overrideIndexes.length); i++) 
    	{
    		if (!NamedUtil.sameName(valueEnums[i], overrideEnums[i]))
    			throw new IllegalArgumentException("Expected same date enum at position " + i + " (was " + valueEnums[i].getName() + " and " + overrideEnums[i]);
    		
    		if (overrideIndexes[i] != -1)
    			valueIndexes[i] = overrideIndexes[i]; 
    	}
    	
    	value.setIndex(valueIndexes);
    }
    
    private boolean isMask(EmisEnumTupleValue date)
    {
    	if (date.getEnumTuple().getDimensions() < targetDateType.getDimensions())
    		return true; 
    	
    	for (byte index : date.getIndex())
    	{
    		if (index == -1)
    			return true; 
    	}
    	
    	return false; 
    }

    public List<EmisEntity> getEntities()
    {
        for (EmisContext c : this.contexts)
        {
            if (c.getEntities() != null)
                return c.getEntities();
        }
        return null;
    }

    public EmisMetaEntity getEntityType()
    {
        for (EmisContext c : this.contexts)
        {
            if (c.getEntityType() != null)
            {
                return c.getEntityType();
            }
        }
        return null;
    }

    public EmisEnumSet getEnumFilter(String name)
    {
        for (EmisContext c : this.contexts)
        {
            if (c.getEnumFilter(name) != null)
            {
                return c.getEnumFilter(name);
            }
        }
        return null;
    }

    public Map<String, EmisEnumSet> getEnumFilters()
    {
        Map result = new HashMap();
        for (EmisContext c : this.contexts)
            result.putAll(c.getEnumFilters());

        return result;
    }

    public int getHierarchyDateIndex()
    {
        for (EmisContext c : this.contexts)
        {
            if (c.getHierarchyDateIndex() != -1)
                return c.getHierarchyDateIndex();
        }
        return -1;
    }

    public boolean allowEntityWithValue(EmisMetaData field, int value)
    {
        for (EmisContext c : this.contexts)
        {
            if (!c.allowEntityWithValue(field, value))
                return false;
        }
        return true;
    }

    public Set<String> getEntityFilterNames(EmisMetaEntity entity)
    {
        Set result = new HashSet();
        for (EmisContext c : this.contexts)
            result.addAll(c.getEntityFilterNames(entity));

        return result;
    }
}
