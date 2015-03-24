package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstEnum;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.EnumValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.results.impl.MetaResultDimensionEnum;
import com.emistoolbox.server.results.ResultDimension;

public class ResultDimensionEnum implements ResultDimension
{
    private EmisMetaEnum enumType;

    public ResultDimensionEnum(MetaResultDimensionEnum meta) {
        this.enumType = meta.getEnumType();
        if (this.enumType == null)
            throw new IllegalArgumentException("MetaResultDimensionEnum - enumType is null.");
    }

    public EmisContext getContext(int index)
    {
        EmisEnumValue enumValue = new EnumValueImpl();
        enumValue.setEnum(this.enumType);
        enumValue.setIndex((byte) index);

        return new ContextConstEnum(enumValue);
    }

    public int getItemCount()
    {
        return this.enumType.getSize();
    }

    public String getItemName(int index)
    {
        return this.enumType.getValue((byte) index);
    }

    public void updateContext(int index, EmisContext context)
    {
        EmisEnumSet values = new EnumSetImpl();
        values.setEnum(this.enumType);
        values.addValue((byte) index);

        context.addEnumFilter(values);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.server.results.impl.ResultDimensionEnum
 * JD-Core Version: 0.6.0
 */