package com.emistoolbox.common.util.impl;

import com.emistoolbox.common.util.Named;
import java.io.Serializable;

public class NamedImpl implements Named, Serializable
{
    private static final long serialVersionUID = 1L;
    private String name;

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.util.impl.NamedImpl JD-Core Version:
 * 0.6.0
 */