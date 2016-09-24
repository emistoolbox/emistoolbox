package com.emistoolbox.lib.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.emistoolbox.lib.util.Multiset;
import com.emistoolbox.lib.util.OrderedPair;

// takes pairs of data that should ideally be in one-to-one correspondence but may be flawed
// either by isolated errors or by the correspondence not being one-to-one in some cases;
// determines the most likely correspondences in both directions
public class DataMatcher<S,T> {
	private List<OrderedPair<S,T>> rawData = new ArrayList<OrderedPair<S,T>> ();
	private Map<S,Multiset<T>> headMap = new HashMap<S,Multiset<T>> (); 
	private Map<T,Multiset<S>> tailMap = new HashMap<T,Multiset<S>> (); 

	public void add (S s,T t) {
		rawData.add (new OrderedPair<S,T> (s,t));
		add (headMap,s,t);
		add (tailMap,t,s);
	}

	private <A,B> void add (Map<A,Multiset<B>> map,A a,B b) {
		Multiset<B> counts = map.get (a);
		if (counts == null) {
			counts = new Multiset<B> ();
			map.put (a,counts);
		}
		counts.add (b);
	}

	public BipartiteGraph<S,T> getMatches () {
		BipartiteGraph<S,T> matches = new BipartiteGraph<S,T> ();
		for (Entry<S,Multiset<T>> entry : headMap.entrySet ())
			matches.add (new OrderedPair<S,T> (entry.getKey (),entry.getValue ().mostFrequentKey ()));
		for (Entry<T,Multiset<S>> entry : tailMap.entrySet ())
			matches.add (new OrderedPair<S,T> (entry.getValue ().mostFrequentKey (),entry.getKey ()));
		return matches;
	}

	public T getMostFrequentValueForHead (S s) {
		return headMap.get (s).mostFrequentKey ();
	}

	public S getMostFrequentValueForTail (T t) {
		return tailMap.get (t).mostFrequentKey ();
	}
	
	public List<OrderedPair<S,T>> getRawData () {
		return rawData;
	}

	public void assertUniqueness (String message) {
		for (Multiset<T> m : headMap.values ())
			if (m.keySet ().size () > 1)
				throw new Error (message);
		for (Multiset<S> m : tailMap.values ())
			if (m.keySet ().size () > 1)
				throw new Error (message);
	}
	
	public Map<S,Multiset<T>> getHeadMap () {
		return headMap;
	}

	public Map<T,Multiset<S>> getTailMap () {
		return tailMap;
	}
}
