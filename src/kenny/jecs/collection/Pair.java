package kenny.jecs.collection;


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
	
	//@Override
	//public Iterator<A> iterator() {
	//	return null;
	//}
	//
	//public Iterator<B> iteratorB() {
	//	return null;
	//}////

	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}

	@Override
	public int compareTo(A o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
