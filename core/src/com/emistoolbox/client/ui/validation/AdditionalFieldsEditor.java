package com.emistoolbox.client.ui.validation;

import java.util.ArrayList;
import java.util.List;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AdditionalFieldsEditor extends FlexTable implements EmisEditor<List<EmisMetaData>>
{
	private PushButton btnAdd = new PushButton("Add"); 
	private PushButton btnDel = new PushButton("Del"); 
	private PushButton btnUp = new PushButton("Up"); 
	private PushButton btnDown = new PushButton("Down"); 
	
	private ListBoxWithUserObjects<EmisMetaData> uiFields = new ListBoxWithUserObjects<EmisMetaData>(); 

	private List<EmisMetaEntity> entityTypes; 
	
	public AdditionalFieldsEditor()
	{
		uiFields.setVisibleItemCount(10);
		uiFields.setWidth("200px");
		
		EmisUtils.init(btnAdd, 50);
		EmisUtils.init(btnDel, 50);
		EmisUtils.init(btnUp, 50);
		EmisUtils.init(btnDown, 50);

		VerticalPanel vp = new VerticalPanel(); 
		vp.add(btnAdd);
		vp.add(btnDel);
		vp.add(btnUp);
		vp.add(btnDown); 
		
		getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
		setWidget(0, 0, uiFields); 
		setWidget(0, 1, vp); 
		
		uiFields.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) 
			{ updateButtons(); }
		}); 
		
		btnAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ showAddDialog(); }  
		}); 

		btnDel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				int index = uiFields.getSelectedIndex(); 
				if (index != -1)
					uiFields.removeItem(index);
				
				updateButtons(); 
			}
		}); 

		btnUp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				uiFields.moveUp(); 
				updateButtons(); 
			} 
		}); 

		btnDown.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{
				uiFields.moveDown(); 
				updateButtons(); 
			} 
		}); 
	}

	public void updateEntities(List<EmisMetaEntity> entityTypes)
	{ 
		this.entityTypes = entityTypes; 

		int index = 0; 
		while (index < uiFields.getItemCount())
		{
			EmisMetaData field = uiFields.getUserObject(index); 
			if (field != null && -1 == NamedUtil.findIndex(field.getEntity(), entityTypes))
				uiFields.removeItem(index);
			else
				index++;
		}
	}
	
	private void showAddDialog()
	{
		FlexTable table = new FlexTable();
		final PopupPanel popup = new PopupPanel(); 
		
		for (int col = 0; col < entityTypes.size(); col++) 
		{
			EmisMetaEntity type = entityTypes.get(col); 
			table.setHTML(0, col, type.getName());
			table.getFlexCellFormatter().setStylePrimaryName(0, col, "title");

			int row = 1; 
			for (final EmisMetaData field : type.getData())
			{
				if (field.getArrayDimensions() != null)
					// We can only display single values 
					continue; 
				
				Label lbl = new Label(field.getName());
				lbl.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						uiFields.addItem(getName(field), field);
						popup.hide(); 
					}
				}); 
				
				table.setWidget(row, col, lbl); 
				row++; 
			}	
		}
		
		popup.setWidget(table);
		popup.setModal(true);
		popup.setAutoHideEnabled(true);
		popup.showRelativeTo(btnAdd);
	}
	
	private String getName(EmisMetaData field)
	{ return field.getEntity().getName() + ": " + field.getName(); }
	
	@Override
	public void commit() 
	{}

	@Override
	public List<EmisMetaData> get() 
	{
		List<EmisMetaData> result = new ArrayList<EmisMetaData>(); 
		for (int i = 0; i < uiFields.getItemCount(); i++) 
			result.add(uiFields.getUserObject(i));  
		
		return result; 
	}

	@Override
	public void set(List<EmisMetaData> fields) 
	{
		uiFields.clear();
		for (EmisMetaData field : fields)
			uiFields.addItem(getName(field), field);

		updateButtons(); 
	}
	
	private void updateButtons()
	{
		int index = uiFields.getSelectedIndex(); 
		btnDel.setEnabled(index != -1); 
		btnUp.setEnabled(index > 0);
		btnDown.setEnabled(index != -1 && index < uiFields.getItemCount() - 1);
	}
}
