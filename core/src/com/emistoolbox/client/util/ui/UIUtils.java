package com.emistoolbox.client.util.ui;

import com.google.gwt.user.client.ui.ListBox;

public class UIUtils 
{
	public static void listBoxInit(ListBox lb, Object[] values)
	{
		lb.clear(); 
		for (Object value : values)
		{
			if (value != null)
				lb.addItem(value.toString(), value.toString());
		}
	}
	
	public static String getListBoxValue(ListBox lb)
	{
		int i = lb.getSelectedIndex();
		if (i == -1)
			return null; 
		
		return lb.getValue(i); 
	}
	
	public static void setListBoxValue(ListBox lb, String value)
	{
		for (int i = 0; i < lb.getItemCount(); i++)
			if (lb.getValue(i).equals(value))
			{
				lb.setSelectedIndex(i);
				return; 
			}
	}
}
