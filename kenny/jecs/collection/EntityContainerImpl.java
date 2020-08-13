package kenny.jecs.collection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is actually implementation of {@link EntityContainer}.
 * 
 * @param <K> is any {@link Number} contains entity (entity identifier).
 * @param <V> is any sequence of components data structure.
 */
public class EntityContainerImpl
			<K extends Number, 
			V extends ComponentSequence<? extends Object>> 
	extends LinkedHashMap<K, V> 
	implements EntityContainer<K, V>, Cloneable, Serializable, Comparable<EntityContainerImpl<K, V>>
{
	private static final long serialVersionUID = 1L;

	@Override public V emplace(K entity, V components) 
	{
		return put(entity, components);
	}

	@Override public void emplace(EntityContainer<? extends K, ? extends V> other) 
	{
		putAll(other);
	}

	@Override public boolean erase(K entity, V components) 
	{
		return remove(entity, components);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        @SuppressWarnings("unchecked")
		Map<K,V> m = (Map<K,V>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key)==null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
	}

	@Override public V erase(K entity)
	{
		return remove(entity);
	}
    
	@Override public boolean isEntity() 
	{
		return true;
	}
    
    @Override public void swap( K first, K second ) 
	{
		K a = first;
		K b = second;
		first = b;
		second = a;
	}
    
	@Override
	public int compareTo(EntityContainerImpl<K, V> o) 
	{
		return equals(o) ? 1 : 0;
	}
}