package com.emistoolbox.common.results.impl;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.util.impl.NamedImpl;

public class MetaResultDimensionEntityFilter extends NamedImpl implements MetaResultDimension
{
    private static final long serialVersionUID = 1L;
    private EmisMetaData field;

    public EmisMetaData getField()
    {
        return this.field;
    }

    public void setField(EmisMetaData field)
    {
        this.field = field;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name:
 * com.emistoolbox.common.results.impl.MetaResultDimensionEntityFilter JD-Core
 * Version: 0.6.0
 */