package com.emistoolbox.client.ui.pdf;

import java.util.Map;

import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.google.gwt.user.client.ui.TextBox;

public class LayoutPageConfigProperties extends LayoutProperties<LayoutPageConfig> 
{
	private LayoutPageConfig config; 

	private ChartColorEditor uiBgColor = new ChartColorEditor(); 
	private Map<String, TextBox> uiTexts; 

	public LayoutPageConfigProperties()
	{
		int row = getRowCount(); 
		uiBgColor.addChangeHandler(getChangeHandler());
		
		setText(row, 0, "Background");
		setWidget(row++, 1, uiBgColor);
		
		uiTexts = initTexts(LayoutPageConfig.TEXT_KEYS, new String[] { "Title", "Subtitle", "Footer" });
		set(null); 
	}
	
	@Override
	public void commit() 
	{
		if (config == null)
			return; 
		
		updateTextSet(uiTexts, config); 
		config.setBackgroundColour(uiBgColor.get()); 
	}

	@Override
	public LayoutPageConfig get() 
	{
		commit(); 
		return config;
	}

	@Override
	public void set(LayoutPageConfig config) 
	{
		this.config = config; 
		if (config == null)
			this.setVisible(false);
		else
		{
			this.setVisible(true);
			
			uiBgColor.set(config.getBackgroundColour());
			setTextSet(uiTexts, config); 
		}
	}
}
