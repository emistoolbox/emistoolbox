package com.emistoolbox.common.renderer.pdfreport.impl;

import java.util.HashMap;
import java.util.Map;

import com.emistoolbox.common.TableCellFormat;
import com.emistoolbox.common.TableCellFormat.HorizontalAlign;
import com.emistoolbox.common.TableCellFormat.VerticalAlign;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;

public class TableStyleImpl implements EmisTableStyle
{
	private TableCellFormat topHeaderFormat; 
	private TableCellFormat leftHeaderFormat; 
	private TableCellFormat dataCellFormat; 
	
	private HorizontalAlign hAlignHeaders = HorizontalAlign.CENTER; 
	private HorizontalAlign hAlignDefault = HorizontalAlign.LEFT; 
	private Map<Integer, HorizontalAlign> hAligns = new HashMap<Integer, HorizontalAlign>(); 
	private VerticalAlign vAlign = VerticalAlign.TOP; 
	
	private Map<BorderType, BorderStyle> borders = new HashMap<BorderType, BorderStyle>(); 
	
	public TableCellFormat getTopHeaderFormat() 
	{ return getHeaderWithAlign(topHeaderFormat, hAlignHeaders); } 

	public TableCellFormat getLeftHeaderFormat() 
	{ return getHeaderWithAlign(leftHeaderFormat, hAlignHeaders); }
	
	private TableCellFormat getHeaderWithAlign(TableCellFormat format, HorizontalAlign hAlign)
	{
		if (format == null || format.getHorizontalAlign() == null || format.getVerticalAlign() == null)
		{
			format = format == null ? new TableCellFormat() : format.clone(); 
			if (format.getHorizontalAlign() == null)
			{
				if (hAlign != null)
					format.setHorizontalAlign(hAlign);
				else
					format.setHorizontalAlign(hAlignDefault);
			}
			
			if (format.getVerticalAlign() == null)
				format.setVerticalAlign(vAlign);
		}
		
		return format; 
	}

	public TableCellFormat getDataCellFormat(int column) 
	{ return getHeaderWithAlign(dataCellFormat, hAligns.get(column)); } 

	protected void setTopHeaderFormat(TableCellFormat format)
	{ this.topHeaderFormat = format; } 
	
	protected void setLeftHeaderFormat(TableCellFormat format)
	{ this.leftHeaderFormat = format; }
	
	protected void setDataCellFormat(TableCellFormat format)
	{ this.dataCellFormat = format; } 

	protected void setDefaultVerticalAlignment(VerticalAlign vAlign)
	{ this.vAlign = vAlign; } 
	
	protected void setDefaultHorizontalAlignment(HorizontalAlign hAlign)
	{ this.hAlignDefault = hAlign; } 

	protected void setHorizontalAlignment(int col, HorizontalAlign hAlign)
	{ this.hAligns.put(col, hAlign); }

	@Override
	public BorderStyle getBorder(BorderType type) 
	{ return borders.get(type); } 
	
	public void setBorder(BorderType type, BorderStyle border) 
	{ borders.put(type,  border); }
}
