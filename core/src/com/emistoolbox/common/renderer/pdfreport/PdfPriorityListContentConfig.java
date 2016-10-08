package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.results.PriorityMetaResult;

public interface PdfPriorityListContentConfig extends PdfMetaResultContentConfig<PriorityMetaResult>, TableStyleConfig 
{
	public boolean getFilterEmpty(); 
	public void setFilterEmpty(boolean filter); 
	
	public Integer getMaxRowCount(); 
	public void setMaxRowCount(Integer rows); 
}
