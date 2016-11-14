package com.emistoolbox.client.ui.pdf;

import java.util.List;
import java.util.Map;

import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfTableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.SimpleTableStyle;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutFrameConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.LayoutPageConfig;
import com.emistoolbox.common.renderer.pdfreport.layout.impl.LayoutFrameConfigImpl;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.TableMetaResult;
import com.emistoolbox.common.util.LayoutSides;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;

public class LayoutFrameConfigProperties extends LayoutProperties<LayoutFrameConfig>
{
	private LayoutFrameConfig config; 
	
	private HTML uiFrameInfo = new HTML(); 
	 
	private Map<String, TextSetEntryUi> uiTextSet; 
	private ChartColorEditor uiBgColor = new ChartColorEditor(); 
	private IntPicker uiBorderRadius = new IntPicker(new int[] { 0, 1, 2, 3, 4, 5, 7, 10}, "", "pt");  
	private BorderEditor uiBorders = new BorderEditor(); 
	private IntPicker uiPadding = new IntPicker(new int[] { 0, 2, 5, 10, 15, 20 }, "", "pt");  

	private int moveRow; 
	
	private PriorityListConfigEditor uiPrioList = new PriorityListConfigEditor(); 
	private int prioListRow; 
	
	private SimpleTableStyleEditor uiTableStyle = new SimpleTableStyleEditor(); 
	private int tableStyleRow; 
	
	private PushButton btnEditText = new PushButton("Edit Text"); 
	private int textEditRow; 
	
	private PushButton btnVarText = new PushButton("Edit Variables"); 
	private int varEditRow; 
	
	private static String[] ALL_TEXT_KEYS = new String[] { PdfText.TEXT_TITLE, PdfText.TEXT_SUBTITLE, PdfText.TEXT_PLAIN, PdfText.TEXT_FOOTER }; 
	private static String[] ALL_TEXT_HEADERS = new String[] { "Title", "Subtitle", "Body", "Footer" };  
	
	public LayoutFrameConfigProperties()
	{		
		int row = 0; 

		setText(row, 0, "Info"); 
		setWidget(row, 1, uiFrameInfo); 
		row++; 
		
		// Priority List styling. 
		prioListRow = row; 
		setText(row, 0, "List Filter");
		setWidget(row, 1, uiPrioList); 
		getRowFormatter().setVisible(row, false); 
		row++; 
		
		tableStyleRow = row; 
		setText(row, 0, "Table Style"); 
		getCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
		setWidget(row, 1, uiTableStyle); 
		getRowFormatter().setVisible(row, false); 
		row++; 
		
		textEditRow = row; 
		setWidget(row, 1, btnEditText);
		EmisUtils.initSmall(btnEditText, 90); 
		getRowFormatter().setVisible(row, false); 
		btnEditText.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editText(); 
			}
		}); 
		row++; 
		
		varEditRow = row; 
		setWidget(row, 1, btnVarText);
		EmisUtils.initSmall(btnVarText, 90); 
		getRowFormatter().setVisible(row, false); 
		btnVarText.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editVariables(); 
			}
		}); 
		row++; 

		// Move control
		moveRow = row; 
		setText(row, 0, "Move/Delete Frame"); 
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

		uiTextSet = initTexts(ALL_TEXT_KEYS, ALL_TEXT_HEADERS); 

		uiBgColor.addValueChangeHandler(getValueChangeHandler());
		uiBorderRadius.addChangeHandler(getChangeHandler()); 
		uiPadding.addChangeHandler(getChangeHandler()); 
		uiBorders.addValueChangeHandler(getValueChangeHandler()); 
		
		set(null);
	}
	
	public void setMoveToPageUi(ListBox uiMoveToPage)
	{ setWidget(moveRow, 1,  uiMoveToPage); } 
	
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

			uiFrameInfo.setHTML(getFrameInfoHtml(config.getContentConfig()));
			
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
			
			if (config.getContentConfig() instanceof PdfTextContentConfig)
				getRowFormatter().setVisible(textEditRow, true);
			else
				getRowFormatter().setVisible(textEditRow, false);

			if (config.getContentConfig() instanceof PdfVariableContentConfig)
				getRowFormatter().setVisible(varEditRow, true);
			else
				getRowFormatter().setVisible(varEditRow, false);

		}
	}
	
	private String getFrameInfoHtml(PdfContentConfig content)
	{
		PdfContentConfigVisitor<String> visitor = new PdfContentConfigVisitor<String>() {
			@Override
			public String visit(PdfTextContentConfig config) 
			{ return "<b>Texts</b>"; }

			@Override
			public String visit(PdfVariableContentConfig config) 
			{ return "<b>Variables</b>"; } 

			@Override
			public String visit(PdfChartContentConfig config) 
			{ return "<b>Chart</b><br />" + getMetaResultInfo(config.getMetaResult()); }

			@Override
			public String visit(PdfGisContentConfig config) 
			{ return "<b>Map</b>"; }

			@Override
			public String visit(PdfPriorityListContentConfig config) 
			{ return "<b>Priority List</b>"; } 

			@Override
			public String visit(TableStyleConfig config) 
			{ 
				if (config instanceof PdfTableContentConfig)
					return "<b>Table</b><br />" + getMetaResultInfo(((PdfTableContentConfig) config).getMetaResult()); 
				
				return null; 
			}
		}; 
		
		return content.accept(visitor);  
	}
	
	private String getMetaResultInfo(MetaResult metaResult)
	{
		StringBuffer result = new StringBuffer(); 
		for (MetaResultValue value : metaResult.getMetaResultValues())
		{
			if (result.length() > 0)
				result.append(", "); 
			result.append(value.getIndicator().getName()); 
			if (value.getAggregatorKey() != null)
			{
				result.append("/"); 
				result.append(value.getAggregatorKey()); 
			}
		}
		
		if (metaResult instanceof TableMetaResult)
		{
			TableMetaResult tableMetaResult = (TableMetaResult) metaResult; 
			for (int i = 0; i < tableMetaResult.getDimensionCount(); i++)
			{
				if (i > 0)
					result.append(", "); 
				result.append(tableMetaResult.getDimension(i).getName());
			}
		}
		
		return result.toString(); 
	}
	
	
	private void editText()
	{
		PdfTextContentConfig textContent = (PdfTextContentConfig) config.getContentConfig(); 
	    PdfReportEditor.editText(textContent, new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				fireValueChangeEvent(); 
			}
	    }); 
	}
	
	private void editVariables()
	{
		PdfVariableContentConfig varContent = (PdfVariableContentConfig) config.getContentConfig(); 
		PdfReportEditor.editVariables(varContent, new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				fireValueChangeEvent(); 
			}
		});
	}
	
	
	public void updatePageIndex(int index, int total)
	{ getRowFormatter().setVisible(0, total > 1); }
}
