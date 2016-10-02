package com.emistoolbox.client.ui.pdf;

import java.util.Arrays;

import com.emistoolbox.client.EmisEditor;
import com.google.gwt.user.client.ui.ListBox;

public class IntPicker extends ListBox implements EmisEditor<Integer>
{
	int[] values; 
	
	public IntPicker(int[] values)
	{ this(values, "", ""); } 
	
	public IntPicker(int[] values, String prefix, String postfix)
	{
		Arrays.sort(values);
		this.values = values; 
		
		for (int i : values)
			addItem(prefix + i + postfix); 
	}

	@Override
	public void commit() 
	{}

	@Override
	public Integer get() 
	{
		int index = getSelectedIndex(); 
		if (index == -1)
			return null; 
		return values[index]; 
	}

	@Override
	public void set(Integer value) 
	{
		if (value == null)
			value = values[0]; 
		
		int index = Arrays.binarySearch(values, value);
		if (index == -1)
			index = searchBestValue(value); 
		
		setSelectedIndex(index); 
	}
	
	private int searchBestValue(int value)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (value == values[i])
				return i; 
			
			if (value < values[i])
			{
				if (i == 0)
					return 0; 
				
				if (Math.abs(value - values[i - 1]) < Math.abs(value - values[i]))
					return i - 1; 
				else
					return i; 
			}
		}
		
		return values.length - 1; 
	}
}
