package com.emistoolbox.client.admin.ui.mapping;

import java.util.List;

public abstract interface MapUiConstraint
{
    public abstract boolean isValid(String paramString);

    public abstract List<String> getValues();
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.client.admin.ui.mapping.MapUiConstraint
 * JD-Core Version: 0.6.0
 */