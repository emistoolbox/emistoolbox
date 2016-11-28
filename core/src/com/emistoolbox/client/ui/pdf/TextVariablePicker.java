package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.EmisPdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfMetaResultContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfPriorityListContentConfig;
import com.emistoolbox.common.util.NamedIndexList;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class TextVariablePicker extends Tree implements HasValueChangeHandlers<String>
{
	public TextVariablePicker(EmisPdfReportConfig reportConfig, PdfContentConfig contentConfig)
	{
		TreeItem reportItem = createItem("Report", new String[] { "Name", "Page Number", "Page Count" }, new String[] { "report_name", "report_page", "report_pageTotal" });
		reportItem.addItem(createLocationItem("Location", "report_location_"));  
		addItem(reportItem); 
		
		TreeItem groupItem = createItem("Group", new String[] { "Page Number", "Page Count" }, new String[] { "group_page", "group_pageTotal"});
		groupItem.addItem(createLocationItem("Location", "group_location_")); 
		addItem(groupItem); 
		
		addItem(createLocationItem("Location", "location_"));  
		addItem(createLocationItem("Parent", "parent_"));  

		EmisMetaHierarchy hierarchy = reportConfig.getHierarchy();
		if (contentConfig == null)
			addItem(createHierarchyItems(hierarchy, reportConfig.getSeniorEntity())); 
		else
		{
			EmisMetaEntity entityType = contentConfig.getSeniorEntity(); 
			if (entityType != null)
				addItem(createHierarchyItems(hierarchy, entityType)); 
			
			if (contentConfig instanceof PdfMetaResultContentConfig && !(contentConfig instanceof PdfPriorityListContentConfig))
				addItem(createItem("Content Title", "content_title"));
		}
		
		addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) 
			{
				String value = (String) event.getSelectedItem().getUserObject(); 
				if (value == null)
					return; 
				
				ValueChangeEvent.fire(TextVariablePicker.this, value);
			}
		}); 
	}
	
	private TreeItem createHierarchyItems(EmisMetaHierarchy hierarchy, EmisMetaEntity entityType)
	{
		TreeItem result = new TreeItem("Hierarchy"); 

		NamedIndexList<EmisMetaEntity> entities = hierarchy.getEntityOrder(); 
		int index = NamedUtil.findIndex(entityType, entities); 
		for (int i = 0; i <= index; i++)
		{
			String level = entities.get(i).getName(); 
			
			TreeItem levelItem = new TreeItem(level); 
			levelItem.addItem(createItem("ID", "hierarchy_" + level + "_id"));  
			levelItem.addItem(createItem("Name", "hierarchy_" + level + "_name")); 
			result.addItem(levelItem); 
		}
		
		return result; 
	}
	
	private TreeItem createLocationItem(String name, String prefix)
	{ return createItem(name, new String[] { "Level", "ID", "Name" }, new String[] { prefix + "level", prefix + "id", prefix + "name" }); }
	
	private TreeItem createItem(String name, String[] children, String[] keys)
	{
		TreeItem result = new TreeItem(name); 
		for (int i = 0; i < children.length; i++)
		{
			TreeItem child = createItem(children[i], keys[i]);  
			result.addItem(child); 
		}
		
		return result; 
	}
	
	private TreeItem createItem(String name, String value)
	{
		TreeItem result = new TreeItem(name); 
		result.setUserObject(value);
		
		return result; 
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) 
	{ return addHandler(handler, ValueChangeEvent.getType()); }
}
