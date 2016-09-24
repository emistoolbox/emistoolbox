package com.emistoolbox.lib.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Multiset<T> {
	Map<T,Integer> map = new HashMap<T,Integer> ();
	
	public void add (T t) {
		Integer count = map.get (t);
		if (count == null)
			count = 0;
		map.put (t,count + 1);
	}
	
	public T mostFrequentKey () {
		T t = null;
		int max = -1;
		for (Map.Entry<T,Integer> entry : map.entrySet ())
			if (entry.getValue () > max) {
				t = entry.getKey ();
				max = entry.getValue ();
			}
		return t;
	}

	public Set<T> keySet () {
		return map.keySet ();
	}
	
	public String toString () {
		return map.toString ();
	}
}
