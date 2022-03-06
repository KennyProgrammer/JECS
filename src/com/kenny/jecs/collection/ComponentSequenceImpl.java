package com.kenny.jecs.collection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is actually implementation of {@link ComponentSequenceImpl}.
 * 
 * @param <Component> The Component Type. This is starting hierarchy of component, by default set by Object.
 * You can use any object as a beginning. 
 */
public class ComponentSequenceImpl<Component> 
	extends ArrayList<Component> 
	implements ComponentSequence<Component>
{
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an empty sequence with an initial capacity of ten.
     */
	public ComponentSequenceImpl() { super(); }
	
	public ComponentSequenceImpl<Component> get(){
		return this;
	}
	
	/**
     * Constructs a sequence containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this sequence
     * @throws NullPointerException if the specified collection is null
     */
	public ComponentSequenceImpl(Collection<? extends Component> c) { super(c); }
	
	@Override public boolean emplace(Component component) 
	{
		return add(component);
	}
	
	@Override public void emplace(int index, Component component) 
	{
		add(index, component);
	}

	@Override public boolean erase(Component component) 
	{
		return remove(component);
	}
	
	@Override public Component erase(int index) 
	{
		return remove(index);
	}

	@Override public boolean isComponent() 
	{
		return true;
	}
}