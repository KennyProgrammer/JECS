package kenny.jecs.collection;

/**
 * Non generic version of {@link Pair}.
 */
public class RawPair
{
	public Object first;
	public Object second;
	
	public RawPair() {
		
	}
	
	public RawPair(Object first, Object second) {
		set(first, second);
	}
	
	public RawPair(Short first, Object second) {
		set(first, second);
	}
	
	public RawPair(Integer first, Object second) {
		set(first, second);
	}
	
	public RawPair(Long first, Object second) {
		set(first, second);
	}
	
	public void set(Integer first, Object second) {
		this.first = first;
		this.second = second;
	}
	
	public void set(Short first, Object second) {
		this.first = first;
		this.second = second;
	}
	
	public void set(Long first, Object second) {
		this.first = first;
		this.second = second;
	}
	
	public void set(Object first, Object second) {
		this.first = first;
		this.second = second;
	}
	
	public Object getFirst() {
		return first;
	}
	
	public Number getFirstAsNumber() {
		return (Number) first;
	}
	
	/**
	 * Return first as int if first passed as EntityT where EntityT is Integer.
	 */
	public Integer getFirstAsInteger() {
		return (Integer) first;
	}
	
	/**
	 * Return first as short if first passed as EntityT where EntityT is Short.
	 */
	public Short getFirstAsShort() {
		return (Short) first;
	}
	
	/**
	 * Return first as long if first passed as EntityT where EntityT is Long.
	 */
	public Long getFirstAsLong() {
		return (Long) first;
	}
	
	public Object getSecond() {
		return second;
	}
	
	public void setFirst(Object first) {
		this.first = first;
	}
	
	public void setSecond(Object second) {
		this.second = second;
	}
	
	@Override
	public String toString() {
		return "[" + first + ", " + second + "]";
	}
}
