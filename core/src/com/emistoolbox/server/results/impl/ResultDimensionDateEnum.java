package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstDate;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.server.results.ResultDimension;

public class ResultDimensionDateEnum implements ResultDimension
{
    EmisMetaDateEnum dateType;
    EmisEnumSet dateFilter; 

    public ResultDimensionDateEnum(MetaResultDimensionDate dimension, EmisContext globalFilter) 
    {
    	this.dateType = dimension.getDateEnumType(); 
    	
    	if (globalFilter != null)
    		dateFilter = globalFilter.getDateEnumFilter(dateType.getName()); 
    	
    	if (dateFilter == null)
    	{
    		dateFilter = new EnumSetImpl();
    		dateFilter.setEnum(dateType);
    		dateFilter.setAll(); 
    	}
    }

    public EmisContext getContext(int index)
    { return new ContextConstDate(get(index)); }

    private EmisEnumTupleValue get(int index)
    {
    	EmisEnumTupleValue value = new EnumTupleValueImpl(); 
    	value.setEnumTuple(dateType); 
    	byte[] indexes = new byte[dateType.getDimensions()];
    	for (int i = 0; i < indexes.length; i++) 
    		indexes[i] = -1; 
    	
    	indexes[indexes.length - 1] = findEnumIndex(index); 
    	value.setIndex(indexes); 

    	return value; 
    }

    public void updateContext(int index, EmisContext context)
    {
    	throw new IllegalArgumentException("DO WE USE THIS?"); 
    	/*
        EmisEnumTupleValue value = get(index);
        context.setDateType((EmisMetaDateEnum) value.getEnumTuple());
        List dates = new ArrayList();
        dates.add(value);

        context.setDates(dates);
        */ 
    }

    public int getItemCount()
    { return this.dateFilter.getSetCount(); }

    public String getItemName(int index)
    { return this.dateType.getValues()[findEnumIndex(index)]; }

    private byte findEnumIndex(int index)
    {
        for (byte enumIndex = 0; enumIndex < dateType.getSize(); enumIndex++)
        {
        	if (dateFilter.hasValue(enumIndex))
        	{
        		if (index == 0)
        			return enumIndex; 
        		
        		index--; 
        	}
        }

        return -1; 
    }
}
