package com.emistoolbox.common.model.mapping;

public abstract interface EmisDbMapBase
{
    public abstract DbContext getDbContext();

    public abstract void setDbContext(DbContext paramDbContext);
    
    public abstract void updateDimensions(); 
}
