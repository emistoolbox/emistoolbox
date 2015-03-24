package com.emistoolbox.common;

import java.io.Serializable;

public class ChartColor implements Serializable
{
    private static final long serialVersionUID = 2L;

    private int value = 0;
    private String textureId = null;

    public ChartColor() 
    {} 
    
    public ChartColor(String texture) 
    { this.textureId = texture; }

    public ChartColor(int rgb) 
    { this.value = rgb; }

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

    public String getTextureId()
    { return textureId; }

    public void setTextureId(String texture)
    { this.textureId = texture; }
}
