package com.emistoolbox.lib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.emistoolbox.lib.util.OrderedPair;

public class BipartiteGraph<S,T> extends HashSet<OrderedPair<S,T>> {
	public Map<S,List<T>> getHeadMap () {
		Map<S,List<T>> map = new HashMap<S,List<T>> ();
		for (OrderedPair<S,T> pair : this) {
			List<T> list = map.get (pair.s);
			if (list == null) {
				list = new ArrayList<T> ();
				map.put (pair.s,list);
			}
			list.add (pair.t);
		}
		return map;
	}

	public Map<T,List<S>> getTailMap () {
		Map<T,List<S>> map = new HashMap<T,List<S>> ();
		for (OrderedPair<S,T> pair : this) {
			List<S> list = map.get (pair.t);
			if (list == null) {
				list = new ArrayList<S> ();
				map.put (pair.t,list);
			}
			list.add (pair.s);
		}
		return map;
	}
	
	public void assertUniqueness () {
		assertUniqueness (getHeadMap ());
		assertUniqueness (getTailMap ());
	}
	
	public static <A,B> void assertUniqueness (Map<A,List<B>> map) {
		for (List<B> value : map.values ())
			if (value.size () > 1)
				throw new Error ();
	}
	
	public static <A,B> Map<A,B> uniquify (Map<A,List<B>> map) {
		Map<A,B> uniqueMap = new HashMap<A,B> ();
		for (Entry<A,List<B>> entry : map.entrySet ()) {
			if (entry.getValue ().size () > 1)
				throw new Error ();
			uniqueMap.put (entry.getKey (),entry.getValue ().get (0));
		}
		return uniqueMap;
	}
}
