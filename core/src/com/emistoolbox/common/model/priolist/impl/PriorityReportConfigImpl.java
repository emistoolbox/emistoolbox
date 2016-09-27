package com.emistoolbox.common.model.priolist.impl;

import java.io.Serializable;

import com.emistoolbox.common.model.priolist.PriorityReportConfig;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.util.impl.NamedImpl;

public class PriorityReportConfigImpl extends NamedImpl implements PriorityReportConfig, Serializable
{
	private PriorityMetaResult metaResult; 
	
	@Override
	public PriorityMetaResult getMetaResult() 
	{ return metaResult; } 

	@Override
	public void setMetaResult(PriorityMetaResult metaResult) 
	{ this.metaResult = metaResult; } 
}
