package com.emistoolbox.server.results.impl;

import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.ContextConstEntityFilter;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter;
import com.emistoolbox.server.results.ResultDimension;

public class ResultDimensionEntityFilter implements ResultDimension
{
    private EmisMetaData field;
    private boolean isBoolField = false;
    private String[] headers;

    public ResultDimensionEntityFilter(MetaResultDimensionEntityFilter meta) {
        this.field = meta.getField();

        this.isBoolField = (this.field.getType() == EmisMetaData.EmisDataType.BOOLEAN);
        if (this.isBoolField)
        {
            this.headers = new String[2];
            this.headers[0] = ("With " + this.field.getName());
            this.headers[1] = ("Without " + this.field.getName());
        }
        else
        {
            this.headers = this.field.getEnumType().getValues();
        }
    }

    public EmisContext getContext(int index)
    {
        if (this.isBoolField)
        {
            return new ContextConstEntityFilter(this.field, 1 - index);
        }
        return new ContextConstEntityFilter(this.field, (byte) index);
    }

    public int getItemCount()
    {
        return this.headers.length;
    }

    public String getItemName(int index)
    {
        return this.headers[index];
    }

    public void updateContext(int index, EmisContext context)
    {
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.server.results.impl.ResultDimensionEntityFilter JD-Core
 * Version: 0.6.0
 */