package com.emistoolbox.server.renderer.pdfreport.impl;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.server.renderer.pdfreport.EmisPageGroup;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;

public class PageGroupImpl implements EmisPageGroup
{
	private List<EmisPageGroup> pageGroups = new ArrayList<EmisPageGroup>(); 
	private List<EmisPdfPage> pages = new ArrayList<EmisPdfPage>();
	
	private String level; 
	private Integer id; 
	private String name; 
	
	@Override
	public void setInfo(String level, Integer id, String name) 
	{
		this.name = name; 
		this.id = id; 
		this.level = level; 
	}

	@Override
	public String getLevel() 
	{ return level; } 

	@Override
	public void setLevel(String level) 
	{ this.level = level; } 

	@Override
	public String getName() 
	{ return name; }

	@Override
	public void setName(String name) 
	{ this.name = name; } 

	@Override
	public Integer getId() 
	{ return id; } 

	@Override
	public void setId(Integer id) 
	{ this.id = id; } 

	@Override
	public List<EmisPageGroup> getPageGroups() 
	{ return pageGroups; } 

	public void addPageGroup(EmisPageGroup group)
	{ pageGroups.add(group); } 
	
	@Override
	public List<EmisPdfPage> getPages() 
	{ return pages; } 

	public void addPage(EmisPdfPage page)
	{ pages.add(page); } 
}
