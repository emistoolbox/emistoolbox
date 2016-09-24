package com.emistoolbox.common.renderer.pdfreport.layout;

import java.util.List;

import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.TextSet;

public interface LayoutPageConfig extends TextSet
{
	public static final String[] TEXT_KEYS = new String[] { PdfText.TEXT_TITLE, PdfText.TEXT_SUBTITLE, PdfText.TEXT_FOOTER }; 
	
	public List<LayoutFrameConfig> getFrames(); 
	public void setFrames(List<LayoutFrameConfig> frames); 
	public void addFrame(LayoutFrameConfig frame); 
}
