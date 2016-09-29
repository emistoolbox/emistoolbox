package com.emistoolbox.server.renderer.pdfreport.impl;

import java.io.Serializable;
import java.util.List;

import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;
import com.emistoolbox.server.renderer.pdfreport.PdfPriorityListContent;

public class PdfPriorityListContentImpl extends PdfContentBase<PdfPriorityListContentConfig> implements PdfPriorityListContent, Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private List<PriorityListItem> result; 
	
	@Override
	public List<PriorityListItem> getResults() 
	{ return result; }

	@Override
	public void setResults(List<PriorityListItem> result) 
	{ this.result = result; }

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return visitor.visit(this); } 
}
