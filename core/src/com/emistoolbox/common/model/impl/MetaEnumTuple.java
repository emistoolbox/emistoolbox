package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;

public class MetaEnumTuple implements EmisMetaEnumTuple, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum[] enums;

    @Override
	public int findEnumPosition(EmisMetaEnum e) 
    {
    	for (int i = 0; i < enums.length; i++) 
    		if (NamedUtil.sameName(e,  enums[i]))
    			return i; 
    	
    	return -1; 
	}

	public int getCombinations()
    {
        int total = 1;
        for (int i = 0; i < this.enums.length; i++)
        {
            total *= this.enums[i].getSize();
        }
        return total;
    }

    public byte getDimensions()
    {
        return (byte) this.enums.length;
    }

    public EmisMetaEnum[] getEnums()
    {
        return this.enums;
    }
    
	public byte[] getSizes() 
	{
		byte[] result = new byte[enums.length]; 
		for (int i = 0; i < enums.length; i++) 
			result[i] = enums[i].getSize(); 

		return result; 
	}

	public int getIndex(byte[] indexes)
    {
        int index = 0;
        for (int i = 0; i < this.enums.length; i++)
        {
            index *= this.enums[i].getSize();
            index += indexes[i];
        }

        return index;
    }

    public int getIndex(String[] values)
    {
        byte[] indexes = new byte[this.enums.length];
        for (int i = 0; i < this.enums.length; i++)
        {
            indexes[i] = this.enums[i].getIndex(values[i]);
        }
        return getIndex(indexes);
    }

    public void setEnums(EmisMetaEnum[] enums)
    {
        this.enums = enums;
    }

    public EmisMetaEnum getMetaEnum()
    {
        if ((this.enums == null) || (this.enums.length == 0))
        {
            return null;
        }
        return this.enums[(this.enums.length - 1)];
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.impl.MetaEnumTuple JD-Core
 * Version: 0.6.0
 */