package kenny.jecs.funcs;

/**
 * Implementation for {@link IDestroy}. Implements a single {@link #invoke(int)}
 * function.
 */
@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
public abstract class Destroy<EntityT> implements IDestroy<EntityT>
{
	/**
	 * Construct the {@link IDestroy} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final <Entity extends Number> Destroy<Entity> 
		create(IDestroy<Entity> destroyI){
		return new Function<Entity>(destroyI);
	}
	
	/**
	 * Represent a single {@link IDestroy#invoke(int)} function.
	 */
	@kenny.jecs.funcs.IEach.JECSApi(since = "0.1.5")
    private static final class Function<EntityT> extends Destroy<EntityT>
    {
    	private final IDestroy<EntityT> destroyI;
    	
		Function(IDestroy<EntityT> destroyI) {
			super();
    		this.destroyI = destroyI;
 
        }
    	
        @Override
        public final void invoke(EntityT entity) {
        	destroyI.invoke(entity);
        }
    }
}
