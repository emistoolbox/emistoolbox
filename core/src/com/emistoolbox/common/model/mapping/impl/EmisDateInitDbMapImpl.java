package com.emistoolbox.common.model.mapping.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.EmisDateInitDbMap;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public class EmisDateInitDbMapImpl extends EmisDbMapBaseImpl implements EmisDateInitDbMap, Serializable
{
    private static final long serialVersionUID = 1L;

    private DbRowAccess access; 
	private EmisMetaDateEnum dateType; 
	
	@Override
	public DbRowAccess getValueAccess() 
	{ return access; } 

	@Override
	public void setValueAccess(DbRowAccess access) 
	{ this.access = access; }

	@Override
	public void updateDimensions() 
	{}

	@Override
	public EmisMetaDateEnum getDateType() 
	{ return dateType; } 

	@Override
	public void setDateType(EmisMetaDateEnum dateType) 
	{ this.dateType = dateType; } 
}
