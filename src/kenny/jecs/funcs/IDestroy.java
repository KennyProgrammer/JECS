package kenny.jecs.funcs;

import kenny.jecs.funcs.IEach.JECSApi;

/**
 * This functional interface implements the {@link kenny.jecs.BaseJECS#destroy(DestroyI)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
public interface IDestroy<EntityT>
{
	/**
	 * This method implements the {@link kenny.jecs.BaseJECS#destroy(int, DestroyI)}. Its allows
	 * to get information about entity destruction. This method can also be 
	 * used as a lambda expression. 
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.destroy(int entity, new DestroyI() {
	 *     public void invoke(int entity) {
	 *         System.out.println(System.out.println("Entity " + entity + " has been destroyed!");
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * system.destroy(int entity, (entity) -> {System.out.println("Entity " + entity + " has been destroyed!")})
	 * </blockquote></pre>
	 * 
	 * @param entity - Get access to current creatable valid entity.
	 */
	@JECSApi(since = "0.1.5")
	public void invoke(EntityT entity);
}
