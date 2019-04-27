package com.emistoolbox.server.renderer.gis.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;

import com.emistoolbox.common.renderer.ChartConfig;
import com.emistoolbox.server.ServerUtil;
import com.emistoolbox.server.renderer.gis.ColourScheme;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

public class GisImageRendererImpl extends GisRendererImpl
{
    private BufferedImage bufferedImage; 
    private AffineTransform transformation; 
    private Graphics2D graphics; 
    private StringBuffer htmlMap; 
    private Map<Point, String> labels = new HashMap<Point, String>();
    private String filename = null; 
    
    public List<String> getFileNames()
    { return Arrays.asList(new String[] { filename }); }

    public List<String> getContentTypes()
    { return Arrays.asList(new String[] { "image/png" }); }

    synchronized public void renderMap(List<GisFeatureSet> results, List<ColourScheme> colours, ChartConfig config, File outputFile) throws IOException
    {
        filename = outputFile.getName(); 
        double[] boundary = getBoundary(results); 
        
        bufferedImage = new BufferedImage(config.getChartWidth(), config.getChartHeight(), 7);
        graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setFont(new Font("Sanserif", Font.PLAIN, 10));
        transformation = getTransformation(boundary[0], boundary[1], boundary[2], boundary[3], config.getChartWidth() - 5, config.getChartHeight() - 5);
        htmlMap = new StringBuffer(); 

        renderAll(results, colours); 

        for (Map.Entry<Point, String> entry : labels.entrySet())
            renderLabel(entry.getValue(), entry.getKey().getX(), entry.getKey().getY());

        // Finish request. 
        ImageIO.write(bufferedImage, "PNG", outputFile);
        
        bufferedImage = null; 
        graphics = null; 
        transformation =  null; 
        labels.clear(); 
    }

    public String getHtmlMap()
    { return htmlMap == null ? null : htmlMap.toString(); }
    
    protected void renderFeatureSet(GisFeatureSet feature, ColourScheme colourScheme, boolean setRange, boolean showLabels, boolean selected)
    {
        if (setRange)
        {
            double max = (-1.0D / 0.0D);
            double min = (1.0D / 0.0D);
            for (int f = 0; f < feature.getCount(); f++)
            {
                double value = feature.getValue(f);
                if (Double.isNaN(value))
                    continue;
                max = Math.max(value, max);
                min = Math.min(value, min);
            }

            colourScheme.setRange(min, max);
        }

        for (int f = 0; f < feature.getCount(); f++)
        {
        	System.out.println(feature.getTitle(f) + " " + feature.getBoundary(feature.getFeature(f))); 
            draw(feature.getFeature(f), colourScheme, feature.getValue(f), feature.getTitle(f), showLabels, selected ? htmlMap : null);
        }
    }
    
    protected void renderLabel(String text, double x, double y)
    {
        graphics.setColor(Color.BLACK);
        graphics.drawString(text, (int) x, (int) y); 
    }

    private void draw(double[] feature, ColourScheme colours, double value, String title, boolean showLabels, StringBuffer mapBuffer)
    {
        graphics.setStroke(colours.getLineStroke(value));
        Color fillColor = colours.getFillColour(value);
        Color lineColor = colours.getLineColour(value);

        Point2D labelPt = null;
        if (feature.length == 2)
        {
            Point2D pt = transformation.transform(new Point2D.Double(feature[0], feature[1]), new Point2D.Double());
            graphics.setBackground(fillColor);
            graphics.setColor(fillColor);
            graphics.fillRect((int) pt.getX() - 1, (int) pt.getY() - 1, 3, 3);

            if (mapBuffer != null)
                addMapBuffer(mapBuffer, value, title, pt);

            if (showLabels)
                labelPt = new Point((int) pt.getX(), (int) pt.getY());
        }
        else
        {
            labelPt = null;
            double bestLabelWeight = 0.0D;

            int index = 0;

            while (index < feature.length - 1)
            {
                Point2D.Double resultPoint = new Point2D.Double();
                double resultFactor = 0.0D;

                GeneralPath path = new GeneralPath();
                int startIndex = index;
                path.moveTo(feature[index], feature[(index + 1)]);
                index += 2;

                while ((index < feature.length - 1) && (!Double.isNaN(feature[index])))
                {
                    path.lineTo(feature[index], feature[(index + 1)]);

                    if (showLabels)
                    {
                        double factor = getFactor(feature, index - 2, index);
                        resultFactor += factor;
                        resultPoint = addPoint(resultPoint, factorPoint(getPoint(feature, index - 2, index), factor));
                    }

                    index += 2;
                }

                if (index <= startIndex + 4)
                {
                    index++;
                    continue;
                }

                path.closePath();
                transformAndDraw(graphics, path, transformation, lineColor, fillColor);
                if (mapBuffer != null)
                    addMapBuffer(mapBuffer, value, title, path, transformation);

                if (showLabels)
                {
                    double factor = getFactor(feature, index - 2, startIndex);
                    resultFactor += factor;
                    resultPoint = addPoint(resultPoint, factorPoint(getPoint(feature, index - 2, startIndex), factor));
                }

                if (Math.abs(resultFactor) > bestLabelWeight)
                {
                    bestLabelWeight = Math.abs(resultFactor);
                    labelPt = factorPoint(resultPoint, 1.0D / (resultFactor * 3.0D));
                }

                index++;
            }

            if (labelPt != null)
                labelPt = transformation.transform(labelPt, null);
        }
        if (showLabels && (labelPt != null))
        {
            graphics.setColor(Color.BLACK);

            title = getText(title, value, false); // (no more values in image) mapBuffer != null);

            FontMetrics fm = graphics.getFontMetrics(graphics.getFont());
            Rectangle2D rect = fm.getStringBounds(title, graphics);

            labels.put(new Point((int) (labelPt.getX() - rect.getWidth() / 2.0D), (int) (labelPt.getY() + rect.getHeight() / 2.0D)), title);
        }
    }

    private AffineTransform getTransformation(double xmin, double ymin, double xmax, double ymax, int width, int height)
    {
        double scale = Math.min(width / (xmax - xmin), height / (ymax - ymin));

        AffineTransform result = new AffineTransform();
        result.translate(0.0D, height);
        result.scale(1.0D, -1.0D);
        result.translate((width - (xmax - xmin) * scale) / 2.0D, (height - (ymax - ymin) * scale) / 2.0D);
        result.scale(scale, scale);
        result.translate(-xmin, -ymin);

        return result;
    }

    private void addMapBuffer(StringBuffer mapBuffer, double value, String text, Point2D pt)
    {
        addMapBuffer(mapBuffer, "rect", value, text, (int) (pt.getX() - 1.0D) + "," + (int) (pt.getY() - 1.0D) + "," + (int) (pt.getX() + 2.0D) + "," + (int) (pt.getY() + 2.0D));
    }

    private void addMapBuffer(StringBuffer mapBuffer, double value, String text, GeneralPath path, AffineTransform transformation)
    {
        double lastX = 0.0D;
        double lastY = 0.0D;

        StringBuffer coords = new StringBuffer();

        for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next())
        {
            double[] pathCoords = new double[6];
            Point2D pt = null;
            switch (iterator.currentSegment(pathCoords)) {
            case 1:
                pt = getPoint(pathCoords, transformation);
                if ((Math.abs(lastX - pt.getX()) < 3.0D) && (Math.abs(lastY - pt.getY()) < 3.0D))
                {
                    continue;
                }
            case 0:
                if (pt == null)
                {
                    pt = getPoint(pathCoords, transformation);
                }
                lastX = pt.getX();
                lastY = pt.getY();
                if (coords.length() > 0)
                {
                    coords.append(",");
                }
                coords.append((int) pt.getX() + "," + (int) pt.getY());
            }

        }

        addMapBuffer(mapBuffer, "poly", value, text, coords.toString());
    }

    private Point2D getPoint(double[] coords, AffineTransform transformation)
    {
        return transformation.transform(new Point2D.Double(coords[0], coords[1]), null);
    }

    private void addMapBuffer(StringBuffer mapBuffer, String shape, double value, String text, String coords)
    {
        String title = getText(text, value, true);
        mapBuffer.append("<area shape='" + shape + "' onClick='return false;' nohref='' alt='" + title + "' title='" + title + "' coords='" + coords + "'>\n");
    }

    private String getText(String text, double value, boolean useValue)
    {
        String tmp = null;
        if (useValue)
        {
            tmp = ServerUtil.getFormattedValue(getValueFormat(), value);
            if (StringUtils.isEmpty(text))
                return tmp;
            
            if (StringUtils.isEmpty(tmp))
                return text;

            return text + " (" + tmp + ")";
        }

        return text;
    }

    private double getFactor(double[] values, int index1, int index2)
    {
        return values[index1] * values[(index2 + 1)] - values[index2] * values[(index1 + 1)];
    }

    private Point2D.Double getPoint(double[] values, int index1, int index2)
    {
        return new Point2D.Double(values[index1] + values[index2], values[(index1 + 1)] + values[(index2 + 1)]);
    }

    private Point2D.Double addPoint(Point2D.Double pt1, Point2D.Double pt2)
    {
        return new Point2D.Double(pt1.x + pt2.x, pt1.y + pt2.y);
    }

    private Point2D.Double factorPoint(Point2D.Double pt, double factor)
    {
        return new Point2D.Double(pt.x * factor, pt.y * factor);
    }

    private Shape transformAndDraw(Graphics2D g, GeneralPath path, AffineTransform t, Color lineColor, Color fillColor)
    {
        Shape s = path.createTransformedShape(t);
        Color old = g.getColor();
        if (fillColor != null)
        {
            g.setColor(fillColor);
            g.fill(s);
        }

        g.setColor(lineColor);
        g.draw(s);
        g.setColor(old);

        return s;
    }
}
