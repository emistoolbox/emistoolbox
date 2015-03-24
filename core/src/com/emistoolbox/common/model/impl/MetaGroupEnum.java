package com.emistoolbox.common.model.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaGroupEnum;

public class MetaGroupEnum implements EmisMetaGroupEnum
{
	private EmisMetaEnum baseEnum;
	
	private String name; 
	private String[] groups; 
	private Map<String, String[]> groupValues = new HashMap<String, String[]>(); 
	
	@Override
	public String[] getValues() 
	{ return groups; } 

	@Override
	public void setValues(String[] groups) 
	{ this.groups = groups; } 

	@Override
	public String getValue(byte index) 
	{ return groups[index]; } 

	@Override
	public byte getIndex(String value) 
	{
		for (byte i = 0; i < groups.length; i++) 
			if (groups[i].equals(value))
				return i; 
		
		return -1; 
	}

	@Override
	public byte getSize() 
	{ return (byte) groups.length; } 

	@Override
	public String getName() 
	{ return name; } 

	@Override
	public void setName(String name) 
	{ this.name = name; } 

	@Override
	public EmisMetaEnum getBaseEnum() 
	{ return baseEnum; } 

	@Override
	public void setBaseEnum(EmisMetaEnum baseEnum) 
	{ this.baseEnum = baseEnum; } 

	@Override
	public String[] getGroupValues(String groupName) 
	{ return groupValues.get(groupName); } 

	@Override
	public void setGroupValues(String groupName, String[] values) 
	{ groupValues.put(groupName, values); }

	@Override
	public String getSetValues(int bits) 
	{ return null; }

	@Override
	public int getSetIndexes(String values) 
	{ return 0; }
}
