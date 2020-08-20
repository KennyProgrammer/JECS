package kenny.jecs.funcs;

/**
 * Implementation for {@link EachI}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
public abstract class Each<Component> implements EachI<Component>
{
	/**
	 * Construct the {@link EachI} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Component> Each<Component> create(EachI<Component> eachI){
		return new Function<Component>(eachI);
	}
	
	/**
	 * Represent a single {@link EachI#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
    private static final class Function<Component> extends Each<Component>
    {
    	private final EachI<Component> eachI;
    	
		Function(EachI<Component> eachI) {
			super();
    		this.eachI = eachI;
 
        }
    	
        @Override
        public final void invoke(int entity, Component component) {
        	eachI.invoke(entity, component);
        }
    }
}