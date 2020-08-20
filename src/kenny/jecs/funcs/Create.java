package kenny.jecs.funcs;

/**
 * Implementation for {@link CreateI}. Implements a single {@link #invoke(int)}
 * function.
 */
@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
public abstract class Create implements CreateI
{
	/**
	 * Construct the {@link CreateI} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final Create create(CreateI createI){
		return new Function(createI);
	}
	
	/**
	 * Represent a single {@link EachI#invoke(int)} function.
	 */
	@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.5")
    private static final class Function extends Create
    {
    	private final CreateI createI;
    	
		Function(CreateI createI) {
			super();
    		this.createI = createI;
 
        }
    	
        @Override
        public final void invoke(int entity) {
        	createI.invoke(entity);
        }
    }
}
