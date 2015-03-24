package com.emistoolbox.server.renderer.pdfreport.itext;

import com.emistoolbox.common.renderer.ChartConfig;
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

public class ItextPdfChartContent extends AbstractPdfContent implements PdfChartContent
{
    private ChartConfig config;
    private Result result;
    private final ChartType type;

    public ItextPdfChartContent(int chartType) {
        this(ChartType.values()[chartType]);
    }

    public ItextPdfChartContent(ChartType type) {
        this.type = type;
    }

    public void setChartConfig(ChartConfig config)
    {
        this.config = config;
    }

    public void setResult(Result result)
    {
        this.result = result;
    }

    private void drawChart(Graphics2D g2, Rectangle2D r2d)
    {
        JFreeChart chart = null;
        switch (type) {
        case BAR:
            chart = new BarChartRenderer().render(this.config, this.result);
            break;
        case PIE:
            chart = new PieChartRenderer().render(this.config, this.result);
            break;
        case STACKED:
            chart = new StackedBarChartRenderer().render(this.config, this.result);
            break;
        case STACKED_SCALED: 
        {    
            if (result.getDimensions() == 1)
                chart = new BarChartRenderer().render(config, result);
            else
                chart = new StackedBarChartRenderer().render(config, result);
            break; 
        }
        case LINE: 
            chart = new LineChartRenderer().render(config, result); 
            break; 
        default:
            throw new UnsupportedOperationException("Type not supported:" + this.type);
        }
        chart.draw(g2, r2d);
    }

    public Image getChart(PdfWriter writer, FontMapper mapper, int width, int height) throws BadElementException
    {
        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(width, height);
        Graphics2D g2 = tp.createGraphics(width, height, mapper);
        Rectangle2D r2D = new Rectangle2D.Double(0.0D, 0.0D, width, height);
        drawChart(g2, r2D);
        g2.dispose();
        return new ImgTemplate(tp);
    }

    public String toString()
    {
        return String.format("%s - %s", new Object[] { getClass().getSimpleName(), this.type });
    }

    public static enum ChartType {
        BAR, STACKED, PIE, STACKED_SCALED, LINE;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.renderer.pdfreport.itext.ItextPdfChartContent JD-Core
 * Version: 0.6.0
 */