package com.emistoolbox.server.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.EmisEntityIterator;
import com.emistoolbox.common.model.impl.IntArrayEntityIterator;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.model.validation.EmisValidationResultItem;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.model.validation.impl.ValidationResult;
import com.emistoolbox.common.model.validation.impl.ValidationResultItem;
import com.emistoolbox.common.results.ValidationMetaResult;
import com.emistoolbox.common.util.NamedUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.model.EmisEntityData;
import com.emistoolbox.server.model.EmisEntityDataSet;
import com.emistoolbox.server.model.impl.EntityDataAccess;
import com.emistoolbox.server.util.TableWriter;

public class ValidationTaskList extends ValidationTaskBase
{
	private EmisMetaEntity entityType; 
	
	public ValidationTaskList(ValidationMetaResult metaData, List<EmisValidation> validations, EmisDataSet dataset)
	{
		super(metaData, validations, dataset); 
		this.entityType = metaData.getListEntity(); 
	}

	public EmisValidationResult validate()
	{
		// Find list of rules that apply to the selected Entity. 
		//
		EmisValidation validation = NamedUtil.find(getValidationId(), validations);
		if (validation == null)
			return null;  

		ValidationResult result = new ValidationResult(); 
		for (EmisValidationRule rule : validation.getRules())
		{
			EmisMetaEntity ruleEntity = rule.getField(0).getEntity(); 
			if (NamedUtil.sameName(ruleEntity, entityType))
				result.add(validation, rule);
		}	

		// Validate each Entity in turn. 
		//
		List<EmisEntity> parents = new ArrayList<EmisEntity>(); 
		for (EmisEntity entity : metaData.getContext().getEntities())
		{
			parents.clear(); 
			validate(entity, result, parents); 
		}
		
		// Update fields in result
		List<EmisMetaData> fieldList = validation.getAdditionalFields();
		if (fieldList != null && fieldList.size() > 0)
		{
			result.setAdditionalFields(fieldList); 
			
			EmisMetaData[] fields = new EmisMetaData[fieldList.size()];
			EmisEntityDataSet[] entityDataSets = new EmisEntityDataSet[fieldList.size()]; 
			EntityDataAccess[] accesses = new EntityDataAccess[fieldList.size()]; 
	
			if (fieldList != null && fieldList.size() != 0)
			{
				for (int i = 0; i < fieldList.size(); i++) 
				{
					fields[i] = fieldList.get(i);
					entityDataSets[i] = dataset.getEntityDataSet(fields[i].getEntity(), fields[i].getDateType());
					accesses[i] = entityDataSets[i].getDataAccess(fields[i].getName()); 
				}
			}
			
			for (EmisValidationResultItem item : result.getItems())
			{
				EmisEntity entity = getEntity(item, entityType); 
				String[] values = new String[fieldList.size()]; 
				for (int i = 0; i < values.length; i++)
				{
					EmisEntityData entityData = entityDataSets[i].getData(getDateIndex(), entity.getId());
					if (entityData != null)
					{
						values[i] = accesses[i].getAsString(entityData.getMasterArray()); 
						if ("null".equals(values[i]))
							values[i] = null; 
					}
				}
				
				item.setAdditionalValues(values);
			}
		}
		
		return result; 
	}
	
	private EmisEntity getEntity(EmisValidationResultItem item, EmisMetaEntity entityType)
	{
		EmisEntity[] entities = item.getEntities();
		if (entities == null || entities.length == 0)
			return null; 
		
		EmisEntity lastEntity = entities[entities.length - 1]; 
		if (!NamedUtil.sameName(lastEntity.getEntityType(), entityType))
			return null; 
		
		return lastEntity; 
	}

	private String getValidationId()
	{
		if (metaData.getValidationIds().size() != 1)
			return null; 
		
		return metaData.getValidationIds().get(0); 
	}

	private void validate(EmisEntity entity, ValidationResult result, List<EmisEntity> parents)
	{
		// Check level in hierarchy. 
		if (NamedUtil.sameName(entity.getEntityType(), entityType))
		{
			ValidationResultItem item = new ValidationResultItem(); 
			String[] results = new String[result.getRules().size()]; 

			// Run validations
			int index = 0; 
			boolean anyError = false; 
			for (EmisValidationRule rule : result.getRules()) 
			{
				String error = getValidationError(rule, entityType, entity.getId(), dataset); 
				if (error != null)
				{
					results[index] = error; 
					anyError = true; 
				}

				index++; 
			}

			if (!anyError)
				// We don't keep track of items without any error.
				return; 
			
			parents.add(entity); 
			item.setEntities(parents.toArray(new EmisEntity[0]));
			parents.remove(parents.size() - 1); 

			item.setResults(results);
			
			result.add(item);
		}
		else
		{
			// Iterate into children. 
			parents.add(entity); 
			EmisEntityIterator iter = getHierarchy().getChildren(getDateIndex(), entity);
			updateNames(iter); 
			while (iter.hasNext())
				validate(iter.next(), result, parents); 

			parents.remove(parents.size() - 1); 
		}
	}
	
	private void updateNames(EmisEntityIterator iter)
	{
		if (iter instanceof IntArrayEntityIterator)
		{
			IntArrayEntityIterator intIter = (IntArrayEntityIterator) iter;  
			Map<Integer, String> names = getNames(intIter.getIds(), intIter.getEntityType()); 
			((IntArrayEntityIterator) iter).setNames(names);
		}
	}
	
	private Map<Integer, String> getNames(List<int[]> ids, EmisMetaEntity entityType)
	{
		int entityIndex = dataset.getMetaDataSet().getEntities().getIndex(entityType);   
        EmisEntityDataSet ds = dataset.getEntityDataSet(entityIndex, 0);
        Map<Integer, String> result = new HashMap<Integer, String>(); 
        for (int[] item : ids)
        	result.putAll(ds.getAllValues(getDateIndex(), "name", item));
        
        return result; 
	}
	
	public static void write(TableWriter out, EmisValidationResult validationResult)
	{
		List<EmisValidationResultItem> items = validationResult.getItems(); 
		if (items == null || items.size() == 0)
			return; 

		List<EmisMetaData> additionalFields = validationResult.getAdditionalFields(); 

		// Output header. 
		//
		writeCells(out, items.get(0).getEntities(), ENTITY_OUTPUT.TYPE); 
		writeCellsFieldNames(out, additionalFields); 
		writeCells(out, validationResult.getRules()); 
		out.nextRow(); 
		
		for (EmisValidationResultItem item : validationResult.getItems())
		{
			writeCells(out, item.getEntities(), ENTITY_OUTPUT.NAME); 
			writeCells(out, item.getAdditionalValues()); 
			writeCells(out, item.getResults());
			
			out.nextRow(); 
		}
	}
	
	private enum ENTITY_OUTPUT { TYPE, NAME }; 
	
	private static void writeCells(TableWriter out, EmisEntity[] entities, ENTITY_OUTPUT output)
	{
		for (int i = 0; i < entities.length; i++) 
		{
			if (entities[i] == null)
				out.nextCell("");
			else
			{
				if (output == ENTITY_OUTPUT.TYPE)
					out.nextCell(entities[i].getEntityType().getName()); 
				else if (output == ENTITY_OUTPUT.NAME)
				{
					if (i == entities.length - 1)
						out.nextCell(entities[i].getName() + " (" + entities[i].getId() + ")");
					else 
						out.nextCell(entities[i].getName());
				}
			}
		}
	}
	
	private static void writeCellsFieldNames(TableWriter out, List<EmisMetaData> fields)
	{
		for (EmisMetaData field : fields)
			out.nextCell(field.getName());
	}
	
	private static void writeCells(TableWriter out, String[] values)
	{
		for (String value : values)
			out.nextCell(value == null ? "" : value); 
	}
	
	private static void writeCells(TableWriter out, List<EmisValidationRule> rules)
	{
		for (EmisValidationRule rule : rules)
			out.nextCell(rule.getName());  
	}
}
