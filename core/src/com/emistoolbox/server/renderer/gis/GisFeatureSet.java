package com.emistoolbox.server.renderer.gis;

import com.emistoolbox.common.model.analysis.EmisIndicator;
import com.emistoolbox.common.model.meta.EmisMetaEntity;

public abstract interface GisFeatureSet
{
    public static final int MIN_X = 0;
    public static final int MIN_Y = 1;
    public static final int MAX_X = 2;
    public static final int MAX_Y = 3;

    public EmisMetaEntity getEntityType(); 
    
    public abstract int getCount();

    public abstract double[] getFeature(int index);

    public abstract int getId(int index); 
    
    public abstract double getValue(int index);
    
    public abstract String getTitle(int index);

    public abstract double[] getBoundary();

    public abstract double[] getBoundary(double[] values);

    public abstract void add(int id, double[] coords, double value, String title);

    public abstract EmisIndicator getIndicator();
}
