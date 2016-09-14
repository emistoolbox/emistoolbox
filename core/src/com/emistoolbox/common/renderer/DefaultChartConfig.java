package com.emistoolbox.common.renderer;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.ChartStroke;

public class DefaultChartConfig implements ChartConfig
{
    public int getChartWidth()
    { return 600; }

    public int getChartHeight()
    { return 450; }

    public void setChartSize(int width, int height)
    {}

    public boolean getShowValues()
    { return true; }

    public void setShowValues(boolean showValues)
    {}

    public boolean getShowTotals()
    { return true; }

    public void setShowTotals(boolean showTotals)
    {}

    public double getMaxValue()
    { return 100.0D; }

    public void setMaxValue(double maxValue)
    {}

    public String getNoDataText()
    { return "n/a"; }

    public void setNoDataText(String text)
    {}

    public double getGoodThreshold()
    { return (0.0D / 0.0D); }

    public String getGoodThresholdText()
    { return null; }

    public double getBadThreshold()
    { return (0.0D / 0.0D); }

    public String getBadThresholdText()
    { return null; }

    public void setThreshold(double thresholdValue, String thresholdText, boolean higherIsGood)
    {}

    public void setThreshold(double threshold, String text, double badValue, String badText)
    {}

    public String getNumberFormat()
    { return "#,##0"; }

    public void setNumberFormat(String format)
    {}

    public String getAxisFormat()
    {
        return "#,##0";
    }

    public void setAxisFormat(String format)
    {
    }

    public ChartColor getTextColour()
    {
        return new ChartColor(0, 0, 0);
    }

    public void setTextColour(ChartColor colour)
    {
    }

    public ChartColor getFillColour()
    {
        return new ChartColor(204, 204, 204);
    }

    public void setFillColour(ChartColor colour)
    {
    }

    public ChartColor getBackgroundColour()
    {
        return new ChartColor(255, 255, 255);
    }

    public void setBackgroundColour(ChartColor background)
    {
    }

    public ChartColor[] getSeriesColours()
    {
        return new ChartColor[] { new ChartColor(255, 185, 25), new ChartColor(255, 227, 25), new ChartColor(228, 203, 33), new ChartColor(247, 238, 33), new ChartColor(247, 183, 52), new ChartColor(247, 202, 110) };
    }

    public void setSeriesColours(ChartColor[] colours)
    {}

	public ChartStroke[] getSeriesStrokes() 
	{ return null; }

	public void setSeriesStrokes(ChartStroke[] strokes) 
	{}

	public ChartColor getGoodColour()
    {
        return new ChartColor(0, 0, 0);
    }

    public ChartColor getNormalColour()
    {
        return new ChartColor(0, 0, 0);
    }

    public ChartColor getBadColour()
    {
        return new ChartColor(0, 0, 0);
    }

    public void setGoodBadColours(ChartColor good, ChartColor normal, ChartColor bad)
    {
    }

    public boolean isGoodThresholdHigherGood()
    {
        return false;
    }

    public String getTitle()
    {
        return null;
    }

    public void setTitle(String title)
    {
    }

    public void setLabelFont(ChartFont font)
    {}

    public ChartFont getLabelFont()
    { return ChartConfigImpl.LABEL_FONT; }

	@Override
	public String getYAxisLabel() 
	{ return null; } 

	@Override
	public void setYAxisLabel(String text) 
	{}     
}
