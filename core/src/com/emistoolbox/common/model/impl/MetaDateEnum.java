package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;

public class MetaDateEnum extends MetaEnum implements EmisMetaDateEnum, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaDateEnum parent;
    
    private boolean allowDynamicInit = false; 

    @Override
	public int findEnumPosition(EmisMetaEnum e) 
    {
    	EmisMetaEnum[] enums = getEnums(); 
    	for (int i = 0; i < enums.length; i++) 
    		if (NamedUtil.sameName(enums[i], e))
    			return i; 
    	
    	return -1; 
	}

	public boolean hasAllowDynamicInit() 
	{ return allowDynamicInit; }

	public void setAllowDynamicInit(boolean allowDynamicInit) 
	{ this.allowDynamicInit = allowDynamicInit; }


	public int getCombinations()
    { return this.parent == null ? getSize() : this.parent.getCombinations() * getSize(); }

    public byte getDimensions()
    { return this.parent == null ? 1 : (byte) (this.parent.getDimensions() + 1); }

    public EmisMetaEnum[] getEnums()
    {
        if (this.parent == null)
        {
            return new EmisMetaEnum[] { this };
        }
        EmisMetaEnum[] parentEnums = this.parent.getEnums();
        EmisMetaEnum[] result = new EmisMetaEnum[parentEnums.length + 1];
        for (int i = 0; i < parentEnums.length; i++)
        {
            result[i] = parentEnums[i];
        }
        result[parentEnums.length] = this;
        return result;
    }

    public byte[] getSizes()
    {
    	EmisMetaEnum[] enums = getEnums(); 
		byte[] result = new byte[enums.length]; 
		for (int i = 0; i < enums.length; i++) 
			result[i] = enums[i].getSize(); 

		return result; 
    }

    public int getIndex(byte[] indexes)
    {
        if (this.parent == null)
        {
            return indexes[0];
        }
        byte[] tmp = new byte[indexes.length - 1];
        for (int i = 0; i < tmp.length; i++)
        {
            tmp[i] = indexes[i];
        }
        return this.parent.getIndex(tmp) * getSize() + indexes[tmp.length];
    }

    public int getIndex(String[] values)
    {
        if (this.parent == null)
        {
            return getIndex(values[0]);
        }
        EmisMetaEnum[] parentEnums = this.parent.getEnums();
        byte[] tmp = new byte[values.length];
        for (int i = 0; i < parentEnums.length; i++)
            tmp[i] = parentEnums[i].getIndex(values[i]);
        tmp[parentEnums.length] = getIndex(values[parentEnums.length]);

        return getIndex(tmp);
    }

    public EmisMetaDateEnum getParent()
    {
        return this.parent;
    }

    public void setParent(EmisMetaDateEnum parent)
    {
        this.parent = parent;
    }

    public void setEnums(EmisMetaEnum[] enums)
    {
        throw new IllegalArgumentException("Cannot set enums directly - use parent date enum instead.");
    }

    public boolean isAncestor(EmisMetaDateEnum ancestor)
    {
        EmisMetaDateEnum child = getParent();
        while (child != null)
        {
            if (NamedUtil.sameName(ancestor, child))
            {
                return true;
            }
            child = child.getParent();
        }

        return false;
    }

    public EmisMetaEnum getMetaEnum()
    {
        return this;
    }
}
