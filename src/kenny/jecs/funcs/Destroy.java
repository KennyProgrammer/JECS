package kenny.jecs.funcs;

/**
 * Implementation for {@link DestroyI}. Implements a single {@link #invoke(int)}
 * function.
 */
@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
public abstract class Destroy implements DestroyI
{
	/**
	 * Construct the {@link DestroyI} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final Destroy create(DestroyI destroyI){
		return new Function(destroyI);
	}
	
	/**
	 * Represent a single {@link DestroyI#invoke(int)} function.
	 */
	@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
    private static final class Function extends Destroy
    {
    	private final DestroyI destroyI;
    	
		Function(DestroyI destroyI) {
			super();
    		this.destroyI = destroyI;
 
        }
    	
        @Override
        public final void invoke(int entity) {
        	destroyI.invoke(entity);
        }
    }
}
