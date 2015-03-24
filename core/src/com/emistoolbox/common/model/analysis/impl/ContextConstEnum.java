package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumValue;
import com.emistoolbox.common.model.analysis.ContextBase;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ContextConstEnum extends ContextBase implements EmisContext, Serializable
{
    private static final long serialVersionUID = 1L;
    private Map<String, EmisEnumSet> enums = new HashMap();

    public ContextConstEnum() {
    }

    public ContextConstEnum(EmisEnumValue enumValue) {
        EmisEnumSet enumSet = new EnumSetImpl();
        enumSet.setEnum(enumValue.getEnum());
        enumSet.addValue(enumValue.getIndex());
        this.enums.put(enumValue.getEnum().getName(), enumSet);
    }

    public ContextConstEnum(EmisEnumSet enumSet) {
        this.enums.put(enumSet.getEnum().getName(), enumSet);
    }

    public EmisEnumSet getEnumFilter(String name)
    {
        return (EmisEnumSet) this.enums.get(name);
    }

    public Map<String, EmisEnumSet> getEnumFilters()
    {
        return this.enums;
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.analysis.impl.ContextConstEnum
 * JD-Core Version: 0.6.0
 */