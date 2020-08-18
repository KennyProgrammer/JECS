package kenny.jecs.collection;

import java.util.TreeMap;

public class Pair<K, V extends K> 
	extends TreeMap<K, V> 
	implements Comparable<Pair<K, V>>
{
	private static final long serialVersionUID = 1L;

	/*
	 * Emplace new pair.
	 */
	public V emplace(K first, V second) 
	{
		return put(first, second);
	}

	/*
	 * Erase this pair.
	 */
	public boolean erase(K first, V second) 
	{
		return remove(first, second);
	}
	
	/**
	 * Return this pair of objects in <code>Object[]</code>.
	 */
	@SuppressWarnings("unchecked")
	public Object[] getPair()
	{
		K k = (K) new Object();
		V v = (V) new Object();
		return new Object[] {k, v};
	}


	@Override
	public int compareTo(Pair<K, V> o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
