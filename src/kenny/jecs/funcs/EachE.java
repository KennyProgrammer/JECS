package kenny.jecs.funcs;

/**
 * Implementation for {@link IEachE}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.8")
public abstract class EachE<EntityT> implements IEachE<EntityT>
{
	/**
	 * Construct the {@link IEachE} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Entity extends Number> EachE<Entity> create(IEachE<Entity> eachEI){
		return new Function<Entity>(eachEI);
	}
	
	/**
	 * Represent a single {@link IEachE#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.8")
    private static final class Function<EntityT> extends EachE<EntityT>
    {
    	private final IEachE<EntityT> eachEI;
    	
		Function(IEachE<EntityT> eachEI) {
			super();
    		this.eachEI = eachEI;
 
        }
    	
        @Override
        public final void invoke(EntityT entity) {
        	eachEI.invoke(entity);
        }
    }
}