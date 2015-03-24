package com.emistoolbox.common.model.validation.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationRule;

public class ValidationImpl implements Serializable, EmisValidation 
{
	private String id; 
	private List<EmisValidationRule> rules = new ArrayList<EmisValidationRule>(); 
	
	private List<EmisMetaData> additionalFields = new ArrayList<EmisMetaData>();  
	private EmisMetaEntity entityType; 
	
	@Override
	public String getId() 
	{ return id; } 

	@Override
	public void setId(String id) 
	{ this.id = id; } 

	@Override
	public List<EmisValidationRule> getRules() 
	{ return rules; } 

	@Override
	public void setRules(List<EmisValidationRule> rules) 
	{ this.rules = rules; }

	@Override
	public String getName() 
	{ return getId(); } 

	@Override
	public void setName(String name) 
	{ setId(name); }

	@Override
	public void clearAdditionalFields()
	{ additionalFields.clear(); }
	
	@Override
	public List<EmisMetaData> getAdditionalFields() 
	{ return additionalFields; } 

	@Override
	public void addAdditionalField(EmisMetaData field) 
	{ 
		if (!additionalFields.contains(field))
			additionalFields.add(field); 
	}

	@Override
	public EmisMetaEntity getEntityType() 
	{ return entityType; }

	@Override
	public void setEntityType(EmisMetaEntity entityType) 
	{ this.entityType = entityType; }

}
