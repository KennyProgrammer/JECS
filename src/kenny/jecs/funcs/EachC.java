package kenny.jecs.funcs;

/**
 * Implementation for {@link EachCI}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.EachCI.JECSApi(since = "0.1.5")
public abstract class EachC<Component> implements EachCI<Component>
{
	/**
	 * Construct the {@link EachCI} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Component> EachC<Component> create(EachCI<Component> eachCI){
		return new Function<Component>(eachCI);
	}
	
	/**
	 * Represent a single {@link EachCI#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.EachCI.JECSApi(since = "0.1.5")
    private static final class Function<Component> extends EachC<Component>
    {
    	private final EachCI<Component> eachCI;
    	
		Function(EachCI<Component> eachI) {
			super();
    		this.eachCI = eachI;
 
        }
    	
        @Override
        public final void invoke(int entity, Component component) {
        	eachCI.invoke(entity, component);
        }
    }
}