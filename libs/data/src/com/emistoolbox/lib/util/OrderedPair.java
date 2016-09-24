package com.emistoolbox.lib.util;


public class OrderedPair<S,T> {
	public S s;
	public T t;
	
	
	public OrderedPair (S s,T t) {
		this.s = s;
		this.t = t;
	}
	
	public boolean equals (Object o) {
		if (!(o instanceof OrderedPair))
			return false;
		OrderedPair<S,T> pair = (OrderedPair<S,T>) o;
		return pair.s.equals (s) && pair.t.equals (t);
	}
	
	public int hashCode () {
		return s.hashCode () + t.hashCode ();
	}
	
	public String toString () {
		return "<" + s + "," + t + ">";
	}
}
