package com.emistoolbox.server.renderer.pdfreport.layout;

import java.util.List;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.server.renderer.pdfreport.EmisPdfPage;

public interface LayoutPage extends EmisPdfPage 
{
	public LayoutPageConfig getPageConfig(); 
	public void setPageConfig(LayoutPageConfig config); 

	public List<LayoutFrame> getFrames(); 
	public void setFrames(List<LayoutFrame> frames); 
	public void addFrame(LayoutFrame frame); 
}
