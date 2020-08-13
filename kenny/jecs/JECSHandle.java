package kenny.jecs;

import static kenny.jecs.JECSHandle.JECSReflect.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Map.Entry;

import kenny.jecs.collection.ComponentSequence;
import kenny.jecs.collection.ComponentSequenceImpl;
import kenny.jecs.collection.EntityContainer;
import kenny.jecs.collection.EntityContainerImpl;

/**
 * <code>JECS</code> or <b>Java Entity-Component-System API</b> this is a small system that holds all entity identifiers in a single object 
 * as well as their component identifiers. It makes it easy to create entities, put a component in it, and
 * when it is not needed, it is also easy to delete it without leaving the object in memory.
 * <p>
 * This is not the best solution for sorting entities and components around the world, I am 
 * sure there are better solutions in other languages like C++ and Entt, but I have not seen 
 * similar systems in Java.
 * <p>
 * <b>Entity</b> The entity is a general purpose object. Usually, it only consists of a unique id. They "tag every coarse gameobject as a 
 * separate item". Implementations typically use a plain integer for this. <p>
 * <b>Component</b> the raw data for one aspect of the object, and how it interacts with the world. "Labels the Entity as possessing this 
 * particular aspect". Implementations typically use structs, classes, or associative arrays. <p>
 * <b>System</b> "Each System runs continuously (as though each System had its own private thread) and performs global actions on every 
 * Entity that possesses a Component of the same aspect as that System."
 * <p>
 * Example: 
 * <blockquote><pre>
 * 		
 * JECSHandle<<Object>Object> jecs = JECSHandle.construct();
 * 
 * int entity = jecs.create(); 
 * jecs.emplace(entity, new TransformComponent()); 
 * jecs.emplace(...); 
 * 	
 * while(jecs.has(TransformComponent.class)) 
 * {
 * 	TransformComponent t = jecs.get(entity, TransformComponent.class);
 * 	t.position.add(0.5f, 0.1f, 0.0f);
 * 	System.out.println(t.position);
 * 		
 * 	if(t.position.x >= 50.0f)
 * 		jecs.erase(entity, TransformComponent.class);
 * }
 *
 * jecs.erase(...);
 * jecs.destroy(entity); 
 * jecs = JECSHandle.deconstruct(jecs);
 * </blockquote></pre>
 * 
 * @param <Component> This is starting hierarchy of component, by default set by Object.
 * You can use any object as a beginning. Any <code>Component</code> object extends of <code>Component</code> will 
 * be recognize for system has a component. 
 * 
 * @author Danil (Kenny) Dukhovenko 
 */
@JECSHandle.JECSApi
public class JECSHandle<Component extends Object> 
{	
	/**
	 * Recognizes the Java Entity Component system API. Indicates that each method or variable 
	 * belongs to the class of this API.
	 */
	@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE})
	private static @interface JECSApi {}
	
	/**
	 * Signifies that a public API (public class, method or field) is subject to incompatible changes,
	 * or even removal, in a future release. An API bearing this annotation is exempt from any
	 * compatibility guarantees made by its containing library. Note that the presence of this
	 * annotation implies nothing about the quality or performance of the API in question, only the fact
	 * that it is not "API-frozen."
	 * 
	 * @author Danil (Kenny) Dukhovenko 
	 */
	@JECSApi
	@Retention(CLASS)
	@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE})
	@Documented
	private static @interface BetaFeature {}
	
	/**
	 * This is {@link JECSReflect} class to help with Java Reflection Framework. Its contains
	 * kinda of utility class for sorting, constructing and short writing Reflection methods.
	 * 
	 * @author Danil (Kenny) Dukhovenko 
	 */
	@JECSApi
	public static final class JECSReflect
	{
		/**
		 * Return class form Object.
		 * 
		 * @see Class getClass()
		 */
		@JECSApi
		static final Class<?> getClass(Object object)
		{
			return object.getClass();
		}
		
		/**
		 * Return class name form Object.
		 * 
		 * @see Class.getClass().getName()
		 */
		@JECSApi
		static final String getClassS(Object object)
		{
			return getClass(object).getName();
		}
		
		/**
		 * Return constructor array form clazz.
		 * 
		 * @see Class.getClass().getConstructors()
		 */
		@JECSApi
		static final Constructor<?>[] getCtors(Class<?> clazz)
		{
			return clazz.getConstructors();
		}
		
		/**
		 * Return constructor form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx]
		 */
		@JECSApi
		static final Constructor<?> getCtor(Class<?> clazz, int ctorIdx)
		{
			return getCtors(clazz)[ctorIdx];
		}

		/**
		 * Return constructor name form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx].getName()
		 */
		@JECSApi
		static final String getCtorS(Class<?> clazz, int ctorIdx)
		{
			return getCtor(clazz, ctorIdx).getName();
		}
		
		/**
		 * Return constructor parameter form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx].getParameters()[argIdx]
		 */
		@JECSApi
		static final Parameter getCtorArg(Class<?> clazz, int ctorIdx, int argIdx)
		{
			return getCtor(clazz, ctorIdx).getParameters()[argIdx];
		}
		
		/**
		 * Return constructor parameter name form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx].getParameters()[argIdx].getTypeName()
		 */
		@JECSApi
		static final String getCtorArgS(Class<?> clazz, int ctorIdx, int argIdx)
		{
			return getCtorArg(clazz, ctorIdx, argIdx).getType().getTypeName();
		}

		/**
		 * Return constructor parameter form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx].getParameters()[argIdx]
		 */
		@JECSApi
		static final Class<?> getCtorArgT(Class<?> сlazz, int ctorIdx, int argIdx)
		{
			return getCtorArg(сlazz, ctorIdx, argIdx).getType();
		}
		
		/**
		 * Return constructor parameter count form clazz.
		 * 
		 * @see Class.getClass().getConstructors()[ctorIdx].getParameterCount()
		 */
		@JECSApi
		static final int getCtorArgsCount(Class<?> clazz, int ctorIdx)
		{
			return getCtor(clazz, ctorIdx).getParameterCount();
		}
		
		/**
		 * Casts this Class object to represent a subclass of the class represented by the specified 
		 * class object. This method not be call {@link ClassCastException}.
		 * 
		 * @see Class.getClass().isAssignableFrom(from) && Class.getClass().asSubclass(as)
		 */
		@JECSApi
		static <T> Class<? extends T> safeAsSubClass(Class<?> from, Class<T> as, String msg) 
		{
	        if (as.isAssignableFrom(from)) { return from.asSubclass(as); }
	        
	        throw new JECSUndifiendBehaviourError(msg);
	    }
	}
	
	//=========================================
	//			JECSHandle Class
	//=========================================
	
	/**Global statistic thing to calculate how many entities were created.*/
	static int entityCount = -1;
	/**Max count of entities available by this handle.*/
	protected static final int MAX_ENTITIES = Integer.MAX_VALUE - 1;
	/**Max count of components available by this handle.*/
	protected static final int MAX_COMPONENTS = Short.MAX_VALUE - 1;
	/**This is random entity generator.*/
	private static final Random GENERATOR = new Random();
	/**{@link JECSUndifiendBehaviourError} message base.*/
	private static final String baseMsg = "Cannot construct component from args.\n";
	/**Array of entities for searching to find component of that entity.*/
	private ArrayList<Integer> entities;
	/**Container of entity identifiers and sequence of all components identifiers and his data.*/
	private EntityContainer<Integer, ComponentSequence<Component>> container;
	
	/**
	 * Return generic class parameters of this class.
	 */
	@JECSApi
	protected static Class<?> getGenericParameterClass(Class<?> actualClass, int parameterIndex) 
	{
	    return (Class<?>) (
	    		(ParameterizedType) actualClass.getGenericSuperclass()).getActualTypeArguments()[parameterIndex];
	}
	
	/**
	 * Get's the K from V.
	 */
	@JECSApi
	protected static <T, E> T getKeyByValue(Map<T, E> map, E value) 
	{
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/**
	 * Sort the string and check if one of primitive type name existing in string. And return
	 * already sorted string with out conflict.
	 */
	@JECSApi
	public static final String sort(String s)
	{
		if(s == "byte[]" || s == "byte" || s == "char" || s == "short" || s == "int" || s == "long" || s == "float" || s == "double" || s == "boolean")
			return s = "java.lang." + (s == "byte[]"?"String":s=="byte"?"Byte":s=="char"?"Character":s=="short"?"Short":s=="int"?"Integer":s=="long"?"Long":s=="float"?"Float":s=="double"?"Double":s=="boolean"?"Boolean":"");
		return s;
	}

	/**
	 * Allocate and construct new handle/system. This construct in runtime check the upper included parameters
	 * for this handle. If <code>CompObj</code> set to not property type is cause undifiend
	 * behaviour.
	 * 
	 * @param <CompObj> is starting hierarchy of component data object, by default set by Object.
	 * You can use any object as a beginning.
	 * 
	 * @return New instance of constructed system.
	 */
	@JECSApi
	public static <CompObj extends Object> JECSHandle<CompObj> construct()
	{
		return new JECSHandle<CompObj>();
	}

	/**
	 * Deallocate and deconstruct this <code>system</code>.
	 * 
	 * @param handle to be destroyed.
	 * @return <code>Null</code> instance of constructed system.
	 */
	@JECSApi
	public static <CompObj extends Object> JECSHandle<CompObj> deconstruct(JECSHandle<CompObj> handle)
	{
		if(handle != null)
		{
			try {
				handle.destroyAll();
				handle = null;
				System.gc(); //attempt to clear memory from handle.
				return handle;
			} catch (JECSException e) {e.printStackTrace();}
		}
		throw new NullPointerException("Input handle not exist or already has been destructed.");
	}
	
	/**
	 * Constructs a newly allocated on the heap {@code JECSHandle} object that represents the specified 
	 * {@code container} value or Entity-Component-System handle. This is private constuctor only for <code>construct</code>
	 * method.
	 * <p>
	 * See for constructing {@link #construct}.
	 */
	@JECSApi
	@Deprecated
	private JECSHandle() 
	{
		entities = new ArrayList<Integer>();
		container = new EntityContainerImpl<Integer, ComponentSequence<Component>>();

		if(!container.isEmpty())
			this.clear();
	}
	
	/**
	 * Creates new entity identifier. In more detail, it generates a random number in 
	 * the range MAX_ENTITIES, which will be recognized by the handler as an valid entity.
	 * <p>
	 * If an entity with this identifier already exists in the handler, it will delete it and
	 * recreate as empty entity.
	 * 
	 * @return A valid entity identifier.
	 */
	@JECSApi
	public final int create()
	{
		int newEntityIdentifier = -1;
		newEntityIdentifier = GENERATOR.nextInt(MAX_ENTITIES);
		
		if(newEntityIdentifier < 0)
			return create();
		
		if(container.containsKey(newEntityIdentifier))
		{
			//if entity id already created its destroyed and recreates.
			try {
				destroy(newEntityIdentifier);
			} catch (JECSException e) {
				e.printStackTrace();
			}
			return create();
		}
		
		//create entity id and count in global order.
		entities.add(newEntityIdentifier);
		entityCount = entities.size() - 1;
		
		//create components array represents component as data structure for entity.
		ComponentSequence<Component> components = new ComponentSequenceImpl<Component>();
		container.emplace(newEntityIdentifier, components);
		return newEntityIdentifier;
	}
	
	/**
	 * Destroy <code>entity</code> associated with all components of removed entity.
	 * 
	 * @param <E> Entity type.
	 * @param entity - identifier of entity to be destroy.
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	
	@SuppressWarnings("unchecked")
	@JECSApi 
	public final <E extends Number> E destroy(E entity)  
			throws JECSException
	{
		validationCheck(entity, "to remove");
		
		//remove all components from this entity.
		ComponentSequence<Component> components = container.get(entity);
		for(int i = 0; i < components.size(); i++)
		{
			Class<? extends Component> componentT = (Class<? extends Component>) components.get(i).getClass();
			erase(entity, componentT);
		}
		
		container.erase((Integer) entity, components);
		entities.remove((Integer) entity);
		entityCount = entities.size() - 1;
		
		//is entity doesn't exist, that its succesfful removed and retuned -1. 
		if(!container.containsKey(entity))
		{
			Integer answer = -1;	
			entity = (E) answer;
		}
			
		return entity;
	}
	
	/**
	 * Destroy 'entities' from global handle. This method destroy all entites doesn't 
	 * matter where or how they were created. After this call the global container 
	 * for handle entities will be empty.
	 * 
	 * @apiNote Entities and all assissiated <b>component data</b> will be equals
	 * <code>null</code>, you cannot get access to it anymore.
	 * 
	 * @throws JECSException 
	 */
	@JECSApi
	public final void destroyAll() throws JECSException
	{
		Integer entity = null;
		ComponentSequence<Component> components = null;
		
		while(!empty())
		{
			Iterator<Integer> enttItr = container.keySet().iterator();
			while(enttItr.hasNext())
			{
				entity = enttItr.next();
				
				if(entity <= -1)
					throw new JECSException("Attempt to remove unvalid entity.");
				
				if(!container.containsKey(entity))
					throw new JECSException("Attempt to remove uncreated entity.");
				
				//remove all components from this entity.
				components = container.get(entity);
				if(!components.isEmpty())
				{
					@SuppressWarnings("unchecked")
					Class<? extends Component> componentT = (Class<? extends Component>) components.get(0).getClass();
					erase(entity, componentT);
				}

				components.clear();	
				break;
			}
			
			container.erase(entity, components);
			entities.remove((Integer)entity);
			entityCount = entities.size() - 1;
		}
	}
	
	/**
	 * Destroy group of entities in range from <code>minRange</code> to <code>maxRange</code> with
	 * associated with all components of removed entity.
	 * 
	 * @param minRange - the starting point from which the entities will removed.
	 * @param maxRange - the end point to which the entities will be removed.
	 * @return If all entity in range success destroyed, return total number of 
	 * removed entities.
	 * 
	 * @warning
	 * minRange and maxRange is not entity indentifiers, its entity indices in handle or,
	 * you can get indices by entities array like <code>entities.indexOf(entity)</code>.
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@JECSApi
	public final int destroyInRange(int minRange, int maxRange)
	{
		int totalRemoved = 0;
		
		for(int entity : this.entities)
		{
			if(entities.indexOf(entity) >= minRange && 
					entities.indexOf(entity) <= maxRange)
			{
				try {
					destroy(entity);
					totalRemoved++;
				} catch (JECSException e) { e.printStackTrace();}
			}
			
			continue;
		}
		return totalRemoved;
	}
	
	/**
	 * Destroy first entity identifier in the sequence of entities with all assigns 
	 * components.
	 * 
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@JECSApi
	@BetaFeature
	public final <E extends Number> E destroyFirst() throws JECSException { return destroy(null); }
	
	/**
	 * Destroy last entity identifier in the sequence of entities with all assigns 
	 * components.
	 * 
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi
	@BetaFeature
	public final <E extends Number> E destroyLast() throws JECSException { return (E) destroy(null); }
	
	/**
	 * Emplace <code>C</code> component with to that <code>entity</code>. Assigns the
	 * given component to an entity.
	 * <p>
	 * Component class type must be the same as the component object, because class of component
	 * reflects its state, and allows you to get a list of variables stored by the constructor
	 * in the component class in runtime.
	 * 
	 * For example:
	 * <blockquote><pre>
	 * system.emplace(entity, Component.class, new Component("First", 0, true).
	 * </blockquote></pre>
	 * 
	 * Advanced version of this method is {@link #emplace(E, Class, Object[])}.
	 * @param <E> Type of entity.
	 * @param <C> Type of component.
	 * @param entity - The entity that the component will be attached to.
	 * @param componentT - Class type of component.
	 * @param component - Data structure to be added how component.
	 */
	@JECSApi
	public final <E extends Number, C extends Component> void emplace(E entity, Class<C> componentT, 
			C component) throws JECSException
	{
		validationCheck(entity, "emplace component to");
		
		if(component == null)
			throw new JECSException("Cannot emplace not initialized component!");
		if(has(entity, componentT))
			throw new JECSException("Component with type <" + componentT.getTypeName() + "> already existing!" );
		if(componentT.getTypeName() != component.getClass().getTypeName())
			throw new JECSException("Class type of < " + componentT.getTypeName() + " component does not match its object!");
		
		ComponentSequence< Component> components = container.get(entity);
		components.emplace(component);
	}
	
	/**
	 * Emplace <code>C</code> component with to that <code>entity</code>. Assigns the given 
	 * component to an entity. This is advanced version of <code>emplace(E entity, Class<C> componentT, 
	 * C component)</code>, instead of object you pass args and object create in automatically.
	 * <p>
	 * This method will take all <code>args</code> arguments and use them to create a new component object. Before this,
	 * the parameters of one of the class <code>C</code> constructors and the specified arguments will be checked for 
	 * compliance. If there is an error related to a conflict between the args and constructor arguments it will 
	 * {@link JECSUndifiendBehaviourError} crash. If there are no errors, the object will be built in runtime.
	 * <p> <b>Warning!</b>
	 * This method not full support checking an enclosing classes. The component class must be the main class, 
	 * not nested. 
	 * <p>
	 * For example:
	 * <blockquote><pre>
	 * 	       | entty | component type | component args |
	 * system.emplace(entity, Component.class, "First", 0, true).
	 * </blockquote></pre>
	 * 
	 * @apiNote If you are interested in performance for your program, we would recommend using the 
	 * {@link #emplace(E, Class, C)}. method, but this will not play a big role because there will be 
	 * a difference in speed of several nanoseconds.
	 * 
	 * @param <E> Entity type
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentT - Class type of component.
	 * @param args - Arguments (VarArgs) of component <code>C</code> constructor.
	 * 
	 * @throws JECSException If entity not valid or <code>null</code>.
	 * @throws JECSUndifiendBehaviourError If conflict between the <code>args</code> and <code>C</code> constructor
	 * arguments.
	 */
	@JECSApi
	public final <E extends Number, C extends Component> void emplace(E entity, Class<C> componentT,
			Object... args) throws JECSException
	{
		boolean isSuccess = false;
		int ctorsCount = getCtors(componentT).length;
		if(ctorsCount == 0 && args.length != 0)
			throw new JECSUndifiendBehaviourError(baseMsg + "Component <"+ componentT.getTypeName() +"> without default constructor cannot have args!");
		
		//for each component ctor check count args
		for(int i = 0; i < ctorsCount; i++)
		{
			int ctorArgsCount = getCtorArgsCount(componentT, i);
			int objArgsCount = args.length;
			if(objArgsCount == ctorArgsCount)
			{
				//if default ctor
				if(ctorArgsCount == 0)
				{
					isSuccess = true; break;
				}
				
				//component ctor args class array
				String[] classArgsType = new String[(ctorArgsCount)];
				String[] objectArgsType = new String[args.length];
				
				//Check args types
				for(int j = 0; j < ctorArgsCount; j++)
				{
					//check enclosing clases
					if(componentT.getEnclosingClass() != null)
						throw new JECSUndifiendBehaviourError(baseMsg + "Component class must be host not enclosing! Is not support for now.");
					
					objectArgsType[j] = getClassS(args[j]); 
					classArgsType[j] = getCtorArgS(componentT, i, j); 
			
					if(getCtorArgT(componentT, i, j).isInterface())
					{
						if(safeAsSubClass(JECSReflect.getClass(args[j]), getCtorArgT(componentT, i, j), baseMsg + "Cannot compare intefrace<"+getCtorArgT(componentT, i, j).getName()+
								"> with class<"+objectArgsType[j]+">.") != null)
						{
							classArgsType[j] = objectArgsType[j]; 
						}
					}
					classArgsType[j] = sort(classArgsType[j]);
			
					if(classArgsType[j].contentEquals(objectArgsType[j]))
					{
						if(j == ctorArgsCount - 1) 
						{
							isSuccess = true;
							break;
						}
						continue;
					}
					else
						throw new JECSUndifiendBehaviourError(baseMsg + "Try to place incorrect arg of component <"+ componentT.getName() + ">. " 
							+ "Component arg"+ j + "["+classArgsType[j]+"] not equals input arg"+j+"["+objectArgsType[j] +"] type!");
				}	
				classArgsType = null;
				objectArgsType = null;
			}
			
			//if args != component.args count and it last ctor then throw error.
			if(args.length != ctorArgsCount && i == ctorsCount - 1 && !isSuccess)
				throw new JECSUndifiendBehaviourError(baseMsg + "To much args for component <"+ componentT.getSimpleName() +">. "
						+ "Requied " + getCtorArgsCount(componentT, 0) + ".");
		}
		
		//if comp success checked on args then construct it and add to map.
		if(isSuccess)
		{
	        try
	        {
				//get the constructor of creaing class by args
				Class<?>[] objectCtorArgs = new Class<?>[args.length];
				for(int l = 0; l < objectCtorArgs.length; l++)
				{
					objectCtorArgs[l] = args[l].getClass();
					if(objectCtorArgs[l].getTypeName() == "java.lang.Boolean")
						objectCtorArgs[l] = boolean.class;
				}

				Constructor<?> ctor = 
						Class.forName(componentT.getName()).getConstructor(objectCtorArgs);
		        
				//create an pointer
		        Object[] objectCtorValues = new Object[args.length];
		        for(int l = 0; l < objectCtorValues.length; l++)
		        	objectCtorValues[l] = args[l];

		        @SuppressWarnings("unchecked")
		        //safty cast bc args == C ctor args
				C componentPointer = (C) ctor.newInstance(objectCtorValues);
		        emplace(entity, componentT, componentPointer);
		        objectCtorArgs = null;
		        objectCtorValues = null;
		        return;
				
			} catch ( ClassNotFoundException | NoSuchMethodException  | SecurityException 
					| InstantiationException | IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException e)
	        {e.printStackTrace();}
	    }
	}
	
	/**
	 * Emplace array of <code>C</code> components with to that <code>entity</code>. This is advanced version 
	 * of {@link #emplace(E, Class, Object[])}.
	 * <p>
	 * For a detailed description, see here -> {@link #emplace(E, Class, Object[])}.
	 * <p>
	 * For example:
	 * <blockquote><pre>
	 * system.emplace(entty, 
	 *  new Class<?>[]{ TagComponent.class,        TransformComponent.class}, 
	 *  new Object  []{ new Object[] { "Taggiy" }, new Object[] { new Vector3f(1, 2, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1) } });                                                  
	 * </blockquote></pre>
	 * 
	 * @param <E> Entity type
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentTs - Array of class types for each component.
	 * @param argsArray -  Array of array of arguments (VarArgs) for each component <code>C</code> constructor. Where
	 * nested array is simple <code>args</code>.
	 * 
	 * @throws JECSException If entity not valid or <code>null</code>.
	 * @throws JECSUndifiendBehaviourError If conflict between the <code>args</code> and <code>C</code> constructor
	 * arguments.
	 */
	@JECSApi
	public final <E extends Number, C extends Component> void emplace(E entity, Class<? extends C>[] componentTs,
			Object[]... argsArray) throws JECSException
	{
		//args is normal array of arguments for each componentTs.
		for(int args = 0; args < componentTs.length; args++)
		{
			//convert argsArray to single array for each args.
			Object[] argss = (Object[]) argsArray[0][args];
			emplace(entity, componentTs[args], argss);
		}
	}
	
	/**
	 * Erase component from <code>entity</code> if it already been mapped to containter
	 * sequence of components. After this calls object component will be equals <code>null</code>, 
	 * so you cannot  use it anymore, because JVM Garbage Collector delete it from memory.
	 * 
	 * @param <E> Entity type
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentT - Class type of component.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@JECSApi
	public final <E extends Number, C extends Component> void erase(E entity, Class<C> componentT)
			throws JECSException
	{
		validationCheck(entity, "erase component from");
		
		if(!has(entity, componentT))
			throw new JECSException("Cannot to remove non-existing component!");
		
		ComponentSequence<Component> components = container.get(entity);
		C component = get(entity, componentT);
		components.erase(component);
		component = null;
	}
	
	/**
	 * Erase one or more components from <code>entity</code> if its already been mapped to containter
	 * sequence of components. After this calls object component(s) will be equals <code>null</code>, 
	 * so you cannot  use it anymore, because JVM Garbage Collector delete it from memory.
	 * 
	 * @param <E> Entity type
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentTs - Class type of component(s).
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@SafeVarargs
	@JECSApi
	public final <E extends Number, C extends Component> void erase(E entity, Class<? extends C>... componentTs)
			throws JECSException
	{
		for(int i = 0; i < componentTs.length; i++)
		{
			Class<? extends C> componentT = (Class<? extends C>) componentTs[i]; 
			erase(entity, componentT);
		}
			
	}
	
	//has for multiplie components
	//add other methods
	//utility for sorting.
	
	/**
	 * Checks if this entity has <code>C</code> component data by its type and return true 
	 * otherwise false. 
	 * <p>
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Example:<p>
	 * <blockquote><pre> 
	 * if(has(Component.class) && has(Component2.class)) 
	 * 	System.out.println("Types contains in container!");
	 * </blockquote></pre>
	 * 
	 * @param <E> Type of entity.
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type <code>Component.class</code> performs to check and get 
	 * if exist'.
	 * 
	 * @return True if type-class of component equals classType.
	 * @throws JECSException 
	 */
	@JECSApi
	public <E extends Number, C extends Component> boolean has(E entity, Class<C> componentT) 
			throws JECSException 
	{
		validationCheck(entity, "check on has component from");
		
		ComponentSequence<Component> components = container.get(entity);
		String typeName = componentT.getTypeName();
		String objectTypeName = "";
		
		int searchedKey = components.isEmpty() ? 0 : components.size();
		for(int i = 0; i < searchedKey; i++)
		{
			if(components.get(i) == null)
			{
				if(i == searchedKey)
					return false;
				continue;
			} else
				objectTypeName = components.get(i).getClass().getTypeName();
				
			if(typeName.contentEquals(objectTypeName))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks if this entity has all <code>?</code> input components data with different types and return true 
	 * otherwise false. 
	 * <p>
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Example:<p>
	 * <blockquote><pre> 
	 * if(has(Component.class, Component2.class, Component3.class)) 
	 * 	System.out.println("Types contains in container!");
	 * </blockquote></pre>
	 * 
	 * @param <E> Type of entity.
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type <code>Component.class</code> performs to check and get 
	 * if exist'.
	 * 
	 * @return True if type-class of component equals classType.
	 * @throws JECSException 
	 */
	@SafeVarargs
	@JECSApi
	public final <E extends Number, C extends Component> boolean has(E entity, Class<? extends C>... componentTs) 
			throws JECSException 
	{
		int similarities = 0;
		for(int i = 0; i < componentTs.length; i++)
		{
			Class<? extends C> componentT = (Class<? extends C>) componentTs[i]; 
			if(has(entity, componentT))
				similarities++;
		}
		
		if(similarities == componentTs.length)
			return true;
		
		return false;
	}

	/**
	 * Get's the component <code>C</code> from class type of that component.
	 *
	 * @param <E> Type of entity. 
	 * @param <C> Type of component.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 * @return Already casted to (C) object from clas type, so you don't need to cast 
	 * it to (C).
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@JECSApi
	public <E extends Number, C extends Component> C get(E entity, Class<C> componentT) 
			throws JECSException 
	{
		validationCheck(entity, "check on has component from");

		ComponentSequence<Component> components = container.get(entity);
		int componentCount = components.isEmpty() ? 0 : components.size();
		for(int i = 0; i < componentCount; i++)
		{
			if(components.get(i) == null)
			{
				if(i != componentCount) 
					continue;
				else break;
			} else {
				if(components.get(i).getClass().getName().equals(componentT.getName()))
				{
					@SuppressWarnings("unchecked")
					//this is absolutly safty cast bc we cast Object to T (i.e Object is parent for T)
					C component = ((C) container.get(entity).get(i));
					return component;
				}
			}
		}
		throw new JECSException("Component with type <" + componentT.getName() + "> non-exist!");
	}
	
	/**
	 * Return the group of existing components <code>?</code> from class type of that component.
	 *
	 * @param <E> Type of entity. 
	 * @param <C> Type of component.
	 * @param <Group> Group / array of returned components.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 * @return Already casted to (C) object from clas type, so you don't need to cast 
	 * it to (C).
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	@JECSApi
	public final <E extends Number, C extends Component, Group extends Component> Group[] 
			get(E entity, Class<? extends C>... componentTs) throws JECSException 
	{
		final List<Group> group = new ArrayList<Group>();
		for(int i = 0; i < componentTs.length; i++)
		{
			Class<? extends C> componentT = (Class<? extends C>)componentTs[i]; 
			Component component = get(entity, componentT);
			if(component != null)
				group.add((Group) component);
		}
		return (Group[]) group.toArray();
	}
	
	/**
	 * Remove all mappings from this handle map.
	 */
	@JECSApi
	public void clear()
	{
		container.clear();
	}
	
	/**
	 * Returns true if this handle contains no entity mappings.
	 */
	@JECSApi
	public boolean empty()
	{
		if(container.isEntity())
			return container.isEmpty();
		
		return false;
	}
	
	/**
	 * Remove all components from this entity.
	 * 
	 * @param entity - A valid entiity.
	 */
	@JECSApi
	public void clear(int entity) 
	{
		ComponentSequence<Component> components = container.get(entity);
		if(!components.isEmpty())
		{
			@SuppressWarnings("unchecked")
			Class<? extends Component> componentT = (Class<? extends Component>) components.get(0).getClass();
			try {
				erase(entity, componentT);
			} catch (JECSException e) {
				e.printStackTrace();
			}
		}
		
		if(!components.isEmpty())
			clear(entity);
		
		components.clear();
	}
	
	/**
	 * Returns the number of components contains in a entity.
	 * 
	 * @param entity - A valid entiity.
	 * @return number of components in a entity.
	 */
	@JECSApi
	public int size(int entity)
	{
		return container.get(entity).size();
	}
	
	/**
	 * Returns the number of entities in a handle map.
	 * 
	 * @return number of entities in a handle map.
	 */
	@JECSApi
	public int size()
	{
		return container.size();
	}
	
	/**
	 * Check entity identifier on valid is or not.
	 * 
	 * @param <E> Type of entity.
	 * @param entity - Entity identifier.
	 * @param msg - Message.
	 * 
	 * @throws JECSException If entity is not valid.
	 */
	@JECSApi
	private <E extends Number> void validationCheck(E entity, String msg) 
			throws JECSException
	{
		final Integer unvalid = -1; //uses Integer instead 'int' because generic type cast.
		@SuppressWarnings("unchecked")
		boolean flag = (entity == null) || (entity == (E) unvalid || !container.containsKey(entity)); 
		if(flag)
			throw new JECSException("Cannot " + msg + " unvalid entity.");
	}
}
