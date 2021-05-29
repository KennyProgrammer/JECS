package kenny.jecs.funcs;

/**
 * This functional interface implements the {@link kenny.jecs.JECS#each(Class, EachCI)} 
 * method as additional parameter. Where it is possible to create this interface and used to
 * invoke an anonymous method or using a lambda.
 * 
 * @author Danil (Kenny) Dukhovenko
 */
@FunctionalInterface
@EachCI.JECSApi(since = "0.1.5")
public interface EachCI<Component>
{
	/**
	 * Recognizes the Java Entity Component system API.
	 */
	@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
	public static @interface JECSApi { String since() default "";}
	
	/**
	 * This method implements the {@link kenny.jecs.JECS#each(Class, EachCI)}. Its allows
	 * to add additional properties inside <code>each</code> function. This method can also be 
	 * used as a lambda expression. For example, in the body of a method/lambda, you can manage 
	 * components and call specific methods for each entity.
	 * <p>
	 * Example code:
	 * <blockquote><pre>
	 * system.each(Component.class, new EachCI<Component>() {
	 *     public void invoke(int entity, AnyComponent anyComponent) {
	 *          if(system.eqs(ComponentAny.class, anyComponent))
	 *               ((ComponentAny) anyComponent).printObj(toString());
	 * }});
	 * </blockquote></pre>
	 * or using lambda
	 * <blockquote><pre>
	 * system.each(AnyComponent.class, (entity, anyComponent) -> {
	 *    if(system.eqs(ComponentAny.class, anyComponent))
	 *        ((ComponentAny) anyComponent).printObj(toString());
	 * });
	 * </blockquote></pre>
	 * 
	 * <p>
	 * Difference between this {@link EachCI} and normal {@link EachI} is:
	 * <p>
	 * - This {@link EachCI} function is for all component types, i.e for all
	 * {@link Object}s. And to get access to its methods/functions it should be cast
	 * to specific component the which you want.
	 * <p>
	 * - But normal {@link EachI} function uses for one type of component and all it
	 * sub-components, i.e those classes that are inherited from the component you selected.
	 * 
	 * @param entity - Get access to current iterable valid entity from each.
	 * @param component - Get access to current iterable component of that entity form each.
	 */
	@JECSApi(since = "0.1.5")
	public void invoke(int entity, Component component);
}