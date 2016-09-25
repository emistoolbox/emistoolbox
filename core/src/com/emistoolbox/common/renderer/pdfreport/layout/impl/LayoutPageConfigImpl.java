package com.emistoolbox.common.renderer.pdfreport.layout.impl;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;

public class LayoutPageConfigImpl extends TextSetImpl implements LayoutPageConfig 
{
	private List<LayoutFrameConfig> frames = new ArrayList<LayoutFrameConfig>(); 

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
}
