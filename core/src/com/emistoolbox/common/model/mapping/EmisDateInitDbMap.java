package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.meta.EmisMetaDateEnum;

public interface EmisDateInitDbMap extends EmisDbMapBase 
{
	public DbRowAccess getValueAccess(); 
	public void setValueAccess(DbRowAccess access); 
	
	public EmisMetaDateEnum getDateType(); 
	public void setDateType(EmisMetaDateEnum dateType); 
}
