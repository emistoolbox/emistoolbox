package com.emistoolbox.common;

import java.io.Serializable;

public class ChartFont implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    private String name;
    private int size;
    private int style;

    public ChartFont() {
    }

    public ChartFont(String name, int style, int size) {
        this.name = name;
        this.size = size;
        this.style = style;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getSize()
    {
        return this.size;
    }

    public void setSize(int size)
    {
        this.size = size;
    }

    public int getStyle()
    {
        return this.style;
    }

    public void setStyle(int style)
    {
        this.style = style;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.ChartFont JD-Core Version: 0.6.0
 */