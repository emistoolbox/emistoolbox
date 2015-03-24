package com.emistoolbox.server.model.impl;

public abstract interface EntityDataAccess
{
    public abstract void init(int paramInt1, int paramInt2);

    public abstract int getOffset();

    public abstract int getSize();

    public abstract String getAsString(Object[] paramArrayOfObject);

    public abstract String getAsString(Object[] paramArrayOfObject, int paramInt);

    public abstract int getAsInt(Object[] paramArrayOfObject, int paramInt);
}
