package com.emistoolbox.server.renderer.gis;

import java.awt.Color;
import java.awt.Stroke;
import java.util.List;

public abstract interface ColourScheme
{
    public abstract void setRange(double paramDouble1, double paramDouble2);

    public abstract Color getLineColour(double paramDouble);

    public abstract Stroke getLineStroke(double paramDouble);

    public abstract Color getFillColour(double paramDouble);

    public abstract List<ColourLegend> getColourLegend();
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.gis.ColourScheme JD-Core
 * Version: 0.6.0
 */