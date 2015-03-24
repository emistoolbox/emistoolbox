package com.emistoolbox.common.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;

public class EmisEnumUtils 
{
	public static List<EmisMetaDateEnum> sort(Set<EmisMetaDateEnum> dateTypes)
	{
		List<EmisMetaDateEnum> result = new ArrayList<EmisMetaDateEnum>(); 
		result.addAll(dateTypes); 

		Collections.sort(result, new Comparator<EmisMetaEnumTuple>() {
			@Override
			public int compare(EmisMetaEnumTuple arg1, EmisMetaEnumTuple arg2) 
			{
				EmisMetaEnum[] enums1 = arg1.getEnums(); 
				EmisMetaEnum[] enums2 = arg2.getEnums(); 
				
				for (int i = 0; i < Math.max(enums1.length, enums2.length); i++)
				{
					if (i >= enums1.length)
						return -1; 
					if (i >= enums2.length)
						return 1; 

					int result = enums1[i].getName().compareTo(enums2[i].getName()); 
					if (result != 0)
						return result;
				}
				
				return 0; 
			}
		});

		return result; 
	}
	
	public static EmisMetaDateEnum findLowestEnum(Set<EmisMetaDateEnum> dateTypes)
	{
		EmisMetaDateEnum result = null; 
		for (EmisMetaDateEnum dateType : dateTypes)
		{
			if (result == null || result.getDimensions() < dateType.getDimensions())
				result = dateType; 
		}
		
		// Now verify that we are including all enums. 
		for (EmisMetaDateEnum dateType : dateTypes)
			if (-1 == result.findEnumPosition(dateType))
				throw new IllegalArgumentException("Failed to find enum " + dateType.getName() + " in parents of " + result.getName()); 
		
		return result; 
	}
	
	public static EmisEnumTupleValue getValueAs(EmisEnumTupleValue value, Set<EmisMetaDateEnum> dateTypes)
	{
		EmisMetaDateEnum dateType = EmisEnumUtils.findLowestEnum(dateTypes); 
		
		EmisMetaEnum[] enums = dateType.getEnums(); 
		byte[] resultIndexes = new byte[dateType.getDimensions()];
		byte[] valueIndexes = value.getIndex(); 
		for (int i = 0; i < resultIndexes.length; i++) 
		{
			int pos = -1; 
			if (dateTypes.contains(enums[i]))
				pos = value.getEnumTuple().findEnumPosition(enums[i]); 
			
			if (pos == -1)
				resultIndexes[i] = -1; 
			else
				resultIndexes[i] = valueIndexes[pos]; 
		}

		EmisEnumTupleValue result = new EnumTupleValueImpl(); 
		result.setEnumTuple(dateType);
		result.setIndex(resultIndexes);
		
		return result; 
	}
}
