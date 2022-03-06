package com.kenny.jecs.collection;

import java.util.List;

/**
 * Represents a group or sequence of components.
 *
 * @param <Component> The Component Type. This is starting hierarchy of component, by default set by Object.
 * You can use any object as a beginning. 
 */
public interface ComponentSequence<Component extends Object> 
	extends List<Component> 
{

	/**
	 * Emplace (place in future) to {@link ComponentSequence} component data structure.
	 *
	 * @param component - data structure that will be added.
	 */
	boolean emplace(Component component);
	
	/**
	 * Emplace to {@link ComponentSequence} component data structure.
	 *
	 * @param index - serial index of component in sequence.
	 * @param component - data structure that will be added.
	 */
	void emplace(int index, Component component);
	
	/**
	 * Erase from {@link ComponentSequence} component data structure.
	 * 
	 * @param number - serial number of component in sequence.
	 * @param component - data structure that will be removed from.
	 */
	boolean erase(Component component);
	
	/**
	 * Erase from {@link ComponentSequence} component data structure.
	 * 
	 * @param index - serial number of component in sequence.
	 */
	Component erase(int index);

	/**
	 * Return always true because this is {@link ComponentSequence}.
	 */
	boolean isComponent();
};
