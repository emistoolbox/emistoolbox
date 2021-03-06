package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;

import info.joriki.graphics.Rectangle;

public abstract class PdfContentBase<T extends PdfContentConfig> implements PdfContent<T>
{
	private T config; 
	
	private Rectangle position; 
	
    private int colSpan = 1;
    private int rowSpan = 1;
    private String title;

	public T getConfig() 
	{ return config; }

	public void setConfig(T config) 
	{ this.config = config; } 

	public void setPosition(Rectangle position) 
	{ this.position = position; }

	public Rectangle getPosition() 
	{ return position; }

	public int getSpanCols()
    { return this.colSpan; }

    public int getSpanRows()
    { return this.rowSpan; }

    public void setSpanCols(int cols)
    { this.colSpan = cols; }

    public void setSpanRows(int rows)
    { this.rowSpan = rows; }

    public void setTitle(String title)
    { this.title = title; }

    public String toString()
    { return String.format("%s (cols:%d,rows:$d)", new Object[] { this.title, Integer.valueOf(this.colSpan), Integer.valueOf(this.rowSpan) }); }

    public String getTitle()
    { return this.title; }
}
