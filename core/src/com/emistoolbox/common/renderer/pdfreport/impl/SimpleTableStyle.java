package com.emistoolbox.common.renderer.pdfreport.impl;

import java.io.Serializable;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.TableCellFormat;
import com.emistoolbox.common.renderer.pdfreport.EmisTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.util.LayoutSides;

public class SimpleTableStyle extends TableStyleImpl implements EmisTableStyle, Serializable
{
	private boolean topHeaders = true; 
	private boolean leftHeaders = true; 
	
	private ChartFont headerFont = ChartFont.DEFAULT_FONT.copy(); 
	private ChartFont dataFont = ChartFont.DEFAULT_FONT.copy();
	
	private BorderStyle tableBorder = BorderStyle.DEFAULT_BORDER.copy();
	private BorderStyle headerBorder = BorderStyle.DEFAULT_BORDER.copy();
	private BorderStyle dataBorder = BorderStyle.DEFAULT_BORDER.copy();

	private ChartColor headerBackground = ChartColor.GRAY.copy(); 
	private ChartColor dataBackground = ChartColor.WHITE.copy(); 
	
	private double padding = 2; 
	
	public void init()
	{
		setBorder(BorderType.TABLE_VERTICAL, tableBorder);
		setBorder(BorderType.TABLE_HORIZONTAL, tableBorder);
		setBorder(BorderType.HEADER_LEFT_HORIZONTAL, headerBorder);
		setBorder(BorderType.HEADER_LEFT_VERTICAL, dataBorder);
		setBorder(BorderType.HEADER_TOP_HORIZONTAL, headerBorder);
		setBorder(BorderType.HEADER_TOP_VERTICAL, dataBorder);
		setBorder(BorderType.DATA_HORIZONTAL, dataBorder); 
		setBorder(BorderType.DATA_VERTICAL, dataBorder); 
		
		setDataCellFormat(getFormat(dataFont, dataBackground));
		if (leftHeaders)
			setLeftHeaderFormat(getFormat(headerFont, headerBackground));
		
		if (topHeaders)
			setTopHeaderFormat(getFormat(headerFont, headerBackground)); 
	}
	
	public void setHeaders(boolean top, boolean left)
	{ 
		topHeaders = top; 
		leftHeaders = left; 
	}
	
	public boolean getTopHeaders()
	{ return topHeaders; } 
	
	public boolean getLeftHeaders()
	{ return leftHeaders; } 
	
	TableCellFormat getFormat(ChartFont font, ChartColor bgColor)
	{
		TableCellFormat format = new TableCellFormat(); 
		format.setFont(font);
		format.setBackgroundColor(bgColor);
		format.setPadding(new LayoutSides<Double>(padding));
		
		return format; 
	}

	public ChartFont getHeaderFont() {
		return headerFont;
	}

	public void setHeaderFont(ChartFont headerFont) {
		this.headerFont = headerFont;
	}

	public ChartFont getDataFont() {
		return dataFont;
	}

	public void setDataFont(ChartFont dataFont) {
		this.dataFont = dataFont;
	}

	public BorderStyle getTableBorder() {
		return tableBorder;
	}

	public void setTableBorder(BorderStyle tableBorder) {
		this.tableBorder = tableBorder;
	}

	public BorderStyle getHeaderBorder() {
		return headerBorder;
	}

	public void setHeaderBorder(BorderStyle headerBorder) {
		this.headerBorder = headerBorder;
	}

	public BorderStyle getDataBorder() {
		return dataBorder;
	}

	public void setDataBorder(BorderStyle dataBorder) {
		this.dataBorder = dataBorder;
	}

	public ChartColor getHeaderBackground() {
		return headerBackground;
	}

	public void setHeaderBackground(ChartColor headerBackground) {
		this.headerBackground = headerBackground;
	}

	public ChartColor getDataBackground() {
		return dataBackground;
	}

	public void setDataBackground(ChartColor dataBackground) {
		this.dataBackground = dataBackground;
	}

	public double getPadding() {
		return padding;
	}

	public void setPadding(double padding) {
		this.padding = padding;
	}
}
