package com.emistoolbox.server.renderer.gis.impl;

import com.emistoolbox.server.renderer.gis.ColourLegend;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import java.awt.Color;
import java.awt.Stroke;
import java.util.List;

public class ConstColourScheme implements ColourScheme
{
    private Color lineColour;
    private Color fillColour;
    private Stroke stroke;

    public ConstColourScheme(Color lineColour, Color fillColour, Stroke stroke) {
        this.lineColour = lineColour;
        this.fillColour = fillColour;
        this.stroke = stroke;
    }

    public Color getFillColour(double value)
    {
        return this.fillColour;
    }

    public Color getLineColour(double value)
    {
        return this.lineColour;
    }

    public Stroke getLineStroke(double value)
    {
        return this.stroke;
    }

    public void setRange(double min, double max)
    {
    }

    public List<ColourLegend> getColourLegend()
    {
        return null;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.gis.impl.ConstColourScheme
 * JD-Core Version: 0.6.0
 */