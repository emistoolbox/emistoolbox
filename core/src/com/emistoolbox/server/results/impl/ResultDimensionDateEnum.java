package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstDate;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.results.impl.MetaResultDimensionDate;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.results.ResultDimension;
import java.util.ArrayList;
import java.util.List;

public class ResultDimensionDateEnum implements ResultDimension
{
    EmisMetaDateEnum dateType;

    public ResultDimensionDateEnum(MetaResultDimensionDate dimension) 
    { this.dateType = dimension.getDateEnumType(); }

    public EmisContext getContext(int index)
    { return new ContextConstDate(get(index)); }

    private EmisEnumTupleValue get(int index)
    {
    	EmisEnumTupleValue value = new EnumTupleValueImpl(); 
    	value.setEnumTuple(dateType); 
    	byte[] indexes = new byte[dateType.getDimensions()];
    	for (int i = 0; i < indexes.length; i++) 
    		indexes[i] = -1; 
    	
    	indexes[indexes.length - 1] = (byte) index; 
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
    {
        return this.dateType.getSize();
    }

    public String getItemName(int index)
    {
        return this.dateType.getValues()[index];
    }
}
