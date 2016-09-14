package com.emistoolbox.common;

import java.util.ArrayList;
import java.util.List;

public class CombinationUtil 
{
	public static <T> List<List<T>> getCombinations(List<List<T>> items)
	{
		List<List<T>> result = new ArrayList<List<T>>(); 
		getCombinations(items, result, 0, new ArrayList<T>());
		
		return result;
	}
	
	private static <T> void getCombinations(List<List<T>> items, List<List<T>> results, int depth, List<T> current)
	{
		if (depth == items.size())
		{
			results.add(new ArrayList<T>(current));
			return; 
		}
		
		for (int i = 0; i < items.get(depth).size(); i++)
		{
			current.add(items.get(depth).get(i)); 
			getCombinations(items, results, depth + 1, current); 
			current.remove(current.size() - 1); 
		}
	}
}
