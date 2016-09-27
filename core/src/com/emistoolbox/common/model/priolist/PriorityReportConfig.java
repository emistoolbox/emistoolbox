package com.emistoolbox.common.model.priolist;

import com.emistoolbox.common.results.PriorityMetaResult;
import com.emistoolbox.common.util.Named;

public interface PriorityReportConfig extends Named 
{	
	public PriorityMetaResult getMetaResult(); 
	public void setMetaResult(PriorityMetaResult metaResult); 
}
