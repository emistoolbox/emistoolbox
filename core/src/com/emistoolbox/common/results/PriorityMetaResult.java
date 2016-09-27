package com.emistoolbox.common.results;

public abstract interface PriorityMetaResult extends ListEntityMetaResult
{
    /** List of additional fields to display in result. */ 
    public String[] getAdditionalFields(); 
    public void setAdditionalFields(String[] fields); 
    
    public boolean getFilterEmpty(); 
    public void setFilterEmpty(boolean filter); 
}
