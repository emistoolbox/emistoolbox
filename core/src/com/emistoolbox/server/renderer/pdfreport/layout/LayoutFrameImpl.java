package com.emistoolbox.server.renderer.pdfreport.layout;

import com.emistoolbox.common.renderer.pdfreport.TextSetImpl;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;

public class LayoutFrameImpl extends TextSetImpl implements LayoutFrame 
{
	private LayoutFrameConfig config; 
	private PdfContent content; 
	
	public LayoutFrameImpl()
	{ super(LayoutFrameConfig.TEXT_KEYS); }
	
	@Override
	public LayoutFrameConfig getFrameConfig() 
	{ return config; }

	@Override
	public void setFrameConfig(LayoutFrameConfig config) 
	{ this.config = config; } 

	@Override
	public PdfContent getContent() 
	{ return content; } 

	@Override
	public void setContent(PdfContent content) 
	{ this.content = content; }

	@Override
	public <T> T accept(LayoutVisitor<T> visitor) 
	{ return visitor.visit(this); } 
}
