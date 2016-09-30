package com.emistoolbox.common.renderer.pdfreport;

import com.emistoolbox.common.TableCellFormat;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;

public interface EmisTableStyle 
{
	public enum BorderType { 
		TABLE_VERTICAL, TABLE_HORIZONTAL, 
		HEADER_TOP_VERTICAL, HEADER_TOP_HORIZONTAL, 
		HEADER_LEFT_VERTICAL, HEADER_LEFT_HORIZONTAL, 
		DATA_VERTICAL, DATA_HORIZONTAL
	}; 
	
	public TableCellFormat getTopHeaderFormat(); 
	public TableCellFormat getLeftHeaderFormat(); 
	public TableCellFormat getDataCellFormat(int col);
	
	public BorderStyle getBorder(BorderType type); 
}
