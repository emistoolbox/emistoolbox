package com.emistoolbox.server.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationError;
import com.emistoolbox.common.model.validation.EmisValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.EmisValidationFilter;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.impl.ValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.impl.ValidationErrorImpl;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;

public class ValidationTask extends ValidationTaskBase
{
	public ValidationTask(ValidationMetaResult metaData, List<EmisValidation> validations, EmisDataSet dataset)
	{ super(metaData, validations, dataset); }

	public EmisValidationErrorHierarchy validate()
	{
		ValidationErrorHierarchy result = new ValidationErrorHierarchy(); 
		for (String validationId : metaData.getValidationIds())
		{
			EmisValidation validation = NamedUtil.find(validationId, validations);
			if (validation != null)
				validate(validation, result); 
		}
		
		return result; 
	}

	private void validate(EmisValidation validation, ValidationErrorHierarchy parentResult)
	{
		EmisMetaEntity entityType = metaData.getContext().getEntityType(); 
		if (entityType == null)
			entityType = getMetaHierarchy().getEntityOrder().get(0); 
		
		for (EmisValidationRule rule : validation.getRules())
		{
			EmisMetaEntity fieldEntityType = rule.getField(0).getEntity(); 
			if (entityType.isChildOf(fieldEntityType, getMetaHierarchy()))
				continue;

			for (EmisEntity entity : metaData.getContext().getEntities())
				addValidationErrors(validation, rule, entity, parentResult); 
		}
	}
	
	private void addValidationErrors(EmisValidation validation, EmisValidationRule rule, EmisEntity entity, EmisValidationErrorHierarchy validationResult)
	{
		// Check if rule applies to entity.  
		// 
		EmisMetaEntity ruleEntity = rule.getField(0).getEntity(); 
		if (NamedUtil.sameName(ruleEntity, entity.getEntityType()))
		{
			// Yes it does - run validation against data in this entity. 
			if (null != getValidationError(rule, ruleEntity, entity.getId(), dataset))
				addError(entity, validation, rule, validationResult);
		}
		else 
		{
			boolean newChildEntry = false; 
			EmisValidationErrorHierarchy childValidationResult = validationResult.findChild(entity); 
			if (childValidationResult == null)
			{
				childValidationResult = new ValidationErrorHierarchy(); 
				childValidationResult.setEntity(entity);
				newChildEntry = true;
			}
			
			// No it doesn't - iterate deeper into the hierarchy. 
			EmisEntityIterator iter = getHierarchy().getChildren(getDateIndex(), entity);
			List<EmisEntity> childrenEntities = new ArrayList<EmisEntity>(); 
			while (iter.hasNext())
			{
				EmisEntity childEntity = iter.next(); 
				childrenEntities.add(childEntity); 
				addValidationErrors(validation, rule, childEntity, childValidationResult); 
			}
			
			updateEntityFields(childrenEntities); 
			
			if (newChildEntry && childValidationResult.hasAnyEntries())
				validationResult.addChild(childValidationResult);
		}
	}
	
	private void updateEntityFields(List<EmisEntity> entities)
	{
		if (entities.size() == 0)
			return; 

		EmisMetaEntity entityType = entities.get(0).getEntityType(); 
		EmisMetaData field = NamedUtil.find("name",  entityType.getData()); 

		EmisEntityDataSet entityDataSet = dataset.getEntityDataSet(field.getEntity(), field.getDateType());
		EntityDataAccess access = entityDataSet.getDataAccess("name"); 
		
		for (EmisEntity entity : entities)
		{
			EmisEntityData entityData = entityDataSet.getData(getDateIndex(), entity.getId());
			if (entityData != null)
				entity.setName(access.getAsString(entityData.getMasterArray())); 
		}
	}
	
	private void addError(EmisEntity entity, EmisValidation validation, EmisValidationRule rule, EmisValidationErrorHierarchy validationErrors)
	{
		EmisValidationError error = validationErrors.findError(validation, rule, entity.getEntityType());
		if (error == null)
		{
			error = new ValidationErrorImpl(); 
			error.setValidation(validation); 
			error.setValidationRule(rule); 
			error.setEntityType(entity.getEntityType());
			
			validationErrors.addError(error);
		}
			
		error.addEntityId(entity.getId());
	}

}

class SingleGroupByValidationFilter implements EmisValidationFilter, Serializable
{
	private Map<String, EmisEnumSet> filters = new HashMap<String, EmisEnumSet>(); 
	
	public SingleGroupByValidationFilter(byte[] groupByIndexes, EmisMetaEnumTuple groupByEnums, EmisValidationFilter filter)
	{
		// By default we use the filters. 
		if (filter != null)
		{
			for (Map.Entry<String, EmisEnumSet> entry : filter.getFilters().entrySet())
				filters.put(entry.getKey(), entry.getValue());
		}
		
		// Override the filters with the fixed values for the group by. 
		EmisMetaEnum[] enums = groupByEnums.getEnums(); 
		for (int i = 0; i < groupByIndexes.length; i++)
		{
			EmisEnumSet indexEnumSet = new EnumSetImpl();
			indexEnumSet.setEnum(enums[i]);
			indexEnumSet.addValue(groupByIndexes[i]); // getCurrentIndex(enums[i], groupByIndexes, targetEnums)); 
			addFilter(indexEnumSet);
		}
	}
	
//	private byte getCurrentIndex(EmisMetaEnum enumType, byte[] indexes, EmisMetaEnum[] dimensions)
//	{
//		for (int i = 0; i < dimensions.length; i++)
//		{
//			if (NamedUtil.sameName(dimensions[i], enumType))
//				return indexes[i];
//		}
//		
//		throw new IllegalArgumentException("Enum '" + enumType.getName() + "' should be part of dimensions.");
//	}

	@Override
	public Map<String, EmisEnumSet> getFilters() 
	{ return filters; }

	@Override
	public void setFilters(Map<String, EmisEnumSet> filters) 
	{ throw new NotImplementedException(); }

	@Override
	public void addFilter(EmisEnumSet filter) 
	{ filters.put(filter.getEnum().getName(), filter); }

	@Override
	public void addAll(Collection<EmisEnumSet> filters) 
	{ throw new NotImplementedException(); }

	@Override
	public EmisEnumSet getFilterFor(EmisMetaEnum enumType) 
	{ return filters.get(enumType.getName()); }
}
