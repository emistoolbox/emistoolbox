package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.common.model.mapping.DataSourceReplace;
import com.emistoolbox.common.model.mapping.impl.DataSourceReplaceImpl;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ReplaceUI extends FlexTable implements EmisEditor<DataSourceReplace> 
{
	private int newRow = 1; 

	@Override
	public void commit() 
	{
		setText(0, 0, "Column");
		setText(0, 1, "Find"); 
		setText(0, 2, "Replace");

		show(newRow, "", "", ""); 
	}
	
	@Override
	public DataSourceReplace get() 
	{
		DataSourceReplace result = new DataSourceReplaceImpl(); 
		for (int i = 1; i < getRowCount(); i++)
		{
			String col = getString(i, 0); 
			String needle = getString(i, 1); 
			String replace = getString(i, 2); 
			if (col == null || needle == null || replace == null)
				continue;
			
			
		}
		
		return result; 
	}
	
	private void show(int row, String col, String needle, String replacement)
	{
		setString(row, 0, col); 
		setString(row, 1, needle); 
		setString(row, 2, replacement); 
	}

	private void setString(int row, int col, String value)
	{
		TextBox tb = new TextBox(); 
		if (value != null)
			tb.setText(value);
		
		setWidget(row, col, tb); 
	}
	
	private String getString(int row, int col)
	{
		Widget w = getWidget(row, col);
		if (w instanceof TextBox)
		{
			String result = ((TextBox) w).getText();
			if ("".equals(result))
				return null; 

			return result; 
		}

		return null; 
	}

	@Override
	public void set(DataSourceReplace replace) 
	{
		
	}
}
