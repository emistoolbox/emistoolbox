package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.server.renderer.charts.impl.BarChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.LineChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.PieChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.StackedBarChartRenderer;
import com.emistoolbox.server.renderer.pdfreport.PdfChartContent;
import com.emistoolbox.server.renderer.pdfreport.impl.AbstractPdfContent;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.ImgTemplate;
import com.itextpdf.text.pdf.FontMapper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.JFreeChart;

public class PdfChartContentImpl extends AbstractPdfContent implements PdfChartContent
{
    private ChartConfig config;
    private Result result;
    private final ChartType type;

    public PdfChartContentImpl(int chartType) 
    { this(ChartType.values()[chartType]); }

    public PdfChartContentImpl(ChartType type) 
    { this.type = type; }

    @Override
	public void setChartConfig(ChartConfig config)
    { this.config = config; }

    @Override
	public void setResult(Result result)
    { this.result = result; }

    @Override
	public ChartConfig getChartConfig() 
    { return config; }

	@Override
	public Result getResult() 
	{ return result; }

	@Override
	public ChartType getType() 
	{ return type; }

	public String toString()
    { return String.format("%s - %s", new Object[] { getClass().getSimpleName(), this.type }); }
}
