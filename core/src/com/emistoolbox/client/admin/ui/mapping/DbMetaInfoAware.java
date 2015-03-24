package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.common.model.mapping.DbContext;
import java.util.List;
import java.util.Map;

public abstract interface DbMetaInfoAware
{
    public abstract void updateDbMetaInfo();

    public abstract void updateDbMetaInfo(DbContext paramDbContext, Map<String, List<String>> paramMap);
}
