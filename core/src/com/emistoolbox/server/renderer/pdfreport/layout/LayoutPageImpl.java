package com.emistoolbox.server.renderer.pdfreport.layout;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.common.ChartFont;
import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.server.renderer.pdfreport.FontIdentifier;

public class LayoutPageImpl extends TextSetImpl implements LayoutPage 
{
	private LayoutPageConfig config; 
	private List<LayoutFrame> frames = new ArrayList<LayoutFrame>(); 
	
	public LayoutPageImpl()
	{ super(TEXT_KEYS); } 
	
	@Override
	public void setFont(FontIdentifier paramFontIdentifier, ChartFont paramChartFont) 
	{}

	@Override
	public LayoutPageConfig getPageConfig() 
	{ return config; } 

	@Override
	public void setPageConfig(LayoutPageConfig config) 
	{ this.config = config; } 

	@Override
	public List<LayoutFrame> getFrames() 
	{ return frames; }

	@Override
	public void setFrames(List<LayoutFrame> frames) 
	{ this.frames = frames; } 

	@Override
	public void addFrame(LayoutFrame frame) 
	{ this.frames.add(frame); }

	@Override
	public <T> T accept(LayoutVisitor<T> visitor) 
	{ return visitor.visit(this); } 
}
