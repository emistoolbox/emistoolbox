package com.emistoolbox.common.model.analysis;

import java.io.Serializable;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;

public class FilterTarget implements Serializable
{
	private EmisMetaData field; 
	private EmisMetaEnum enumType;
	
	public FilterTarget()
	{}
	
	public FilterTarget(EmisMetaData field)
	{ this.field = field; }
	
	public FilterTarget(EmisMetaData field, EmisMetaEnum enumType)
	{ 
		this(field); 
		this.enumType = enumType; 
	}
	
	public EmisMetaData getField() 
	{ return field; }
	
	public void setField(EmisMetaData field) 
	{ this.field = field; }
	
	public EmisMetaEnum getEnumType() 
	{ return enumType; }
	
	public void setEnumType(EmisMetaEnum enumType) 
	{ this.enumType = enumType; } 
	
	public boolean matches(EmisMetaData field)
	{ return matches(field, null); }
	
	public boolean matches(EmisMetaData field, EmisMetaEnum enumType)
	{
		boolean sameEnum = false; 
		if (enumType == null && this.enumType == null)
			sameEnum = true; 
		else if (enumType != null && this.enumType != null && NamedUtil.sameName(enumType, this.enumType))
			sameEnum = true; 
		
		if (!sameEnum)
			return false; 
		
		return NamedUtil.sameName(field,  this.field) && NamedUtil.sameName(field.getEntity(), this.field.getEntity());
	}
}
