package com.kenny.jecs.funcs;

import com.kenny.jecs.funcs.IEach.JECSApi;

/**
 * This functional interface implements the {@link kenny.jecs.BaseJECS#create(CreateI)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
public interface IPatch<Component extends Object> 
{
	/**
	 * This method implements the {@link kenny.jecs.BaseJECS#create(CreateI)}. Its allows
	 * to get information about entity creation. This method can also be 
	 * used as a lambda expression. 
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.create(new CreateI() {
	 *     public void invoke(int entity) {
	 *         System.out.println(System.out.println("Entity " + entity + " created!");
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * system.create((entity) -> {System.out.println("Entity " + entity + " created!")})
	 * </blockquote></pre>
	 * 
	 * @param entity - Get access to current creatable valid entity.
	 */
	@JECSApi(since = "0.1.5")
	public void invoke(Component component);
}
