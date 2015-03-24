package com.emistoolbox.common.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.Named;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;

public class EmisUser implements Named, Serializable
{
    public enum AccessLevel { VIEWER, REPORT_ADMIN, SYSTEM_ADMIN }; 

    private String username;
    private String passwordHash;
    private String password;
    private String dataset;
    
    private List<String> rootEntityTypes = new ArrayList<String>(); 
    private List<Integer> rootEntityIds = new ArrayList<Integer>(); 
    private List<EmisEntity> entities = null;
    
    private AccessLevel level = AccessLevel.VIEWER;; 

    public void setAccessLevel(AccessLevel level)
    { this.level = level; } 
    
    public AccessLevel getAccessLevel()
    { return level; } 

    public String getUsername()
    { return getName(); } 
    
    public String getName()
    { return username; } 
    
    public void setUsername(String username)
    { setName(username); } 
    
    public void setName(String username)
    { this.username = username; } 
    
    public String getPassword()
    { return password; } 

    public void setPassword(String password)
    { this.password = password; } 
    
    public String getPasswordHash()
    { return passwordHash; } 
    
    public void setPasswordHash(String hash)
    { 
        this.password = null; 
        this.passwordHash = hash; 
    }

	public String getDataset() 
	{ return dataset; }

	public void setDataset(String dataset) 
	{ this.dataset = dataset; }

	public void addRootEntity(String type, int id)
	{
		rootEntityTypes.add(type); 
		rootEntityIds.add(id); 
	}

	public List<String> getRootEntityTypes()
	{ return rootEntityTypes; } 
	
	public List<Integer> getRootEntityIds()
	{ return rootEntityIds; } 
	
	public List<EmisEntity> getRootEntities() 
	{ return entities; }

	public void setRootEntities(List<EmisEntity> entities) {
		this.entities = entities;
	}
	
	public void clearRootEntities()
	{
		rootEntityTypes.clear(); 
		rootEntityIds.clear(); 
	} 
}
