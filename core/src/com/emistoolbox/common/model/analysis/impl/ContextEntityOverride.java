package com.emistoolbox.common.model.analysis.impl;

import java.util.Collections;
import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public class ContextEntityOverride extends ContextAdaptor 
{
	private EmisEntity entity; 
	
	public ContextEntityOverride(EmisEntity entity, EmisContext context)
	{
		super(context); 
		this.entity = entity; 
	}

	@Override
	public List<EmisEntity> getEntities() 
	{ return Collections.singletonList(entity); }

	@Override
	public EmisMetaEntity getEntityType() 
	{ return entity.getEntityType(); } 

	@Override
	public void setEntities(List<EmisEntity> entities) 
	{}

	@Override
	public void setEntityType(EmisMetaEntity entityType) 
	{}
}
