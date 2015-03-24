package com.emistoolbox.server.renderer.gis;

import com.emistoolbox.common.model.meta.GisContext;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.renderer.ChartConfigImpl;
import com.emistoolbox.common.results.GisMetaResult;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.model.EmisDataSet;
import com.emistoolbox.server.renderer.charts.impl.ChartUtil;
import com.emistoolbox.server.renderer.gis.impl.ClassificationColourScheme;
import com.emistoolbox.server.renderer.gis.impl.ConstColourScheme;
import com.emistoolbox.server.renderer.gis.impl.GisImageRendererImpl;
import com.emistoolbox.server.renderer.gis.impl.GisShapeFileRendererImpl;
import com.emistoolbox.server.renderer.gis.impl.RangeColourScheme;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGenerator;
import com.emistoolbox.server.renderer.pdfreport.html.ResultToTableGeneratorImpl;
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

    public static String[] getGisResult(GisMetaResult metaResult, EmisDataSet dataSet, String mapType) 
        throws IOException
    {
        List<GisFeatureSet> features = getGisResults(metaResult, dataSet);
        ChartConfig chartConfig = new ChartConfigImpl();

        File outputFile = ServerUtil.getNewFile("charts", "gis", ".png");
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
        
        GisRenderer gisRenderer = getGisRenderer(mapType, dataSet.getMetaDataSet().getGisContext()); 
        synchronized(gisRenderer) { 
            gisRenderer.setValueFormat(metaResult.getMetaResultValues().get(0).getFormat());
            gisRenderer.renderMap(features, colours, chartConfig, outputFile);
            
            List<String> result = new ArrayList<String>(); 
            result.addAll(gisRenderer.getFileNames());
            if (gisRenderer instanceof GisImageRendererImpl)
                result.add(((GisImageRendererImpl) gisRenderer).getHtmlMap());
            
            result.add(gisRenderer.getRawColourLegend(legendColourScheme)); 
            result.add(gisRenderer.getColourLegend(legendColourScheme)); 
            try { result.add(tableGenerator.getHtmlTableAsString(features.get(features.size() - 1), metaResult.getMetaResultValue(0).getFormat())); }
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
