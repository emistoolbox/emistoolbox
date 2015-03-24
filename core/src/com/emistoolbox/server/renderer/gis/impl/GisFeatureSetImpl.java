package com.emistoolbox.server.renderer.gis.impl;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.server.renderer.gis.GisFeatureSet;

import java.util.ArrayList;
import java.util.List;

public class GisFeatureSetImpl implements GisFeatureSet
{
    private EmisMetaEntity entityType; 
    private List<double[]> features = new ArrayList<double[]>();
    private List<Integer> ids = new ArrayList<Integer>();
    private List<Double> values = new ArrayList<Double>();
    private List<String> titles = new ArrayList<String>();
    private EmisIndicator indicator;
    
    public GisFeatureSetImpl(EmisMetaEntity entityType)
    { this.entityType = entityType; } 
    
    public GisFeatureSetImpl(EmisMetaEntity entityType, EmisIndicator indicator)
    {
        this.entityType = entityType; 
        this.indicator = indicator;
    }
    
    @Override
    public EmisIndicator getIndicator()
    { return indicator; }

    public EmisMetaEntity getEntityType()
    { return entityType; } 
    
    public void add(int id, double[] coords, double value, String title)
    {
        if (coords == null)
            return;

        ids.add(id); 
        features.add(coords);
        values.add(Double.valueOf(value));
        titles.add(title);
    }

    public double[] getBoundary()
    { return getBoundary(null); }

    public double[] getBoundary(double[] boundary)
    {
        for (double[] feature : this.features)
            boundary = getBoundary(feature, boundary); 

        if (boundary == null || boundary[0] == (1.0D / 0.0D))
            return null;

        return boundary;
    }
    
    public static double[] getBoundary(double[] feature, double[] boundary)
    {
        if (boundary== null)
            boundary = new double[] { (1.0D / 0.0D), (1.0D / 0.0D), (-1.0D / 0.0D), (-1.0D / 0.0D) };

        for (int i = 0; i < feature.length - 1; i += 2)
        {
            if (Double.isNaN(feature[i]))
            {
                i--;
            }
            else
            {
                if (Double.isNaN(feature[(i + 1)]))
                    throw new IllegalArgumentException();

                boundary[0] = Math.min(boundary[0], feature[i]);
                boundary[1] = Math.min(boundary[1], feature[(i + 1)]);
                boundary[2] = Math.max(boundary[2], feature[i]);
                boundary[3] = Math.max(boundary[3], feature[(i + 1)]);
            }
        }

        return boundary;
    }
    
    public int getId(int index)
    { return ids.get(index); }
    
    public static void showBoundary(double[] boundary)
    {
        String delim = "[";
        for (int i = 0; i < boundary.length; i++)
        {
            System.out.print(delim);
            System.out.print(boundary[i]);
            delim = ", ";
        }
        System.out.println("]");
    }

    public int getCount()
    {
        return this.features.size();
    }

    public double[] getFeature(int index)
    {
        return (double[]) this.features.get(index);
    }

    public double getValue(int index)
    {
        return ((Double) this.values.get(index)).doubleValue();
    }

    public String getTitle(int index)
    {
        return (String) this.titles.get(index);
    }
}

