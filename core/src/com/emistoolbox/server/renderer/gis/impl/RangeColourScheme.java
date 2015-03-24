package com.emistoolbox.server.renderer.gis.impl;

import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.gis.ColourLegend;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

public class RangeColourScheme implements ColourScheme
{
    public static final Color[] BLUES = { new Color(187, 210, 253), new Color(128, 170, 253), new Color(99, 151, 253), new Color(47, 116, 253), new Color(7, 90, 253), new Color(0, 79, 241), new Color(0, 72, 219), new Color(0, 66, 199) };

    public static final Color[] RED_YELLOW_GREEN = { new Color(255, 0, 0), new Color(255, 70, 0), new Color(255, 145, 0), new Color(255, 218, 0), new Color(217, 255, 0), new Color(143, 255, 0), new Color(70, 255, 0), new Color(0, 255, 0) };

    private double min = (0.0D / 0.0D);
    private double max = (0.0D / 0.0D);
    private double step = (0.0D / 0.0D);

    private Color[] colours = null;
    private Color lineColour = null;
    private Color fillColour = null;
    private Stroke stroke = null;
    private String valueFormat; 
    
    public RangeColourScheme(Color[] colours, Color lineColour, Color fillColour, Stroke stroke, String valueFormat) {
        this.colours = colours;
        this.lineColour = lineColour;
        this.fillColour = fillColour;
        this.stroke = stroke;
        this.valueFormat = valueFormat; 
    }

    public double getMin()
    { return min; } 
    
    public double getMax()
    { return max; } 
    
    public double getStep()
    { return step; } 
    
    public Color[] getColours()
    { return colours; } 
    
    public Color getFillColour(double value)
    {
        if (this.fillColour != null)
        {
            return this.fillColour;
        }
        return getColour(value);
    }

    public Color getLineColour(double value)
    {
        if (this.lineColour != null)
        {
            return this.lineColour;
        }
        return getColour(value);
    }

    public Stroke getLineStroke(double value)
    {
        return this.stroke;
    }

    private Color getColour(double value)
    {
        if ((this.colours == null) || (Double.isNaN(this.step)))
        {
            return Color.BLACK;
        }
        if (value <= this.min)
            return this.colours[0];
        if (value >= this.max)
        {
            return this.colours[(this.colours.length - 1)];
        }
        double index = (value - this.min) / (this.max - this.min) * (this.colours.length - 1);
        return mix(this.colours[(int) Math.floor(index)], this.colours[(int) Math.ceil(index)], index - Math.floor(index));
    }

    public static Color mix(Color floorColour, Color ceilColour, double distance)
    {
        int red = mix(floorColour.getRed(), ceilColour.getRed(), distance);
        int green = mix(floorColour.getGreen(), ceilColour.getGreen(), distance);
        int blue = mix(floorColour.getBlue(), ceilColour.getBlue(), distance);

        return new Color(red, green, blue);
    }

    public static int mix(int floorByte, int ceilByte, double distance)
    {
        double tmp = ceilByte - floorByte;
        tmp *= distance;
        tmp += floorByte;

        return (int) tmp;
    }

    public void setRange(double min, double max)
    {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        this.step = ((max - min) / (this.colours.length - 1));
    }

    public List<ColourLegend> getColourLegend()
    {
        if (this.colours == null)
        {
            return null;
        }
        List<ColourLegend> result = new ArrayList<ColourLegend>();

        double value = this.min;
        for (Color colour : this.colours)
        {
            result.add(new ColourLegend(colour, ServerUtil.getFormattedValue(valueFormat, value)));
            value += this.step;
        }

        return result;
    }
}
