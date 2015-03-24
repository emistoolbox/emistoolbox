package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbContext;
import com.emistoolbox.common.model.mapping.EmisDbMapBase;
import java.io.Serializable;

public abstract class EmisDbMapBaseImpl implements EmisDbMapBase, Serializable
{
    private static final long serialVersionUID = 1L;
    private DbContext dbContext;

    public DbContext getDbContext()
    { return this.dbContext; }

    public void setDbContext(DbContext dbContext)
    { this.dbContext = dbContext; }
}
