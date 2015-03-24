package com.emistoolbox.server.renderer.gis.impl;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.gis.ColourLegend;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

public class ClassificationColourScheme implements ColourScheme
{
    private double[] thresholds;
    private Color[] colours;
    private Color fillColour;
    private Color lineColour;
    private Stroke stroke;
    private String valueFormat; 
    
    public ClassificationColourScheme(EmisIndicator indicator, String valueFormat, Color lineColour, Color fillColour, Stroke stroke) 
    {
        this.valueFormat = valueFormat; 
        this.lineColour = lineColour;
        this.fillColour = fillColour;
        this.stroke = stroke;

        if (Double.isNaN(indicator.getGoodThreshold()))
        {
            this.thresholds = new double[0];
            this.colours = new Color[] { Color.BLACK };
        }
        else if (Double.isNaN(indicator.getBadThreshold()))
        {
            this.thresholds = new double[] { indicator.getGoodThreshold() };
            if (indicator.getBiggerIsBetter())
                this.colours = new Color[] { Color.RED, Color.GREEN };
            else
            {
                this.colours = new Color[] { Color.GREEN, Color.RED };
            }

        }
        else if (indicator.getGoodThreshold() < indicator.getBadThreshold())
        {
            this.thresholds = new double[] { indicator.getGoodThreshold(), indicator.getBadThreshold() };
            this.colours = new Color[] { Color.GREEN, Color.YELLOW, Color.RED };
        }
        else
        {
            this.thresholds = new double[] { indicator.getBadThreshold(), indicator.getGoodThreshold() };
            this.colours = new Color[] { Color.RED, Color.YELLOW, Color.GREEN };
        }
    }

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
            return this.lineColour;
        return getColour(value);
    }

    public Stroke getLineStroke(double value)
    {
        return this.stroke;
    }

    private Color getColour(double value)
    {
        for (int i = 0; i < this.thresholds.length; i++)
        {
            if (value < this.thresholds[i])
                return this.colours[i];
        }
        return this.colours[this.thresholds.length];
    }

    public void setRange(double min, double max)
    {
    }

    public List<ColourLegend> getColourLegend()
    {
        if (this.colours.length == 1)
        {
            return null;
        }
        List<ColourLegend> result = new ArrayList<ColourLegend>();
        for (int i = 0; i < this.colours.length; i++)
        {
            if (i == 0)
                result.add(new ColourLegend(this.colours[i], "< " + ServerUtil.getFormattedValue(valueFormat, thresholds[i])));
            else if (i == this.colours.length - 1)
                result.add(new ColourLegend(this.colours[i], "> " + ServerUtil.getFormattedValue(valueFormat, thresholds[(i - 1)])));
            else
                result.add(new ColourLegend(this.colours[i], ServerUtil.getFormattedValue(valueFormat, this.thresholds[(i - 1)]) + " - " + ServerUtil.getFormattedValue(valueFormat, this.thresholds[i])));
        }
        return result;
    }
    
    public Color[] getColours()
    { return colours; } 
    
    public double[] getThresholds()
    { return thresholds; } 
}
