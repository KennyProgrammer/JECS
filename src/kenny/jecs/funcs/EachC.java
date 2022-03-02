package kenny.jecs.funcs;

/**
 * Implementation for {@link IEachC}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.IEachC.JECSApi(since = "0.1.5")
public abstract class EachC<EntityT, Component> implements IEachC<EntityT, Component>
{
	/**
	 * Construct the {@link IEachC} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Entity extends Number, Component> EachC<Entity, Component> 
		create(IEachC<Entity, Component> eachCI){
		return new Function<Entity, Component>(eachCI);
	}
	
	/**
	 * Represent a single {@link IEachC#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.IEachC.JECSApi(since = "0.1.5")
    private static final class Function<EntityT, Component> extends EachC<EntityT, Component>
    {
    	private final IEachC<EntityT, Component> eachCI;
    	
		Function(IEachC<EntityT, Component> eachI) {
			super();
    		this.eachCI = eachI;
 
        }
    	
        @Override
        public final void invoke(EntityT entity, Component component) {
        	eachCI.invoke(entity, component);
        }
    }
}