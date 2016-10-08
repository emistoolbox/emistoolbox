package com.emistoolbox.client.ui.pdf;

import java.util.Map;

import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.SimpleTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.util.LayoutSides;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

public class LayoutFrameConfigProperties extends LayoutProperties<LayoutFrameConfig>
{
	private LayoutFrameConfig config; 
	 
	private Map<String, TextSetEntryUi> uiTextSet; 
	private ChartColorEditor uiBgColor = new ChartColorEditor(); 
	private IntPicker uiBorderRadius = new IntPicker(new int[] { 0, 1, 2, 3, 4, 5, 7, 10}, "", "px");  
	private BorderEditor uiBorders = new BorderEditor(); 
	private IntPicker uiPadding = new IntPicker(new int[] { 0, 2, 5, 10, 15, 20 }, "", "px");  

	private PriorityListConfigEditor uiPrioList = new PriorityListConfigEditor(); 
	private int prioListRow; 
	
	private SimpleTableStyleEditor uiTableStyle = new SimpleTableStyleEditor(); 
	private int tableStyleRow; 
	
	public LayoutFrameConfigProperties()
	{		
		int row = 0; 

		// Leave a row for uiMoveToPage control
		row++;
		
		setHTML(row, 0, "<hr>"); 
		row++; 

		// Priority List styling. 
		prioListRow = row; 
		setText(row, 0, "List Filter");
		setWidget(row, 1, uiPrioList); 
		row++; 
		
		tableStyleRow = row; 
		setText(row, 0, "Table Style"); 
		setWidget(row, 1, uiTableStyle); 
		row++; 
		
		setText(row, 0, "Background");
		setWidget(row++, 1, uiBgColor);

		setText(row, 0, "Borders");
		getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
		setWidget(row++, 1, uiBorders);

		setText(row, 0, "Border Radius");
		setWidget(row++, 1, uiBorderRadius);

		setText(row, 0, "Padding"); 
		setWidget(row, 1, uiPadding); 
		row++; 
		
		uiTextSet = initTexts(LayoutPageConfig.TEXT_KEYS, new String[] { "Title", "Subtitle", "Footer" });

		uiBgColor.addValueChangeHandler(getValueChangeHandler());
		uiBorderRadius.addChangeHandler(getChangeHandler()); 
		uiPadding.addChangeHandler(getChangeHandler()); 
		uiBorders.addValueChangeHandler(getValueChangeHandler()); 
		
		set(null);
	}
	
	public void setMoveToPageUi(IntPicker uiMoveToPage)
	{ setWidget(0, 1,  uiMoveToPage); } 
	
	@Override
	public void commit() 
	{
		if (config == null)
			return; 
		
		config.setBackgroundColour(uiBgColor.get());
		config.setBorders(uiBorders.get());

		try { config.setBorderRadius(uiBorderRadius.get()); }
		catch (Throwable err)
		{ config.setBorderRadius(0); }
		
		config.setPadding(new LayoutSides<Double>((double) uiPadding.get()));
		
		updateTextSet(uiTextSet, config); 
		
		if (config.getContentConfig() instanceof PdfPriorityListContentConfig)
			config.setContentConfig(uiPrioList.get());
		
		if (config.getContentConfig() instanceof TableStyleConfig)
		{
			TableStyleConfig tableConfig = (TableStyleConfig) config.getContentConfig(); 
			tableConfig.setTableStyle(uiTableStyle.get());
		}
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
			uiBorderRadius.set(config.getBorderRadius()); 
			uiBorders.set(config.getBorders()); 

			uiPadding.set((int) Math.round(config.getPadding().getLeft()));
			
			if (config.getContentConfig() instanceof PdfPriorityListContentConfig)
			{
				uiPrioList.set((PdfPriorityListContentConfig) config.getContentConfig()); 
				getRowFormatter().setVisible(prioListRow, true);
			}
			else
				getRowFormatter().setVisible(prioListRow, false);

			if (config.getContentConfig() instanceof TableStyleConfig)
			{
				TableStyleConfig tableConfig = (TableStyleConfig) config.getContentConfig(); 
				
				if (tableConfig.getTableStyle() == null || !(tableConfig.getTableStyle() instanceof SimpleTableStyle))
					tableConfig.setTableStyle(new SimpleTableStyle()); 
				
				uiTableStyle.set((SimpleTableStyle) tableConfig.getTableStyle()); 
				getRowFormatter().setVisible(tableStyleRow, true);
			}
			else
				getRowFormatter().setVisible(tableStyleRow, false);
		}
	}
	
	public void updatePageIndex(int index, int total)
	{ getRowFormatter().setVisible(0, total > 1); }
	
}
