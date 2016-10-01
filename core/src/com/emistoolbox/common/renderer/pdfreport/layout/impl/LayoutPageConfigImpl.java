package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.ChartColor;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;

public class LayoutPageConfigImpl extends TextSetImpl implements LayoutPageConfig, Serializable
{
	private List<LayoutFrameConfig> frames = new ArrayList<LayoutFrameConfig>(); 
	private ChartColor backgroundColor; 
	private String imagePath; 

	public LayoutPageConfigImpl()
	{ super(TEXT_KEYS); }
	
	@Override
	public List<LayoutFrameConfig> getFrames() 
	{ return frames; }

	@Override
	public void setFrames(List<LayoutFrameConfig> frames) 
	{ this.frames = frames; } 

	@Override
	public void addFrame(LayoutFrameConfig frame) 
	{ frames.add(frame); }

	@Override
	public ChartColor getBackgroundColour() 
	{ return backgroundColor; } 

	@Override
	public void setBackgroundColour(ChartColor color) 
	{ this.backgroundColor = color; }

	@Override
	public String getBackgroundImagePath() 
	{ return imagePath; }

	@Override
	public void setBackgroundImagePath(String path) 
	{ this.imagePath = path; }
}
