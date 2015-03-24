package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.common.model.meta.EmisMetaEnum;
import java.util.Arrays;
import java.util.List;

public class MapUiEnumConstraint implements MapUiConstraint
{
    public EmisMetaEnum metaEnum;

    public MapUiEnumConstraint(EmisMetaEnum metaEnum) {
        this.metaEnum = metaEnum;
    }

    public List<String> getValues()
    {
        return Arrays.asList(this.metaEnum.getValues());
    }

    public boolean isValid(String value)
    {
        return -1 != this.metaEnum.getIndex(value);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiEnumConstraint
 * JD-Core Version: 0.6.0
 */