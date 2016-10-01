package com.emistoolbox.client.ui.pdf;

import java.util.Map;

import com.emistoolbox.client.util.ui.UIUtils;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageOrientation;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig.PageSize;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class LayoutReportConfigProperties extends LayoutProperties<EmisPdfReportConfig> 
{
	private EmisPdfReportConfig config = null; 
	private ListBox uiPageSize = new ListBox();  
	private ListBox uiPageOrientation = new ListBox(); 
	
	private Map<String, TextBox> uiTexts; 
	
	public LayoutReportConfigProperties()
	{	
		uiPageSize.addChangeHandler(getChangeHandler()); 
		UIUtils.listBoxInit(uiPageSize, PageSize.values()); 

		uiPageOrientation.addChangeHandler(getChangeHandler()); 
		UIUtils.listBoxInit(uiPageOrientation, PageOrientation.values());
		
		int row = getRowCount(); 

		setText(row, 0, "Page Size");
		setWidget(row++, 1, uiPageSize); 
		
		setText(row, 0, "Page Orientation"); 
		setWidget(row++, 1, uiPageOrientation);
		
		uiTexts = initTexts(LayoutPageConfig.TEXT_KEYS, new String[] { "Default Title", "Default Subtitle", "Default Footer"}); 
		set(null); 
	}

	@Override
	public void commit() 
	{
		if (config == null)
			return; 
	
		updateTextSet(uiTexts, config);
		config.setPage(PageSize.valueOf(UIUtils.getListBoxValue(uiPageSize)), PageOrientation.valueOf(UIUtils.getListBoxValue(uiPageOrientation)));
	}

	@Override
	public EmisPdfReportConfig get() 
	{
		commit(); 
		return config;
	}

	@Override
	public void set(EmisPdfReportConfig config) 
	{
		this.config = config; 
		if (config == null)
			setVisible(false); 
		else
		{
			setVisible(true); 
			
			UIUtils.setListBoxValue(uiPageSize, config.getPageSize().toString());
			UIUtils.setListBoxValue(uiPageOrientation, config.getOrientation().toString());
	
			setTextSet(uiTexts, config);
		}
	}
}
