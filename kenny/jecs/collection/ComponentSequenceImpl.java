package kenny.jecs.collection;

import java.util.ArrayList;

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