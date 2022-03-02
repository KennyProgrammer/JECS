package kenny.jecs.funcs;

/**
 * Implementation for {@link ISort}. Implements a single {@link #invoke(EntityA, EntityB)}
 * function.
 */
@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.9")
public abstract class Sort<EntityT extends Number> implements ISort<EntityT>
{
	/**
	 * Construct the {@link ISort} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <EntityT extends Number> Sort<EntityT> 
		create(ISort<EntityT> sortI){
		return new Function<EntityT>(sortI);
	}
	
	/**
	 * Represent a single {@link IEach#invoke(EntityT, EntityT)} function.
	 */
	@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.9")
    private static final class Function<EntityT extends Number> extends Sort<EntityT>
    {
    	private final ISort<EntityT> sortI;
    	
		Function(ISort<EntityT> sortI) {
			super();
    		this.sortI = sortI;
 
        }
    	
        @Override
        public final int invoke(EntityT a, EntityT b) {
        	return sortI.invoke(a, b);
        }
    }
}
