package com.emistoolbox.common.fileDialog; 
// package com.emistoolbox.gwt.module.filelistdialog.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileDirectoryInfo implements Serializable 
{
    private static final long serialVersionUID = 1L;
    private String path;
    private String prefix; 
    private List<String> directories = new ArrayList<String>();  
    private List<String> files = new ArrayList<String>(); 
    
    public FileDirectoryInfo()
    {}
    
    public FileDirectoryInfo(String path, String prefix) 
    {
    	this.path = path;
    	this.prefix = prefix; 
    }

    public void addDirectory(String directory)
    { directories.add(directory); } 
    
    public List<String> getDirectories()
    { return directories; } 

    public void addFile(String file)
    { files.add(file); } 
    
    public List<String> getFiles()
    { return files; }
    
    public String getPath() 
    { return path; }

    public String getPrefix() 
    { return prefix; }

    public String getFullPath() 
    { return prefix + path; }
}
