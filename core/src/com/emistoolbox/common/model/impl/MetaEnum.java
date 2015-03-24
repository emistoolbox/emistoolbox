package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.impl.NamedImpl;

import java.io.Serializable;

public class MetaEnum extends NamedImpl implements EmisMetaEnum, Serializable
{
    private static final long serialVersionUID = 1L;
    private String[] values = new String[0];

    public byte getIndex(String value)
    {
        for (byte i = 0; i < this.values.length; i = (byte) (i + 1))
        {
            if (this.values[i].equals(value))
                return i;
        }
        return -1;
    }

    public String getValue(byte index)
    {
        return this.values[index];
    }

    public String[] getValues()
    {
        return this.values;
    }

    public void setValues(String[] values)
    {
        if (values.length > 127)
        {
            throw new IllegalArgumentException("Enum can only have 256 entries.");
        }
        this.values = values;
    }

    public byte getSize()
    {
        return (byte) this.values.length;
    }

	@Override
	public int getSetIndexes(String values) 
	{
		if (values == null || values.equals(""))
			return 0; 
		
		int result = 0; 
		for (String value : values.split(","))
		{
			int index = getIndex(value); 
			if (index == -1)
				continue; 
			
			result |= 1 << index; 
		}
		
		return result;
	}

	@Override
	public String getSetValues(int bits) 
	{
		StringBuffer result = new StringBuffer(); 
		String[] values = getValues();
		for (int i = 0; i < values.length; i++)
		{
			if ((bits & (1 << i)) > 0)
			{
				if (result.length() > 0)
					result.append(","); 
				result.append(values[i]); 
			}
		}
		
		return result.toString(); 
	}
}
