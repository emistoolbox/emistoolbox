package com.emistoolbox.client.ui.pdf.layout;

import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.google.gwt.user.client.ui.FlexTable;

public class LayoutContentList extends FlexTable implements EmisEditor<List<PdfContentConfig>>
{
	public LayoutContentList()
	{
		
	}

	@Override
	public void commit() 
	{
	}

	@Override
	public List<PdfContentConfig> get() 
	{
		return null;
	}

	@Override
	public void set(List<PdfContentConfig> paramT) 
	{
	}
}
