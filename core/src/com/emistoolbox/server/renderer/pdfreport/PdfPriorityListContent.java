package com.emistoolbox.server.renderer.pdfreport;

import java.util.List;

import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;

public interface PdfPriorityListContent extends PdfContent<PdfPriorityListContentConfig> 
{
	public List<PriorityListItem> getResults();
	public void setResults(List<PriorityListItem> result); 
}
