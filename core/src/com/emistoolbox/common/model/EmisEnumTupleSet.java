package com.emistoolbox.common.model;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import java.util.Set;

public abstract interface EmisEnumTupleSet
{
    public abstract EmisMetaEnumTuple getEnumTuple();

    public abstract void setEnumTuple(EmisMetaEnumTuple paramEmisMetaEnumTuple);

    public abstract void addValue(EmisMetaEnum paramEmisMetaEnum, String paramString);

    public abstract void removeValue(EmisMetaEnum paramEmisMetaEnum, String paramString);

    public abstract void addValue(EmisMetaEnum paramEmisMetaEnum, int paramInt);

    public abstract void removeValue(EmisMetaEnum paramEmisMetaEnum, int paramInt);

    public abstract Set<String[]> getAll();

    public abstract void setAll(Set<String[]> paramSet);

    public abstract Set<Integer[]> getAllIndexes();

    public abstract void setAllIndexes(Set<Integer[]> paramSet);

    public abstract int size();
}
