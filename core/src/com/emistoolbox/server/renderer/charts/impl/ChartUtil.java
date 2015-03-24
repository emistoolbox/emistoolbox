package com.emistoolbox.server.renderer.charts.impl;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.ChartStroke;
import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.Result;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

public class ChartUtil
{
    public static Paint getJFreePaint(Paint colour)
    {
        if (colour instanceof Color)
            return new org.jfree.chart.ChartColor(((Color) colour).getRed(), ((Color) colour).getGreen(), ((Color) colour).getBlue());
        else
            return colour;
    }
    
    public static BasicStroke getStroke(ChartStroke stroke)
    { return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, stroke.getPattern(), 0.0f); }

    public static Paint getColor(ChartColor color)
    {
        if (color.getTextureId() != null)
            return getTexture(color.getTextureId());
        else
            return new Color(color.getRGB());
    }

    public static ChartColor getColor(Color color)
    {
        return new ChartColor(color.getRGB());
    }

    private static Map<String, TexturePaint> textures = new HashMap<String, TexturePaint>();

    public static TexturePaint getTexture(String id)
    {
        if (textures.get(id) != null)
            return textures.get(id);

        InputStream is = null;
        BufferedImage img = null;
        try
        {
            is = ChartUtil.class.getResourceAsStream(id + ".png");
            img = ImageIO.read(is);
        }
        catch (IOException ex)
        {
            return null;
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        TexturePaint tp = new TexturePaint(img, new Rectangle(0, 0, img.getWidth(), img.getHeight()));
        textures.put(id, tp);
        return tp;
    }

    public static void setMetaResultValueConfiguration(MetaResultValue value, ChartConfig config)
    {
        EmisIndicator indicator = value.getIndicator();
        if (indicator == null)
        {
            return;
        }
        if ((!Double.isNaN(indicator.getBadThreshold())) && (!Double.isNaN(indicator.getGoodThreshold())))
            config.setThreshold(indicator.getGoodThreshold(), indicator.getGoodThresholdText(), indicator.getBadThreshold(), indicator.getBadThresholdText());
        else if (!Double.isNaN(indicator.getGoodThreshold()))
        {
            config.setThreshold(indicator.getGoodThreshold(), indicator.getGoodThresholdText(), indicator.getBiggerIsBetter());
        }
        if (!Double.isNaN(indicator.getMaxValue()))
        {
            config.setMaxValue(indicator.getMaxValue());
        }
        config.setAxisFormat(value.getFormat());
    }

    public static void scaleChartWidth(ChartConfig config, Result result, int dimensions)
    {
        dimensions = Math.min(result.getDimensions(), dimensions);
        int totalSize = 1;
        for (int i = 0; i < dimensions; i++)
        {
            totalSize *= result.getDimensionSize(i);
        }
        if (totalSize > 10)
            config.setChartSize(config.getChartWidth() * totalSize / 25, config.getChartHeight());
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.renderer.charts.impl.ChartUtil JD-Core
 * Version: 0.6.0
 */