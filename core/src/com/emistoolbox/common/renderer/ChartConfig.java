package com.emistoolbox.common.renderer;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.ChartStroke;

public abstract interface ChartConfig
{
    public static final ChartColor[] PALLET_YELLOW = { new ChartColor(255, 185, 25), new ChartColor(255, 227, 25), new ChartColor(228, 203, 33), new ChartColor(247, 238, 33), new ChartColor(247, 183, 52), new ChartColor(247, 202, 110) };

    public static final ChartColor[] PALLET_GRAYS = { new ChartColor(117, 124, 130), new ChartColor(220, 222, 224), new ChartColor(152, 157, 162), new ChartColor(187, 190, 193), new ChartColor(99, 105, 111), new ChartColor(88, 93, 98),
            new ChartColor(59, 62, 65) };

//    public static final ChartColor[] PALLET_SHADES = { new ChartColor("pattern1"), new ChartColor("patternX"), new ChartColor("pattern2"), new ChartColor("pattern7"), new ChartColor("pattern8"), new ChartColor("pattern15"),
//        new ChartColor("pattern16") };
    public static final ChartColor[] PALLET_SHADES = { new ChartColor("shadeSolid"), new ChartColor("shadeDiagonal"), new ChartColor("shadeHorizontal"), new ChartColor("shadeDiagonal2"), new ChartColor("shadeVertical") };

    public static final ChartColor[] PALLET_VARIED = {
        new ChartColor(240, 180, 0), new ChartColor(30, 108, 11), new ChartColor(0, 72, 140), new ChartColor(51, 38, 0),
        new ChartColor(216, 64, 0), new ChartColor(179, 0, 35), new ChartColor(163, 204, 75), new ChartColor(196, 0, 196),
        new ChartColor(150, 81, 54), new ChartColor(77, 166, 25)
    };
    
    public static final ChartStroke[] STROKE_DASHED = { 
    	new ChartStroke(new float[] { 6.0f, 6.0f }), 
    	new ChartStroke(new float[] { 3.0f, 3.0f }), 
    	new ChartStroke(new float[] { 3.0f, 6.0f }),
    	new ChartStroke(new float[] { 6.0f, 4.0f })
    }; 
    
    public abstract int getChartWidth();

    public abstract int getChartHeight();

    public abstract void setChartSize(int paramInt1, int paramInt2);

    public abstract boolean getShowValues();

    public abstract void setShowValues(boolean paramBoolean);

    public abstract boolean getShowTotals();

    public abstract void setShowTotals(boolean paramBoolean);

    public abstract double getMaxValue();

    public abstract void setMaxValue(double paramDouble);

    public abstract String getNoDataText();

    public abstract void setNoDataText(String paramString);

    public abstract double getGoodThreshold();

    public abstract String getGoodThresholdText();

    public abstract double getBadThreshold();

    public abstract String getBadThresholdText();

    public abstract boolean isGoodThresholdHigherGood();

    public abstract void setThreshold(double paramDouble, String paramString, boolean paramBoolean);

    public abstract void setThreshold(double paramDouble1, String paramString1, double paramDouble2, String paramString2);

    public abstract String getNumberFormat();

    public abstract void setNumberFormat(String paramString);

    public abstract String getAxisFormat();

    public abstract void setAxisFormat(String paramString);
    
    public abstract String getYAxisLabel(); 
    
    public abstract void setYAxisLabel(String text); 

    public abstract ChartColor getTextColour();

    public abstract void setTextColour(ChartColor paramChartColor);

    public abstract ChartColor getFillColour();

    public abstract void setFillColour(ChartColor paramChartColor);

    public abstract ChartColor getBackgroundColour();

    public abstract void setBackgroundColour(ChartColor paramChartColor);

    public abstract ChartColor[] getSeriesColours();

    public abstract void setSeriesColours(ChartColor[] paramArrayOfChartColor);
    
    public ChartStroke[] getSeriesStrokes(); 

    public void setSeriesStrokes(ChartStroke[] strokes); 

    public abstract ChartColor getGoodColour();

    public abstract ChartColor getNormalColour();

    public abstract ChartColor getBadColour();

    public abstract void setGoodBadColours(ChartColor paramChartColor1, ChartColor paramChartColor2, ChartColor paramChartColor3);
    
    public abstract void setLabelFont(ChartFont font); 
    
    public abstract ChartFont getLabelFont(); 
}
