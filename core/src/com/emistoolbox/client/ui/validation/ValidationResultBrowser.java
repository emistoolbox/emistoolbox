package com.emistoolbox.client.ui.validation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.GwtUtils;
import com.emistoolbox.client.ui.DownloadPanel;
import com.emistoolbox.common.model.EmisEntity;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.validation.EmisValidationResult;
import com.emistoolbox.common.model.validation.EmisValidationResultItem;
import com.emistoolbox.common.model.validation.EmisValidationRule;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationResultBrowser extends FlexTable 
{
	private EmisValidationResult validationResult; 
	
	private FlexTable uiContent = new FlexTable(); 
	private Tree uiTree = new Tree();
	private ScrollPanel uiTreeScroll = new ScrollPanel(); 

	private Image uiExportExcel = new Image("css/icon_xls.gif");  
	private Image uiExportCsv = new Image("css/icon_csv.gif");
	private Image uiResetSort = new Image("css/icon_tree.png");
	
	private EmisToolbox toolbox;

	private int currentSortColumn; 
	private Comparator<EmisValidationResultItem> currentSort = getDefaultSort();  
	
	public ValidationResultBrowser(EmisToolbox toolbox)
	{
		this.toolbox = toolbox; 
		
		uiTreeScroll.setWidget(uiTree);
		uiTreeScroll.addStyleName("fullborder");
		uiTreeScroll.setWidth("250px"); 
		uiTreeScroll.setHeight("400px");
		
		VerticalPanel vp = new VerticalPanel(); 
		vp.add(uiTreeScroll);
		vp.add(getButtons());
		setWidget(1, 0, vp);
		getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);

		setWidget(1, 1, uiContent); 
		getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
		getFlexCellFormatter().setRowSpan(1, 1, 2); 
		
		uiTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent<TreeItem> event) 
			{ selectItem(event.getSelectedItem(), false); }
		}); 
		
		uiTree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(OpenEvent<TreeItem> event) 
			{ loadChildren(event.getTarget(), 2); }
		}); 
	}
	
	private Widget getButtons()
	{
		uiExportExcel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ export(".xls", uiExportExcel); }
		});
		
		uiExportCsv.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ export(".csv", uiExportCsv); }
		}); 
	
		uiResetSort.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) 
			{ setSort(getDefaultSort(), -1); }
		}); 
		uiResetSort.setTitle("Sort by hierarchy");
		
		uiExportExcel.setVisible(false);
		uiExportCsv.setVisible(false);
		uiResetSort.setVisible(false);
			
		HorizontalPanel hp = new HorizontalPanel(); 
		hp.setSpacing(2);
		hp.add(uiExportCsv);
		hp.add(uiExportExcel);
		hp.add(uiResetSort);

		return hp; 
	}

    private void export(String ext, Widget anchor)
    {
        DownloadPanel download = new DownloadPanel(new Label("Please wait while the file with the validation result is prepared."), "Validation Result", anchor);
        toolbox.getService().saveValidationResult(validationResult, ext, download.getDownloadCallback(null));
    }
	
	private void selectItem(TreeItem item, boolean selectInTree)
	{
		if (selectInTree)
			uiTree.setSelectedItem(item);

		loadChildren(item, 2); 

		updateContent((EmisEntity) item.getUserObject()); 
	}
	
	private void setSort(Comparator<EmisValidationResultItem> sort, int col)
	{
		currentSort = sort; 
		currentSortColumn = col; 
		uiResetSort.setVisible(col != -1);

		updateContent((EmisEntity) uiTree.getSelectedItem().getUserObject());
	}
	
	private void setTextWithSort(int row, final int col, String text, final Comparator<EmisValidationResultItem> comparator)
	{
        ClickHandler handler = new ClickHandler() {
            public void onClick(ClickEvent event)
            {
            	Comparator<EmisValidationResultItem> sort = comparator; 
            	if (currentSortColumn == col && !(currentSort instanceof InverseComparator))
            		sort = new InverseComparator(sort); 

            	setSort(sort, col); 
            }
        };

        boolean inverse = currentSort instanceof InverseComparator;
        Image img = new Image(currentSortColumn == col && !inverse ? "css/down.png" : "css/up.png");
        img.addClickHandler(handler);

        Label label = new Label(text);
        label.addClickHandler(handler);

		HorizontalPanel hp = new HorizontalPanel();
        hp.add(label);
        hp.add(img);
        hp.setSpacing(2);

        uiContent.setWidget(row, col, hp); 
        if (currentSortColumn == col)
        	uiContent.getCellFormatter().addStyleName(row, col, "prioHeaderSelected");
	}
	
	private void loadChildren(TreeItem item, int depth)
	{
		if (item.getChildCount() == 0)
		{
			// Load children
			List<EmisEntity> entities = validationResult.getChildren((EmisEntity) item.getUserObject());  
			Collections.sort(entities, new Comparator<EmisEntity>() {
				@Override
				public int compare(EmisEntity entity1, EmisEntity entity2) 
				{ return entity1.getName().compareTo(entity2.getName()); }
			}); 
			
			for (EmisEntity entity : entities)
			{
				TreeItem tmp= GwtUtils.getTreeItem(entity.getName());
				item.addItem(tmp); 
				tmp.setUserObject(entity); 
			}
		}
		
		if (depth > 1)
		{
			for (int i = 0; i < item.getChildCount(); i++)
				loadChildren(item.getChild(i), depth - 1); 
		}
	}

	private Comparator<EmisValidationResultItem> getDefaultSort()
	{
		return new Comparator<EmisValidationResultItem>() {
			@Override
			public int compare(EmisValidationResultItem item1, EmisValidationResultItem item2) 
			{ return ValidationResultBrowser.this.compare(item1.getEntities(), item2.getEntities()); }
		};
	}
	
	private Comparator<EmisValidationResultItem> getAdditionalValueSort(final int index)
	{
		return new Comparator<EmisValidationResultItem>() {
			@Override
			public int compare(EmisValidationResultItem item1, EmisValidationResultItem item2) 
			{ return ValidationResultBrowser.this.compare(item1.getAdditionalValues(), item2.getAdditionalValues(), index); }
		}; 
	}
	
	private Comparator<EmisValidationResultItem> getResultSort(final int index)
	{
		return new Comparator<EmisValidationResultItem>() {
			@Override
			public int compare(EmisValidationResultItem item1, EmisValidationResultItem item2) 
			{ return ValidationResultBrowser.this.compareResults(item1.getResults(), item2.getResults(), index); }
		};
	}
	
	private void updateContent(EmisEntity contextEntity)
	{
		uiExportCsv.setVisible(true);
		uiExportExcel.setVisible(true);

		while (uiContent.getRowCount() > 0)
			uiContent.removeRow(0);
		
 		List<EmisValidationResultItem> items = validationResult.getItems(contextEntity);
		Collections.sort(items, currentSort);

		int row = 0; 

		// Output headers
		//
		EmisEntity[] lastEntities = new EmisEntity[items.get(0).getEntities().length]; 
		List<EmisMetaData> additionalFields = validationResult.getAdditionalFields(); 
		
		int col = 0; 
		for (EmisEntity entity : items.get(0).getEntities())
		{
			uiContent.setText(row, col, entity.getEntityType().getName());
			col++; 
		}

		int startCol = col; 
		for (EmisMetaData field : additionalFields)
		{
			setTextWithSort(row, col, field.getName(), getAdditionalValueSort(col - startCol)); 
			col++; 
		}
		
		startCol = col; 
		for (EmisValidationRule rule : validationResult.getRules())
		{
			setTextWithSort(row, col, rule.getName(), getResultSort(col - startCol));
			col++; 
		}

		uiContent.getRowFormatter().addStyleName(row, "prioHeader");
		row++; 
		
		// Output items
		//
		for (EmisValidationResultItem item : items)
		{
			EmisEntity[] entities = item.getEntities();
			boolean contextEquals = true; 
			for (int i = 0; i < entities.length - 1; i++)
				contextEquals = contextEquals && entities[i].equals(lastEntities[i]); 
			
			for (col = 0; col < entities.length - 1; col++)
			{
				if (!contextEquals)
					uiContent.setText(row, col, entities[col].getName());
			}
			
			uiContent.setText(row,  col, entities[col].getName() + " (" + entities[col].getId() + ")"); 
			col++; 

			lastEntities = entities; 
		
			String[] additionalValues = item.getAdditionalValues(); 
			for (int i = 0; i < additionalFields.size(); i++)
			{
				if (additionalValues == null || i >= additionalValues.length)
				{
					col++; 
					continue;
				}
				
				uiContent.setText(row, col, additionalValues[i]);
				col++; 
			}
			
			for (String result : item.getResults())
			{
				uiContent.setText(row, col, result); 
				col++; 
			}

			row++; 
		}
	}
	
	private int compare(EmisEntity[] entities1, EmisEntity[] entities2)
	{
		for (int i = 0; i < entities1.length; i++) 
		{
			int value = entities1[i].getName().compareTo(entities2[i].getName());  
			if (value != 0)
				return  value; 
		}
		
		return 0;
	}
	
	private int compare(String[] item1, String[] item2, int index)
	{
		String s1 = getValue(item1, index); 
		String s2 = getValue(item2, index); 

		if (s1 == null && s2 == null)
			return 0; 
		
		if (s1 == null)
			return -1; 
		
		if (s2 == null)
			return 1; 
		
		return s1.compareTo(s2); 
	}
	
	private int compareResults(String[] item1, String[] item2, int index)
	{
		double d1 = getResultValue(item1, index); 
		double d2 = getResultValue(item2, index); 
		
		if (d1 == d2)
			return 0; 
		
		return d1 < d2 ? -1 : 1; 
	}

	private double getResultValue(String[] items, int index)
	{
		String value = getValue(items, index); 
		if (value == null)
			return Double.MIN_NORMAL; 
		
		int pos = value.indexOf("/"); 
		if (pos == -1)
			return Integer.parseInt(value); 
		
		int end = value.indexOf("/", pos + 1); 
		if (end == -1)
			end = value.length();
		
		int i1 = Integer.parseInt(value.substring(0, pos)); 
		int i2 = Integer.parseInt(value.substring(pos + 1, end)); 
		if (i2 == 0)
			return Double.MAX_VALUE; 
		
		return (double) i1 / (double) i2; 
	}
	
	private String getValue(String[] items, int index)
	{
		if (items == null)
			return null; 
		
		if (index < items.length)
			return items[index]; 
		
		return null; 
	}
	
	public void set(EmisValidationResult validationResult)
	{ 
		this.validationResult = validationResult; 

		uiTree.clear(); 
		for (EmisEntity entity : validationResult.getChildren(null))
		{
			TreeItem tmp = GwtUtils.getTreeItem(entity.getName());
			uiTree.addItem(tmp); 
			tmp.setUserObject(entity);
		}
		
		if (uiTree.getItemCount() > 0)
			selectItem(uiTree.getItem(0), true); 
	}
}

class InverseComparator implements Comparator<EmisValidationResultItem>
{
	private Comparator<EmisValidationResultItem> comparator; 
	
	public InverseComparator(Comparator<EmisValidationResultItem> comparator)
	{ this.comparator = comparator; }
	
	@Override
	public int compare(EmisValidationResultItem item1, EmisValidationResultItem item2) 
	{ return -1 * comparator.compare(item1, item2); }
}
