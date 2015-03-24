package com.emistoolbox.common.results;

public abstract interface Result
{
    public abstract int getDimensions();

    public abstract int getDimensionSize(int paramInt);

    public abstract String getValueLabel();

    public abstract String getHeading(int paramInt1, int paramInt2);

    public abstract String[] getHeadings(int paramInt);

    public abstract String[][] getHeadings();

    public abstract void setValueLabel(String paramString);

    public abstract void setHeading(int paramInt1, int paramInt2, String paramString);

    public abstract double get(int[] paramArrayOfInt);

    public abstract double get(String[] paramArrayOfString);
    
    public abstract void set(int[] paramArrayOfInt, double paramDouble);

    public abstract void set(String[] paramArrayOfString, double paramDouble);

    public abstract String getFormat(int paramInt);

    public abstract void setDefaultFormat(String paramString);

    public abstract void setFormat(int paramInt, String paramString);
}
