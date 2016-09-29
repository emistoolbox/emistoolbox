package com.emistoolbox.server.renderer.pdfreport.impl;

import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;
import com.emistoolbox.server.renderer.pdfreport.PdfContentVisitor;

import info.joriki.graphics.Rectangle;

public class PdfNullContent implements PdfContent<PdfContentConfig>
{
	public void setPosition(Rectangle position) 
    {}

	public Rectangle getPosition() 
	{ return null; }

	private static PdfNullContent instance = new PdfNullContent();

    public void setTitle(String title)
    {
        throw new UnsupportedOperationException();
    }

    public void setSpanCols(int cols)
    {
        throw new UnsupportedOperationException();
    }

    public void setSpanRows(int rows)
    {
        throw new UnsupportedOperationException();
    }

    public int getSpanCols()
    { return 0; }

    public int getSpanRows()
    { return 0; }

    public String toString()
    { return "PdfNullContent"; }

    public String getTitle()
    { return "PdfNullContent"; }

    public static PdfContent<PdfContentConfig> getInstance()
    { return instance; }

	@Override
	public PdfContentConfig getConfig() 
	{ return null; }

	@Override
	public void setConfig(PdfContentConfig config) 
	{}

	@Override
	public <T> T accept(PdfContentVisitor<T> visitor) 
	{ return null; }
}
