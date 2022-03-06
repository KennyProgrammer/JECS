package com.kenny.jecs.funcs;

/**
 * Implementation for {@link ICreate}. Implements a single {@link #invoke(int)}
 * function.
 */
@com.kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
public abstract class Patch<Component> implements IPatch<Component>
{
	/**
	 * Construct the {@link IPatch} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Component extends Object> Patch<Component> 
		create(IPatch<Component> patchI){
		return new Function<Component>(patchI);
	}
	
	/**
	 * Represent a single {@link IEach#patch(entity, IPatch)} function.
	 */
	@com.kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
    private static final class Function<Component> extends Patch<Component>
    {
    	private final IPatch<Component> patchI;
    	
		Function(IPatch<Component> patchI) {
			super();
    		this.patchI = patchI;
 
        }
    	
        @Override
        public final void invoke(Component component) {
        	patchI.invoke(component);
        }
    }
}
