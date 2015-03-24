package com.emistoolbox.common.model.mapping.impl;

import com.emistoolbox.common.model.mapping.DbRowAccess;
import com.emistoolbox.common.model.mapping.GisEntityDbMap;

public class GisEntityDbMapImpl extends EntityBaseDbMapImpl implements GisEntityDbMap
{
    private DbRowAccess primaryAccess;
    private DbRowAccess secondaryAccess;

    public DbRowAccess getPrimaryAccess()
    { return this.primaryAccess; }

    public DbRowAccess getSecondaryAccess()
    { return this.secondaryAccess; }

    public void setPrimaryAccess(DbRowAccess access)
    { this.primaryAccess = access; }

    public void setSecondaryAccess(DbRowAccess access)
    { this.secondaryAccess = access; }

	@Override
	public void updateDimensions() 
	{
		super.updateDimensions(); 
		DbRowAccessBase.updateDimensions(primaryAccess);
		DbRowAccessBase.updateDimensions(secondaryAccess);
	}
}
