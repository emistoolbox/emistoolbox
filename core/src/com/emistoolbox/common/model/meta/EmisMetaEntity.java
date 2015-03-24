package com.emistoolbox.common.model.meta;

import java.util.Set;

import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;

public abstract interface EmisMetaEntity extends Named
{
    public abstract NamedIndexList<EmisMetaData> getData();

    public abstract void setData(NamedIndexList<EmisMetaData> paramNamedIndexList);

    public abstract EmisGisType getGisType();

    public abstract void setGisType(EmisGisType paramEmisGisType);

    public abstract boolean isChildOf(EmisMetaEntity paramEmisMetaEntity, EmisMetaHierarchy paramEmisMetaHierarchy);

    public abstract EmisMetaDateEnum getRequiredDateEnum();

    public abstract Set<EmisMetaDateEnum> getUsedDateTypes();

    public static enum EmisGisType {
        NONE, COORDINATE, POLYGON;
    }
}
