package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartStroke;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.results.ReportMetaResult;

import java.io.Serializable;

public class ReportMetaResultImpl extends MetaResultImpl implements ReportMetaResult, Serializable
{
    private static final long serialVersionUID = 1L;
    private PdfReportConfig reportConfig;
    private int[] entityIds;
    private String[] entityNames;
    private ChartColor[] colourScheme = ChartConfig.PALLET_VARIED; 
    private ChartStroke[] colourStrokes = null; 

    public PdfReportConfig getReportConfig()
    { return this.reportConfig; }

    public void setReportConfig(PdfReportConfig reportConfig)
    { this.reportConfig = reportConfig; }

    public int[] getEntityPathIds()
    { return this.entityIds; }

    public String[] getEntityPathNames()
    { return this.entityNames; }

    public void setEntityPath(int[] ids, String[] names)
    {
        this.entityIds = ids;
        this.entityNames = names;
    }

    public ChartColor[] getColourScheme()
    { return colourScheme; }

    public void setColourScheme(ChartColor[] colors)
    { this.colourScheme = colors; }

	public ChartStroke[] getStrokes() 
	{ return colourStrokes; } 

	@Override
	public void setStrokes(ChartStroke[] strokes) 
	{ this.colourStrokes = strokes; } 
}

