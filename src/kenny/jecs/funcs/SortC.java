package kenny.jecs.funcs;


/**
 * Implementation for {@link ISortC}. Implements a single {@link #sort(EntityA, EntityB)}
 * function.
 */
@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.9")
public abstract class SortC<Component extends Object> implements ISortC<Component>
{
	/**
	 * Construct the {@link ISortC} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Component extends Object> SortC<Component> 
		create(ISortC<Component> sortI){
		return new Function<Component>(sortI);
	}
	
	/**
	 * Represent a single {@link IEach#invoke(EntityT, EntityT)} function.
	 */
	@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.9")
    private static final class Function<Component extends Object> extends SortC<Component>
    {
    	private final ISortC<Component> sortI;
    	
		Function(ISortC<Component> sortI) {
			super();
    		this.sortI = sortI;
 
        }
    	
        @Override
        public final int invoke(Component a, Component b) {
        	return sortI.invoke(a, b);
        }
    }
}
