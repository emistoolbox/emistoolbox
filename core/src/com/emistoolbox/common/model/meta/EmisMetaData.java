package com.emistoolbox.common.model.meta;

import com.emistoolbox.common.util.Named;

public abstract interface EmisMetaData extends Named
{
    public abstract EmisMetaEntity getEntity();

    public abstract void setEntity(EmisMetaEntity paramEmisMetaEntity);

    public abstract EmisDataType getType();

    public abstract void setType(EmisDataType paramEmisDataType);

    public abstract EmisMetaDateEnum getDateType();

    public abstract void setDateType(EmisMetaDateEnum paramEmisMetaDateEnum);

    public abstract EmisMetaEnum getEnumType();

    public abstract void setEnumType(EmisMetaEnum paramEmisMetaEnum);

    public abstract EmisMetaEnumTuple getArrayDimensions();

    public abstract void setArrayDimentsions(EmisMetaEnumTuple paramEmisMetaEnumTuple);

    public abstract String getDefaultValue();

    public abstract void setDefaultValue(String paramString);

    public abstract boolean allowEmptyValue();

    public abstract void setAllowEmptyValue(boolean paramBoolean);

    public abstract String getEmptyValue();

    public abstract void setEmptyValue(String paramString);

    public abstract boolean isPlanningResource();

    public abstract void setPlanningResource(boolean paramBoolean);

    public static enum EmisDataType {
        BOOLEAN, BYTE, INTEGER, STRING, ENUM, ENUM_SET;
    }
}
