package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.model.validation.EmisValidationResultItem;
import com.emistoolbox.common.model.validation.EmisValidationRule;

public class ValidationResult implements EmisValidationResult, Serializable
{
	private List<EmisValidation> validations = new ArrayList<EmisValidation>(); 
	private List<EmisValidationRule> rules = new ArrayList<EmisValidationRule>(); 
	
	private List<EmisValidationResultItem> items = new ArrayList<EmisValidationResultItem>(); 
	private List<EmisMetaData> additionalFields = new ArrayList<EmisMetaData>(); 
	
	@Override
	public List<EmisValidation> getValidations() 
	{ return validations; } 

	@Override
	public List<EmisValidationRule> getRules() 
	{ return rules; }
	
	public void add(EmisValidation validation, EmisValidationRule rule)
	{ 
		validations.add(validation); 
		rules.add(rule); 
	}

	@Override
	public List<EmisValidationResultItem> getItems() 
	{ return items; }
	
	public void add(EmisValidationResultItem item)
	{ items.add(item); }

	@Override
	public List<EmisEntity> getChildren(EmisEntity parent) 
	{
		Map<Integer, EmisEntity> entityMap = new HashMap<Integer, EmisEntity>(); 
		for (EmisValidationResultItem item : items)
		{
			EmisEntity[] entities = item.getEntities(); 

			EmisEntity entity = null; 
			if (parent == null)
				entity = entities[0]; 
			else
			{
				int index = findIndex(parent, entities); 
				if (index != -1 && index + 1 < entities.length)
					entity = entities[index + 1];
			}
			
			if (entity != null)
				entityMap.put(entity.getId(), entity); 
		}
		
		List<EmisEntity> result = new ArrayList<EmisEntity>(); 
		result.addAll(entityMap.values());
		return result; 
	}

	private int findIndex(EmisEntity entity, EmisEntity[] entities)
	{
		for (int i = 0; i < entities.length; i++) 
			if (entity.equals(entities[i]))
				return i; 
		
		return -1; 
	}
	
	@Override
	public List<EmisValidationResultItem> getItems(EmisEntity entity) 
	{
		List<EmisValidationResultItem> result = new ArrayList<EmisValidationResultItem>(); 
		for (EmisValidationResultItem item : items)
		{
			if (-1 != findIndex(entity, item.getEntities()))
				result.add(item); 
		}

		return result;
	}

	public void clearAdditionalFields() 
	{ additionalFields.clear(); }
	
	@Override
	public List<EmisMetaData> getAdditionalFields() 
	{ return additionalFields; } 

	@Override
	public void setAdditionalFields(List<EmisMetaData> additionalFields) 
	{ this.additionalFields = additionalFields; }
	
	public void addAdditionalField(EmisMetaData field)
	{
		if (!additionalFields.contains(field))
			additionalFields.add(field); 
	}
}
