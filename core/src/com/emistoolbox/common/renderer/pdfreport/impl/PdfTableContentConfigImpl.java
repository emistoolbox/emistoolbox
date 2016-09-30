package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfTableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;
import com.emistoolbox.common.results.TableMetaResult;
import java.io.Serializable;

public class PdfTableContentConfigImpl extends PdfMetaResultContentConfigImpl<TableMetaResult> implements PdfTableContentConfig, Serializable
{
	private EmisTableStyle tableStyle; 
	
	@Override
	public <T> T accept(PdfContentConfigVisitor<T> visitor) 
	{ return visitor.visit(this); }

	public EmisTableStyle getTableStyle()
	{ return tableStyle; } 
	
	public void setTableStyle(EmisTableStyle tableStyle) 
	{ this.tableStyle = tableStyle; }
}
