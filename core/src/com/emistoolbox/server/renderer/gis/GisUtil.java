package com.emistoolbox.server.renderer.gis;

import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.gis.impl.ClassificationColourScheme;
import com.emistoolbox.server.renderer.gis.impl.ConstColourScheme;
import com.emistoolbox.server.renderer.gis.impl.GisImageRendererImpl;
import com.emistoolbox.server.renderer.gis.impl.GisShapeFileRendererImpl;
import com.emistoolbox.server.renderer.gis.impl.RangeColourScheme;
import com.emistoolbox.server.renderer.pdfreport.PdfGisContent;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGenerator;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGeneratorImpl;
import com.emistoolbox.server.renderer.pdfreport.impl.PdfGisContentImpl;
import com.emistoolbox.server.results.GisResultCollector;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GisUtil
{
    private static ResultToTableGenerator tableGenerator = new ResultToTableGeneratorImpl();

    public static PdfGisContent getGisContent(GisMetaResult metaResult, EmisDataSet dataSet, String mapType)
    	throws IOException
    {
    	PdfGisContent result = new PdfGisContentImpl(); 
    	    	
        List<GisFeatureSet> features = getGisResults(metaResult, dataSet);
        result.setFeatures(features);
        
        ChartConfig chartConfig = new ChartConfigImpl();

        ChartUtil.setMetaResultValueConfiguration(metaResult.getMetaResultValue(0), chartConfig);

        List<ColourScheme> colours = new ArrayList<ColourScheme>();
        ColourScheme legendColourScheme = null;

        for (int i = 0; i < features.size(); i++)
        {
            if (i == features.size() - 1)
            {
                String valueFormat = metaResult.getMetaResultValue(0).getFormat();
                ColourScheme colourScheme = null;
                if (Double.isNaN(metaResult.getIndicator().getGoodThreshold()))
                {
                    colourScheme = new RangeColourScheme(RangeColourScheme.BLUES, Color.BLACK, null, new BasicStroke(1.0F), valueFormat);
                    setRange((RangeColourScheme) colourScheme, features.get(i)); 
                }
                else
                    colourScheme = new ClassificationColourScheme(metaResult.getIndicator(), valueFormat, Color.BLACK, null, new BasicStroke(1.0F));

                colours.add(colourScheme);
                legendColourScheme = colourScheme;
            }
            else
                colours.add(new ConstColourScheme(new Color(0, 0, 0), null, new BasicStroke(features.size() - i)));
        }
    	
    	result.setFormat(metaResult.getMetaResultValues().get(0).getFormat());
    	result.setMapType(mapType);
    	result.setColourSchemes(colours);
        result.setLegendColourScheme(legendColourScheme);
        result.setChartConfig(chartConfig); 
    	result.setGisContext(dataSet.getMetaDataSet().getGisContext()); 
        
    	PdfGisContentConfig config = new PdfGisContentConfigImpl(); 
    	config.setMetaResult(metaResult);
    	result.setConfig(config);
    	
        return result; 
    }
    
    public static String[] getGisResult(GisMetaResult metaResult, EmisDataSet dataSet, String mapType) 
        throws IOException
    {
    	PdfGisContent content = getGisContent(metaResult, dataSet, mapType); 
    	return renderGisResult(content); 
    }
    
    public static String[] renderGisResult(PdfGisContent content)
    	throws IOException
    {
        File outputFile = ServerUtil.getNewFile("charts", "gis", ".png");
        List<GisFeatureSet> features = content.getFeatures(); 

        GisRenderer gisRenderer = getGisRenderer(content.getMapType(), content.getGisContext()); 
        synchronized(gisRenderer) { 
            gisRenderer.setValueFormat(content.getFormat());
            gisRenderer.renderMap(features, content.getColourSchemes(), content.getChartConfig(), outputFile);
            
            List<String> result = new ArrayList<String>(); 
            result.addAll(gisRenderer.getFileNames());
            if (gisRenderer instanceof GisImageRendererImpl)
                result.add(((GisImageRendererImpl) gisRenderer).getHtmlMap());
            
            result.add(gisRenderer.getRawColourLegend(content.getLegendColourScheme())); 
            result.add(gisRenderer.getColourLegend(content.getLegendColourScheme())); 
            try { result.add(tableGenerator.getHtmlTableAsString(features.get(features.size() - 1), content.getFormat())); }
            catch (Exception ex)
            { throw new IOException("Failed to get table with values for map.", ex); }

            return result.toArray(new String[] {});
        }
    }
    
    private static void setRange(RangeColourScheme scheme, GisFeatureSet feature)
    {
        double min = Double.MAX_VALUE; 
        double max = Double.MIN_VALUE; 
        for (int i = 0; i < feature.getCount(); i++) 
        {
            min = Math.min(min, feature.getValue(i));
            max = Math.max(max,  feature.getValue(i)); 
        }
        
        scheme.setRange(min,  max); 
    }

    private static GisRenderer getGisRenderer(String format, GisContext gisContext)
    {
        if ("interactive".equals(format))
            return new GisShapeFileRendererImpl(); 
        
        return new GisImageRendererImpl(); 
    }

    public static List<GisFeatureSet> getGisResults(GisMetaResult gisMetaResult, EmisDataSet dataSet) throws IOException
    {
        GisResultCollector collector = new GisResultCollector(dataSet, gisMetaResult);
        return collector.getResults();
    }
}
