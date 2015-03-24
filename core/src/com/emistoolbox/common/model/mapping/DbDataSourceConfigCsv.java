package com.emistoolbox.common.model.mapping;

public class DbDataSourceConfigCsv extends DbDataSourceConfigFile 
{
    private static final long serialVersionUID = 1L;
    
    private DataSourceReplaceSet replace; 

    public DbDataSourceConfigCsv()
	{ super(false, false); }
    
    public DataSourceReplaceSet getReplace()
    { return replace; } 
    
    public void setReplace(DataSourceReplaceSet replace)
    { this.replace = replace; } 
}
