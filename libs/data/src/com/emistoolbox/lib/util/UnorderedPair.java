package com.emistoolbox.lib.util;

public class UnorderedPair<T> {
	public T t1,t2;
	
	public UnorderedPair (T t1,T t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public boolean equals (Object o) {
		if (!(o instanceof UnorderedPair))
			return false;
		UnorderedPair<T> pair = (UnorderedPair<T>) o;
		return pair.t1.equals (t1) && pair.t2.equals (t2) || pair.t2.equals (t1) && pair.t1.equals (t2);
	}
	
	public int hashCode () {
		return t1.hashCode () + t2.hashCode ();
	}
	
	public String toString () {
		return "(" + t1 + "," + t2 + ")";
	}
}