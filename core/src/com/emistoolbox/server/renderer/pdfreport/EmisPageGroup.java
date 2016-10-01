package com.emistoolbox.server.renderer.pdfreport;

import java.util.List;

public interface EmisPageGroup 
{
	public void setInfo(String level, Integer id, String name); 
	
	public String getLevel(); 
	public void setLevel(String level); 
	
	public String getName(); 
	public void setName(String name); 
	
	public Integer getId(); 
	public void setId(Integer id); 
	
	public List<EmisPageGroup> getPageGroups(); 
	public void addPageGroup(EmisPageGroup group); 
	
	public List<EmisPdfPage> getPages(); 
	public void addPage(EmisPdfPage page); 
}
