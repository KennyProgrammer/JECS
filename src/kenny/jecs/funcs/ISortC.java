package kenny.jecs.funcs;

/**
 * This functional interface implements the {@link kenny.jecs.BaseJECS#sort(Compponent, ISortE)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
public interface ISortC<Component extends Object> 
{
	/**
	 * This method implements the {@link kenny.jecs.BaseJECS#sort(Component, ISortC)}. Its allows
	 * to pass information about how entity should be sorted. This method can also be 
	 * used as a lambda expression. 
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.sort(new ISort() {
	 *     public void invoke(EntityT entityType) {
	 *         System.out.println(System.out.println("Entity " + entity + " created!");
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * SortI<<EntityT>EntityT> sort = (a, b) -> { return a - b; };
	 * system.sort(sort)
	 * </blockquote></pre>
	 * 
	 * @param a - First entity to be compared.
	 * @param b - Second entity to be compared.
	 */
	@kenny.jecs.funcs.IEachC.JECSApi(since = "0.1.9")
	public int invoke(Component a, Component b);
}

