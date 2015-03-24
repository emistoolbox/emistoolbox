package com.emistoolbox.common.renderer;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.ChartStroke;

public class ChartConfigImpl implements ChartConfig
{
    public static final ChartFont LABEL_FONT = new ChartFont("SansSerif", 1, 11);

    private String axisFormat = "#,##0";
    private ChartColor backgroundColour = new ChartColor(255, 255, 255);
    private ChartColor badColour = new ChartColor(0, 0, 0);
    private double badThreshold = (0.0D / 0.0D);
    private String badThresholdText;
    private int height = 450;
    private int width = 600;
    private ChartColor fillColour = new ChartColor(204, 204, 204);
    private ChartColor goodColour = new ChartColor(0, 0, 0);
    private double goodThreshold = (0.0D / 0.0D);
    private String goodThresholdText;
    private double maxValue = (0.0D / 0.0D);
    private String noDataText = "n/a";
    private ChartColor normalColour = new ChartColor(0, 0, 0);
    private String numberFormat = "#,##0";
    private ChartColor[] seriesColours = PALLET_VARIED;
    private boolean showTotals = true;
    private boolean showValues = true;
    private ChartColor textColour = new ChartColor(0, 0, 0);
    private boolean higherIsGood;
    private ChartFont labelFont = LABEL_FONT; 

    private ChartStroke[] chartStrokes;
    
    public String getAxisFormat()
    {
        return this.axisFormat;
    }

    public ChartColor getBackgroundColour()
    {
        return this.backgroundColour;
    }

    public ChartColor getBadColour()
    {
        return this.badColour;
    }

    public double getBadThreshold()
    {
        return this.badThreshold;
    }

    public String getBadThresholdText()
    {
        return this.badThresholdText;
    }

    public int getChartHeight()
    {
        return this.height;
    }

    public int getChartWidth()
    {
        return this.width;
    }

    public ChartColor getFillColour()
    {
        return this.fillColour;
    }

    public ChartColor getGoodColour()
    {
        return this.goodColour;
    }

    public double getGoodThreshold()
    {
        return this.goodThreshold;
    }

    public String getGoodThresholdText()
    {
        return this.goodThresholdText;
    }

    public double getMaxValue()
    {
        return this.maxValue;
    }

    public String getNoDataText()
    {
        return this.noDataText;
    }

    public ChartColor getNormalColour()
    {
        return this.normalColour;
    }

    public String getNumberFormat()
    {
        return this.numberFormat;
    }

    public ChartColor[] getSeriesColours()
    {
        return this.seriesColours;
    }

    public boolean getShowTotals()
    {
        return this.showTotals;
    }

    public boolean getShowValues()
    {
        return this.showValues;
    }

    public ChartColor getTextColour()
    {
        return this.textColour;
    }

    public boolean isGoodThresholdHigherGood()
    {
        return this.higherIsGood;
    }

    public void setAxisFormat(String format)
    {
        this.axisFormat = format;
    }

    public void setBackgroundColour(ChartColor background)
    {
        this.backgroundColour = background;
    }

    public void setChartSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void setFillColour(ChartColor colour)
    {
        this.fillColour = colour;
    }

    public void setGoodBadColours(ChartColor good, ChartColor normal, ChartColor bad)
    {
        this.goodColour = good;
        this.badColour = bad;
        this.normalColour = normal;
    }

    public void setMaxValue(double maxValue)
    {
        this.maxValue = maxValue;
    }

    public void setNoDataText(String text)
    {
        this.noDataText = text;
    }

    public void setNumberFormat(String format)
    {
        this.numberFormat = format;
    }

    public void setSeriesColours(ChartColor[] colours)
    {
        this.seriesColours = colours;
    }

	public ChartStroke[] getSeriesStrokes() 
	{ return chartStrokes; }

	public void setSeriesStrokes(ChartStroke[] strokes) 
	{ this.chartStrokes = strokes; }

	public void setShowTotals(boolean showTotals)
    {
        this.showTotals = showTotals;
    }

    public void setShowValues(boolean showValues)
    {
        this.showValues = showValues;
    }

    public void setTextColour(ChartColor colour)
    {
        this.textColour = colour;
    }

    public void setThreshold(double thresholdValue, String thresholdText, boolean higherIsGood)
    {
        this.goodThreshold = thresholdValue;
        this.goodThresholdText = thresholdText;
        this.higherIsGood = higherIsGood;
    }

    public void setThreshold(double threshold, String text, double badValue, String badText)
    {
        this.goodThreshold = threshold;
        this.goodThresholdText = text;
        this.badThreshold = badValue;
        this.badThresholdText = badText;
    }

    public ChartFont getLabelFont()
    { return labelFont; }

    public void setLabelFont(ChartFont labelFont)
    { this.labelFont = labelFont; }
}
