package kenny.jecs.collection;

import java.util.Map;

/**
 * Represents a group of entities assissiated with theirs components, where components
 * can be modified and store data.
 * 
 * @param <K> is any {@link Number} contains definition numbers is represents entity identifier.
 * @param <V> is any sequence of components data objects. Where data can be any {@link java.lang.Object}.
 */
public interface EntityContainer
			<K extends Number, 
			 V extends ComponentSequence<? extends Object>> 
	extends Map<K, V>
{
	/**
	 * Emplace to {@link EntityContainer} entity identifier with assissiated
	 * components map. 
	 * 
	 * @param entity - entity definition identifier.
	 * @param components - definition of array of components-data for that entity.
	 */
	V emplace(K entity, V components);
	
	/**
	 * Emplace other {@link EntityContainer} to this.
	 * 
	 * @param other - other entity map.
	 */
	void emplace(EntityContainer<? extends K, ? extends V> other);
	
	/**
	 * Erase from {@link EntityContainer} this entity assissiated with all components.
	 * 
	 * @param entity - entity definition identifier.
	 * @param components - definition of array of components-data for that entity.
	 */
	boolean erase(K entity, V components);
	
	/**
	 * Erase from {@link EntityContainer} this entity assissiated with all components.
	 * 
	 * @param entity - entity definition identifier.
	 */
	V erase(K entity);
	
	/**
	 * Swap two entities.
	 */
	void swap(K first, K second);
	
	/**
	 * Return always true because this is {@link EntityContainer}.
	 */
	boolean isEntity();
};
