package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.BitSet;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class EnumSetImpl implements EmisEnumSet, Serializable
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum enumType;
    private BitSet bits;

    public EnumSetImpl()
    {}
    
    public EnumSetImpl(EmisMetaEnum enumType, Set<Byte> values)
    {
    	setEnum(enumType); 
    	for (byte val : values)
    		bits.set(val);
    }

    public EnumSetImpl(EmisMetaEnum enumType, byte[] values)
    {
    	setEnum(enumType); 
    	for (byte val : values)
    		bits.set(val);
    }

    public void clear()
    {
        this.bits.clear();
    }

    public void setAll()
    {
        for (int i = 0; i < this.enumType.getSize(); i++)
            this.bits.set(i);
    }

    public void addValue(String value)
    {
        addValue(this.enumType.getIndex(value));
    }

    public void addValue(byte index)
    {
        this.bits.set(index);
    }

    public boolean hasValue(byte index)
    {
        return this.bits.get(index);
    }

    public boolean hasValue(String value)
    {
        return hasValue(this.enumType.getIndex(value));
    }

    public boolean hasAllValues()
    {
        for (int i = 0; i < this.enumType.getSize(); i++)
        {
            if (!this.bits.get(i))
                return false;
        }
        return true;
    }

    public Set<String> getAll()
    {
        Set result = new HashSet();
        for (int i = this.bits.nextSetBit(0); i >= 0; i = this.bits.nextSetBit(i + 1))
        {
            result.add(this.enumType.getValue((byte) i));
        }
        return result;
    }

    public Set<Byte> getAllIndexes()
    {
        Set result = new HashSet();
        for (int i = this.bits.nextSetBit(0); i >= 0; i = this.bits.nextSetBit(i + 1))
        {
            result.add(Byte.valueOf((byte) i));
        }
        return result;
    }

    public EmisMetaEnum getEnum()
    {
        return this.enumType;
    }

    public void removeValue(String value)
    {
        removeValue(this.enumType.getIndex(value));
    }

    public void removeValue(byte index)
    {
        this.bits.clear(index);
    }

    public void setAll(Set<String> values)
    {
        for (String value : values)
            addValue(value);
    }

    public void setAllIndexes(Set<Byte> indexes)
    {
        for (Byte index : indexes)
            addValue(index.byteValue());
    }

    public void setEnum(EmisMetaEnum newEnum)
    {
        this.enumType = newEnum;
        this.bits = new BitSet(this.enumType.getSize());
        this.bits.clear();
    }

    public int getSetCount()
    {
    	int count = 0; 
        for (int i = this.bits.nextSetBit(0); i >= 0; i = this.bits.nextSetBit(i + 1))
        	count++;
        
        return count; 
    }

    public int getTotalCount()
    { return enumType.getSize(); }

	@Override
	public void opAnd(EmisEnumSet values) 
	{
		if (!NamedUtil.sameName(getEnum(), values.getEnum()))
			throw new IllegalArgumentException("Mismatching enum types."); 
		
		bits.and(((EnumSetImpl) values).bits); 
	}

	@Override
	public void opOr(EmisEnumSet values) 
	{
		if (!NamedUtil.sameName(getEnum(), values.getEnum()))
				throw new IllegalArgumentException("Mismatching enum types."); 

		bits.or(((EnumSetImpl) values).bits); 
	}

	@Override
	public void opNot() 
	{ bits.not(); }
	
	public EmisEnumSet createCopy()
	{ 
		EnumSetImpl result = new EnumSetImpl(); 
		result.enumType = enumType; 
		result.bits = bits.createCopy(); 
		
		return result; 
	}
}
