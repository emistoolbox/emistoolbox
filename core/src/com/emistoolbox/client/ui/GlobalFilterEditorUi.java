package com.emistoolbox.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.ui.analysis.BoolFilterEditor;
import com.emistoolbox.client.ui.analysis.EnumSetEditor;
import com.emistoolbox.common.CombinationUtil;
import com.emistoolbox.common.model.EmisEnumSet;
import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.EmisEnumValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.analysis.impl.Context;
import com.emistoolbox.common.model.impl.EnumSetImpl;
import com.emistoolbox.common.model.impl.EnumValueImpl;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.util.NamedUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class GlobalFilterEditorUi extends FlexTable implements EmisEditor<EmisContext>
{
	private PushButton btnAddDateFilter = new PushButton(Message.messageReport().globalFilterAdd()); 
	private DropDownSelect uiDateFilterDropDown = new DropDownSelect(); 
	private FlexTable uiDateFilters = new FlexTable(); 
	
	private PushButton btnAddEnumFilter = new PushButton(Message.messageReport().globalFilterAdd()); 
	private DropDownSelect uiEnumFilterDropDown = new DropDownSelect(); 
	private FlexTable uiEnumFilters = new FlexTable();

	private PushButton btnAddEntityFilter = new PushButton(Message.messageReport().globalFilterAdd()); 
	private DropDownSelect uiEntityFilterDropDown = new DropDownSelect(); 
	private FlexTable uiEntityFilters = new FlexTable(); 
	
	private EmisMeta meta; 

	private EmisContext context; 
	
	public GlobalFilterEditorUi(EmisMeta meta)
	{
		this.meta = meta; 

		for (String name : getDateFilterNames())
			uiDateFilterDropDown.addItem(name, name); 
			
		EmisUtils.initSmall(btnAddDateFilter, 80);
		uiDateFilterDropDown.setWidget(btnAddDateFilter);
		btnAddDateFilter.addClickHandler(uiDateFilterDropDown.getClickHandler()); 
		uiDateFilterDropDown.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) 
			{
				if (event.getValue() != -1)
					addDateFilter(uiDateFilterDropDown.getValue(event.getValue()), null);  
			}
		}); 

		for (String name : getEnumFilterNames())
			uiEnumFilterDropDown.addItem(name, name); 

		EmisUtils.initSmall(btnAddEnumFilter, 80); 
		uiEnumFilterDropDown.setWidget(btnAddEnumFilter);
		btnAddEnumFilter.addClickHandler(uiEnumFilterDropDown.getClickHandler()); 
		uiEnumFilterDropDown.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) 
			{
				if (event.getValue() != -1)
					addEnumFilter(uiEnumFilterDropDown.getValue(event.getValue()));  
			}
		}); 
		
		for (String name : getEntityFilterNames())
			uiEntityFilterDropDown.addItem(name, name); 

		EmisUtils.initSmall(btnAddEntityFilter, 80); 
		uiEntityFilterDropDown.setWidget(btnAddEntityFilter);
		btnAddEntityFilter.addClickHandler(uiEntityFilterDropDown.getClickHandler()); 
		uiEntityFilterDropDown.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) 
			{
				if (event.getValue() != -1)
					addEntityFilter(uiEntityFilterDropDown.getValue(event.getValue()));  
			}
		}); 
		
		int row = 0; 
		setText(row, 0, Message.messageReport().globalFilterDates()); 
		getCellFormatter().setStyleName(row, 0, "prioHeader");
		getCellFormatter().setStyleName(row, 1, "prioHeader");
		getFlexCellFormatter().setWidth(row, 0, "400px"); 
		setWidget(row, 1, uiDateFilterDropDown); 
		row++; 

		getFlexCellFormatter().setColSpan(row, 0, 2);
		setWidget(row, 0, uiDateFilters);
		updateNoneText(uiDateFilters, true); 
		row++; 

		setText(row, 0, Message.messageReport().globalFilterEnums()); 
		getCellFormatter().setStyleName(row, 0, "prioHeader");
		getCellFormatter().setStyleName(row, 1, "prioHeader");
		setWidget(row, 1, uiEnumFilterDropDown); 
		row++; 

		getFlexCellFormatter().setColSpan(row, 0, 2);
		setWidget(row, 0, uiEnumFilters);
		updateNoneText(uiEnumFilters, true); 
		row++; 
		
		setText(row, 0, Message.messageReport().globalFilterEntities()); 
		getCellFormatter().setStyleName(row, 0, "prioHeader");
		getCellFormatter().setStyleName(row, 1, "prioHeader");
		setWidget(row, 1, uiEntityFilterDropDown); 
		row++; 

		getFlexCellFormatter().setColSpan(row, 0, 2);
		setWidget(row, 0, uiEntityFilters);
		updateNoneText(uiEntityFilters, true); 
		row++; 
	}

	@Override
	public void commit() 
	{
		context = new Context();
		
		List<List<EmisEnumValue>> dateFilters = new ArrayList<List<EmisEnumValue>>(); 
		for (EnumSetEditor editor : getEnumSetEditors(uiDateFilters, 1))
		{
			List<EmisEnumValue> enumValues = new ArrayList<EmisEnumValue>(); 
			
			EmisEnumSet enumSet = editor.get(); 
			if (enumSet != null && !enumSet.hasAllValues())
				context.addDateEnumFilter(enumSet);

			dateFilters.add(enumValues);
		}

		for (EnumSetEditor editor : getEnumSetEditors(uiEnumFilters, 1))
		{
			EmisEnumSet value = editor.get();
			if (value != null && !value.hasAllValues())
				context.addEnumFilter(value); 
		}
		
		for (EnumSetEditor editor : getEnumSetEditors(uiEntityFilters, 1))
		{
			EmisEnumSet value = editor.get();
			if (value != null && !value.hasAllValues())
				context.addEnumEntityFilter(editor.getMetaData(), value); 
		}
		
		for (BoolFilterEditor editor : getBoolFilterEditors(uiEntityFilters, 1))
		{
			Boolean value = editor.get(); 
			if (value != null)
				context.addBooleanEntityFilter(editor.getMetaData(), value); 
		}
	}

//	private List<EmisEnumTupleValue> getDatesFromParts(List<List<EmisEnumValue>> datePartsSet)
//	{
//		List<EmisEnumTupleValue> result = new ArrayList<EmisEnumTupleValue>(); 
//		for (List<EmisEnumValue> dateParts : datePartsSet)
//		{
//			EmisEnumTupleValue dt = getDateFromParts(dateParts);
//			if (dt != null)
//				result.add(dt); 
//		}
//		
//		return result; 
//	}
//	
//	private EmisEnumTupleValue getDateFromParts(List<EmisEnumValue> dateParts)
//	{
//		EmisMetaDateEnum enumType = null;
//		for (EmisEnumValue datePart : dateParts)
//		{ 
////			if (enumType == null || enumType.getDimensions(). || enumType.getDimensions().)
//		}
//		
//		return null; // jb2do
//	}
//	
	private List<EnumSetEditor> getEnumSetEditors(FlexTable table, int col)
	{
		List<EnumSetEditor> result = new ArrayList<EnumSetEditor>(); 
		for (int row = 0; row < table.getRowCount(); row++)
		{
			if (table.getCellCount(row) > col && table.getWidget(row, col) instanceof EnumSetEditor)
				result.add((EnumSetEditor) table.getWidget(row, col)); 
		}

		return result; 
	}

	private List<BoolFilterEditor> getBoolFilterEditors(FlexTable table, int col)
	{ 
		List<BoolFilterEditor> result = new ArrayList<BoolFilterEditor>(); 
		for (int row = 0; row < table.getRowCount(); row++)
		{
			if (table.getCellCount(row) > col && table.getWidget(row, col) instanceof BoolFilterEditor)
				result.add((BoolFilterEditor) table.getWidget(row, col)); 
		}

		return result; 
	}

	@Override
	public EmisContext get() 
	{
		commit(); 
		return context;
	}

	@Override
	public void set(EmisContext newContext) 
	{
		uiDateFilters.removeAllRows(); 
		uiEnumFilters.removeAllRows(); 
		uiEntityFilters.removeAllRows(); 

		if (newContext == null)
			context = new Context(); 
		else
			context = newContext; 

		if (context.getDateEnumFilters() != null)
			for (Map.Entry<String, EmisEnumSet> entry : context.getDateEnumFilters().entrySet())
			{
				EmisMetaDateEnum dateEnumType = NamedUtil.find(entry.getKey(), meta.getDateEnums());
				if (dateEnumType == null)
					continue; 
					
				addDateFilter(dateEnumType.getName(), entry.getValue()); 
			}
		
		if (context.getEnumFilters() != null)
			for (Map.Entry<String, EmisEnumSet> entry : context.getEnumFilters().entrySet())
			{
				EmisMetaEnum enumType = NamedUtil.find(entry.getKey(), meta.getEnums());
				if (enumType == null)
					continue; 
				
				addEnumFilter(enumType, entry.getValue()); 
			}

		for (EmisMetaEntity entityType : meta.getEntities())
		{
			for (String fieldName : context.getEntityFilterNames(entityType))
			{
				EmisMetaData field = NamedUtil.find(fieldName, entityType.getData());  
				if (field == null)
					continue; 
				
				addEntityFilter(field, context.getEntityFilterValues(field)); 
			}
		}
		
		updateNoneText(uiDateFilters, true); 
		updateNoneText(uiEnumFilters, true); 
		updateNoneText(uiEntityFilters, true); 
	}
	
	private List<String> getEntityFilterNames()
	{
		List<String> result = new ArrayList<String>(); 
		
		for (EmisMetaEntity entityType : meta.getEntities())
		{
			for (EmisMetaData field : entityType.getData())
			{
				if (field.getType() == EmisDataType.BOOLEAN || field.getType() == EmisDataType.ENUM)
					result.add(entityType.getName() + "." + field.getName());
			}
		}
		
		return result; 
	}
	
	private void addEntityFilter(String filterName)
	{
		EmisMetaData field = findField(filterName);
		if (field == null)
			return; 
		
		if (field.getArrayDimensions() != null && field.getArrayDimensions().getDimensions() != 1)
			return; 
		
		if (field.getType() != EmisDataType.BOOLEAN && field.getType() != EmisDataType.ENUM)
			return; 
		
		addEntityFilter(field, null); 
	}
	
	private void addEntityFilter(EmisMetaData field, byte[] values)
	{
		updateNoneText(uiEntityFilters, false); 

		int index = findIndex(getFieldName(field), uiEntityFilters); 
		if (index != -1)
			uiEntityFilters.removeRow(index); 
	
		Widget editor = null; 
		if (field.getType() == EmisDataType.BOOLEAN)
		{
			editor = new BoolFilterEditor(field); 	
			if (values != null && values.length > 0)
				((BoolFilterEditor) editor).set(values[0] == 1);
		}
		else if (field.getType() == EmisDataType.ENUM)
		{
			editor = new EnumSetEditor(field);
			if (values != null)
				((EnumSetEditor) editor).set(getEnumSet(field.getEnumType(), values));  
		}
		
		int row = uiEntityFilters.getRowCount(); 
		uiEntityFilters.setText(row, 0, getFieldName(field)); 
		uiEntityFilters.getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
		
		uiEntityFilters.setWidget(row, 1, editor); 
		uiEntityFilters.setWidget(row, 2, getDelButton(uiEntityFilters, getFieldName(field))); 
		
		updateNoneText(uiEntityFilters, true); 
	}
	
	private EmisMetaData findField(String name)
	{
		int pos = name.indexOf("."); 
		if (pos == -1)
			return null; 
		
		EmisMetaEntity entityType = NamedUtil.find(name.substring(0, pos), meta.getEntities());
		if (entityType == null)
			return null;
		
		return NamedUtil.find(name.substring(pos + 1), entityType.getData()); 
	}

	private List<String> getEnumFilterNames()
	{
		List<String> result = new ArrayList<String>(); 
		
		for (EmisMetaEnum enumType : meta.getEnums())
			result.add(enumType.getName()); 
		
		return result; 
	}
	
	private void addEnumFilter(String enumName)
	{
		EmisMetaEnum enumType = NamedUtil.find(enumName, meta.getEnums()); 
		if (enumType != null)
			addEnumFilter(enumType, null); 
	}
	
	private void addEnumFilter(EmisMetaEnum enumType, EmisEnumSet values)
	{
		updateNoneText(uiEnumFilters, false); 

		int index = findIndex(enumType.getName(), uiEnumFilters); 
		if (index != -1)
			uiEnumFilters.removeRow(index);

		int row = uiEnumFilters.getRowCount();
		uiEnumFilters.setText(row, 0, enumType.getName());
		uiEnumFilters.getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);

		EnumSetEditor editor = new EnumSetEditor(enumType);
		uiEnumFilters.setWidget(row, 1, editor); 
		uiEnumFilters.setWidget(row, 2, getDelButton(uiEnumFilters, enumType.getName())); 
		if (values != null)
			editor.set(values);

		updateNoneText(uiEnumFilters, true); 
	}

	private List<String> getDateFilterNames()
	{
		List<String> result = new ArrayList<String>(); 
		for (EmisMetaEnum enumType : meta.getDateEnums())
			result.add(enumType.getName()); 

		return result; 
	}
	
	private void addDateFilter(String dateName, EmisEnumSet values)
	{
		updateNoneText(uiDateFilters, false); 

		int index = findIndex(dateName, uiDateFilters); 
		if (index != -1)
			uiDateFilters.removeRow(index);

		int row = uiDateFilters.getRowCount();
		
		EmisMetaDateEnum dateEnumType = NamedUtil.find(dateName, meta.getDateEnums());
		if (dateEnumType != null)
		{
			EnumSetEditor editor = new EnumSetEditor(dateEnumType); 
			if (values != null)
				editor.set(values);
			uiDateFilters.setText(row, 0, dateName); 
			uiDateFilters.getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
			uiDateFilters.setWidget(row, 1, editor); 
			uiDateFilters.setWidget(row, 2, getDelButton(uiDateFilters, dateName)); 
		}
		
		updateNoneText(uiDateFilters, true); 
	}
	
	private int findIndex(String name, FlexTable grid)
	{
		for (int row = 0; row < grid.getRowCount(); row++) 
		{
			if (grid.getText(row, 0).equals(name))
				return row; 
		}
		
		return -1; 
	}
	
	private void updateNoneText(FlexTable table, boolean display)
	{
		if (display && table.getRowCount() == 0)
			table.setText(0, 0, Message.messageReport().globalFilterNone());
		
		if (!display && table.getRowCount() == 1 && table.getCellCount(0) == 1)
			table.removeRow(0);
	}
	
	private PushButton getDelButton(final FlexTable table, final String name)
	{
		PushButton btn = new PushButton(Message.messageReport().globalFilterDel()); 
		EmisUtils.initSmall(btn, 40); 
		btn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) 
			{ deleteFilter(table, name); }
		}); 
		
		return btn; 
	}
	
	private void deleteFilter(FlexTable table, String name)
	{
		int index = findIndex(name, table);
		if (index != -1)
		{
			updateNoneText(table, false);
			table.removeRow(index);
			updateNoneText(table, true); 
		}
	}
	
	private String getFieldName(EmisMetaData field)
	{ return field.getEntity().getName() + "." + field.getName(); } 

	public static EmisEnumSet getEnumSet(EmisMetaEnum enumType, byte[] indexes)
	{
		EmisEnumSet values = new EnumSetImpl(); 
		values.setEnum(enumType); 
		
		Set<Byte> byteSet = new HashSet<Byte>(); 
		for (byte index : indexes)
			byteSet.add(index); 
		values.setAllIndexes(byteSet);
		
		return values; 
	}
	
}
