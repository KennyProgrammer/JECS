package kenny.jecs.collection;

import java.util.Iterator;

public class Pair<A, B> implements Iterable<A>
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
	public Iterator<A> iterator() {
		return null;
	}
	
	public Iterator<B> iteratorB() {
		return null;
	}

	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}

}
