package com.emistoolbox.common.model.priolist;

import java.io.Serializable;

public abstract interface PriorityListItem extends Serializable
{
    public abstract int getId();

    public abstract void setId(int paramInt);

    public abstract String getName();

    public abstract void setName(String paramString);

    public abstract double[] getValues();

    public abstract void setValues(double[] paramArrayOfDouble);
    
    public abstract String[] getEntityValues();
    public abstract void setEntityValues(String[] values); 
    
    public boolean isEmpty(); 
}
