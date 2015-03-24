package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import com.emistoolbox.common.util.impl.NamedImpl;
import java.io.Serializable;

public class MetaData extends NamedImpl implements EmisMetaData, Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean allowEmptyValues;
    private EmisMetaEntity entity;
    private EmisMetaDateEnum dateType;
    private EmisMetaData.EmisDataType type;
    private EmisMetaEnum enumType;
    private EmisMetaEnumTuple dimensions;
    private String defaultValue;
    private String emptyValue;
    private boolean planningResource;

    public boolean allowEmptyValue()
    {
        return this.allowEmptyValues;
    }

    public EmisMetaEnumTuple getArrayDimensions()
    {
        return this.dimensions;
    }

    public EmisMetaDateEnum getDateType()
    {
        return this.dateType;
    }

    public String getDefaultValue()
    {
        return this.defaultValue;
    }

    public String getEmptyValue()
    {
        return this.emptyValue;
    }

    public EmisMetaEntity getEntity()
    {
        return this.entity;
    }

    public EmisMetaEnum getEnumType()
    {
        return this.enumType;
    }

    public EmisMetaData.EmisDataType getType()
    {
        return this.type;
    }

    public void setAllowEmptyValue(boolean allow)
    {
        this.allowEmptyValues = allow;
    }

    public void setArrayDimentsions(EmisMetaEnumTuple dimensions)
    {
        this.dimensions = dimensions;
    }

    public void setDateType(EmisMetaDateEnum dateType)
    {
        this.dateType = dateType;
    }

    public void setDefaultValue(String value)
    {
        this.defaultValue = value;
    }

    public void setEmptyValue(String emptyValue)
    {
        this.emptyValue = emptyValue;
    }

    public void setEntity(EmisMetaEntity entity)
    {
        this.entity = entity;
    }

    public void setEnumType(EmisMetaEnum enumType)
    {
        this.enumType = enumType;
    }

    public void setType(EmisMetaData.EmisDataType type)
    {
        this.type = type;
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof EmisMetaData))
            return false;

        EmisMetaData field2 = (EmisMetaData) obj;
        if ((this.entity == null) || (this.entity.getName() == null) || (field2.getEntity() == null) || (!this.entity.getName().equals(field2.getEntity().getName())))
            return false;

        return (getName() != null) && (getName().equals(field2.getName()));
    }

    public int hashCode()
    {
        int result = 0;
        if ((this.entity != null) && (this.entity.getName() != null))
        {
            result = this.entity.getName().hashCode();
        }
        if (getName() != null)
        {
            result ^= getName().hashCode();
        }
        return result;
    }

    public boolean isPlanningResource()
    {
        return this.planningResource;
    }

    public void setPlanningResource(boolean planningResource)
    {
        this.planningResource = planningResource;
    }
}
