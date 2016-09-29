package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.google.gwt.user.client.ui.FlexTable;

public class LayoutPageEditor extends FlexTable implements EmisEditor<LayoutPageConfig> 
{
	private LayoutPageConfig pageConfig; 
	
	@Override
	public void commit() 
	{}

	@Override
	public LayoutPageConfig get() 
	{ 
		commit(); 
		return pageConfig; 
	} 

	@Override
	public void set(LayoutPageConfig pageConfig) 
	{
		this.pageConfig = pageConfig; 
		updateUi(); 
	}

	private void updateUi()
	{
		if (pageConfig == null)
			setText(0, 0, "none"); 
		else
			setText(0, 0, "PAGE SET"); 
	}
}
