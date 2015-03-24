package com.emistoolbox.common.model.mapping;

import com.emistoolbox.common.model.mapping.impl.DbDataFileSourceImpl;
import com.emistoolbox.common.model.mapping.impl.DbDataSourceConfigBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class DbDataSourceConfigFile extends DbDataSourceConfigBase implements DbDataSourceConfig, Serializable
{
    private static final long serialVersionUID = 2L;

    private String contextName;
    private List<DbDataFileSource> fileSources = new ArrayList<DbDataFileSource>(); 
    private boolean connected = false;

    public DbDataSourceConfigFile(boolean hasQueries, boolean allowUnflattenXml) 
    { super(hasQueries, allowUnflattenXml); }

    public List<DbDataFileSource> getFileSources()
    { return fileSources; } 
    
    public void setFileSources(List<DbDataFileSource> fileSources)
    { this.fileSources = fileSources; }
    
    public void clear()
    { this.fileSources.clear(); }

    public void addFileSource(DbDataFileSource fileSource)
    { fileSources.add(fileSource); }
    
    public DbDataFileSource addFileSource(String path, String value)
    {
        if ((path == null) || (path.equals("")))
            return null;

        DbDataFileSource fileSource = new DbDataFileSourceImpl(); 
        fileSource.setUrl(path);
        fileSource.setContextValue(value);
        fileSources.add(fileSource); 
        
        return fileSource; 
    }
    
    public String getContextName()
    { return this.contextName; }

    public void setContextName(String name)
    { this.contextName = name; }

    public String getName()
    {
        if (this.fileSources.size() == 0)
        {
            return "none";
        }
        String path = (String) this.fileSources.get(0).getUrl();
        return getFileSourceTypeName(path) + path.substring(Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/')) + 1) + " " + (this.connected ? "[OK]" : "");
    }
    
    private String getFileSourceTypeName(String path)
    {
    	if (path == null || path.equals(""))
    		return ""; 
    	
    	if (path.startsWith("http://"))
    		return "URL:"; 
    	
    	return ""; 
    }

    public boolean hasConnected()
    { return this.connected; }

    public void setHasConnected(boolean connected)
    {
        this.connected = connected;
    }
}
