package com.emistoolbox.common;

import java.io.Serializable;

public class ChartColor implements Serializable
{
    private static final long serialVersionUID = 2L;

    private int value = 0;
    private int a = 0xFF;

    private String textureId = null;
    
    public ChartColor() 
    {} 
    
    public ChartColor(String texture) 
    { this.textureId = texture; }

    public ChartColor(int rgb) 
    { this.value = rgb; }

    public ChartColor(int r, int g, int b, int a) 
    {
    	this(r, g, b); 
    	this.a = a;  
    }
    
    public ChartColor(int r, int g, int b) 
    { this.value = ((r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) << 0); }

    public int getRGB()
    { return this.value; }

    public int getRed()
    { return this.value >> 16 & 0xFF; }

    public int getGreen()
    { return this.value >> 8 & 0xFF; }

    public int getBlue()
    { return this.value >> 0 & 0xFF; }

    public int getAlpha()
    { return a; } 
    
    public void setAlpha(int a)
    { this.a = a; }
    
    public String getTextureId()
    { return textureId; }

    public void setTextureId(String texture)
    { this.textureId = texture; }

	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null || !(obj instanceof ChartColor))
			return false; 
		
		ChartColor col = (ChartColor) obj; 
		return a == col.a && value == col.value;  
	}
   
    
}
