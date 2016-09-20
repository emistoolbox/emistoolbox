package com.emistoolbox.server.renderer.pdfreport;

import info.joriki.graphics.Rectangle;

public interface PdfContent
{
	public void setPosition(Rectangle position); 
	
	public Rectangle getPosition(); 

    public void setTitle(String paramString);

    public void setSpanRows(int paramInt);

    public void setSpanCols(int paramInt);

    public int getSpanRows();

    public int getSpanCols();

    public String getTitle();
}
