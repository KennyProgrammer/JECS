package com.kenny.jecs.funcs;

/**
 * Implementation for {@link ICreate}. Implements a single {@link #invoke(int)}
 * function.
 */
@com.kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
public abstract class Create<EntityT> implements ICreate<EntityT>
{
	/**
	 * Construct the {@link ICreate} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Entity extends Number> Create<Entity> 
		create(ICreate<Entity> createI){
		return new Function<Entity>(createI);
	}
	
	/**
	 * Represent a single {@link IEach#invoke(int)} function.
	 */
	@com.kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
    private static final class Function<EntityT> extends Create<EntityT>
    {
    	private final ICreate<EntityT> createI;
    	
		Function(ICreate<EntityT> createI) {
			super();
    		this.createI = createI;
 
        }
    	
        @Override
        public final void invoke(EntityT entity) {
        	createI.invoke(entity);
        }
    }
}
