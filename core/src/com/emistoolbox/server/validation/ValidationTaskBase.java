package com.emistoolbox.server.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.FlatArrayUtil;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisHierarchy;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.MetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;

public class ValidationTaskBase 
{
	protected ValidationMetaResult metaData; 
	protected List<EmisValidation> validations; 
	protected EmisDataSet dataset = null; 
	
	public ValidationTaskBase(ValidationMetaResult metaData, List<EmisValidation> validations, EmisDataSet dataset)
	{
		this.metaData = metaData; 
		this.validations = validations; 
		this.dataset = dataset; 
	}

	protected EmisMetaHierarchy getMetaHierarchy()
	{ return metaData.getHierarchy(); } 
	
	protected EmisHierarchy getHierarchy()
	{
		EmisMetaHierarchy metaHierarchy = metaData.getHierarchy();
		return dataset.getHierarchy(metaHierarchy.getName());
	}
		
	protected byte getDateIndex()
	{ return metaData.getDateIndex(); } 

	public String getValidationError(EmisValidationRule rule, EmisMetaEntity entityType, int entityId, EmisDataSet dataset)
	{
		EmisMetaDateEnum dateType = rule.getField(0).getDateType(); 
		EmisEntityDataSet entityDataSet = dataset.getEntityDataSet(entityType, dateType);

		// List of values for each field in the rule. 
		// The List<Integer> contains a value for each group by item (or just a single value if no group by is specified). 
		List<Integer>[] valuesByField = new List[rule.getFieldCount()]; 
		for (int i = 0; i < rule.getFieldCount(); i++)
		{
			EmisMetaData field = rule.getField(i); 
			EntityDataAccess access = entityDataSet.getDataAccess(field.getName());  
			
			int dateIndex = getDateIndex() + rule.getDateOffset(i);
			if (dateIndex < 0)
				return null;

			EmisEntityData entityData = entityDataSet.getData(getDateIndex(), entityId);
			if (entityData != null)
				valuesByField[i] = getValues(field, rule.getFilter(i), rule.getGroupBy(), access, entityData.getMasterArray()); 
		}

		// For each value in the list we check if they conform the rule. 
		int[] values = new int[rule.getFieldCount()];
		int valuesCount = valuesByField[0].size();
		for (int i = 0; i < valuesCount; i++)
		{
			for (int fieldIndex = 0; fieldIndex < values.length; fieldIndex++)
				values[fieldIndex] = valuesByField[fieldIndex].get(i);

			if (null != rule.getValidationError(values))
				return getValueString(values); 
		}
		
		return null; 
	}
	
	private String getValueString(int[] values)
	{
		StringBuffer result = new StringBuffer(); 
		for (int value : values)
		{
			if (result.length() > 0)
				result.append("/"); 
			
			result.append(value); 
		}
		
		return result.toString(); 
	}

	private List<Integer> getValues(EmisMetaData field, EmisValidationFilter filter, EmisValidationFilter groupBy, EntityDataAccess access, Object[] data)
	{
		List<Integer> result = new ArrayList<Integer>(); 
		if (groupBy == null)
			result.add(getValue(field,  filter,  access,  data));
		else
		{
			EmisMetaEnumTuple groupDimensions = getGroupDimensions(groupBy); 
			for (byte[] groupByIndex : getIndexes(groupDimensions, groupBy))
				result.add(getValue(field, new SingleGroupByValidationFilter(groupByIndex, groupDimensions, filter), access, data));
		}
		
		return result; 
	}
	
	private EmisMetaEnumTuple getGroupDimensions(EmisValidationFilter groupBy)
	{
		Map<String, EmisEnumSet> filters = groupBy.getFilters(); 
		EmisMetaEnum[] enums = new EmisMetaEnum[filters.size()]; 

		int i = 0; 
		for (EmisEnumSet value : filters.values())
			enums[i++] = value.getEnum(); 
		
		EmisMetaEnumTuple result = new MetaEnumTuple(); 
		result.setEnums(enums);

		return result; 
	}
	
	
	private Integer getValue(EmisMetaData field, EmisValidationFilter filter, EntityDataAccess access, Object[] data)
	{
		if (field.getArrayDimensions() == null)
			return access.getAsInt(data, 0); 
		
		int result = -1; 
		byte[] dimensionSizes = field.getArrayDimensions().getSizes();
		for (byte[] index : getIndexes(field.getArrayDimensions(), filter))
		{
			int arrayIndex = FlatArrayUtil.getFlatIndex(index, dimensionSizes); 
			result = add(result, access.getAsInt(data, arrayIndex));  
		}
		
		return result;
	}
	
	private List<byte[]> getIndexes(EmisMetaEnumTuple dimensions, EmisValidationFilter filter)
	{
		List<byte[]> result = new ArrayList<byte[]>(); 
		
		EmisMetaEnum[] enumTypes = dimensions.getEnums();
		EmisEnumSet[] enumValues = new EmisEnumSet[dimensions.getDimensions()];
		for (int i = 0; i < enumTypes.length; i++)
			enumValues[i] = getFilterWithDefault(filter, enumTypes[i]); 
		
		// Set indexes to initial values. 
		byte[] indexes = new byte[enumTypes.length];
		for (int i = 0; i < indexes.length; i++) 
			indexes[i] = nextIndex((byte) -1, enumValues[i]);

		// Add initial value. 
		if (!contains((byte) -1, indexes))
			result.add(indexes.clone());

		do
		{
			// 
			// To find next index value.
			// - increase index at first position
			// - if there is no new index value - go back to initial value and 
			//   increase next index value. 
			// - continue until we've reached highest index and have no more values. 
			//
			for (int i = 0; i < enumTypes.length; i++) 
			{
				indexes[i] = nextIndex(indexes[i], enumValues[i]);
				if (indexes[i] != -1)
					// Got new index value - exit for-loop and add value to results. 
					break; 
				else if (i != enumTypes.length - 1)
					// Set index value to first and allow for-loop to find next value in next position. 
					indexes[i] = nextIndex((byte) -1, enumValues[i]); 
				else
					// Reached the end of indexes - return result
					return result; 
			}

			// Add valid value to result. 
			result.add(indexes.clone());
		} while (!contains((byte) -1, indexes));

		return result; 
	}
	
	private boolean contains(byte needle, byte[] haystack)
	{
		for (byte b : haystack)
			if (needle == b)
				return true; 
		
		return false; 
	}
	
	private int add(int value1, int value2)
	{
		if (value1 == -1)
			return value2; 
		
		if (value2 == -1)
			return value1; 
		
		return value1 + value2; 
	}
		
	private byte nextIndex(byte pos, EmisEnumSet values)
	{
		if (pos == -1)
			pos = 0; 
		else
			pos++; 

		EmisMetaEnum enumType = values.getEnum(); 
		while (pos < enumType.getSize())
		{
			if (values.hasValue(pos))
				return pos;

			pos++; 
		}

		return -1;
	}

	private EmisEnumSet getFilterWithDefault(EmisValidationFilter filter, EmisMetaEnum entityType)
	{
		EmisEnumSet result = null; 
		if (filter != null)
			result = filter.getFilterFor(entityType);
		
		if (result == null)
		{
			result = new EnumSetImpl();
			result.setEnum(entityType);
			result.setAll(); 
		}
		
		return result; 
	}
}
