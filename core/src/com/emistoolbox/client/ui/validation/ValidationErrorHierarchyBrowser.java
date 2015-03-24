package com.emistoolbox.client.ui.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.StatusAsyncCallback;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.impl.Entity;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.model.validation.EmisValidation;
import com.emistoolbox.common.model.validation.EmisValidationError;
import com.emistoolbox.common.model.validation.EmisValidationErrorHierarchy;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ValidationErrorHierarchyBrowser extends FlexTable 
{
	private FlexTable uiContent = new FlexTable(); 
	private EmisMetaHierarchy hierarchy; 
	private Tree uiTree = new Tree();
	private ScrollPanel uiTreeScroll = new ScrollPanel(); 
	private EmisToolbox toolbox; 
	private String dataset; 
	private int dateIndex; 
	
	public ValidationErrorHierarchyBrowser(EmisToolbox toolbox, EmisMetaHierarchy hierarchy, List<EmisValidation> validations, String dataset, int dateIndex)
	{
		this.toolbox = toolbox; 
		this.hierarchy = hierarchy; 
		
		this.dataset = dataset; 
		this.dateIndex = dateIndex; 
		
		// TODO i18n
		setHTML(0, 0, "<b>Error Browser</b>"); 
		setHTML(0, 1, "<b>Error Details</b>");  
		uiTreeScroll.setWidget(uiTree);
		uiTreeScroll.addStyleName("fullborder");
		uiTreeScroll.setWidth("250px"); 
		uiTreeScroll.setHeight("400px");
		setWidget(1, 0, uiTreeScroll);
		getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

		setWidget(1, 1, uiContent); 
		getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
		getFlexCellFormatter().setRowSpan(1, 1, 2); 
		
		setWidget(2, 0, getRuleSummary(validations)); 
		getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

		uiTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) 
			{ show((EmisValidationErrorHierarchy) event.getSelectedItem().getUserObject()); }
		}); 
	}
	
	private VerticalPanel getRuleSummary(List<EmisValidation> validations)
	{
		VerticalPanel vp = new VerticalPanel();

		vp.add(new HTML("<b>Validation Summary</b>")); 
		
		for (EmisValidation validation : validations)
		{
			vp.add(new HTML(validation.getId())); 
			for (EmisValidationRule rule : validation.getRules())
				vp.add(new HTML("&nbsp;&nbsp;" + rule.getName()));
		}
		
		return vp; 
	}
	
	public void set(EmisValidationErrorHierarchy hierarchy)
	{
		uiTree.clear();
		TreeItem item = getTreeItem(hierarchy); 
		uiTree.addItem(item); 
		uiTree.setSelectedItem(item);
}
	
	private void show(final EmisValidationErrorHierarchy validationHierarchy)
	{
		while (uiContent.getRowCount() > 0)
			uiContent.removeRow(0);
		
		if (validationHierarchy.getErrors().size() == 0)
			showUpdate(validationHierarchy, null); 
		else
		{
			EmisEntity entity = validationHierarchy.getEntity(); 
			// What about entity == null
			
			uiContent.setHTML(0, 0,  "Loading...");
			toolbox.getService().getHierarchyEntities(dataset, hierarchy.getName(), dateIndex, entity.getEntityType(), entity.getId(),
				new StatusAsyncCallback<Map<Integer, String>>("") 
				{
					@Override
					public void onFailure(Throwable caught) 
					{
						super.onFailure(caught);
						uiContent.setHTML(0, 0, "Failed to load names");
						showUpdate(validationHierarchy, null); 
					}

					@Override
					public void onSuccess(Map<Integer, String> result) 
					{ showUpdate(validationHierarchy, result); }
			}); 
			
		}
	}
	
	private void addAllEntries(List<ValidationErrorEntry> result, EmisValidationErrorHierarchy validationHierarchy)
	{
		for (EmisValidationError error : validationHierarchy.getErrors())
		{
			for (Integer id : error.getEntityIds())
			{
				ValidationErrorEntry newEntry = new ValidationErrorEntry();
				newEntry.entity = new Entity(error.getEntityType(), id);
				newEntry.validation = error.getValidation();
				newEntry.rule = error.getValidationRule();
			
				result.add(newEntry); 
			}
		}
		
		for (EmisValidationErrorHierarchy child : validationHierarchy.getChildren())
			addAllEntries(result, child); 
	}

	private void showUpdateAsList(EmisValidationErrorHierarchy validationHierarchy)
	{
		// Find list of errors based on currently selected item. 
		//
		List<ValidationErrorEntry> entries = new ArrayList<ValidationErrorEntry>(); 
		addAllEntries(entries, validationHierarchy); 
		
		Collections.sort(entries, new SortByEntity(hierarchy, true));
	}
	
	private void showUpdate(EmisValidationErrorHierarchy validationHierarchy, Map<Integer, String> entityNames)
	{
		uiContent.clear();
		int row = 0; 
		List<ValidationErrorEntry> entries = getEntries(validationHierarchy.getErrors(), entityNames);
		Collections.sort(entries, new SortByEntity(hierarchy, true));
		
		if (entries.size() == 0)
		{
			uiContent.setHTML(0, 0,  "No errors at this level.");
			return;
		}
		
		EmisValidation lastValidation = null; 
		EmisValidationRule lastRule = null; 
		for (ValidationErrorEntry entry : entries)
		{
			if (!NamedUtil.sameName(lastValidation, entry.validation))
				lastValidation = null; 
			
			if (!NamedUtil.sameName(lastRule, entry.rule))
				lastRule = null; 
			
			if (lastValidation == null)
			{
				uiContent.setHTML(row, 0, "<h2>" + entry.validation.getName() + "</h2>"); 
				row++; 
			}

			if (lastRule == null)
			{
				String description = entry.rule.getDescription(); 
				if (description == null)
					description = "";
				else
					description = "<br>" + description; 
				uiContent.setHTML(row, 0, "<b>" + entry.rule.getName() + "</b>" + description); 
				row++; 
			}

			uiContent.setText(row, 0, "" + entry.entity.getId());
			uiContent.setText(row, 1, "" + entry.entity.getName()); 
			row++; 
			
			lastValidation = entry.validation; 
			lastRule = entry.rule; 
		}
	}

	private List<ValidationErrorEntry> getEntries(List<EmisValidationError> errors, Map<Integer, String> entityNames)
	{
		List<ValidationErrorEntry> result = new ArrayList<ValidationErrorEntry>();
		for (EmisValidationError error : errors)
		{
			for (Integer id : error.getEntityIds())
			{
				ValidationErrorEntry newEntry = new ValidationErrorEntry();
				newEntry.entity = new Entity(error.getEntityType(), id);
				if (entityNames != null)
					newEntry.entity.setName(entityNames.get(newEntry.entity.getId()));
				
				newEntry.validation = error.getValidation();
				newEntry.rule = error.getValidationRule();
				
				result.add(newEntry); 
			}
		}

		return result;
	}

	private TreeItem getTreeItem(EmisValidationErrorHierarchy hierarchy) 
	{
		TreeItem child = new TreeItem(); 
		if (hierarchy.getEntity() == null)
			child.setText("(TOP)"); 
		else
		{
			EmisEntity entity = hierarchy.getEntity(); 
			child.setText(entity.getName() == null ? "" + entity.getId() : entity.getName());
		}

		child.setUserObject(hierarchy);
		
		for (EmisValidationErrorHierarchy childHierarchy : hierarchy.getChildren())
			child.addItem(getTreeItem(childHierarchy)); 

		return child; 
	}
}

class ValidationErrorEntry
{
	public EmisValidation validation; 
	public EmisValidationRule rule; 
	public EmisEntity entity; 
}

abstract class SortByBase implements Comparator<ValidationErrorEntry>
{
	private EmisMetaHierarchy hierarchy; 

	public SortByBase(EmisMetaHierarchy hierarchy)
	{ this.hierarchy = hierarchy; } 
	
	protected int compareInternal(EmisEntity entity1, EmisEntity entity2)
	{ return compareInternal(entity1, entity2, false); }
	
	protected int compareInternal(EmisEntity entity1, EmisEntity entity2, boolean byName)
	{
		if (!NamedUtil.sameName(entity1, entity2))
			return compareInternal(entity1.getEntityType(), entity2.getEntityType());
		
		int nameCompare = compare(entity1.getName(), entity2.getName());
		int idCompare = compare(entity1.getId(), entity2.getId()); 
		if (byName)
			return nameCompare != 0 ? nameCompare : idCompare; 
		else
			return idCompare != 0 ? idCompare : nameCompare; 
	}
	
	protected int compareInternal(EmisMetaEntity entityType1, EmisMetaEntity entityType2)
	{
		int index1 = NamedUtil.findIndex(entityType1, hierarchy.getEntityOrder()); 
		int index2 = NamedUtil.findIndex(entityType2, hierarchy.getEntityOrder());
		
		return compare(index1, index2); 
	}
	
	protected int compareInternal(EmisValidationRule rule1, EmisValidationRule rule2)
	{ return rule1.getName().compareTo(rule2.getName()); }
	
	protected int compare(int i1, int i2)
	{
		if (i1 == i2)
			return 0; 
		if (i1 < i2)
			return -1; 
		return 1; 
	}
	
	protected int compare(String s1, String s2)
	{
		if (s1 == null)
			return s2 == null ? 0 : 1; 

		if (s2 == null)
			return -1; 

		return s1.compareTo(s2); 
	}
}

class SortByEntity extends SortByBase
{
	public SortByEntity(EmisMetaHierarchy hierarchy, boolean byName)
	{ super(hierarchy); } 
	
	@Override
	public int compare(ValidationErrorEntry entry1, ValidationErrorEntry entry2) 
	{ 
		int result = compareInternal(entry1.entity, entry2.entity, true); 
		if (result != 0)
			return result; 
		
		return compareInternal(entry1.rule, entry2.rule); 
	}
}

class SortByValidationRule extends SortByBase
{
	public SortByValidationRule(EmisMetaHierarchy hierarchy)
	{ super(hierarchy); }
	
	@Override
	public int compare(ValidationErrorEntry entry1, ValidationErrorEntry entry2) 
	{
		int result = compareInternal(entry1.rule, entry2.rule);
		if (result != 0)
			return result; 
		
		return compareInternal(entry1.entity, entry2.entity); 
	}
}
