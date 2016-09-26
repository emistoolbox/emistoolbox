package com.emistoolbox.server.renderer.charts.impl.highcharts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.Result;
import com.emistoolbox.lib.highchart.renderer.HighchartRenderer;
import com.emistoolbox.lib.highchart.renderer.HighchartRenderingType;
import com.emistoolbox.server.renderer.charts.ChartRenderer;
import com.emistoolbox.server.renderer.charts.impl.BaseChartRenderer;
import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.CreditOptions;
import com.googlecode.wickedcharts.highcharts.options.DataLabels;
import com.googlecode.wickedcharts.highcharts.options.Events;
import com.googlecode.wickedcharts.highcharts.options.Function;
import com.googlecode.wickedcharts.highcharts.options.Labels;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.PlotLine;
import com.googlecode.wickedcharts.highcharts.options.PlotOptions;
import com.googlecode.wickedcharts.highcharts.options.PlotOptionsChoice;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Stacking;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.options.series.Series;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;

import es.jbauer.lib.io.IOInput;
import es.jbauer.lib.io.IOOutput;
import es.jbauer.lib.io.impl.IOFileOutput;

public class HighchartChartRenderer extends BaseChartRenderer implements ChartRenderer 
{
	private HighchartRenderer renderer; 
	
	public HighchartChartRenderer(HighchartRenderer renderer)
	{ this.renderer = renderer;  }
	
	@Override
	public boolean canCreateContentType(String contentType) 
	{ return contentType.equals("application/json") || contentType.equals("application/pdf") || contentType.equals("image/png"); }

	private void render(Options hcOptions, IOOutput out)
		throws IOException
	{
		String json = getJson(hcOptions); 

		HighchartRenderingType renderType = HighchartRenderingType.PNG; 
		if (out.getName().toLowerCase().endsWith(".json"))
		{
			OutputStream os = out.getOutputStream(); 
			PrintWriter pout = new PrintWriter(os); 
			pout.print(json);
			pout.flush();
			IOUtils.closeQuietly(pout);
			IOUtils.closeQuietly(os); 
			
			return; 
		}
		
		if (out.getName().toLowerCase().endsWith(".pdf"))
			renderType = HighchartRenderingType.PDF; 
		
		IOInput input = null; 
		try { input = renderer.render(json, renderType, 0, 600); }
		catch (InterruptedException ex)
		{ throw new IOException("Failed to render Highchart", ex); }
		
		InputStream is = null;  
		OutputStream os = null; 
		try {
			is = input.getInputStream();
			os = out.getOutputStream(); 
			IOUtils.copy(is, os);
			os.flush(); 
		}
		finally { 
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os); 
		}
	}
	
	private String getJson(Options opts)
	{
		JsonRenderer renderer = new JsonRenderer(); 
		return renderer.toJson(opts).replaceAll("'" + EMIS_NULL + "'", "null").replaceAll("\"" + EMIS_NULL + "\"", "null"); 
	}
	
	
	private PointSeries getPointSeriesData(Result data, int dimension, int[] indexes, String[] headers, String name, Color color)
	{
		PointSeries result = new PointSeries();
		result.setType(SeriesType.PIE); 

		for (int i = 0; i < data.getDimensionSize(dimension); i++)
		{
			indexes[dimension] = i; 
			double value = data.get(indexes); 
			result.addPoint(new Point(headers[i], value > 0.0D ? value : 0.0D)); 
		}
		
		if (color != null)
			result.setColor(color); 
		
		if (!StringUtils.isEmpty(name))
			result.setName(name); 
		
		return result; 
	}
	
	private Series<Number> getSeriesData(Result data, int dimension, int[] indexes, String name, Color color)
	{
		List<Number> values = new ArrayList<Number>(); 
		for (int i = 0; i < data.getDimensionSize(dimension); i++) 
		{
			indexes[dimension] = i; 
			double value = data.get(indexes); 
            values.add(value > 0.0D ? value : 0.0D);  
		}

		Series<Number> result = new SimpleSeries(); 
		result.setData(values);
		if (color != null)
			result.setColor(color);
		
		if (!StringUtils.isEmpty(name))
			result.setName(name); 
		else
			result.setShowInLegend(false); 
			
		return result;
	}
	
	private Options init(Result data, ChartConfig config, SeriesType chartType)
	{
		Options opts = new Options(); 
		opts.setCreditOptions(new CreditOptions().setEnabled(false)); 
		opts.setTitle(new Title().setText(nullify(null))); 
		opts.setSubtitle(new Title().setText(nullify(null))); 

		ChartOptions chartOpt = new ChartOptions(); 
		chartOpt.setType(chartType); 
		chartOpt.setWidth(config.getChartWidth()); 
		chartOpt.setHeight(config.getChartHeight()); 
		opts.setChart(chartOpt); 

		String[] headers = data.getHeadings(0); 
		Axis xAxis = new Axis(); 
		xAxis.setCategories(headers); 
		xAxis.setTitle(new Title().setText(nullify(null))); 
		opts.setxAxis(xAxis);
		
		opts.setPlotOptions(initPlotOptions(chartType));  
		
		Axis yAxis = new Axis(); 
		// plot lines
		List<PlotLine> plotlines = new ArrayList<PlotLine>(); 
		if (config.getGoodThresholdText() != null)
			plotlines.add(getPlotLine(config.getGoodColour(), config.getGoodThreshold(), config.getGoodThresholdText())); 
		if (config.getBadThresholdText() != null)
			plotlines.add(getPlotLine(config.getBadColour(), config.getBadThreshold(), config.getBadThresholdText())); 
		if (plotlines.size() > 0)
			yAxis.setPlotLines(plotlines);
		if (!Double.isNaN(config.getMaxValue()))
			yAxis.setMax(config.getMaxValue()); 
		yAxis.setTitle(new Title().setText(nullify(config.getYAxisLabel())));
//		yAxis.setLabels(new Labels().setFormat(config.getAxisFormat())); 
		opts.setyAxis(yAxis);
		
		List<Series<?>> seriesList = new ArrayList<Series<?>>(); 
		if (data.getDimensions() == 1)
		{
			if (chartType == SeriesType.PIE)
				seriesList.add(getPointSeriesData(data, 0, new int[1], headers, "default", getColor(config, 0))); 
			else
				seriesList.add(getSeriesData(data, 0, new int[1], "default", getColor(config, 0))); 
				
		}
		else if (data.getDimensions() == 2)
		{
			String[] seriesNames = data.getHeadings(1); 
			for (int i = 0; i < seriesNames.length; i++)
			{

				if (chartType == SeriesType.PIE)
					seriesList.add(getSeriesData(data, 0, new int[] {0, i}, seriesNames[i], getColor(config, i)));  
				else
					seriesList.add(getSeriesData(data, 0, new int[] {0, i}, seriesNames[i], getColor(config, i)));  
			}
		}
		
		opts.setSeries(seriesList); 
		
		return opts; 
	}
	
	private PlotOptionsChoice initPlotOptions(SeriesType chartType)
	{
		PlotOptionsChoice result = new PlotOptionsChoice(); 
		
		PlotOptions opts = new PlotOptions(); 
		switch (chartType)
		{
		case PIE: 
			result.setPie(opts); 
			break; 
			
		case COLUMN:
			result.setColumn(opts); 
			break;
			
		case LINE:
			result.setLine(opts); 
			break;
		}
		
		return result;
	}
	
	
	private PlotLine getPlotLine(ChartColor color, double threshold, String text)
	{
		PlotLine result = new PlotLine();
		result.setValue((float) threshold); 
		result.setColor(new Color(color.getRGB())); 
		result.setLabel(new Labels().setText(text));
		
		return result; 
	}
	
	private Color getColor(ChartConfig config, int index)
	{
		ChartColor[] colors = config.getSeriesColours(); 
		if (colors == null || colors.length == 0)
			return null; 
		
		ChartColor c = colors[index % colors.length]; 
		return new Color(c.getRGB());  
	}
	
	@Override
	public void renderBar(Result data, ChartConfig config, IOOutput out) 
		throws IOException 
	{
		Options opts = init(data, config, SeriesType.COLUMN); 
		render(opts, out);
	}

	@Override
	public void renderStackedBar(Result data, ChartConfig config, IOOutput out) 
		throws IOException 
	{
		Options opts = init(data, config, SeriesType.COLUMN); 
		
		PlotOptions colOpts = ensure(opts.getPlotOptions().getColumn());
		colOpts = colOpts.setStacking(Stacking.NORMAL); 
		opts.getPlotOptions().setColumn(colOpts);

		render(opts, out); 
	}
	
	private PlotOptions ensure(PlotOptions opt)
	{
		if (opt == null)
			opt = new PlotOptions(); 
		
		return opt; 
	}
	
	private Events getEvents()
	{ return new Events().setMouseOver(new Function().setFunction("console.log(event, this);")); }
	

	@Override
	public void renderPie(Result data, ChartConfig config, IOOutput out) 
		throws IOException 
	{
		Options opts = init(data, config, SeriesType.PIE); 
		render(opts, out); 
	}

	@Override
	public void renderLines(Result data, ChartConfig config, IOOutput out) 
		throws IOException 
	{
		Options opts = init(data, config, SeriesType.LINE); 
		render(opts, out); 
	}

	@Override
	public void renderNormalizedStackedBar(Result data, ChartConfig config, IOOutput out)
		throws IOException 
	{
		Options opts = init(data, config, SeriesType.COLUMN); 

		DataLabels dataLabels = new DataLabels(); 
		dataLabels.setEnabled(true); 

		PlotOptionsChoice plotOptions = new PlotOptionsChoice(); 
		plotOptions.setColumn(new PlotOptions().setStacking(Stacking.PERCENT).setDataLabels(dataLabels));
		opts.setPlotOptions(plotOptions); 

		render(opts, out); 
	}
	
	private static final String EMIS_NULL = "%emisnull%"; 
	private String nullify(String text)
	{
		return text == null ? "%emisnull%" : text; 
	}
}
