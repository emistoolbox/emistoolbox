package com.emistoolbox.client.ui.pdf;

import java.util.Map;

import com.emistoolbox.common.renderer.pdfreport.layout.BorderStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.util.LayoutSides;
import com.google.gwt.user.client.ui.TextBox;

public class LayoutFrameConfigProperties extends LayoutProperties<LayoutFrameConfig>
{
	private LayoutFrameConfig config; 
	
	private Map<String, TextBox> uiTexts; 
	private ChartColorEditor uiBgColor = new ChartColorEditor(); 
	private TextBox uiBorderRadius = new TextBox(); 
	private BorderEditor uiBorders = new BorderEditor(); 

	public LayoutFrameConfigProperties()
	{
		int row = getRowCount(); 
		uiBgColor.addChangeHandler(getChangeHandler());
		
		setText(row, 0, "Background");
		setWidget(row++, 1, uiBgColor);

		setText(row, 0, "Borders");
		setWidget(row++, 1, uiBorders);

		setText(row, 0, "Border Radius");
		setWidget(row++, 1, uiBorderRadius);

		uiTexts = initTexts(LayoutPageConfig.TEXT_KEYS, new String[] { "Title", "Subtitle", "Footer" });

		set(null); 
	}
	
	@Override
	public void commit() 
	{
		if (config == null)
			return; 
		
		config.setBackgroundColour(uiBgColor.get());
		config.setBorders(uiBorders.get());

		try { config.setBorderRadius(new Integer(uiBorderRadius.getText())); }
		catch (Throwable err)
		{ config.setBorderRadius(0); }
		
		updateTextSet(uiTexts, config); 
	}

	@Override
	public LayoutFrameConfig get() 
	{
		commit(); 
		return config;
	}

	@Override
	public void set(LayoutFrameConfig config) 
	{
		this.config = config; 
		if (config == null)
			setVisible(false);
		else
		{
			setVisible(true); 

			uiBgColor.set(config.getBackgroundColour()); 
			uiBorderRadius.setText("" + config.getBorderRadius()); 
			uiBorders.set(config.getBorders()); 
		}
	}
}
