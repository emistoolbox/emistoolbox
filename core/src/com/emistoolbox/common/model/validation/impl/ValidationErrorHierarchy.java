package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationError;
import com.emistoolbox.common.model.validation.EmisValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.util.NamedUtil;

public class ValidationErrorHierarchy implements EmisValidationErrorHierarchy, Serializable 
{
	private EmisEntity entity; 
	private List<EmisValidationError> errors = new ArrayList<EmisValidationError>(); 
	private List<EmisValidationErrorHierarchy> children = new ArrayList<EmisValidationErrorHierarchy>(); 
	
	@Override
	public EmisEntity getEntity() 
	{ return entity; } 

	@Override
	public void setEntity(EmisEntity entity) 
	{ this.entity = entity; } 

	@Override
	public List<EmisValidationError> getErrors() 
	{ return errors; } 

	@Override
	public void setErrors(List<EmisValidationError> errors) 
	{ this.errors = errors; }

	@Override
	public void addError(EmisValidationError error) 
	{ this.errors.add(error); } 
	
	@Override
	public EmisValidationError findError(EmisValidation validation, EmisValidationRule rule, EmisMetaEntity entityType) 
	{
		for (EmisValidationError error : errors)
		{
			if (!NamedUtil.sameName(error.getEntityType(), entityType))
				continue; 
		
			if (!NamedUtil.sameName(validation, error.getValidation()))
				continue;

			if (!NamedUtil.sameName(rule, error.getValidationRule()))
				continue; 
			
			return error; 
		}
		
		return null;
	}

	@Override
	public List<EmisValidationErrorHierarchy> getChildren() 
	{ return children; } 

	@Override
	public void setChildren(List<EmisValidationErrorHierarchy> children) 
	{ this.children = children; } 

	@Override
	public void addChild(EmisValidationErrorHierarchy child) 
	{ this.children.add(child); }
	
	public boolean hasAnyEntries()
	{ return errors.size() + children.size() > 0; }

	public EmisValidationErrorHierarchy findChild(EmisEntity entity)
	{
		for (EmisValidationErrorHierarchy child : children)
		{
			if (child.getEntity().equals(entity))
				return child; 
		}
		
		return null; 
	}
}
