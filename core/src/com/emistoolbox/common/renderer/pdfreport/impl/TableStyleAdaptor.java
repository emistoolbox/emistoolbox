package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.TableCellFormat;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;

public class TableStyleAdaptor implements EmisTableStyle 
{
	private EmisTableStyle style; 
	
	public TableStyleAdaptor(EmisTableStyle style)
	{ this.style = style; } 
	
	@Override
	public TableCellFormat getTopHeaderFormat() 
	{ return style.getTopHeaderFormat(); } 

	@Override
	public TableCellFormat getLeftHeaderFormat() 
	{ return style.getLeftHeaderFormat(); } 

	@Override
	public TableCellFormat getDataCellFormat(int col) 
	{ return style.getDataCellFormat(col); } 

	@Override
	public BorderStyle getBorder(BorderType type) 
	{ return style.getBorder(type); } 
}
