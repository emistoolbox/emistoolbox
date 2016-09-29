package com.emistoolbox.common;

import java.io.Serializable;

public class ChartFont implements Serializable
{
    private static final long serialVersionUID = 1L;
    
	public static final String FONT_TIMES = "Times"; 
	public static final String FONT_HELVETICA = "Helvetica"; 
	public static final String FONT_COURIER= "Courier"; 

    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;

    public static String[] FONTS = new String[] { 
		FONT_TIMES, FONT_HELVETICA, FONT_COURIER
	}; 
	
    private String name;
    private int size;
    private int style;
    private ChartColor color = new ChartColor(0, 0, 0); 

    public ChartFont() 
    {}

    public ChartFont(String name, int style, int size) 
    {
        this.name = name;
        this.size = size;
        this.style = style;
    }

    public String getName()
    { return this.name; }

    public void setName(String name)
    { this.name = name; }

    public int getSize()
    { return this.size; }

    public void setSize(int size)
    { this.size = size; }

    public int getStyle()
    { return this.style; }

    public void setStyle(int style)
    { this.style = style; }

	public ChartColor getColor() 
	{ return color; }

	public void setColor(ChartColor color) 
	{ this.color = color; }
}
