package com.kenny.jecs.funcs;

/**
 * This functional interface implements the {@link kenny.jecs.BaseJECS#each(Class, IEach)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
@IEach.JECSApi(since = "0.1.5")
public interface IEach<EntityT, Component>
{
	/**
	 * Recognizes the Java Entity Component system API.
	 */
	@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
	public static @interface JECSApi { String since() default "";}
	
	/**
	 * This method implements the {@link kenny.jecs.BaseJECS#each(Class, IEach)}. Its allows
	 * to add additional properties inside <code>each</code> function. This method can also be 
	 * used as a lambda expression. For example, in the body of a method/lambda, you can manage components and call specific
	 * methods for each entity.
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.each(Component.class, new EachI<Component>() {
	 *     public void invoke(int entity, Component component) {
	 *         component.printObj(toString());
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * system.each(Component.class, (entity, component) ->
	 * {
	 *     component.printObj(toString());
	 * });
	 * </blockquote></pre>
	 * 
	 * @param entity - Get access to current iterable valid entity from each.
	 * @param component - Get access to current iterable component of that entity form each.
	 */
	@JECSApi(since = "0.1.5")
	public void invoke(EntityT entity, Component component);
}
