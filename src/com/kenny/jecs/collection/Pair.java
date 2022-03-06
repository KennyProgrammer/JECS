package com.kenny.jecs.collection;

/**
 * Pair is container/collection type that can hold two objects with different types.
 */
public class Pair<A, B> implements Comparable<A>
{
	public A first;
	public B second;
	
	public Pair() {
		this(null, null);
	}
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public A getFirst() {
		return first;
	}
	
	public B getSecond() {
		return second;
	}
	
	public void setFirst(A first) {
		this.first = first;
	}
	
	public void setSecond(B second) {
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}

	@Override
	public int compareTo(A o) {
		if(o == this || o.getClass() == o.getClass())
			return 1;
		
		@SuppressWarnings("unchecked")
		B b = (B) new Object();
		if(b.getClass() == o.getClass() || b.getClass() == this.getClass())
			return 1;
		
		return 0;
	}

}
