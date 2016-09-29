package com.emistoolbox.client.ui.pdf.layout;

import java.util.List;

import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.renderer.pdfreport.PdfChartContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfigVisitor;
import com.emistoolbox.common.renderer.pdfreport.PdfGisContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfTableContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfTextContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfVariableContentConfig;
import com.emistoolbox.common.results.MetaResult;
import com.emistoolbox.common.results.MetaResultDimension;
import com.emistoolbox.common.results.TableMetaResult;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LayoutContentListEditor extends VerticalPanel
{
	private ListBoxWithUserObjects<PdfContentConfig> uiConfigs = new ListBoxWithUserObjects<PdfContentConfig>(); 
	private PushButton btnDel = new PushButton("Del"); 

	public LayoutContentListEditor()
	{
		add(new HTML("Available Content")); 
		add(uiConfigs); 
		
		// Buttons. 
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.add(btnDel);
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		add(hp); 
	}
	
	public void set(List<PdfContentConfig> configs)
	{
		uiConfigs.clear(); 
		for (PdfContentConfig config : configs)
			uiConfigs.add(getGroup(config), getTitle(config), config); 
	}
	
	private String getGroup(PdfContentConfig config)
	{ return config.accept(new GroupNameVisitor()); }
	
	private String getTitle(PdfContentConfig config)
	{ return null;  }
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
	{ return "Priority Lists"; }

	@Override
	public String visit(PdfTableContentConfig config) 
	{ return "Data Table"; } 
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
	
	public String visit(PdfChartContentConfig config) 
	{
		StringBuffer result = new StringBuffer(); 
		result.append(config.getChartType() + " for ");  
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
	public String visit(PdfTableContentConfig config) 
	{ return "Data Table"; } 
}
