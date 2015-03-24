package com.emistoolbox.common.renderer.pdfreport.impl;

import com.emistoolbox.common.results.TableMetaResult;
import java.io.Serializable;

public class PdfChartContentConfigImpl extends PdfMetaResultContentConfigImpl<TableMetaResult> implements Serializable
{
    private int chartType;

    public int getChartType()
    {
        return this.chartType;
    }

    public void setChartType(int chartType)
    {
        this.chartType = chartType;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl
 * JD-Core Version: 0.6.0
 */