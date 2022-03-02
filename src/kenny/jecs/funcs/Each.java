package kenny.jecs.funcs;

/**
 * Implementation for {@link IEach}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
public abstract class Each<EntityT, Component> implements IEach<EntityT, Component>
{
	/**
	 * Construct the {@link IEach} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Entity extends Number, Component> Each<Entity, Component> 
		create(IEach<Entity, Component> eachI){
		return new Function<Entity, Component>(eachI);
	}
	
	/**
	 * Represent a single {@link IEach#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
    private static final class Function<EntityT, Component> extends Each<EntityT, Component>
    {
    	private final IEach<EntityT, Component> eachI;
    	
		Function(IEach<EntityT, Component> eachI) {
			super();
    		this.eachI = eachI;
 
        }
    	
        @Override
        public final void invoke(EntityT entity, Component component) {
        	eachI.invoke(entity, component);
        }
    }
}