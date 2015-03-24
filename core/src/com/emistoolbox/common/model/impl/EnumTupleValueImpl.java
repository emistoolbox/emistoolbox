package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;
import java.util.Set;

public class EnumTupleValueImpl implements EmisEnumTupleValue, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnumTuple meta;
    private byte[] indexes;

    public EmisMetaEnumTuple getEnumTuple()
    { return this.meta; }

    public byte[] getIndex()
    { return this.indexes; }

    public String[] getValue()
    {
        EmisMetaEnum[] enums = this.meta.getEnums();
        String[] result = new String[this.indexes.length];
        for (int i = 0; i < result.length; i++)
            result[i] = enums[i].getValue(this.indexes[i]);
        return result;
    }

    public void setEnumTuple(EmisMetaEnumTuple tuple)
    {
        this.meta = tuple;
        this.indexes = new byte[this.meta.getDimensions()];
    }

    public void setIndex(byte[] indexes)
    {
        if (indexes.length != this.meta.getDimensions())
            throw new IllegalArgumentException("Expected index with " + this.meta.getDimensions() + " indexes, not " + indexes.length);

        this.indexes = indexes;
    }

    public void setValue(String[] values)
    {
        EmisMetaEnum[] enums = this.meta.getEnums();
        for (int i = 0; i < values.length; i++)
            this.indexes[i] = enums[i].getIndex(values[i]);
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();
        String delim = ""; 
        for (int i = 0; i < this.indexes.length; i++)
        {
            if (this.indexes[i] == -1)
                continue; 

            result.append(delim);
            delim = " "; 

            EmisMetaEnum[] enums = this.meta.getEnums();
            result.append(enums[i].getValue(this.indexes[i]));
        }

        return result.toString();
    }

    public <T extends EmisMetaEnumTuple> EmisEnumTupleValue get(T enumTupleType)
    {
        if (enumTupleType == null)
            return null; 
        
        EmisMetaDateEnum targetEnum = (EmisMetaDateEnum) enumTupleType.getMetaEnum();
        EmisMetaDateEnum currentEnum = (EmisMetaDateEnum) getEnumTuple().getMetaEnum();

        if (NamedUtil.sameName(currentEnum, targetEnum))
            return this;

        if (!currentEnum.isAncestor(targetEnum))
            return null;

        EnumTupleValueImpl result = new EnumTupleValueImpl();
        result.setEnumTuple(enumTupleType);
        byte[] newIndexes = new byte[enumTupleType.getDimensions()];
        for (int i = 0; i < Math.min(indexes.length, newIndexes.length); i++)
            newIndexes[i] = this.indexes[i];
        result.setIndex(newIndexes);

        return result;
    }
    
    public <T extends EmisMetaEnumTuple> EmisEnumTupleValue get(Set<T> dateTypes)
    {
    	EmisEnumTupleValue bestValue = null; 
    	for (T dateType : dateTypes)
    	{
    		EmisEnumTupleValue value = get(dateType);
    		if (value == null)
    			continue; 
    		
    		if (bestValue == null || bestValue.getEnumTuple().getDimensions() < value.getEnumTuple().getDimensions())
    			bestValue = value; 
    	}
    	
    	return bestValue; 
    }
}

