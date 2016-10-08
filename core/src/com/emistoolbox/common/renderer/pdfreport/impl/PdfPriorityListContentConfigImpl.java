package com.emistoolbox.common.renderer.pdfreport.impl;

import java.io.Serializable;

import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.results.PriorityMetaResult;

public class PdfPriorityListContentConfigImpl extends PdfMetaResultContentConfigImpl<PriorityMetaResult> implements PdfPriorityListContentConfig, Serializable
{
	private EmisTableStyle tableStyle; 
	
	private boolean filterEmpty; 
	private Integer maxRowCount; 

	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }

	public EmisTableStyle getTableStyle()
	{ return tableStyle; } 
	
	public void setTableStyle(EmisTableStyle tableStyle) 
	{ this.tableStyle = tableStyle; }

	@Override
	public boolean getFilterEmpty() 
	{ return filterEmpty; }

	@Override
	public void setFilterEmpty(boolean filter) 
	{ this.filterEmpty = filter; } 

	@Override
	public Integer getMaxRowCount() 
	{ return maxRowCount; } 

	@Override
	public void setMaxRowCount(Integer rows) 
	{ this.maxRowCount = rows; } 
}
