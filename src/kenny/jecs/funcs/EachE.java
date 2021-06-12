package kenny.jecs.funcs;

/**
 * Implementation for {@link EachEI}. Implements a single {@link #invoke(int, C)}
 * function.
 */
@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.8")
public abstract class EachE implements EachEI
{
	/**
	 * Construct the {@link EachEI} function implementation. 
	 * 
	 * @return Returns a {@link Function} that contains the entire implementation of the
	 * functional interface.
	 */
	public static final EachE create(EachEI eachEI){
		return new Function(eachEI);
	}
	
	/**
	 * Represent a single {@link EachEI#invoke(int, Object)} function.
	 */
	@kenny.jecs.funcs.EachI.JECSApi(since = "0.1.8")
    private static final class Function extends EachE
    {
    	private final EachEI eachEI;
    	
		Function(EachEI eachEI) {
			super();
    		this.eachEI = eachEI;
 
        }
    	
        @Override
        public final void invoke(int entity) {
        	eachEI.invoke(entity);
        }
    }
}