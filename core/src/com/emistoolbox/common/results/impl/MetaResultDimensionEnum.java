package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.results.EnumAwareDimension;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.util.impl.NamedImpl;

public class MetaResultDimensionEnum extends NamedImpl implements EnumAwareDimension, MetaResultDimension
{
    private static final long serialVersionUID = 1L;
    private EmisMetaEnum enumType;

    public EmisMetaEnum getEnumType()
    {
        return this.enumType;
    }

    public void setEnumType(EmisMetaEnum enumType)
    {
        this.enumType = enumType;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.results.impl.MetaResultDimensionEnum
 * JD-Core Version: 0.6.0
 */