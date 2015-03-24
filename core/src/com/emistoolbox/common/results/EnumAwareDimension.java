package com.emistoolbox.common.results;

import com.emistoolbox.common.model.meta.EmisMetaEnum;

public abstract interface EnumAwareDimension
{
    public abstract EmisMetaEnum getEnumType();

    public abstract void setEnumType(EmisMetaEnum paramEmisMetaEnum);
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.results.EnumAwareDimension JD-Core
 * Version: 0.6.0
 */