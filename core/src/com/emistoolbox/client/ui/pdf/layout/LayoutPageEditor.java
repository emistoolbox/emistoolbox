package com.emistoolbox.client.ui.pdf.layout;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class LayoutPageEditor extends SimplePanel implements EmisEditor<LayoutPageConfig> 
{
	private LayoutPageConfig pageConfig; 
	private HTML uiLayout = new HTML(); 
	
	public LayoutPageEditor()
	{
		setWidget(uiLayout); 
	}
	
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
		
		StringBuffer html = new StringBuffer();
		
		updateUi(); 
	}

	private void updateUi()
	{
		if (pageConfig == null)
			setWidget(new HTML("none")); 
		else
			setWidget(new HTML("PAGE SET")); 
	}
}
