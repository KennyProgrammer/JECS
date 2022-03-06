package com.kenny.jecs.funcs;

/**
 * This functional interface implements the {@link kenny.jecs.BaseJECS#each(EachEI)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
@IEach.JECSApi(since = "0.1.8")
public interface IEachE<EntityT>
{
	/**
	 * Recognizes the Java Entity Component system API.
	 */
	@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
	public static @interface JECSApi { String since() default "";}
	
	/**
	 * This method implements the {@link kenny.jecs.BaseJECS#each(EachEI)}. Its allows
	 * to add additional properties inside <code>each</code> function. This method can also be 
	 * used as a lambda expression. For example, in the body of a method/lambda, you can manage components and call specific
	 * methods for each entity.
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.each(new EachEI() {
	 *     public void invoke(int entity) {
	 *        ...
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * system.each((entity) -> {
	 *     ...
	 * });
	 * </blockquote></pre>
	 * 
	 * @param entity - Get access to current iterable valid entity from each.
	 */
	@JECSApi(since = "0.1.8")
	public void invoke(EntityT entity);
}
