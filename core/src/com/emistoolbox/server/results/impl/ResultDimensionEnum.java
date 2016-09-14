package com.emistoolbox.server.results.impl;

import java.util.EnumSet;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstEnum;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.EnumValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.server.results.ResultDimension;

public class ResultDimensionEnum implements ResultDimension
{
    private EmisMetaEnum enumType;
    private EmisEnumSet enumFilter; 
    
    public ResultDimensionEnum(MetaResultDimensionEnum meta, EmisContext globalFilter) 
    {
        this.enumType = meta.getEnumType();
        if (this.enumType == null)
            throw new IllegalArgumentException("MetaResultDimensionEnum - enumType is null.");

        if (globalFilter != null)
    		enumFilter = globalFilter.getEnumFilter(enumType.getName()); 

        if (enumFilter == null)
    	{
    		enumFilter = new EnumSetImpl(); 
    		enumFilter.setEnum(enumType);
    		enumFilter.setAll(); 
    	}
    }

    public EmisContext getContext(int index)
    {
        EmisEnumValue enumValue = new EnumValueImpl();
        enumValue.setEnum(this.enumType);

        enumValue.setIndex(findEnumIndex(index));
        return new ContextConstEnum(enumValue);
    }

    public int getItemCount()
    { return this.enumFilter.getSetCount(); }

    public String getItemName(int index)
    { return this.enumType.getValue(findEnumIndex(index)); }
    
    private byte findEnumIndex(int index)
    {
        for (byte enumIndex = 0; enumIndex < enumType.getSize(); enumIndex++)
        {
        	if (enumFilter.hasValue(enumIndex))
        	{
        		if (index == 0)
        			return enumIndex; 
        		
        		index--; 
        	}
        }

        throw new IllegalArgumentException("Invalid index: " + index + " exceeds size " + enumFilter.getSetCount()); 
    }

    public void updateContext(int index, EmisContext context)
    {
        EmisEnumSet values = new EnumSetImpl();
        values.setEnum(this.enumType);
        values.addValue(findEnumIndex(index));
        context.addEnumFilter(values);
    }
}
