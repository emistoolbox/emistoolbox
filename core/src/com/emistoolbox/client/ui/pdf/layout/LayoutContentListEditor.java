package com.emistoolbox.client.ui.pdf.layout;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.renderer.ChartConfig.ChartType;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.TableStyleConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.TableMetaResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LayoutContentListEditor extends VerticalPanel implements EmisEditor<List<PdfContentConfig>>
{
	private ListBoxWithUserObjects<PdfContentConfig> uiConfigs = new ListBoxWithUserObjects<PdfContentConfig>(); 
	private PushButton btnAddToPage = new PushButton("Add to Page"); 
	private PushButton btnDel = new PushButton("Del"); 

	public LayoutContentListEditor()
	{
		setWidth("100%"); 
		
		add(new HTML("<div class='section'>New Content</div>")); 
		add(uiConfigs); 
		uiConfigs.setVisibleItemCount(5);
		uiConfigs.setWidth("100%");
		
		// Buttons. 
		EmisUtils.init(btnAddToPage, 100);
		EmisUtils.init(btnDel, 60);
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.setSpacing(3);
		hp.add(btnDel);
		hp.add(btnAddToPage);
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		add(hp); 
		add(new HTML("<p>This list shows content not, yet, placed on the page.</p><p>You can add new content by going to the 'Analysis' tab and display a chart, a table or a priority list. Then click <b>[Add to Report]</b>.")); 
		
		btnDel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				removeContent(); 
			}
		}); 
	}
	
	public void addAddToPageHandler(ClickHandler handler)
	{ btnAddToPage.addClickHandler(handler); } 

	public PdfContentConfig removeContent()
	{
		int index = uiConfigs.getSelectedIndex(); 
		if (index == -1)
			return null; 
		
		PdfContentConfig content = uiConfigs.getUserObject(index); 
		uiConfigs.removeItem(index);
		uiConfigs.removeEmptyGroups();
		
		if (index >= uiConfigs.getItemCount())
			index--; 
		
		if (index != -1)
			uiConfigs.setSelectedIndex(index); 
		
		return content; 
	}

	public void enableAddButton(boolean enabled)
	{ btnAddToPage.setEnabled(enabled); }
	
	public void set(List<PdfContentConfig> configs)
	{
		uiConfigs.clear(); 
		if (configs == null)
			return; 
		
		for (PdfContentConfig config : configs)
		{
			if (config  == null)
				continue; 
			
			uiConfigs.add(getGroup(config), getTitle(config), config); 
		}
	}
	
	public void commit()
	{}
	
	public List<PdfContentConfig> get()
	{
		List<PdfContentConfig> result = new ArrayList<PdfContentConfig>(); 
		for (int i = 0; i < uiConfigs.getItemCount(); i++)
			result.add(uiConfigs.getUserObject(i));
		
		return result; 
	}
	
	private String getGroup(PdfContentConfig config)
	{ return config.accept(new GroupNameVisitor()); }
	
	private String getTitle(PdfContentConfig config)
	{ return config.accept(new SummaryVisitor()); }
}

class GroupNameVisitor implements PdfContentConfigVisitor<String>
{
	public String visit(PdfTextContentConfig config) 
	{ return "Text"; } 

	public String visit(PdfVariableContentConfig config)
	{ return "Variables"; } 

	public String visit(PdfChartContentConfig config) 
	{ return "Charts"; } 

	public String visit(PdfGisContentConfig config) 
	{ return "Maps"; } 

	@Override
	public String visit(PdfPriorityListContentConfig config) 
	{ return "Prio Lists"; }

	@Override
	public String visit(TableStyleConfig config) 
	{ return "Tables"; } 
}

class SummaryVisitor implements PdfContentConfigVisitor<String>
{
	public String visit(PdfTextContentConfig config) 
	{ return trimTo(config.getText(), "(empty)", 20); } 

	public String visit(PdfVariableContentConfig config)
	{
		StringBuffer result = new StringBuffer();

		List<String> vars = config.getVariables(); 
		if (vars != null)
			for (String var : vars)
			{
				if (result.length() > 0)
					result.append(","); 
				result.append(var); 
			}
		
		return trimTo(result.toString(), "(none)", 20);  
	} 

	private String trimTo(String text, String defaultText, int maxLength)
	{ 
		if (text == null || text.equals(""))
			return defaultText; 
		
		if (text.length() < maxLength)
			return text; 
		
		return text.substring(0, maxLength) + ".."; 
	}
	
	private String getChartTypeName(int chartType)
	{
		ChartType result = ChartType.values()[chartType];
		return result.name().toLowerCase();
	}
	
	public String visit(PdfChartContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		result.append(getChartTypeName(config.getChartType()) + " for ");  
		result.append(config.getSeniorEntity().getName());

		String delim = " shown by "; 
		for (int i = 0; i < config.getMetaResult().getDimensionCount(); i++)
		{
			MetaResultDimension dim = config.getMetaResult().getDimension(i); 
			result.append(delim);
			result.append(dim.getName());
			delim = ", "; 
		}
		
		return result.toString(); 
	} 

	public String visit(PdfGisContentConfig config) 
	{
		config.getMetaResult().getIndicator(); 
		return "";
	} 

	private boolean appendEntity(StringBuffer result, MetaResult metaResult)
	{
		if (metaResult == null || metaResult.getContext() == null || metaResult.getContext().getEntityType() == null)
			return false; 
		
		result.append(metaResult.getContext().getEntityType().getName()); 
		return true; 
	}
	
	private boolean appendIndicator(StringBuffer result, MetaResult metaResult)
	{
		if (metaResult == null || metaResult.getIndicator() == null)
			return false; 
		
		result.append(metaResult.getIndicator().getName()); 
		return true; 
	}
	
	private boolean appendDimensions(StringBuffer result, TableMetaResult metaResult)
	{
		if (metaResult == null || metaResult.getDimensionCount() == 0)
			return false; 
		
		for (int i = 0; i < metaResult.getDimensionCount(); i++)
		{
			if (i > 0)
				result.append(", "); 
			result.append(metaResult.getDimension(i).getName());
		}
		
		return true; 
	}
	

	@Override
	public String visit(PdfPriorityListContentConfig config) 
	{ return "Priority Lists"; }

	@Override
	public String visit(TableStyleConfig config) 
	{ return "Data Table"; } 
}
