package com.emistoolbox.common.model.meta;

public interface EmisMetaGroupEnum extends EmisMetaEnum 
{
	// Groupings are defined like any normal enum. 

	/** Enum on which grouping is based. */ 
	public EmisMetaEnum getBaseEnum();
	public void setBaseEnum(EmisMetaEnum baseEnum); 

	/** @return List of enum values from the base enum that are grouped in the specified group. */
	public String[] getGroupValues(String groupName);
	
	public void setGroupValues(String groupName, String[] values);
}
