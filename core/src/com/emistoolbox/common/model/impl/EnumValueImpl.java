package com.emistoolbox.common.model.impl;

import com.emistoolbox.common.model.EmisEnumValue;
import com.emistoolbox.common.model.meta.EmisMetaEnum;

public class EnumValueImpl implements EmisEnumValue
{
    private EmisMetaEnum enumType;
    private byte index;

    public EmisMetaEnum getEnum()
    {
        return this.enumType;
    }

    public byte getIndex()
    {
        return this.index;
    }

    public String getValue()
    {
        return this.enumType.getValue(this.index);
    }

    public void setEnum(EmisMetaEnum newEnum)
    {
        this.enumType = newEnum;
    }

    public void setIndex(byte index)
    {
        this.index = index;
    }

    public void setValue(String value)
    {
        this.index = this.enumType.getIndex(value);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.impl.EnumValueImpl JD-Core
 * Version: 0.6.0
 */