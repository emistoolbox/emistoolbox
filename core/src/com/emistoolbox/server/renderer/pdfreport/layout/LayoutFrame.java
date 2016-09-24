package com.emistoolbox.server.renderer.pdfreport.layout;

import com.emistoolbox.common.renderer.pdfreport.TextSet;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.server.renderer.pdfreport.PdfContent;

public interface LayoutFrame extends TextSet
{
	public LayoutFrameConfig getFrameConfig(); 
	public void setFrameConfig(LayoutFrameConfig config);
	
	public PdfContent getContent(); 
	public void setContent(PdfContent content); 
}
