package com.kenny.jecs;

import static com.kenny.jecs.BaseJECS.Reflect.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map.Entry;

import com.kenny.jecs.collection.ComponentSequence;
import com.kenny.jecs.collection.ComponentSequenceImpl;
import com.kenny.jecs.collection.Pair;
import com.kenny.jecs.collection.RawPair;
import com.kenny.jecs.collection.ReversedIterator;
import com.kenny.jecs.collection.ReversedIteratorList;
import com.kenny.jecs.funcs.Create;
import com.kenny.jecs.funcs.ICreate;
import com.kenny.jecs.funcs.Destroy;
import com.kenny.jecs.funcs.IDestroy;
import com.kenny.jecs.funcs.Each;
import com.kenny.jecs.funcs.EachC;
import com.kenny.jecs.funcs.IEachC;
import com.kenny.jecs.funcs.EachE;
import com.kenny.jecs.funcs.IEachE;
import com.kenny.jecs.funcs.IEach;
import com.kenny.jecs.funcs.Patch;
import com.kenny.jecs.funcs.IPatch;
import com.kenny.jecs.funcs.Sort;
import com.kenny.jecs.funcs.SortC;
import com.kenny.jecs.funcs.ISort;
import com.kenny.jecs.funcs.ISortC;

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
 * <code><pre>
 * JECS<<Object>Object> system = JECS.construct();
 * 
 * int entity = jecs.create(); 
 * system.emplace(entity, new TransformComponent()); 
 * or
 * system.emplace(entity, TransformComponent.class, NULL); 
 *
 * while(system.has(TransformComponent.class)) 
 * {
 * 	var t = system.get(entity, TransformComponent.class);
 * 	t.position.add(0.5f, 0.1f, 0.0f);
 * 	System.out.println(t.position);
 * 		
 * 	if(t.position.x >= 50.0f)
 * 		system.erase(entity, TransformComponent.class);
 * }
 *
 * system.destroy(entity); 
 * system = JECS.deconstruct(jecs);
 * </code></pre>
 * 
 * @param <Component> - This is starting hierarchy of component, by default set by Object.
 * You can use any object as a beginning. Any <code>Component</code> object extends of <code>Component</code> will 
 * be recognize for system has a component. 
 * 
 * @author Danil (Kenny) Dukhovenko 
 * @version 0.1.9
 * 
 * TODO: Multithreading support.
 */
@BaseJECS.JECSApi(since =  "0.1.9")
public abstract class BaseJECS<EntityT extends Number, Component extends Object> implements Runnable {	
	private static final String TAB      = "\n    ";
	private static final String BASE_MSG = "Cannot construct component from args.\n" + TAB + "[DETAIL DESCRIPTION]:\n";
	
	/**
	 * Basic implementation of the system using default entity indentifer as {@link Integer}.
	 */
	@BaseJECS.JECSApi(since = "0.1.8")
	public static final class JECS extends BaseJECS<Integer, Object> {
		protected JECS(Class<Integer> entityType) {
			super(entityType);
		}	
	}
	
	/**
	 * Generic implementation of the system using entity indentifer as T.
	 * 
	 * @param <EntityT> - Use entity identifier as {@link Integer} or {@link Long} or {@link Short}.
	 */
	@BaseJECS.JECSApi(since =  "0.1.8")
	public static final class JECSGeneric<EntityT extends Number> extends BaseJECS<EntityT, Object> {
		protected JECSGeneric(Class<EntityT> entityType) {
			super(entityType);
		}
	}
	
	/**
	 * Recognizes the Java Entity Component system API. Indicates that each method or variable 
	 * belongs to the class of this API.
	 * 
	 */
	@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, METHOD, TYPE})
	static @interface JECSApi { String since() default ""; String funcDesc() default "";}

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
	static @interface BetaFeature {}
	
	/**
	 * This is {@link Reflect} class to help with Java Reflection Framework. Its contains
	 * kinda of utility class for sorting, constructing and short writing Reflection methods.
	 * 
	 * @author Danil (Kenny) Dukhovenko 
	 */
	@JECSApi(since = "0.1.1")
	static final class Reflect
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
		static final Class<?> getCtorArgT(Class<?> clazz, int ctorIdx, int argIdx)
		{
			return getCtorArg(clazz, ctorIdx, argIdx).getType();
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
		
		/**
		 * Casts this Class object to represent a subclass of the class represented by the specified 
		 * class object. This method not be call {@link ClassCastException}.
		 * 
		 * @see Class.getClass().isAssignableFrom(from) && Class.getClass().asSubclass(as)
		 */
		@JECSApi
		static <T> Class<? extends T> safeAsSubClass(Class<?> from, Class<T> as, String msg, boolean t) 
		{
	        if (as.isAssignableFrom(from)) { return from.asSubclass(as); }
	        
	        if(t)
	        	throw new JECSUndifiendBehaviourError(msg);
	        
	        return null;
	    }
	}
	
	/**
	 * This class controls and is responsible for the current context of the JECS used. It 
	 * contains detailed information about the system, and we don't recommend changing the 
	 * values unless you know what to do.
	 */
	@JECSApi(since = "0.1.6")
	public static final class Context
	{
		/**Global system counter.*/
	    static final ArrayList<BaseJECS<?, ?>> systems;
		static{
			systems = new ArrayList<BaseJECS<?, ?>>();
		}
		
		/**
		 * Instantiate new system instance.
		 */
		@JECSApi(since = "0.1.6")
		static <EntityT extends Number, CompObj extends Object> JECS 
			newInstance() {
			BaseJECS<?, ?> instance = null;
			systems.add(instance);
			return (JECS) (instance = new JECS(Integer.class));
		}
		
		/**
		 * Instantiate new generic system instance.
		 */
		@SuppressWarnings("unchecked")
		@JECSApi(since = "0.1.8")
		static <EntityT extends Number, CompObj extends Object> JECSGeneric<EntityT>
			newInstance(Class<EntityT> entityType) {
			BaseJECS<?, ?> instance = null;
			systems.add(instance);
			return (JECSGeneric<EntityT>) (instance = new JECSGeneric<EntityT>(entityType));
		}
		
		/**
		 * Delete system instance.
		 */
		@JECSApi(since = "0.1.6")
		static <EntityT extends Number, CompObj extends Object> BaseJECS<EntityT, CompObj> 
			deleteInstance(BaseJECS<EntityT, CompObj> instance) {
			systems.remove(instance);
			instance = null;
			Runtime.getRuntime().gc(); // Attempt to clear memory from handle.
			return instance;
		}
		
		/**
		 * Return new system instance by index.
		 */
		@JECSApi(since = "0.1.6")
		public static BaseJECS<?, ?> getInstance(int index) {
			return systems.get(index);
		}
		
		/**If {@link #randomEntityGenerator} if enabled then will runs random generator of valid entities, otherwise basic 
		 * increment generator.*/
		public boolean ctxRandomEntityGenerator = true;
		/**Max count of entities available by this handle.*/
		public long    ctxMaxEntitiesInt64 = Long.MAX_VALUE - 1;
		public int     ctxMaxEntitiesInt32 = Integer.MAX_VALUE - 1;
		public short   ctxMaxEntitiesInt16 = Short.MAX_VALUE - 1;
		/**Max count of components available by this handle.*/
		public int     ctxMaxComponents = Short.MAX_VALUE - 1;
		/**Default value of pack capacity.*/
		public int     ctxDefaultPackCapacity = 1;
		/**If true, then {@link JECSException} would'nt throws with validation error msg if need.*/
		public boolean ctxDisableExceptionMessages = false;
		/*True its enable optimization.*/
		public static boolean ctxRelease = false;
	}
	
	/**
	 * This class represents null arguments accepted by {@link BaseJECS#emplace(Number, Class, Object...)} or
	 * {@link BaseJECS#replace(Number, Class, Object...)} or its one of array versions to call the appropriate 
	 * method or other similar method where instead of empty args should pass some arg to call approporate
	 * method. 
	 * <P>
	 * Because there is an <code>emplace</code> method that accepts the entity and the object of the
	 * component (already created) and an <code>emplace</code> that  accepts the entity, the type of the 
	 * component and the argument, which can be 0, that is, empty and  in this situation will be called 
	 * by an <code>emplace</code> with an instance and we do not need this, so if you do not have arguments,
	 * just add NULL_ARGS as the first argument.
	 */
	@JECSApi(since = "0.1.7", funcDesc = "constant")
	static final class NullArgs {}
	
	/** NULL_ARGS constant. See {@link NullArgs}. */ 
	public static final BaseJECS.NullArgs NULL_ARGS; 
	static {
		NULL_ARGS = new NullArgs();
	}
	
	/**
	 * Represents a null enttiy identifier. This means that as long as the entity is null, we cannot 
	 * delete, add, or edit its components. This is still a beta concept of null entity and will still 
	 * look for its application in the future. 
	 * <p>
	 * Every time you create an entity or make it null, its identifier will change, and it will be re-created 
	 * with the same components. This class is able to determine this type of entity by a special mask that is added to 
	 * the beginning of the identifier of each entity.
	 * <p>
	 * I also want to note that you should not confuse the usual null keyword with this type of entity
	 * as these are different implementations.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8", funcDesc = "null entity")
	public static final class NullEntity<EntityT extends Number> extends Number {
		static final long serialVersionUID = 1L;

		/** Null entity identifier. */
		EntityT value = null;
		/** Reference to JECS context. */
		BaseJECS<?, ?> context = null;
		
		/**
		 * Construct a new {@link NullEntity} identifier. 
		 */
		NullEntity(final BaseJECS<?, ?> context, final EntityT value) {
			this.context = context;
			this.value = value;
		}
		
		static <EntityT extends Number> NullEntity<EntityT> nullEntity(EntityT entity) {
			var context = JECS.instnace;
			if(context == null)
				throw new JECSException("Cannot convert to NullEntity, context of system is not defined!");
			
			String conversion = "-" + entity;
			EntityT null_entity = null;
			if(!isNullEntity(entity)) {
				null_entity = (EntityT) context.setEntityT(conversion);
				null_entity = (EntityT) replace(entity, null_entity);
			} else 
				throw new JECSException("This is already null entity.");
			return new NullEntity<EntityT>(context, null_entity);
		}
		
		static <EntityT extends Number> EntityT entity(NullEntity<EntityT> nullEntity) {
			var context = JECS.instnace;
			if(context == null)
				throw new JECSException("Cannot convert to entity, context of system is not defined!");
			
			String conversion = "" + (context.isInt16() ? nullEntity.shortValue() : context.isInt32() ? nullEntity.intValue() : 
				nullEntity.longValue());
			EntityT entity = null;
			if(isNullEntity(nullEntity)) {
				entity = (EntityT) context.setEntityT(conversion.substring(1));
				entity = (EntityT) replace(nullEntity, entity);
			} else
				throw new JECSException("This is not null entity to make it normal.");

			return entity;
		}
		
		static <EntityT extends Number> boolean isNullEntity(EntityT entity) {
			String conversion = "" + entity;
			if(conversion.startsWith("-") && conversion.length() > 1)
				return true;
			return false;
		}
		
		static <EntityT extends Number> boolean isNullEntity(NullEntity<EntityT> entity) {
			String conversion = "" + (JECS.instnace.isInt16() ? entity.shortValue() : 
				JECS.instnace.isInt32() ? entity.intValue() : entity.longValue());
			if(conversion.startsWith("-") && conversion.length() > 1) 
				return true;
			return false;
		}
		
		static <EntityT extends Number> EntityT replace(EntityT o, EntityT n) {
			if(NullEntity.class.isAssignableFrom(o.getClass()))
				o = nullEntityToEntityT(o);
			if(NullEntity.class.isAssignableFrom(n.getClass()))
				n = nullEntityToEntityT(n);
			BaseJECS.instnace.raw_destroy(o);
			BaseJECS.instnace.raw_insert(n, entityCount++);
			return n;
		}
		
		// Create tempraty entity null identifier from 'null' keyword.
		static <EntityT extends Number> NullEntity<EntityT> nullEntityFromNullConstnat() {
			return new NullEntity<EntityT>(BaseJECS.instnace, (EntityT)BaseJECS.instnace.setEntityT(-1));
		}
		
		// Converts the EntityT underline type NullEntity to EntityT.
		static <EntityT extends Number> EntityT nullEntityToEntityT(EntityT e) {
			return JECS.instnace.isInt16() ? (EntityT)(Short)e.shortValue() : 
				   JECS.instnace.isInt32() ? (EntityT)(Integer)e.intValue() : (EntityT)(Long)e.longValue();
		}

		@Override
		public int intValue() {
			if(context.isInt32())
				if(value != null)
					return value.intValue();
			throw new JECSException("NullEntity is not int/int32 value!");
		}

		@Override
		public long longValue() {
			if(context.isInt64())
				if(value != null)
					return value.longValue();
			throw new JECSException("NullEntity is not long/int64 value!");
		}
		
		@Override
		public short shortValue() {
			if(context.isInt16())
				if(value != null)
					return value.shortValue();
			throw new JECSException("NullEntity is not short/int16 value!");
		}

		@Override
		public float floatValue() {
			throw new JECSException("NullEntity cannot be float!");
		}

		@Override
		public double doubleValue() {
			throw new JECSException("NullEntity cannot be double!");
		}
	}
	/** NullEntity static constant accessor. See {@link NullEntity}. */ 
	public static final BaseJECS.NullEntity<?> NULL_ENTITY;
	static {
		NULL_ENTITY = new BaseJECS.NullEntity<>(null, null); 
	}
	
	@JECSApi(since = "0.1.8")
	public static final class Group<EntityT extends Number, Component extends Object> implements Iterable<EntityT> {
		BaseJECS<EntityT, Component> system;
		/**Groupped entity by component / components.*/
		List<EntityT> pool = new ArrayList<>();
		
		public Group(BaseJECS<EntityT, Component> system) {
			this.system = system;
		}
		
		/**
		 * See {@link #BaseJECS.get(Number, Class)}.
		 */
		@JECSApi(since = "0.1.9")
		public <C> C get(EntityT entity, Class<C> componentT) {
			return system.get(entity, componentT);
		}
		
		/**
		 * Pair components. See {@link #BaseJECS.getPair(Number, Class, Class)}
		 */
		@JECSApi(since = "0.1.9")
		public <C extends Component, C1 extends Component> 
			Pair<C, C1> getPair(EntityT entity, Class<C> a, Class<C1> b) {
			return system.getPair(entity, a, b);
		}
		
		/**
		 * Two components. See {@link #BaseJECS.getPair(Number, Class, Class)}
		 */
		@JECSApi(since = "0.1.9")
		public <C extends Component, C1 extends Component> 
			C[] get(EntityT entity, Class<C> a, Class<C1> b) {
			return system.get(entity, a, b);
		}
		
		/**
		 * Three components. See {@link #BaseJECS.get(Number, Class...)}
		 */
		@JECSApi(since = "0.1.9")
		public <C extends Component, C1 extends Component, C2 extends Component> 
			C[] get(EntityT entity, Class<C> a, Class<C1> b, Class<C2> c) {
			return system.get(entity, a, b ,c);
		}
		
		/**
		 * Four components. See {@link #BaseJECS.get(Number, Class...)}
		 */
		@JECSApi(since = "0.1.9")
		public <C extends Component, C1 extends Component, C2 extends Component, C3 extends Component> 
			C[] get(EntityT entity, Class<C> a, Class<C1> b, Class<C2> c, Class<C3> d) {
			return system.get(entity, a, b, c, d);
		}
		

		@Override
		public Iterator<EntityT> iterator() {
			return pool.iterator();
		}
		
	}
	
	@JECSApi(since = "0.1.8")
	public static final class View<EntityT extends Number> implements Iterable<EntityT> {
		BaseJECS<EntityT, ?> system;
		List<EntityT> pool = new ArrayList<>();
		
		public View(BaseJECS<EntityT, ?> system) {
			this.system = system;
		}
		
		/**
		 * See {@link #BaseJECS.get(Number, Class)}.
		 */
		@JECSApi(since = "0.1.9")
		public <C> C get(EntityT entity, Class<C> componentT) {
			return system.get(entity, componentT);
		}

		@Override
		public Iterator<EntityT> iterator() {
			return pool.iterator();
		}
	}

	//==========      Entity Identifier      ==============================================
	private final Class<EntityT>                          entityType;
	
	//===========        JECS Handle         ==============================================
	static  BaseJECS<?, ?>                                instnace    = null;
	/**Global statistic thing to calculate how many entities were created.*/
	static  int                                           entityCount = -1;
	/**This is random entity generator.*/
	static  ThreadLocalRandom                             randomEngine = null;
	/**This is non-random entity generator.*/
	EntityT                                               incrementalEngine;
	/**Current emplaced entity.*/
	EntityT                                               currentEmplacedEntity = null;
	/**Contains information about this handle.*/
	Context                                               context;
	
	//===========  Entities, Pools, Containers ==============================================
	ArrayList<EntityT>                                    entities;
	/**Container of entity identifiers and sequence of all components identifiers and his data.*/
	Map<EntityT, ComponentSequence<Component>>            container;
	/**Packs of components to store and for better iteration time.*/
	Map<Integer, ComponentSequence<Component>>            packs;
	/**Pool store the components each type in different sequence. Pool is efficiently faster then container.*/
	Map<Class<Component>, List<Pair<EntityT, Component>>> pool;
	/**Instance of entity group by components.**/
	Group<EntityT, Component>                             group = new Group<>(this);
	/**Instance of entity view by components.**/
	View<EntityT>                                         view = new View<>(this);
	/**Temparary component array stored by #push and released by #pop.*/
	Object[]                                              arr = null;
	/** Global pack index. */
	int                                                   packIndex = 0;
	
	/**Just null is already exist + im love C++.*/
	@JECSApi
	private static final Object nullptr(Object o) { return o = null; }
	
	/**
	 * Return generic class parameters of this class.
	 */
	@JECSApi(since = "0.1.0")
	protected static Class<?> getGenericParameterClass(Class<?> actualClass, int parameterIndex) 
	{
	    return (Class<?>) (
	    		(ParameterizedType) actualClass.getGenericSuperclass()).getActualTypeArguments()[parameterIndex];
	}
	
	/**
	 * Get's the K from V.
	 */
	@JECSApi(since = "0.1.0")
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
	 * String to ByteBuffer.
	 */
	@JECSApi(since = "0.1.3")
	private static ByteBuffer strToBb(String msg, Charset charset)
	{
		return ByteBuffer.wrap(msg.getBytes(charset));
	}
	
	/**
	 * ByteBuffer to String.
	 */
	@JECSApi(since = "0.1.3")
	private static String bbToStr(ByteBuffer buff, Charset charset)
	{
		byte[] bytes;
		if (buff.hasArray()) {
			bytes = buff.array();
			} else {
			bytes = new byte[buff.remaining()];
			buff.get(bytes);
		}
		return new String(bytes);
	}
	
	/**
	 * Sort the string and maps primitive types of classes to objects of these types.
	 */
	@JECSApi(since = "0.1.3")
	private static final Class<?> sort(String s, Class<?> t){
		switch (s) 	{
			case "byte[]"  : return String.class;
			case "int"	   : return Integer.class;
			case "float"   : return Float.class;
			case "boolean" : return Boolean.class;
			case "double"  : return Double.class;
			case "byte"    : return Byte.class;
			case "long"    : return Long.class;
			case "short"   : return Short.class;
			case "char"    : return Character.class;
			default        : return t;
		}
	}
	
	/**
	 * Sort the string and maps objects of primitive types to their normal non-object types.
	 */
	@JECSApi(since = "0.1.3")
	private static final Class<?> sortR(String s, Class<?> t){
		switch (s) {
			case "java.lang.Integer"  : return int.class;
			case "java.lang.Float"    : return float.class;
			case "java.lang.Boolean"  : return boolean.class;
			case "java.lang.Double"   : return double.class;
			case "java.lang.Byte"     : return byte.class;
			case "java.lang.Long"     : return long.class;
			case "java.lang.Short"    : return short.class;
			case "java.lang.Character": return char.class;
			default                   : return t;
		}
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
	@JECSApi(since = "0.1.1", funcDesc = "constructor")
	public static <EntityT extends Number, CompObj extends Object> JECS 
		construct() {
		return Context.newInstance();
	}
	
	/**
	 * Generic version of {@link #construct()}. Type of entity should be {@link Integer} or {@link Long} or {@link Short}, otherwise
	 * you get {@link JECSException}.
	 * 
	 * @param entityType - Acceptable entity type {@link Integer} or {@link Long} or {@link Short}.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "constructor")
	public static <EntityT extends Number, CompObj extends Object> JECSGeneric<EntityT>
		construct(Class<EntityT> entityType) {
		boolean validType = false;
		if(Short.class.isAssignableFrom(entityType) || Integer.class.isAssignableFrom(entityType) || Long.class.isAssignableFrom(entityType))
			validType = true;
		if(!validType)
			throw new JECSException("Cannot costruct the system. The " + entityType.getName() + " is not valid entity identifier type.");
		
		return Context.newInstance(entityType);
	}
	
	/**
	 * Construct instance without validation check. Look at {@link BaseJECS#construct()}.
	 */
	@JECSApi(since = "0.1.7", funcDesc = "constructor")
	public static <EntityT extends Number, CompObj extends Object> JECS 
		construct(boolean optimizedMode) {
		Context.ctxRelease = optimizedMode;	
		return construct();
	}
	
	/**
	 * Construct instance without validation check. Look at {@link BaseJECS#construct(Class))}.
	 * 
	 * @param entityType - Acceptable entity type {@link Integer} or {@link Long}.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "constructor")
	public static <EntityT extends Number, CompObj extends Object> JECSGeneric<EntityT> 
		construct(Class<EntityT> entityType, boolean optimizedMode) {
		Context.ctxRelease = optimizedMode;	
		return construct(entityType);
	}

	/**
	 * Deallocate and deconstruct this <code>system</code>.
	 * 
	 * @param instance to be destroyed.
	 * @return <code>Null</code> instance of constructed system.
	 */
	@JECSApi(since = "0.1.1", funcDesc = "destructor")
	public static <EntityT extends Number, CompObj extends Object> BaseJECS<EntityT, CompObj> 
		deconstruct(BaseJECS<EntityT, CompObj> instance) {
		if(instance != null) {
			try {
				instance.destroyAll();
			} catch (JECSException e) {e.printStackTrace();}
			return Context.deleteInstance(instance);
		}
		throw new NullPointerException("Input instance not exist or already has been destructed.");
	}
	
	/**
	 * Constructs a newly allocated on the heap {@code JECS} object that represents the specified 
	 * {@code container} value or Entity-Component-System handle. This is private constuctor only for <code>construct</code>
	 * method.
	 * <p>
	 * See for constructing {@link #construct}.
	 */
	@JECSApi(funcDesc = "constructor")
	@Deprecated(since = "use JECS.construct() instead", forRemoval = false)
	protected BaseJECS(Class<EntityT> entityType) {
		Thread thread = new Thread(this, "JECS System Thread");
		thread.start();
		
		if(context == null)
			context = new Context();
		
		this.entityType = entityType;
		
		entities = new ArrayList<EntityT>();			
		container = new HashMap<EntityT, ComponentSequence<Component>>();
		pool = new HashMap<Class<Component>, List<Pair<EntityT, Component>>>();
		packs = new TreeMap<Integer, ComponentSequence<Component>>();
		for(int i = 0; i < context.ctxDefaultPackCapacity; i++)
			packs.put(i, new ComponentSequenceImpl<Component>());
			
		if(context.ctxRandomEntityGenerator)
			randomEngine = ThreadLocalRandom.current();
		else {
			if(isInt32())
				incrementalEngine = (EntityT) setEntityT(-1);
			else if(isInt64())
				incrementalEngine = (EntityT) setEntityT(-1);
		}
			
		if(!container.isEmpty())
			this.clear();
		
		instnace = this;
	}
	
	@JECSApi(since = "0.1.7")
	@FunctionalInterface
	public interface ElapseFn { public void fn(); }
	
   /**
 	* The "traditional" way to measure time of JECS method invokation.
 	* 
 	* @param fn - Method where will called JECS method/s to measure time in milliseconds.
 	* @return Meausered time in milliseconds.
 	*/
	@JECSApi(since = "0.1.7")
	public final long elapsed(ElapseFn fn) {
		long s = System.currentTimeMillis();
		fn.fn();
		return System.currentTimeMillis() - s;
	}
	
	/**
	 * Generate a simple valid entity indentifier. If {@link #randomEntityGenerator} if enabled
	 * then will runs random generator of valid entities, otherwise basic increment generator.
	 * 
	 * @return Returns new generated valid entity.
	 */
	@JECSApi(since = "0.1.5")
	private final EntityT generateEntity() {
		if(context.ctxRandomEntityGenerator)
			return generateRandomEntity();
		else 
			return generateNonRandomEntity();
	}
	
	/**
	 * Generate a random valid entity indentifier.
	 * 
	 * @return Returns new generated valid entity.
	 * 
	 * @throws JECSException if type of entity identifier is not acceptable.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.5")
	private final EntityT generateRandomEntity() {
		if (isInt16()) {
			return (EntityT)(Short)(short)randomEngine.nextInt(0, context.ctxMaxEntitiesInt16);
		} else if(isInt32())
			return (EntityT)(Integer)randomEngine.nextInt(0, context.ctxMaxEntitiesInt32);
		else if(isInt64())
			return (EntityT)(Long)randomEngine.nextLong(0, context.ctxMaxEntitiesInt64);
		
		throw new JECSException("Type " + entityType.getName() + " not valid for entity identifier.");
	}
	
	/**
	 * Increase a NON_RAN_GENERATOR by one.
	 * 
	 * @throws JECSException if type of entity identifier is not acceptable.
	 */
	@SuppressWarnings("unchecked")
	private final EntityT generateNonRandomEntity() {
		if(isInt16()) {
			var i = (short)incrementalEngine; i++;
			return incrementalEngine = (EntityT)Short.valueOf(i);
		} else if(isInt32()) {
			var i = (int)incrementalEngine; i++;
			return incrementalEngine = (EntityT)Integer.valueOf(i);
		} else if(isInt64()) {
			var i = (long)incrementalEngine; i++;
			return incrementalEngine = (EntityT)Long.valueOf(i);
		}
		
		throw new JECSException("Type " + entityType.getName() + " not valid for entity identifier.");
	}
	
	/**
	 * Check if this entity type is Integer (short) = 16 bit int.
	 */
	private final boolean isInt16() {
		if(Short.class.isAssignableFrom(entityType))
			return true;
		return false;
	}
	
	/**
	 * Check if this entity type is Integer (int) = 32 bit int.
	 */
	private final boolean isInt32() {
		if(Integer.class.isAssignableFrom(entityType))
			return true;
		return false;
	}
	
	/**
	 * Check if this entity type is Long (long) = 64 bit int.
	 */
	private final boolean isInt64() {
		if(Long.class.isAssignableFrom(entityType))
			return true;
		return false;
	}
	
	/**
	 * Assigment operator for EntityT. Set the value depending on EntityT.
	 */
	@SuppressWarnings("unchecked")
	private final EntityT setEntityT(long v) {
		return isInt16() ? (EntityT) Short.valueOf((short)v) : isInt32() ? (EntityT) Integer.valueOf((int)v) : (EntityT)Long.valueOf(v);
	}
	
	/**
	 * Assigment operator for EntityT.Set the string value depending on EntityT.
	 */
	@SuppressWarnings("unchecked")
	private final EntityT setEntityT(String s) {
		return isInt16() ? (EntityT) Short.valueOf(s) : isInt32() ? (EntityT) Integer.valueOf(s) : (EntityT)Long.valueOf(s);
	}
	
	/**
	 * {@link #create()}.
	 */
	@JECSApi(since = "0.1.0")
	private final EntityT create0() {
		EntityT entity = generateEntity();
		if(entity.intValue() < 0) {
			if(isInt16()) {
				if(entity.shortValue() > context.ctxMaxEntitiesInt16) return create();
			} else if(isInt32()) {
				if(entity.intValue()   > context.ctxMaxEntitiesInt32) return create();
			} else if(isInt64()) {
				if(entity.longValue()  > context.ctxMaxEntitiesInt64) return create();
			}
		}
		
		if(container.containsKey(entity)) {
			// If entity id already created its destroyed and recreates.
			try {
				destroy(entity);
			} catch (JECSException e) {
				e.printStackTrace();
			}
			return create();
		}
		return entity;
	}
	
	/**
	 * Creates new entity identifier. In more detail, it generates a random number in 
	 * the range MAX_ENTITIES, which will be recognized by the handler as an valid entity.
	 * <p>
	 * If an entity with this identifier already exists in the handler, it will delete it and
	 * recreate as empty entity.
	 * <p>
	 * Example:
	 * <blockquote><pre>
	 * var entity = system.create();
	 * </blockquote></pre>
	 * 
	 * @return A valid entity identifier.
	 */
	@JECSApi(since = "0.1.0")
	public final EntityT create() {	

		// Insert entity id and count in global order.
		EntityT entity = create0();
		return insert(entity, entityCount++); 	
	}
	
	/**
	 * Creates new entity identifier. In more detail, it generates a random number in 
	 * the range MAX_ENTITIES, which will be recognized by the handler as an valid entity.
	 * <p>
	 * If an entity with this identifier already exists in the handler, it will delete it and
	 * recreate as empty entity.
	 * <p>
	 * This create accept in parameter {@link ICreate} to send information about entity creation.
	 * <p>
	 * {@link Create} are invoked **after** the object has been assigned to the entity.
	 * <p>
	 * Example:
	 * <pre>
	 * var entity = system.create((entity)->{System.out.println("Entity " + entity + " created!")});
	 * </pre>
	 * 
	 * @param func {@link ICreate} func implementation.
	 * 
	 * @return A valid entity identifier.
	 */
	@JECSApi(since = "0.1.5")
	public final EntityT create(ICreate<EntityT> func) {
		EntityT entity = create0();
		
		// Insert entity id and count in global order.
		entity = insert(entity, entityCount++);
		Create<EntityT> funcImpl = Create.create(func);
		funcImpl.invoke(entity);
		return entity;
	}
	
	/**
	 * See {@link #insert(int)}
	 */
	@Deprecated(since = "use .insert(int) instead")
	@JECSApi(since = "0.1.2")
	public final EntityT[] create(int count) {
		return insert(count);
	}
	
	/**
	 * Unsafe.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public final EntityT raw_insert(Number entity, int globalOrder) {
		return insert((EntityT) entity, globalOrder);
	}
	
	/**
	 * Assign <code>entity</code> identifier to container. 
	 * <p>
	 * Example:
	 * <pre>
	 * system.insert(entity, system.size() - 1);
	 * </pre>
	 * @param entity - The entity to be inserted.
	 * @param globalOrder - Count order of entity to be created.
	 * 
	 * @return  Valid entity identifier.
	 */
	@JECSApi(since = "0.1.2")
	public final EntityT insert(EntityT entity, int globalOrder) {
		if(contains(entity))
			return insert(generateEntity(), globalOrder == 1 ? entityCount++ : globalOrder);
		
		currentEmplacedEntity = entity;
		entities.add(entity);
		
		// Create components array represents component as data structure for entity.
		ComponentSequence<Component> components = new ComponentSequenceImpl<Component>();
		container.put(entity, components);
		return entity;
	}
	
	/**
	 * Assign one or more entities identifiers to container. For more detail, see 
	 * {@link JECSHandle.create()}.
	 * 
	 * @param count - count of entities to be created.
	 * 
	 * @return A array of valid entities identifiers.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.2")
	public final EntityT[] insert(int count){
		EntityT[] entities = isInt16() ? (EntityT[]) new Short[count] : isInt32() ? (EntityT[]) new Integer[count] : (EntityT[]) new Long[count];
		for(int i = 0; i < count; i++)
			entities[i] = create();
		
		return entities;
	}
	
	/**
	 * Unsafe.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public final EntityT raw_destroy(Number entity)  
			throws JECSException {
		return destroy((EntityT) entity);
	}
	
	/**
	 * Destroy <code>entity</code> associated with all components of removed entity.
	 * 
	 * @param entity - identifier of entity to be destroy.
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.0")
	public final EntityT destroy(EntityT entity)  
			throws JECSException {
		validationCheck(entity, "to remove");
		if(NullEntity.class.isAssignableFrom(entity.getClass()))
			throw new JECSException("NullEntity is not destroyable. Make sure that NullEntity was converted to entity."); 
		
		// Remove all components from this entity.
		ComponentSequence<Component> components = container.get(entity);
		for(int i = 0; i < components.size(); i++) {
			Class<? extends Component> componentT = (Class<? extends Component>) components.get(i).getClass();
			erase(entity, componentT);
		}
		
		pool.clear();
		container.remove(entity, components);
		entities.remove(entity);
		if(group.pool.contains(entity))
			group.pool.remove(entity);
		if(view.pool.contains(entity))
			view.pool.remove(entity);
		
		entityCount = entities.size() - 1;
		
		// Is entity doesn't exist, that its succesfful removed and retuned -1. 
		if(!container.containsKey(entity)) {
			Integer answer = -1;	
			entity = (EntityT) answer;
		}
			
		return entity;
	}
	
	/**
	 * Destroy <code>entity</code> associated with all components of removed entity.
	 * 
	 * This destroy accept in parameter {@link IDestroy} to send information about entity destruction.
	 * <p>
	 * {@link IDestroy} are invoked **after** the object has been erased from the entity.
	 * <p>
	 * Example:
	 * <pre>
	 * system.destroy(entity, (entity)->{System.out.println("Entity " + entity + " has been destroyed!")});
	 * </pre>
	 * 
	 * @param func {@link IDestroy} func implementation.
	 * @param <E> Entity type.
	 * @param entity - identifier of entity to be destroy.
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@JECSApi(since = "0.1.5")
	public final EntityT destroy(EntityT entity, IDestroy<EntityT> func)  
			throws JECSException {
		EntityT removedEntity = destroy(entity);
		Destroy<EntityT> funcImpl = Destroy.create(func);
		funcImpl.invoke(entity);
		return removedEntity;
	}
	
	/**
	 * Destroy one or more entities identifiers. For more detail, see 
	 * {@link #destroy()}.
	 * 
	 * @param entities - List of entities to be removed.
	 * 
	 * @return If all entity success destroyed, return total number of 
	 * removed entities.
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.0")
	public final int destroy(EntityT... entities) {
		int totalRemoved = 0;
		for(var e : entities) {
			destroy(e);
			totalRemoved = totalRemoved + 1;
		}
		
		return totalRemoved;
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
	@JECSApi(since = "0.1.0")
	public final void destroyAll() throws JECSException {
		EntityT entity = setEntityT(0);
		ComponentSequence<Component> components = null;
		
		while(!empty()) {
			Iterator<EntityT> enttItr = container.keySet().iterator();
			while(enttItr.hasNext()) {
				entity = enttItr.next();
				
				if(entity.intValue() <= -1) {
					if(isNullEntity(entity))
						throw new JECSException("Attempt to remove NULL_ENTITY.");
					
					throw new JECSException("Attempt to remove unvalid entity.");
				}
					
				
				if(!container.containsKey(entity))
					throw new JECSException("Attempt to remove uncreated entity.");

				//remove all components from this entity.
				components = container.get(entity);
				if(!components.isEmpty()) {
					@SuppressWarnings("unchecked")
					Class<? extends Component> componentT = (Class<? extends Component>) components.get(0).getClass();
					erase(entity, componentT);
				}

				components.clear();	
				break;
			}
			
			pool.clear();
			container.remove(entity, components);
			entities.remove(entity);
			entityCount = entities.size() - 1;
		}
		pop();
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
	@JECSApi(since = "0.1.1")
	public final int destroyInRange(int minRange, int maxRange) {
		int totalRemoved = 0;
		
		for(var entity : this.entities) {
			if(entities.indexOf(entity) >= minRange && 
					entities.indexOf(entity) <= maxRange) {
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
	@JECSApi(since = "0.1.1")
	@BetaFeature
	public final EntityT destroyFirst() throws JECSException { 
		return destroy(entities.get(0));
	}
	
	/**
	 * Destroy last entity identifier in the sequence of entities with all assigns 
	 * components.
	 * 
	 * @return If entity success destroyed, return -1, otherwise other positive number. 
	 * 
	 * @throws JECSException if try to destory unexisting entity.
	 */
	@JECSApi(since = "0.1.1")
	@BetaFeature
	public final EntityT destroyLast() throws JECSException { 
		return destroy(entities.get(entities.size() - 1)); 
	}
	
	/**
	 * Construct new {@link java.lang.Object} from input arguments. There is a detailed description under 
	 * each {@link #emplace(E, Class, Object...)} implementation. If user try construct inner class it doesn't need to put the top class 
	 * argument this method determines this automatically.
	 * <p>
	 * <b>Inner class support:</b> (<code>p</code> - package, <code>A, B, C</code>, ... ,  <code>n</code> - inner
	 * class name, <code>$</code> - class seperator. ) If <code>B</code> inside <code>A</code> or <code>C</code> inside
	 * <code>B</code> (inner classes) etc... then  this should work, because im call the invisible (default) ctor
	 * of inner class with only one arg of top class ( <code>public p.A$B(p.A)</code> ) or zero arg (<code> public p.A()</code> ).
	 * <p>
	 * If some of top class have user ctor with args then they will be ignored because it is impossible 
	 * to understand what exactly the user needs a ctor, such are the victims of reflection. 
	 * <p>
	 * In general this function not safty if use it internal, it can throw many unhandled exceptions and can accept
	 * <code>null</code> objects to also can break the system.
	 * 
	 * @param <C> - Type of component.
	 * @param componentT - {@link Object} will return.
	 * @param args - Argument to {@link Object} constructor.
	 * @return New instance of {@link Object} component.
	 *
	 * @throws JECSException If user try to place incorrect arg of component.
	 * @throws ClassNotFoundException If type <code>C</code> is not exist.
	 * @throws NoSuchMethodException If Java does not find a suitable constructor for the passed arguments of object <code>C</code>. 
	 * In this case, create this constructor in the class with the following arguments.
	 * @throws InvocationTargetException When invalid invokation.
	 * @throws SecurityException If cause error with {@link ProtectionDomain}.
	 * @throws IllegalArgumentException Invalid argument.
	 * @throws IllegalAccessException Invalid access.
	 * @throws InstantiationException Invalid initialization.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@JECSApi(since = "0.1.3, last = 0.1.7", funcDesc = "constructFromArgs obj")
	private final < C extends Component> C constructFromArgs(Class<C> componentT, Object... args) 
			throws JECSException, SecurityException, 
					ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, 
							InvocationTargetException {
		
		// This arguments will be pass to get currect costructor and invoke it.
		Class<?>[] finalCtorArgs = null;
		
		// If somehow object converts to array with size one and parsed in to another array (Object...), we
		// get the elements from 'fake' array and convert to normal single-array.
		if(args.length == 1) {
			if(args[0].getClass().isArray()) {
				Object[] argss = (Object[]) args[0];
				Object[] newargs = new Object[argss.length];
				for(int i = 0; i < argss.length; i++)
					newargs[i] = argss[i];
				args = newargs;
			}
		}
		
		// Top level class info if componentT is member.
		Class<?> enclosingClassInfo = componentT.getEnclosingClass();
		boolean isInnerClass = enclosingClassInfo != null;
		boolean isInnerStaticClass = false;
		boolean isInnerCheck = false;
		boolean isNullArgs = false;
		
		// Convert java null arguments to constant NULL.
		if(args == null)    
			args = new Object[] { NULL_ARGS };
		else if(args[0] == NULL_ARGS) 
			isNullArgs = true;
		
		// Inner and NULL arguments situation.
		if(isNullArgs && isInnerClass) {
			finalCtorArgs = new Class<?>[1];
			finalCtorArgs[0] = enclosingClassInfo;
		}
		
		// If user pass NULL constant skip hole code and invoke.
		if(!isNullArgs) {
			// Compare stage: The stage of checking constructors and their parameters.
			int argsCount = 0, passedArgsCount = args.length;
			if(componentT.getConstructors().length == 0 && args.length != 0)
				throw new JECSUndifiendBehaviourError(BASE_MSG + "Component <"+ componentT.getTypeName() +"> without default constructor cannot have args!");
			
			// Compare each component ctor with input args.
			boolean validArgument = false;
			boolean foundCtor = false;
			int ctorIdx = 0;
			for(Constructor<?> ctor : componentT.getConstructors()) {
				boolean isLastCtor = ctorIdx == componentT.getConstructors().length - 1;
				argsCount = ctor.getParameterCount();
				
				if(isInnerClass && !isInnerCheck) {
					isInnerStaticClass = Modifier.isStatic(componentT.getModifiers());
					if(!isInnerStaticClass) {
						passedArgsCount++;
						isInnerCheck = true;
					}
				}
				
				// If is empty ctor, stop iterate over all ctors and go to invoke stage.
				boolean isEmptyCtor = passedArgsCount == 0 && argsCount == 0;
				if(isEmptyCtor) {
					break; //true
				}
				
				// True if args.length zero i.e default ctor or if number of arguments will the same 
				// from args.length and componentT ctor count.
				boolean isSuitableCtor = isEmptyCtor || passedArgsCount == ctor.getParameterCount();
				if(isSuitableCtor) {
					finalCtorArgs = new Class<?>[passedArgsCount];
					
					//Compare each component param/arg in current ctor.
					int argIdx = 0, argShift = 0;
					for(Class<?> arg : ctor.getParameterTypes()) {
						// Non-static inner situation. We shoold add instance of top class as zero arg before 
						// invoke.
						if(isInnerClass && !isInnerStaticClass && argIdx == 0) {
							finalCtorArgs[0] = enclosingClassInfo;
							argShift++;
							argIdx++;
							continue;
						}
						
						Class<?> passedArg = args[argIdx - argShift].getClass();
											
						// Interface/annotation situation.
						if(arg.isInterface()) {
							if(safeAsSubClass(passedArg, arg, BASE_MSG + 
									"Cannot compare interface <" + arg + "> with class <" + passedArg + ">.") != null) {
								arg = passedArg;
							}
						}
						
						// Narrow conversion beetwen primitives and its wrappers.
						boolean isPrimitive = arg.isPrimitive();
						if(isPrimitive) 
							arg = sort(arg.getName(), arg);
						
						// Compare normal arguments.
						if(passedArg != arg) {
							if(!isLastCtor)  {
								validArgument = false;
								break;
							}

							throw new JECSUndifiendBehaviourError(BASE_MSG + TAB + "Try to place incorrect arg of component <" + componentT.getName() + ">. " 
									+ "Component " + TAB + "arg"+ argIdx + "[" + arg + "] not equals passed arg" + argIdx + "["+ passedArg +"] type of costructor" + TAB + ctor +"!");
						} else {
							if(!isPrimitive)
								finalCtorArgs[argIdx] = passedArg;
							else
								finalCtorArgs[argIdx] = sortR(arg.getName(), arg);
							validArgument = true;
						}
						
						// If argIdx == arg element length and is valid arg, that means we found constructor.
						if(argIdx == ctor.getParameterTypes().length - 1 && validArgument) {
							foundCtor = true;
							break;
						}
						
						argIdx++;
					}
				}
		
				if(!isSuitableCtor && isLastCtor && !validArgument) {
					String errorMsg = BASE_MSG + TAB + "There was no suitable constructor for creating " + componentT.getSimpleName() +".";
					String avaliableCtors = TAB + "Avaliable constructors:";
					for(Constructor<?> ct : componentT.getConstructors()) avaliableCtors += TAB + " " + ct;
						throw new JECSUndifiendBehaviourError(errorMsg + avaliableCtors);
				} 
				
				if(foundCtor)
					break;
					
				ctorIdx++;
			}
		} else {
			//Constructing stage for NULL:
			Constructor<?> ctor = componentT.getDeclaredConstructors()[0];
			ctor.setAccessible(true);
			if(isInnerClass && !isInnerStaticClass) {
				Object[] argss = getInnerObjects(args, finalCtorArgs, isNullArgs);;
				return (C) ctor.newInstance(argss);
			} else 
				return (C) ctor.newInstance((Object[])null);
		}
		
		//Constructing stage:
		Constructor<?> ctor = componentT.getDeclaredConstructor(finalCtorArgs);
		ctor.setAccessible(true);
		if(isInnerClass && !isInnerStaticClass) {
			Object[] argss = getInnerObjects(args, finalCtorArgs, isNullArgs);
			return (C) ctor.newInstance(argss);
		} else 
			return (C) ctor.newInstance(args);
	}
	
	private Object[] getInnerObjects(Object[] args, Class<?>[] finalCtorArgs, boolean isNullArgs) throws InstantiationException, IllegalAccessException, 
		IllegalArgumentException, InvocationTargetException, SecurityException, ClassNotFoundException {
		// Split main full inner class name to each sub-inner classes and put values in array.
		String[] innerClassesNames = finalCtorArgs[0].getName().split("\\$");

		// Construct all inner classes from 'first top' to 'last inner'.
		Object inner = null;
		for(int i = 0; i < innerClassesNames.length; i++) {
			Constructor<?> innerCtor = null;
			if(i == 0) {
				innerCtor = Class.forName(innerClassesNames[i]).getDeclaredConstructors()[0];
				inner = innerCtor.newInstance((Object[])null);
				continue;
			} else {
				innerClassesNames[0] += "$" + innerClassesNames[i];
				innerCtor = Class.forName(innerClassesNames[0]).getDeclaredConstructors()[0];
				inner = innerCtor.newInstance(inner);
			}
			
		}
		
		// Adding the inner class to the main arguments by redefining the 'args' array.
		Object[] argss;
		if(!isNullArgs) {
			argss = new Object[args.length + 1];
			argss[0] = inner;
			for(int i = 1; i < argss.length; i++) 
				argss[i] = args[i - 1];
		} else {
			argss = new Object[1];
			argss[0] = inner;
		}
		return argss;
	}
	
	/**
	 * Emplace <code>C</code> component with to that <code>entity</code>. Assigns the
	 * given component to an entity.
	 * <p>
	 * If component object is <code>null</code> then you get {@link JECSException} because you cannot
	 * contain and hold not initialized objects.
	 * <p>
	 * For example:
	 * <pre>
	 * system.emplace(entity, Component.class, new Component("First", 0, true).
	 * </pre>
	 * 
	 * Advanced version of this method is emplace component by args {@link #emplace(E, Class, Object[])}.
	 * <p>
	 * NOTE: If C type of component is class type, then it will change the overload to {@link #emplace(Number, Class, Object...)}.
	 * 
	 * @param <C> Type of component.
	 * @param entity - The entity that the component will be attached to.
	 * @param componentT - Class type of component.
	 * @param component - Data structure to be added how component.
	 */
	@JECSApi(since = "0.1.0", funcDesc = "emplace c-object")
	@SuppressWarnings("unchecked")
	public final <C extends Component> C emplace(EntityT entity, C component) 
			throws JECSException {
		
		if(component == null)
			throw new JECSException("Cannot emplace not initialized component!");
		
		// Check if C component is instance and not a class type. If class type we change the overload.
		if(component.getClass().getTypeName().contains("java.lang.Class"))
			return emplace(entity, (Class<C>) component, NULL_ARGS);
		
		validationCheck(entity, "emplace component to");
		if(!contains(entity))
			return null;
		
		if(has(entity, component))
			throw new JECSException("Component with type <" + component.getClass().getTypeName() + "> already existing!" );
		
		ComponentSequence< Component> components = container.get(entity);
		components.emplace(component);
		
		// Check if poll has C type of component. And add new pair with entity-component.
		List<Pair<EntityT, Component>> pairs = null;
		if(pool.containsKey(component.getClass())) {
			pairs = pool.get(component.getClass());
			pairs.add(new Pair<EntityT, Component>(entity, (Component) component.getClass()));
		// Otherwise create new list with pairs and add new pair with entity-component and then put it in to poll.
		} else {
			pairs = new ArrayList<Pair<EntityT, Component>>();
			pairs.add(new Pair<EntityT, Component>(entity, (Component) component.getClass()));
			pool.put((Class<Component>) component.getClass(), pairs);
		}
		return component;
	}
	
	/**
	 * Emplace <code>C</code> component with to that <code>entity</code>. Assigns the given 
	 * component to an entity. This is advanced version of <code>emplace(E entity, Class<C> componentT, 
	 * C component)</code>, instead of object you pass args and object create in automatically. Component can 
	 * also be with private or protected modifiers or inner private modifiers.
	 * <p>
	 * This method will take all <code>args</code> arguments and use them to create a new component object. Before this,
	 * the parameters of one of the class <code>C</code> constructors and the specified arguments will be checked for 
	 * compliance. If there is an error related to a conflict between the args and constructor arguments it will 
	 * {@link JECSUndifiendBehaviourError} crash. If there are no errors, the object will be built in runtime.
	 * For more deep information about see constructing {@link #constructFromArgs(Class, Object...)}.
	 * <p>
	 * If component arguments is <code>null</code> then you get {@link JECSException} because you cannot
	 * contain and hold not initialized objects. Do not confuse this with null (zero) argument passing, since 
	 * then the default constructor will be called if it is present. Better solution for this be use in-build
	 * class-constant-indicator {@link NullArgs} to say that this constructor doesen't accept any number of arguments.
	 * <p>
	 * For example:
	 * <pre>
	 * 	       | entty | component type | component args |
	 * system.emplace(entity, Component.class, "First", 0, true).
	 * </pre>
	 * <p>
	 * If component constructor doesn't apply zero arguments pass {@link NullArgs}. If user pass {@link NullArgs} and
	 * component won't have constructor with zero arguments its crash.
	 * @apiNote If you are interested in performance for your program, we would recommend using the 
	 * {@link #emplace(E, Class, C)}. method, but this will not play a big role because there will be 
	 * a difference in speed of several nanoseconds. If your components are created once at the time of 
	 * initialization of the program, then this will not play a role at all, and if for some reason you add
	 * components during the operation of the application, it is better to use the option above. The conclusion
	 * is that in fact both methods are optimized only the upper one is a couple of nanoseconds faster.
	 * 
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentT - Class type of component.
	 * @param args - Arguments (VarArgs) of component <code>C</code> constructor.
	 * 
	 * @return The emplaced component instance.
	 *
	 * @throws NoSuchMethodException If Java does not find a suitable constructor for the passed arguments of object <code>C</code>. 
	 * 								 In this case, create this constructor in the class with the following arguments.
	 * @throws JECSException If entity not valid or <code>null</code>.
	 * @throws JECSUndifiendBehaviourError If conflict between the <code>args</code> and <code>C</code> constructor
	 * arguments.
	 */
	@JECSApi(since = "0.1.1", funcDesc = "emplace c-args")
	public final <C extends Component> C emplace(EntityT entity, Class<C> componentT, Object... args) 
			throws JECSException {
		try {
			currentEmplacedEntity = entity;
			return emplace(entity, (C) constructFromArgs(componentT, args));
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | 
				InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException e) { e.printStackTrace(); }
		
		return null;
	}
	
	/**
	 * Emplace array of <code>C</code> components with different types to that <code>entity</code>. Sequence of arrays with component arguments
	 * should be in the same order. Basically this is extended version of {@link #emplace(E, Class, Object[])}.
	 * <p>
	 * For a detailed description, see here -> {@link #emplace(E, Class, Object[])}.
	 * For more deep information about constructing see -> {@link #constructFromArgs(Class, Object...)}.
	 * <p>
	 * For example:
	 * <pre>
	 * system.emplace(entty, 
	 * 	new Class<?>[]{ TagComponent.class,      TransformComponent.class}, 
	 * 	new Object[] { "Taggiy" }, new Object[] { new Vector3f(1, 2, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1) });                                                  
	 * </pre>
	 * <p>
	 * If component constructor doesn't apply zero arguments pass {@link NullArgs}. If user pass {@link NullArgs} and
	 * component won't have constructor with zero arguments its crash.
	 * 
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentTs - Array of class types for each component.
	 * @param args - A sequence of arrays with the passed component arguments of each <code>C</code> constructor, in the same order as
	 * the type transfer. 
	 * 
	 * @retruns The Object[] array represent components with different type witch user emplaced.
	 *
	 * @throws JECSException If entity not valid or <code>null</code>.
	 * @throws JECSUndifiendBehaviourError If conflict between the <code>args</code> and <code>C</code> constructor
	 * arguments.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.1, last = 0.1.7", funcDesc = "emplace array of c-args")
	public final <C extends Component> Component[] emplace(EntityT entity, Class<?>[] componentTs,
			Object[]... args) throws JECSException {
		Component[] components = null;
		// Args is normal array of arguments for each component type.
		for(int i = 0; i < componentTs.length; i++) {
			if(components == null) 
				components = (Component[]) new Object[componentTs.length];
			
			// Convert args array of arrays to single args[i] array for each component type.
			components[i] = emplace(entity, (Class<C>)componentTs[i], args[i]);
		}
		return components;
	}
	
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8", funcDesc = "emplace ComponentSequence")
	public final void emplaceEmptySequence(EntityT entity, final ComponentSequence<?> components) {
		if(container.get(entity) == null || container.get(entity).isEmpty()) 
			container.put(entity, (ComponentSequence<Component>) components);
	}
	
	/**
	 * Replaces <code>entity</code> with component only if this entity already exist in the container of entities. If 
	 * previusly entity with identiefier <code>entity</code> has component with this type it will be 
	 * replaced with new <code>componentT</code> and its <code>args</code> to sequence, and old component data
	 * returns.
	 * <p>
	 * If this entity doesnt exitst in container this method does nothing.
	 * <p>
	 * If component constructor doesn't apply zero arguments pass {@link NullArgs}. If user pass {@link NullArgs} and
	 * component won't have constructor with zero arguments its crash.
	 * 
	 * @param args - Arguments of component <code>C</code> constructor.
	 * @param componentT - Class type of component.
	 * @param entity - The entity whose components will be reassembled.
	 * @param <C> Component Type
	 * @return The previous instance of that component if exist, else null.
	 */
	@JECSApi(since = "0.1.2, last = 0.1.7")
	public <C extends Component> C replace(EntityT entity, Class<C> componentT, Object... args) 
			throws JECSException {
		try {
			C replacedComponent = (C) constructFromArgs(componentT, args);
			C oldComponent = replace(entity, replacedComponent);
			return oldComponent;
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException | 
				InstantiationException | IllegalAccessException | IllegalArgumentException |
				InvocationTargetException e) { e.printStackTrace(); };
		return null;
	}
	
	/**
	 * Replaces <code>entity</code> with component only if this entity already exist in the container of entities. If 
	 * previusly entity with identiefier <code>entity</code> has one or more  components it will be 
	 * discarded and replaced with new <code>componentT</code> and its <code>args</code> to sequence.
	 * <p>
	 * If this entity doesnt exitst in container this method does nothing.
	 * 
	 * @param args - Arguments (VarArgs) of component <code>C</code> constructor.
	 * @param entity - The entity whose components will be reassembled.
	 * @param <C> Component Type
	 * @return The replaced instance of that component.
	 */
	@JECSApi(since = "0.1.7")
	public <C extends Component> C replace(EntityT entity, C component) throws JECSException {
		try {
			if(contains(entity)) {
				ComponentSequence<Component> components = container.get(entity);
				C oldComponent = get(entity, component.getClass());
				int replacedIdx = components.indexOf(oldComponent);
				components.remove(replacedIdx);
				components.add(replacedIdx, component);
				
				// Replace component for the pool.
				@SuppressWarnings("unchecked")
				Class<Component> type = (Class<Component>) component.getClass();
				List<Pair<EntityT, Component>> pairs = pool.get(type);
				for(Pair<EntityT, Component> pair : pairs) {
					if(isInt16()) {
						if(pair.first.shortValue() == entity.shortValue()) {
							pair.second = component;
							break;
						}
					} else if(isInt32()) {
						if(pair.first.intValue() == entity.intValue()) {
							pair.second = component;
							break;
						}
					} else if(isInt64()) {
						if(pair.first.longValue() == entity.longValue()) {
							pair.second = component;
							break;
						}
					}
				}
				return oldComponent;
			}
		} catch ( SecurityException | IllegalArgumentException e) { e.printStackTrace(); };
		return null;
	}
	
	/**
	 * Replaces <code>entity</code> with one or more components only if this entity already exist in the container 
	 * of entities. If previusly entity with identiefier <code>entity</code> has one or more  components it will be 
	 * discarded and replaced with new <code>componentTs</code> and its <code>arrayArgs</code> to sequence.
	 * <p>
	 * If this entity doesnt exitst in container this method does nothing.
	 *
	 * @param arrayArgs - Array of arguments (VarArgs) of component <code>C</code> constructor.
	 * @param entity - The entity whose components will be reassembled.
	 * @param componentTs -  Array of class types for each component.
	 * @param <C> Component Type
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.2")
	public <C extends Component> Component[] replace(EntityT entity, Class<?>[] componentTs, 
			Object[]... arrayArgs) throws JECSException {
		try {
			if(contains(entity)) {
				Component[] components = null;
				// Args is normal array of arguments for each componentTs.
				for(int i = 0; i < componentTs.length; i++) {
					if(components == null) 
						components = (Component[]) new Object[componentTs.length];
					
					// Convert argsArray to single array for each args.
					components[i] = replace(entity, (Class<C>)componentTs[i], arrayArgs[i]);
				}
				return components;
			}
		} catch (SecurityException | IllegalArgumentException  e) { e.printStackTrace(); };
		return null;
	}
	
	/**
	 * This method will be replace or emplace <code>entity</code> with all assisisated components. If 
	 * 
	 * Replaces <code>entity</code> only if this entity already exist in the container of entities. If previusly 
	 * entity with identiefier <code>entity</code> has component it will be discarded and replaced.
	 * 
	 * <p> Its identical to this code snipped:
	 * <pre>
	 * if(contains(entity)
	 * 	replace(entity, componentT, args...)
	 * else {
	 * 	insert(entity, size() - 1);
	 * 	emplace(entity, componentT, args...)
	 * }
	 * </pre>
	 * @param args - Arguments (VarArgs) of component <code>C</code> constructor.
	 * @param entity - The entity whose components will be replaced or emplaced.
	 * @param componentT - Class type of component.
	 * @param <C> Component Type
	 */
	@JECSApi(since = "0.1.2, last = '0.1.8'")
	public <C extends Component> void replaceOrEmplace(EntityT entity, Class<C> componentT, 
			Object... args) {
		try {
			if(contains(entity) && has(entity, componentT))
				replace(entity, componentT, args);
			else if(contains(entity)) {
				emplace(entity, componentT, args);
			} else { 
			 	insert(entity, size() - 1);
				emplace(entity, componentT, args);
		    }
		} catch (JECSException e) {e.printStackTrace();}
	}
	
	/**
	 * This method will be replace or emplace <code>entity</code> with all assisisated components. If 
	 * 
	 * Replaces <code>newEntity</code> only if this entity already exist in the container of entities. If previusly 
	 * entity with identiefier <code>newEntity</code> has one or more components it will be discarded and replaced 
	 * with empty component sequence.
	 * 
	 * <p> Its identical to this code snipped:
	 * <pre>
	 * if(contains(entity)
	 * 	replace(entity, componentTs, arrayArgs...)
	 * else {
	 * 	insert(entity.intValue(), size() - 1);
	 * 	emplace(entity, componentTs, arrayArgs...)
	 * }
	 * </pre>
	 * @param arrayArgs - Array of arguments (VarArgs) of component <code>C</code> constructor.
	 * @param entity - The entity whose components will be replaced or emplaced.
	 * @param componentTs - Array of class types for each component.
	 * @param <C> Component Type
	 */
	@JECSApi(since = "0.1.2, last = '0.1.8'")
	public <E extends Number, C extends Component> void replaceOrEmplace(EntityT entity, Class<?>[] componentTs,
			Object[]... argsArray) {
		try {
			if(contains(entity) && anyArray(entity, componentTs))
				replace(entity, componentTs, argsArray);
			else if(contains(entity)) {
				emplace(entity, componentTs, argsArray);
			} else { 
			 	insert(entity, size() - 1);
				emplace(entity, componentTs, argsArray);
		    }
		} catch (JECSException e) {e.printStackTrace();}
	}

	/**
	 * Erase component from <code>entity</code> if it already been mapped to containter
	 * sequence of components. After this calls object component will be equals <code>null</code>, 
	 * so you cannot  use it anymore, because JVM Garbage Collector delete it from memory.
	 * 
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentT - Class type of component.
	 * 
	 * @return The erased component instance.
	 * @throws JECSException If entity not valid.
	 */
	@JECSApi(since = "0.1.0")
	public final <C extends Component> Component erase(EntityT entity, Class<C> componentT)
			throws JECSException {
		validationCheck(entity, "erase component from");
		
		if(!has(entity, componentT))
			throw new JECSException("Cannot to remove non-existing component!");
		
		ComponentSequence<Component> components = container.get(entity);
		C component = get(entity, componentT), componentInst = null;
		componentInst = component;
		components.erase(component);
		return componentInst;
	}
	
	/**
	 * Erase one or more components from <code>entity</code> if its already been mapped to containter
	 * sequence of components. After this calls object component(s) will be equals <code>null</code>, 
	 * so you cannot use it anymore, because JVM Garbage Collector delete it from memory.
	 * 
	 * @param <C> Component type.
	 * @param entity - The entity identifier that the component will be attached to.
	 * @param componentTs - Class type of component(s).
	 * 
	 * @return The erased components array.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.0")
	public final <C extends Component> List<Component> erase(EntityT entity, Class<? extends C>... componentTs)
			throws JECSException {
		List<Component> componentInst = new ArrayList<Component>();
		for(int i = 0; i < componentTs.length; i++)
			componentInst.add(erase(entity, (Class<? extends C>) componentTs[i]));
		return componentInst;
	}
	
	/**
	 * Release a entity. Removes all components from <code>entity</code> if its already been mapped to containter
	 * sequence of components. After this calls object component(s) will be equals <code>null</code>, 
	 * so you cannot use it anymore, because JVM Garbage Collector delete it from memory.
	 * 
	 * @param entity - The entity identifier that the component will be attached to.
	 * 
	 * @return The released components array.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@JECSApi(since = "0.1.8")
	public final <C extends Component> List<Component> release(EntityT entity)
			throws JECSException {
		clear(entity);
		return null;
	}
	
	/**
	 * Safe version of {@link #erase(Number, Class)}. 
	 */
	@JECSApi(since = "0.1.2")
	public final <C extends Component> Component remove(EntityT entity, Class<C> componentT)
			throws JECSException {
		if(has(entity, componentT)) {
			ComponentSequence<Component> components = container.get(entity);
			C component = get(entity, componentT), componentInst = null;
			componentInst = component;
			components.erase(component);
			return componentInst;
		}
		return null;
	}
	
	/**
	 * Safe version of {@link #erase(Number, Class...)} 
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.2")
	public final <C extends Component> List<Component> remove(EntityT entity, Class<? extends C>... componentTs)
			throws JECSException {
		List<Component> componentInst = new ArrayList<Component>();
		for(int i = 0; i < componentTs.length; i++)
			componentInst.add(remove(entity, (Class<? extends C>) componentTs[i]));
		return componentInst;
	}
	
	/**
	 * Remove all components from this entity.
	 * 
	 * @param entity - A valid entiity.
	 * @param <C> Component type.
	 */
	@JECSApi(since = "0.1.2")
	public <C extends Component> void removeAll(EntityT entity) {
		ComponentSequence<Component> components = container.get(entity);
		while(!components.isEmpty()) {
			@SuppressWarnings("unchecked")
			Class<? extends Component> componentT = (Class<? extends Component>) components.get(0).getClass();
			try {
				erase(entity, componentT);
			} catch (JECSException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Removes the given components from an entity.
     * <p>
     * Equivalent to the following snippet:
     * <p>
     * <blockquote><pre>
     * if(system.has<Component>(entity)) 
     * 	system.remove<Component>(entity); 
     * </blockquote></pre>
     * Prefer this function anyway because it has slightly better performance.
     * <p>
     * @param <Component> Types of components to remove.
     * @param entity A valid entity identifier.
     * @param <C> - Var types of components.
     * @param componentTs - Component class types.
     */
	@JECSApi(since = "0.1.2")
	public <C extends Component> void removeIfExist(EntityT entity, Class<?>... componentTs) 
			throws JECSException {
		validationCheck(entity, "remove from");
		if(container.containsKey(entity)) {			
			for(int i = 0; i < componentTs.length; i++) {
				@SuppressWarnings("unchecked")
				Class<? extends C> componentT = (Class<? extends C>) componentTs[i]; 
				container.get(entity).erase(get(entity, componentT));
			}
		}
	}

	/**
	 * This method invoke function/method from entity <code>C</code> component at runtime.
	 * This method is usually not the best and fastest, but it is very effective and useful when 
	 * the reference to the component is unknown and only the type is known.
	 * <p>
	 * I would try using the fastest ways to call the method, such as writing algorithms in JNI C++ and
	 * loading in Java, maybe Yes, but in Java I don't know any faster ways.
	 * 
	 * @param <C> Component type.
	 * @param entity - The valid entity.
	 * @param componentT - The component that the function will be called from.
	 * @param funcName - Name of function/method which will be called.
	 * @param funcArgs - Arguments to function/method.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@JECSApi(since = "0.1.3", funcDesc = "invoke")
	public synchronized <C extends Component> void invoke(EntityT entity, Class<C> componentT, String funcName,
			Object... funcArgs) throws JECSException {		
		try  {
			Class<?>[] funcArgsTypes = new Class<?>[funcArgs.length]; 
			for(int t = 0; t < funcArgs.length; t++) 
				funcArgsTypes[t] = sortR(funcArgs[t].getClass().getTypeName(), funcArgs[t].getClass());
			
			Method func = componentT.getMethod(funcName, funcArgsTypes);
			func.setAccessible(true);
			func.invoke(get(entity, componentT), funcArgs);
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException |
				IllegalArgumentException | InvocationTargetException e) 
					{ e.printStackTrace();}
	}
	
	/**
	 * This method invoke function/method from entity for each <code>C</code> components at runtime. If 
	 * at least one component doesen't have <code>funcName</code> with <code>funcArgs</code> this method 
	 * throws one of system exceptions, namely {@link NoSuchMethodException} or {@link InvocationTargetException}.
	 * <p>
	 * This method is usually not the best and fastest, but it is very effective and useful when 
	 * the reference to the component is unknown and only the type is known.
	 * <p>
	 * I would try using the fastest ways to call the method, such as writing algorithms in JNI C++ and
	 * loading in Java, maybe Yes, but in Java I don't know any faster ways.
	 * 
	 * @param <C> Component type.
	 * @param entity - The valid entity.
	 * @param componentTs - The components that for each of it the function will be called from.
	 * @param funcName - Name of function/method which will be called.
	 * @param funcArgs - Arguments to function/method.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.3", funcDesc = "invoke for each type")
	public synchronized <C extends Component> void invokeEach(EntityT entity, Class<?>[] componentTs, String funcName,
			Object... funcArgs)
	{
		for(int c = 0; c < componentTs.length; c++)
			try {
				invoke(entity, (Class<C>)componentTs[c], funcName, funcArgs);
			} catch (JECSException e) {e.printStackTrace();}
	}
	
	/**
	 * This method invoke function/method from entity for each <code>C</code> components if it subclass 
	 * of <code>componentT</code> at runtime. If at least one component doesen't have <code>funcName</code> 
	 * with <code>funcArgs</code> this method throws one of system exceptions, namely {@link NoSuchMethodException} 
	 * or {@link InvocationTargetException}.
	 * <p>
	 * This method is usually not the best and fastest, but it is very effective and useful when 
	 * the reference to the component is unknown and only the type is known.
	 * <p>
	 * I would try using the fastest ways to call the method, such as writing algorithms in JNI C++ and
	 * loading in Java, maybe Yes, but in Java I don't know any faster ways.
	 * 
	 * @param <C> Component type.
	 * @param entity - The valid entity.
	 * @param componentT - The component base that for each of it subclass the function 
	 * will be called from.
	 * @param funcName - Name of function/method which will be called.
	 * @param funcArgs - Arguments to function/method.
	 * 
	 * @throws JECSException If entity not valid.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.4", funcDesc = "invoke for each if subclass")
	public synchronized <C extends Component> void invokeEach(EntityT entity, Class<C> componentT, String funcName,
			Object... funcArgs) throws JECSException {
		try {
			// For each checks if comp subclass of componentT.
			for(int i = 0; i < container.get(entity).size(); i++) {
				Component child = container.get(entity).get(i);
				if(safeAsSubClass(child.getClass(), componentT, "", false) != null) {
					invoke(entity, (Class<C>)child.getClass(), funcName, funcArgs);
				}
			}
		} catch (  SecurityException | IllegalArgumentException  e) 
			{ e.printStackTrace();}
	}
	
	/**
	 * Compare class a with other class b. If it equals return true, otherwise false.
	 * 
	 * @param a - Class to compare with.
	 * @param b - Class that is being compared.
	 */
	@JECSApi(since = "0.1.5", funcDesc = "class with class")
	public boolean eqs(Class<?> a, Class<?> b) {
		return a == b ? true : false;
	}
	
	/**
	 * Compare object-class a with other class b. If it equals return true, otherwise 
	 * false.
	 * 
	 * @param a - Class-object to compare with.
	 * @param b - Class that is being compared.
	 */
	@JECSApi(since = "0.1.5", funcDesc = "object with class")
	public boolean eqs(Object a, Class<?> b) {
		return a.getClass() == b ? true : false;
	}
	
	/**
	 * Compare class a with other object-class b. If it equals return true, otherwise 
	 * false.
	 * 
	 * @param a - Class to compare with.
	 * @param b - Class-object that is being compared.
	 */
	@JECSApi(since = "0.1.5", funcDesc = "class with object")
	public boolean eqs(Class<?> a, Object b) {
		return a == b.getClass() ? true : false;
	}
	
	/**
	 * Compare object-class a with other object-class b. If it equals return true, otherwise 
	 * false.
	 * 
	 * @param a - Class-object to compare with.
	 * @param b - Class-object that is being compared.
	 */
	@JECSApi(since = "0.1.5", funcDesc = "object with object")
	public boolean eqs(Object a, Object b) {
		return a.getClass() == b.getClass() ? true : false;
	}
	
	/**
	 * Cast the <code>in</code> class to <code>from</code> class with type C without exceptions.
	 */
	@SuppressWarnings("unchecked")
	@BetaFeature
	@JECSApi(since = "0.1.5", funcDesc = "save cast")
	public <C extends Component> C cast(Class<C> in, Object from) {
		if(eqs(in, from))
			return ((C) from);
		return null;	
	}

	/**
	 * This method iterate over each entity including all components of <code>componentT</code> and all sub-components
	 * extended, implemented, inherted from it at runtime. Each uses {@link IEach} functional interface as additional
	 * parameter. Its allows to add additional properties inside <code>each</code> function for specific entity and
	 * components. More detail about <code>EachI</code> see in {@link IEach#invoke(int, Object)}.
	 * <p>
	 * This method is usually the best and fastest choise, and very effective than {@link #invokeEach(Number, Class, String, Object...)}
	 * and {@link #invokeEachPack(Object[], String, Object...)}.
	 * <p>
	 * If some entity component(s) in sequence of components will not be sub-component of <code>componentT</code>, it just skip it and return 
	 * <code>null</code> instance. This will not affect the iteration process, but it will not be called for this component
	 * {@link IEach#invoke(int, Object)} implementation function.
	 * <p>
	 * Example:
	 * <pre>
	 * system.each(ComponentBase.class, (entity, component) ->
	 * {
	 *       component.printName();
	 * });
	 * </pre>
	 * 
	 * @param <C> Component type.
	 * @param componentT - Component as super-component.
	 * @param funcImpl - {@link IEach} function interface, or lambda expression.
	 */
	@JECSApi(since = "0.1.5", funcDesc = "each for all entities with c & sub-c")
	public synchronized <C extends Component> void each(Class<C> componentT, IEach<EntityT, C> funcImpl) 
			throws JECSException {
		Each<EntityT, C> eachFuncImpl = Each.create(funcImpl);
		Iterator<EntityT> itr = (Iterator<EntityT>) entities.iterator();
		
		while(itr.hasNext()) {
			var entity = itr.next();
			for(int componentIndex = 0; componentIndex < size(entity); componentIndex++) {
				C component = safeAsSubClass(container.get(entity).get(componentIndex).getClass(), componentT, "", false) != null 
					? get(entity, container.get(entity).get(componentIndex).getClass()) 
					: null;
				
				if(component != null)
					eachFuncImpl.invoke(entity, component);
			}
		}
	}
	
	/**
	 * This method iterate over each entity including all components which are contains at each entity in runtime. Each uses 
	 * {@link IEachC} functional interface as additional parameter. Its allows to add additional properties inside <code>each</code> 
	 * function for specific entity and components. <b>Remember</b> that if you want to call methods, functions, or parameters of a special
	 * component, you must first check that this type of component exists at all and then cast to specific type to invoke content. 
	 * <p>
	 * Basic example:
	 * <pre>
	 *  ... //body of functional interface
	 * 
	 *  if(system.eqs(ComponentAny.class, component))
	 *        ((ComponentAny) component).printName();
	 *
	 *  ...
	 * 
	 * </pre>
	 * More detail about <code>EachI</code> see in {@link IEachC#invoke(int, Object)}.
	 * <p>
	 * This method is usually the best and fastest choise, and very effective than {@link #invokeEach(Number, Class, String, Object...)}
	 * and {@link #invokeEachPack(Object[], String, Object...)}.
	 * <p>
	 * If some entity component(s) in sequence of components will not be sub-component of <code>componentT</code>, it just skip it and return 
	 * <code>null</code> instance. This will not affect the iteration process, but it will not be called for this component
	 * {@link IEach#invoke(int, Object)} implementation function, but slighty slow than {@link #each(Class, IEach)} for fully justified reasons.
	 * <p>
	 * Example:
	 * <pre>
	 * system.each((entity, anyComponent) ->
     * {
     *      if(system.eqs(ComponentAny.class, anyComponent))
     *           ((ComponentAny) anyComponent).printObj(toString());
     * });
     *
	 * </pre>
	 * @param <C> Component type.
	 * @param componentT - Component as super-component.
	 * @param funcImpl - {@link IEach} function interface, or lambda expression.
	 */
	@BetaFeature
	@JECSApi(since = "0.1.5", funcDesc = "each for all entities with any c")
	public synchronized <C extends Component> void each(IEachC<EntityT, C> funcImpl) 
			throws JECSException {
		EachC<EntityT, C> eachFuncImpl = EachC.create(funcImpl);
		Iterator<EntityT> itr = entities.iterator();	
		while(itr.hasNext()){
			var entity = itr.next();
			for(int componentIndex = 0; componentIndex < size(entity); componentIndex++) {
				@SuppressWarnings("unchecked") //undifiend behaviour if Object not C.
				C component = (C)getObject(entity, componentIndex);
				if(component != null)
					eachFuncImpl.invoke(entity, component);
			}
		}
	}
	
	/**
	 * This method iterate over each entity. Each uses {@link IEachE} functional interface as additional parameter. Its 
	 * allows to add additional properties inside <code>each</code> function for specific entity. 
	 * <p>
	 * Basic example:
	 * <pre>
	 * system.each(new EachEI() {
	 *     public void invoke(int entity) {
	 *        ...
	 * }});
	 * </pre>
	 * More detail about <code>EachEI</code> see in {@link EachE#invoke(int)}.
	 * <p>
	 * Lambda Example:
	 * <pre>
	 * system.each((entity) -> {
     *      ...
     * });
     *
	 * </pre>
	 * @param funcImpl - {@link IEach} function interface, or lambda expression.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "iterate over each entity")
	public void each(IEachE<EntityT> funcImpl) {
		EachE<EntityT> eachFuncImpl = EachE.create(funcImpl);
		Iterator<EntityT> itr = entities.iterator();
		while(itr.hasNext()) {
			var entity = itr.next();
			eachFuncImpl.invoke(entity);
		}
	}
	
	/**
     * Returns an iterable object of components on current entity.
     * <p>
     * The iterable object returns ComponentSequence that contain set of references to its 
     * non-empty components. 
	 * <p>
	 * This method non type safe, its means to get specific component you need to cast it to
	 * that exactly type.
	 *
     * @note
     * Empty types aren't explicitly instantiated and therefore they are never
     * returned during iterations.
     *
     * @return An iterable object of components on current entity.
     */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.7", funcDesc = "range for each")
	public <C extends Component> ComponentSequence<C> each(EntityT entity) {
		return (ComponentSequence<C>) container.get(entity);
	}
	
	/**
	 * Sorts the global container of entities-components by component order induced in {@link ISortC} callback.
	 * <p>
	 * Basically this method can sort components by value, from lower to higher or vise-versa.
	 * <p>
	 * This method will discard sort changes if it made by {@link #sort(ISort)}, because it will reorder entities
	 * by component order. Also sorting not be affecting on groups or views.
	 * <p>
	 * Example:
	 * <code><pre>
	 * ISortC<<C>C> sort = (a, b) -> { return a - b; };
	 * system.sort(component, sort);
	 * </code></pre>
	 * @param func - Sorting callback.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.9", funcDesc = "sort entities by component order")
	public <C extends Component> void sort(Class<C> component, ISortC<C> func) {
		ISortC<C> funcImpl = SortC.create(func);
		
		// Store a all 'map' containes values in new storage 'pair' to we can sort it and iterate propertly.
		RawPair[] storage = new RawPair[container.size()];
		for(int i = 0; i < storage.length; i++) {
			var entity = this.entities.get(i);
			storage[i] = new RawPair(entity, container.get(entity));
		}
			
		
		// 1) Get first  entity and component map and found component with type C.
		// 2) Get second entity and component map and found component with type C.
		// 3) Compare it, if equals swap two keys with its values around.
		
		Arrays.sort(storage, new Comparator<RawPair>() {
			public int compare(RawPair o1, RawPair o2) {
				
				ComponentSequence<Component> cs1 = (ComponentSequence<Component>) o1.second; 
				ComponentSequence<Component> cs2 = (ComponentSequence<Component>) o2.second; 
				if(cs1 != null && cs2 != null) {
					Component c1 = tryGet((EntityT) o1.first, component);
					Component c2 = tryGet((EntityT) o2.first, component);
			
					// Compare it.
					if((c1 != null && c2 != null)) {
						if(c1.getClass() == c2.getClass()) {
							int result = funcImpl.invoke((C)c1, (C)c2);
							return result;
						}
					}
				}
				
				return 0;
			}
		});
		
		// Replace map values by new sorted from storage.
		// This will reorder entities values if it was sorted by #sort(ISort).
		for(int i = 0; i < this.container.size(); i++) {
			this.entities.set(i, (EntityT)storage[i].first);
			this.container.put((EntityT)storage[i].first, (ComponentSequence<Component>)storage[i].second);
		}
	}
	
	/**
	 * Sorts the specified array list of entities according to the order induced in {@link ISort} callback.
	 * <p>
	 * Basically this method can sort entities by value, from lower to higher or vise-versa.
	 * <p>
	 * Example:
	 * <code><pre>
	 * SortI<<EntityT>EntityT> sort = (a, b) -> { return a - b; };
	 * system.sort(sort);
	 * </code></pre>
	 * @param func - Sorting callback.
	 */
	@JECSApi(since = "0.1.9", funcDesc = "sort entities by value")
	@SuppressWarnings("unchecked")
	public void sort(ISort<EntityT> func) {
		ISort<EntityT> funcImpl = Sort.create(func);
		var entitiesArray = entities.toArray();
		Arrays.sort(entitiesArray, new Comparator<Object>() {
			public int compare(Object a, Object b) {
				return funcImpl.invoke((EntityT)a, (EntityT)b);
			}
		});
		
		// Update the sorted entities list.
		entities = new ArrayList<>();
		for(int i = 0; i < entitiesArray.length; i++) 
			entities.add((EntityT) entitiesArray[i]);
	}
	
	/**
	 * Sorts the specified array list of entities according to the order induced in {@link ISort} callback.
	 * <p>
	 * Basically this method can sort entities by value, from lower to higher or vise-versa.
	 * <p>
	 * The sorting algorithm is a parallel sort-merge that breaks thearray into entities sub-arrays that are themselves 
	 * sorted and then merged. When the sub-array length reaches a minimum granularity, the sub-array is sorted 
	 * using the appropriate {@link #sort(ISort)} method. If the length of the specified entities is less than the
	 * minimum granularity, then it is sorted using the appropriate Arrays.sort method. The algorithm
	 * requires aworking space no greater than the size of the original entities array. The ForkJoin common pool is
	 * used toexecute any parallel tasks.
	 * <p>
	 * Example:
	 * <code><pre>
	 * SortI<<EntityT>EntityT> sort = (a, b) -> { return a - b; };
	 * system.parallelSort(sort);
	 * </code></pre>
	 * @param func - Sorting callback.
	 */
	@JECSApi(since = "0.1.9", funcDesc = "sort entities by value")
	@SuppressWarnings("unchecked")
	public void parallelSort(ISort<EntityT> func) {
		ISort<EntityT> funcImpl = Sort.create(func);
		var entitiesArray = entities.toArray();
		Arrays.parallelSort(entitiesArray, new Comparator<Object>() {
			public int compare(Object a, Object b) {
				return funcImpl.invoke((EntityT)a, (EntityT)b);
			}
		});
		
		// Update the sorted entities list.
		entities = new ArrayList<>();
		for(int i = 0; i < entitiesArray.length; i++) 
			entities.add((EntityT) entitiesArray[i]);
	}
	
	/**
	 * Non-owning group. 
	 * <p>
	 * Creates the group by checking all typed compoents on all entities, and
	 * if some entity has given components its put to group pool, otherwise that entity will be
	 * skipped.
	 * <p>
	 * A non-owning group returns all entities and only the entities that have at
	 * least the given components.
	 * <p>
	 * Groups share references to the underlying data structures/classes of the context
	 * that generated them. Therefore any change to the entities and to the components made by means
	 * of the registry are immediately reflected by all the groups.
	 * Moreover, sorting a non-owning group affects all the instances of the same
	 * group (it means that users don't have to call <code>sort</code> on each instance to sort
	 * all of them because they share entities and components).
	 * <p>
	 * Lifetime of a group must not overcome that of the context that generated it.
	 * In any other case, attempting to use a group results in undefined behavior.
	 * <p>
	 * Example:
	 * <code><pre>
	 * var group = system.group(A.class, B.class);
	 * for(var entity : group) {
	 * 	... use entities ...
	 * }
	 * </code></pre>
	 * 
	 * @param components Exclude Types of components used to filter the group.
	 * @returns The only that entities that has given components.
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.8", funcDesc = "group entity by components")
	public final <C extends Component> Group<EntityT, Component> group(Class<? extends C>... components) {
		group.pool.clear();
		Iterator<EntityT> itr = entities.iterator();
		while(itr.hasNext()) {
			var entity = itr.next();
			if(has(entity, components))
				group.pool.add(entity);
		}
	
		return group;
	}
	
	/**
	 * View.
	 * <p>
	 * Creates the view by checking one type component on all entities, and
	 * if some entity has given component its put to view pool, otherwise that entity will be
	 * skipped.
	 * <p>
	 * A view returns all entities and only the entities that have at least the given components.
	 * <p>
	 * Difference with {@link #group(Class...)} that is this method is faster when user wan't to
	 * iterate over all entities with one type component, but not with all types, that is increase
	 * iteration perfomance and in finally should be faster.
	 * <p>
	 * Example:
	 * <code><pre>
	 * var view = system.view(A.class);
	 * for(var entity : view) {
	 * 	... use entities ...
	 * }
	 * </code></pre>
	 * 
	 * @param component Exclude Type of component used to filter the view.
	 * @returns The only that entities that has component with given type.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "view entities by one type component")
	public final <C extends Component> View<EntityT> view(Class<C> component) {
		view.pool.clear();
		Iterator<EntityT> itr = entities.iterator();
		while(itr.hasNext()) {
			var entity = itr.next();
			if(has(entity, component))
				view.pool.add(entity);
		}
		
		return view;
	}

	/**
	 * Find first entity in entities container with input <code>C</code> component and return it. If no one entity hasn't this
	 * component its returns -1.
	 * 
	 * @param component - Type of component.
	 * @param <EntityT> - Exclude type of entity.
	 * @param <C> - Exclude type of component.
	 * @return The entity with finded component.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "find entity by component")
	public <C extends Component> EntityT find(Class<? extends C> component) {
		Iterator<EntityT> itr = entities.iterator();
		while(itr.hasNext()) {
			var entity = itr.next();
			if(has(entity, component))
				return entity;
		}
		return setEntityT(-1);
	}
	
	/**
	 * Find last entity in entities container with input <code>C</code> component and return it. If no one entity hasn't this
	 * component its returns -1.
	 * 
	 * @param component - Type of component.
	 * @param <EntityT> - Exclude type of entity.
	 * @param <C> - Exclude type of component.
	 * @return The last entity with finded component.
	 */
	@JECSApi(since = "0.1.8", funcDesc = "find last entity by component")
	public <C extends Component> EntityT findLast(Class<? extends C> component) {
		Iterator<EntityT> itr = entities.iterator();
		var entity = setEntityT(-1);
		while(itr.hasNext()) {
			if(has(itr.next(), component)) {
				entity = itr.next();
				continue;
			}
		}
		return entity;
	}

	/**
	 * Checks if this entity has <code>C</code> component data by its type and return true 
	 * otherwise false. 
	 * <p>
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Example:<p>
	 * <pre> 
	 * if(has(Component.class) && has(Component2.class)) 
	 * 	System.out.println("Types contains in container!");
	 * </pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type <code>Component.class</code> performs to check and get 
	 * if exist'.
	 * 
	 * @return True if type-class of component equals classType.
	 * @throws JECSException 
	 */
	@JECSApi(since = "0.1.0")
	public <C extends Component> boolean has(EntityT entity, Class<C> componentT) throws JECSException {
		validationCheck(entity, "check on has component from");
		
		ComponentSequence<Component> components = container.get(entity);
		String typeName = componentT.getTypeName();
		String objectTypeName = "";
		
		int searchedKey = components.isEmpty() ? 0 : components.size();
		for(int i = 0; i < searchedKey; i++) {
			if(components.get(i) == null) {
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
	 * Checks if this entity has instance of <code>?</code> input component and return true otherwise false. 
	 * <p>
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Example:<p>
	 * <pre> 
	 * if(has(myComponent)) 
	 * 	System.out.println("Component contains in container!");
	 * </pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param component - Instnace of component.
	 * 
	 * @return True if instance of component exist in this entity container.
	 * @throws JECSException If the entity is not in the container or is negative, invalid.
	 */
	public <C extends Component> boolean has(EntityT entity, C component) throws JECSException {
		validationCheck(entity, "check on has component from");
		
		ComponentSequence<Component> components = container.get(entity);
		int searchedKey = components.isEmpty() ? 0 : components.size();
		for(int i = 0; i < searchedKey; i++) {
			if(components.get(i) == null)
				continue;
				
			if(component.equals(components.get(i)))
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
	 * <pre> 
	 * if(has(Component.class, Component2.class, Component3.class)) 
	 * 	System.out.println("Types contains in container!");
	 * </pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type <code>Component.class</code> performs to check and get 
	 * if exist'.
	 * 
	 * @return True if type-class of component equals classType.
	 * @throws JECSException 
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.0")
	public final <C extends Component> boolean has(EntityT entity, Class<? extends C>... componentTs) 
			throws JECSException {
		int similarities = 0;
		for(int i = 0; i < componentTs.length; i++)
			if(has(entity, (Class<? extends C>) componentTs[i]))
				similarities++;
		
		if(similarities == componentTs.length)
			return true;
		
		return false;
	}
	
	/**
	 * Checks if this entity has at least one of <code>?</code> input components.
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Code snippet for:
	 * <blockquote><pre> 
	 * if(has(entity, Component.class) || has(entity, Component2.class || ...)) 
	 * 	System.out.println("Type contains in container!");
	 * </blockquote></pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type.
	 * 
	 * @return True if at least one type-class of component equals classType.
	 * @throws JECSException 
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.3")
	public final <C extends Component> boolean any(EntityT entity, Class<? extends C>... componentTs) 
			throws JECSException {
		for(int i = 0; i < componentTs.length; i++)
			if(has(entity, (Class<? extends C>) componentTs[i])) 
				return true;
		return false;
	}
	
	/**
	 * Checks if this entity has all of <code>?</code> input components.
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Code snippet for:
	 * <blockquote><pre> 
	 * if(has(entity, Component.class) && has(entity, Component2.class && ...)) 
	 * 	System.out.println("Type contains in container!");
	 * </blockquote></pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentTs - Classes of component type.
	 * 
	 * @return True if has all type-class of component equals classType.
	 * @throws JECSException 
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.9")
	public final <C extends Component> boolean all(EntityT entity, Class<? extends C>... componentTs) 
			throws JECSException {
		int hasCount = 0;
		for(int i = 0; i < componentTs.length; i++)
			if(has(entity, (Class<? extends C>) componentTs[i])) 
				hasCount++;
		return componentTs.length == hasCount;
	}
	
	/**
	 * Checks if this entity has at least one of <code>?</code> input components.
	 * If the entity is not in the container or is negative, invalid, then cause an 
	 * {@link JECSException}.
	 * 
	 * <p>Code snippet for:
	 * <blockquote><pre> 
	 * if(has(entity, Component.class) || has(entity, Component2.class || ...)) 
	 * 	System.out.println("Type contains in container!");
	 * </blockquote></pre>
	 * 
	 * @param <C> Type of component.
	 * @param entity - A valid entity identifier.
	 * @param componentT - Class of component type.
	 * 
	 * @return True if at least one type-class of component equals classType.
	 * @throws JECSException 
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public final <C extends Component> boolean anyArray(EntityT entity, Class<?>[] componentTs) 
			throws JECSException {
		for(int i = 0; i < componentTs.length; i++)
			if(has(entity, (Class<? extends C>) componentTs[i])) 
				return true;
		return false;
	}
	
	/**
	 * Checks if this entity contains and already mapped to container.
	 * 
	 * @param entity - A valid entity.
	 * @return True if entity exist in container otherwise false.
	 */
	@JECSApi(since = "0.1.2")
	public final boolean contains(EntityT entity) {
		if(container.containsKey(entity)) 
			return true;
		return false;
	}
	
	/**
	 * See {@link #has(Number, Class)}.
	 */
	@JECSApi(since = "0.1.2")
	@Deprecated(since = "use .has(EntityT, Class<C>) instead", forRemoval = true)
	public <C extends Component> boolean contains(EntityT entity, Class<C> componentT) 
			throws JECSException { 
		return has(entity, componentT); 
	}
	
	/**
	 * See {@link #has(Number, Class...)}
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.2")
	@Deprecated(since = "use .has(EntityT, Class<C>...) instead", forRemoval = true)
	public final <C extends Component> boolean contains(EntityT entity, Class<? extends C>... componentTs) 
			throws JECSException { 
		return has(entity, componentTs); 
	}
	
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	private <C extends Component> C getInternal(EntityT entity, Class<?> componentT) {
		ComponentSequence<Component> components = container.get((EntityT)entity);
		for(int i = 0; i < components.size(); i++) {
			Component component = components.get(i);
			if(component == null) {
				if(i != components.size()) 
					continue;
				else break;
			} else {
				if(component.getClass().getName().equals(componentT.getName()))
					// This is absolutly safty cast bc we cast Object to T (i.e Object is parent for T)
					return ((C) container.get(entity).get(i));
			}
		}
		return null;
	}
	
	/**
	 * Gets all existing components from <code>entity</code> entity.
	 * 
	 * @param <C> Type of component.
	 * @param entity - The entity from which will be get all components.
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public <C extends Component> C[] get(EntityT entity) {
		return (C[]) container.get(entity).toArray();
	}

	/**
	 * Gets the component <code>C</code> from class type of that component.
	 *
	 * @param <C> Type of component.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 * @return Already casted to (C) object from clas type, so you don't need to cast 
	 * it to (C).
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 * 
	 */
	@JECSApi(since = "0.1.2")
	public <C extends Component> C get(EntityT entity, Class<?> componentT) 
			throws JECSException {
		validationCheck(entity, "check on has component from");
		
		C component = getInternal(entity, componentT);
		if(component != null) 
			return component;
		
		throw new JECSException("Component with type <" + componentT.getName() + "> non-exist!");
	}

	/**
	 * Try get the component <code>C</code> from class type of that component. If component not exist or
	 * null return null.
	 *
	 * @param <C> Type of component.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 */
	@JECSApi(since = "0.1.9")
	public <C extends Component> C tryGet(EntityT entity, Class<?> componentT) 
			throws JECSException {
		validationCheck(entity, "check on has component from");
		
		C component = getInternal(entity, componentT);
		if(component != null) 
			return component;
		return null;
	}
	
	/**
	 * Gets the entity attached to this instance by <code>C</code> component.
	 * <p>
	 * If all entities doesen't contains this <code>C</code> type of component in the pool of 
	 * entity-component then returns current emplaced entity. If current component@hashcode 
	 * instance doenst exist in any entites this return null.
	 * <p>
	 * If this method was called by constructor of some component on intitializaion or emplacing 
	 * this also returns current emplaced entity or entity that will be emplaced, with this point of
	 * view is should be safe.
	 * 
	 * @param <C> - component type.
	 * @param component - Its of instance of component.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public <C extends Component> EntityT get(C component) {
		// Check if C component is instance and not a class type. If class type we change the overload.
		if(component.getClass().getTypeName().contains("java.lang.Class"))
			return get((Class<C>) component);
		
		var pairs = pool.get(component.getClass());
		if(pairs == null) 
			return currentEmplacedEntity;
		
		for(var entity : entities) {
			for(Pair<EntityT, Component> pair : pairs) {
				if(pair.first == entity)
					return entity;
			}
		}
	
		if(contains(currentEmplacedEntity))
			return currentEmplacedEntity;
		
		return null;
	}
	
	public <C extends Component> EntityT get(Class<C> component) {
		var pairs = pool.get(component);
		if(pairs == null) 
			return currentEmplacedEntity;
		
		for(var entity : entities) {
			for(Pair<EntityT, Component> pair : pairs) {
				if(pair.first == entity)
					return entity;
			}
		}
	
		if(contains(currentEmplacedEntity))
			return currentEmplacedEntity;
		
		return null;
	}
	
	/**
	 * Gets all entities that attached to this  component.
	 * <p>
	 * If this method was called by constructor of some component on intitializaion or emplacing 
	 * this also returns current emplaced entity or entity that will be emplaced, with this point of
	 * view is should be safe.
	 * 
	 * @param <C> - component type.
	 * @param component - Class type of component.
	 */
	@JECSApi(since = "0.1.9")
	public <C extends Component> List<EntityT> getAll(Class<C> component) {
		var pairs = pool.get(component);
		if(pairs == null) 
			return null;
		
		List<EntityT> ne = new ArrayList<EntityT>();
		for(var entity : entities) {
			for(Pair<EntityT, Component> pair : pairs) {
				if(pair.first == entity)
					ne.add(entity);
			}
		}
	
		return ne;
	}

	/**
	 * Return next avaliable component object of entity from index.
	 * 
	 * @param entity - A valid entity.
	 * @param index - Index of returned component from entity.
	 */
	@BetaFeature
	@JECSApi(since = "0.1.5")
	private Object getObject(EntityT entity, int index) {
		validationCheck(entity, "check on has component from");

		ComponentSequence<Component> components = container.get(entity);
		if(components.get(index) == null)
			return null;
		else
			return components.get(index);
	}

	/**
	 * Get's the component <code>C</code> from class type of that component.
	 *
	 * @param <C> Type of component.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 * @return Already casted to (C) object from clas type, so you don't need to cast 
	 * it to (C).
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@SuppressWarnings("unchecked")
	@BetaFeature
	@JECSApi(since = "0.1.5")
	public <C extends Component> C getIfExist(EntityT entity, Class<C> componentT) 
			throws JECSException {
		validationCheck(entity, "check on has component from");

		ComponentSequence<Component> components = container.get(entity);
		for(int i = 0; i < components.size(); i++) {
			if(components.get(i) == null) {
				if(i != components.size()) 
					continue;
				else break;
			} else {
				if(components.get(i).getClass().getName().equals(componentT.getName()))
					// This is absolutly safty cast bc we cast Object to T (i.e Object is parent for T)
					return ((C) container.get(entity).get(i));
			}
		}
		return null;
	}
	
	/**
	 * Get's the component <code>C</code> from class type of that component is it exist, but
	 * if this component doesn't exist that it will be automatically contruct by args.
	 * <p>Code snippet for:
	 * <pre> 
	 * var component = system.has(entity, componentT) ? system.get(entity, componentT) : system.emplace(entity, componentT, args);
	 * </pre>
	 * 
	 * @param <C> Type of component.
	 * @param componentT - Its of type of class.
	 * @return Existed or emplaced C component.
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@JECSApi(since = "0.1.3")
	public <C extends Component> C getOrEmplace(EntityT entity, Class<C> componentT,
			Object... args) throws JECSException {
		return has(entity, componentT) ? get(entity, componentT) : emplace(entity, componentT, args);
	}

	/**
	 * Return the group of existing components <code>?</code> from class type of that component.
	 * 
	 * @param <C> Type of component.
	 * @param <G> Group / array of returned components.
	 * @param componentT - Its of type of class like <code>String.class</code>.
	 * @return Already casted to (C) object from class type, so you don't need to cast 
	 * it to (C).
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	@JECSApi(since = "0.1.2")
	public final <C extends Component, G extends Component> G[] 
			get(EntityT entity, Class<? extends C>... componentTs) throws JECSException {
		final List<G> group = new ArrayList<G>();
		for(int i = 0; i < componentTs.length; i++) {
			Class<? extends C> componentT = (Class<? extends C>)componentTs[i]; 
			Component component = get(entity, componentT);
			if(component != null)
				group.add((G) component);
		}
		return (G[]) group.toArray();
	}
	

	/**
	 * Return the pair of existing components from class type of that component.
	 * 
	 * @param <C> Type of component 1.
	 * @param <C1> Type of component 2.
	 * @param entity - Entity identifier.
	 * @param a - First component class.
	 * @param b - Second component class.
	 * @return Pair of components. If component not exist its return null.
	 */
	@SuppressWarnings({ "unchecked" })
	@JECSApi(since = "0.1.9")
	public final <C extends Component, C1 extends Component> Pair<C, C1> 
			getPair(EntityT entity, Class<? extends C> a, Class<? extends C1> b) throws JECSException {
		var component1 = tryGet(entity, a);
		var component2 = tryGet(entity, b);
		return new Pair<C, C1>((C)component1, (C1)component2);
	}

	/**
	 * Get's the component <code>C</code> from class type of that component if <code>childComponentT</code>
	 * subcomponent of <code>parentComponentT</code>, otherwise this method return <code>null</code>
	 *
	 * @param <C> Type of component.
	 * @param entity - A valid entity.
	 * @param parentComponentT - Its of type of parent class.
	 * @param childComponentT - Its of type of child return class.
	 * @return Component if it subcomponent otherwise null.
	 * 
	 * @throws JECSException If class type null or not exist in component map. Or if
	 * handle is not created.
	 */
	@JECSApi(since = "0.1.4")
	public <C extends Component> C getIfFamily(EntityT entity, Class<? extends Component> parentComponentT, 
			Class<C> childComponentT) throws JECSException {
		return safeAsSubClass(childComponentT, parentComponentT, "", false) != null ? get(entity, childComponentT) : null;
	}
	
	/**
	 * Creates new pack with specific components.
	 * <p>
	 * <b>Pack</b> is a specific set or group, where you can store components of entity. Pack should be 
	 * used when you need to group certain components of an entity and iterate them separately, 
	 * regardless of the main sequence of components. This gives a small performance gain, but 
	 * when an entity has many components, the gain is significantly felt.
	 * <p>
	 * I also want to note that when components are placed in a pack, they lose their connection 
	 * with their entity. This is also done in the case of performance. Technically, these component 
	 * references will be associated with the entity, but not in the pack. To get a pack of entity and
	 * iterate over components, you must specify the exact entity that the pack was originally created from.
	 * 
	 * @param <E> Entity type.
	 * @param <C> Component type.
	 * @param entity - A valid entity.
	 * @param componentTs - Component types were be placed to pack.
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.4")
	public final <C extends Component> void createPack(EntityT entity, Class<? extends Component>... componentTs) {
	
		ComponentSequence<Component> sequence = new ComponentSequenceImpl<Component>();

		if(sequence.isEmpty()) {
			try {
				Component[] component = get(entity, componentTs);
				for(int c = 0; c < component.length; c++)
					sequence.add(component[c]); 
				packs.put(packIndex, sequence);
			} catch (JECSException e) { e.printStackTrace(); }	
		} else {
			createPack(entity, componentTs);
		}
		
		packIndex++;
	}
	
	/**
	 * Get the pack of components of their entity. If at least one of compoenent will not contains in that
	 * <code>entity</code> returns <code>null</code>.
	 * <p>
	 * <b>Pack</b> is a specific set or group, where you can store components of entity. Pack should be 
	 * used when you need to group certain components of an entity and iterate them separately, 
	 * regardless of the main sequence of components. This gives a small performance gain, but 
	 * when an entity has many components, the gain is significantly felt.
	 * 
	 * @param entity - A valid entity.
	 * @param index - Pack index.
	 * @param <E> Entity type.
	 * @param <C> Component type.
	 * @throws JECSException 
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.4")
	public final <C extends Component> Component[] pack(EntityT entity, int index) 
			throws JECSException {
		validationCheck(entity, "get pack of components from");
		
		for(int i = 0; i < packs.get(index).size(); i++)
			if(container.get(entity).contains(packs.get(index).get(i)))
				continue;
			else return null;
		return (Component[]) packs.get(index).toArray();
	}
	
	/**
	 * Iterates over pack with <code>index</code> with specific components, and invoke
	 * <code>funcName</code> with <code>funcArgs</code> of each components from pack. This
	 * method is identical to {@link #invokeEach(Number, Class, String, Object...)} but with
	 * better perfomance.This does not mean that it will always need to be used and <code>invokeach</code> 
	 * will be forgotten.<code>InvokeEach</code> should be used when an entity has several components, and 
	 * <code>pack</code> is better for entities with a large number of components.
	 * <p>
	 * If you don wan't to use the packs, you also can achive the same result follow this code
	 * snippet below:
	 * <pre>
	 * Object[] components = system.get(entity, Script1.class, Script2.class);
	 *     for(Object component : components)
	 *        ((ScriptComponent)component).func();
	 * </pre>
	 * where <code>Script1</code> and <code>Script2</code> extends from <code>ScriptComponent</code>.
	 * <p>
	 * <b>Pack</b> is a specific set or group, where you can store components of entity. Pack should be 
	 * used when you need to group certain components of an entity and iterate them separately, 
	 * regardless of the main sequence of components. This gives a small performance gain, but 
	 * when an entity has many components, the gain is significantly felt.
	 * <p>
	 * I also want to note that when components are placed in a pack, they lose their connection 
	 * with their entity. This is also done in the case of performance. Technically, these component 
	 * references will be associated with the entity, but not in the pack. To get a pack of entity and
	 * iterate over components, you must specify the exact entity that the pack was originally created from.
	 * <p>
	 * Example:
	 * <pre>
	 * system.createPack(entity, Component1.class, Component2.class, ...); //pack creation
	 * var pack = system.pack(entity, 0); //get the pack
	 * 
	 * while(true)
	 *     system.invokeEachPack(pack, "func"); //invoke functions from each compoennt
	 * </pre>
	 * <p>
	 * @param <Pack> Pack of Component types.
	 * @param pack - Pack of component or basically array.
	 * @param index - Index of pack to iterate.
	 * @param funcName - Name of function/method which will be called.
	 * @param funcArgs - Arguments to function/method.
	 */
	@JECSApi(since = "0.1.4")
	@SafeVarargs
	public final synchronized <Pack extends Component> void invokeEachPack(Pack[] pack, String funcName, Object... funcArgs) {	
		for(int i = 0; i < pack.length; i++) {
			try {
				Class<?>[] funcArgsTypes = new Class<?>[funcArgs.length]; 
				for(int t = 0; t < funcArgs.length; t++) 
					funcArgsTypes[t] = sortR(funcArgs[t].getClass().getTypeName(), funcArgs[t].getClass());
					
				// Invokes the function
				Method func = pack[i].getClass().getDeclaredMethod(funcName, funcArgsTypes);
				func.invoke(pack[i], funcArgs);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException |
					IllegalArgumentException | InvocationTargetException e) 
						{ e.printStackTrace();}
		}
	}
	
	/**
	 * Iterates over raw components array of object instances and gets/return the component with type C if it
	 * can found it, otherwise return null.
	 * <p>
	 * This method not faster but syntaxity more readable. It you want best perfomance use regular casting to type
	 * of component you want.
	 * <p> Example:
	 * <code><pre>
	 * var group = system.group(A.class, B.class);
	 * for(var entity: group) {
	 *  	var components = system.get(entity, A.class, B.class);
	 * 		A a = system.get(components, A.class);
	 * 		B b = system.get(components, B.class);
	 * 
	 * 		... use a & b components ...
	 *  }
	 *    
	 * </code></pre>
	 * @param rawComponents - Raw array of objects representing the components.
	 * @param component - Component type that be will searching.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public final <C extends Component> C get(final Object[] rawComponents, Class<C> component) {
		for(var c : rawComponents)
			if(c.getClass() == component) 
				return (C) c;
		return null;
	}
	
	/**
	 * Iterates over in-placed in ECS place-holder raw components array of object instances and gets/return the 
	 * component with type C if it can found it, otherwise return null.
	 * <p>
	 * This method not faster but syntaxity more readable. It you want best perfomance use regular casting to type
	 * of component you want.
	 * <p> Example:
	 * <code><pre>
	 * var group = system.group(A.class, B.class);
	 * for(var entity: group) {
	 *  	var components = system.get(entity, A.class, B.class);
	 *  	system.push(components);
	 * 		A a = system.get(A.class);
	 * 		B b = system.get(B.class);
	 * 
	 * 		... use a & b components ...
	 * 
	 *  	system.pop();
	 *  }
	 *    
	 * </code></pre>
	 * 
	 * @param rawComponents - Raw array of objects representing the components.
	 * @param component - Component type that be will searching.
	 */
	@SuppressWarnings("unchecked")
	@JECSApi(since = "0.1.8")
	public final <C extends Component> C getArr(Class<C> component) {
		for(var c : arr)
			if(c.getClass() == component) 
				return (C) c;
		return null;
	}
	
	/**
	 * Replace the component in-place. This method will change the component data in-place with out additional call
	 * of {@link #get(Number, Class)}.
	 * 
	 * @param entity - The entity identifier.
	 * @param component - The component class type.
	 * @param func - Callback that will apply on component.
	 * @param <Component> - Generic type of component.
	 */
	@SuppressWarnings("hiding")
	@JECSApi(since = "0.1.9")
	public final <Component> void patch(EntityT entity, Class<Component> component, IPatch<Component> func) {
		IPatch<Component> funcImpl = Patch.create(func);
		Component c = get(entity, component);
		funcImpl.invoke(c);
	}
	
	/**
	 * Assign the same component with default constructor initializer to all entities at once when a type is 
	 * specified as a generic parameter.
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 */
	@JECSApi(since = "0.1.9")
	public final <C extends Component> void insert(Class<C> component) {
		for(var entity : entities) {
			if(has(entity, component))
				replace(entity, component, NULL_ARGS);
			else
				emplace(entity, component, NULL_ARGS);
		}
	}
	
	/**
	 * Assign the same component with default constructor initializer to all entities in range of first to 
	 * last at once when a type is specified as a generic parameter.
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 * @param first - First bound in entities storage.
	 * @param last - Second bound in entities storage.
	 */
	@JECSApi(since = "0.1.9")
	public final <C extends Component> void insert(Class<C> component, int first, int last) {
		for(int i = 0; i < entities.size(); i++) 
			if(i >= first && i <= last) 
				insert(component);
	}
	
	/**
	 * Assign the same component that will be constructed from agruments if in not exist on entity or if entity has that
	 * component it will be replaced to all entities at once when a type is specified as a generic parameter.
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 * @param args - Component arguments.
	 */
	@JECSApi(since = "0.1.9")
	public final <C extends Component> void insert(Class<C> component, Object... args) {
		for(var entity : entities) {
			if(has(entity, component))
				replace(entity, component, args);
			else
				emplace(entity, component, args);
		}
	}
	
	/**
	 * Assign the same component that will be constructed from agruments if in not exist on entity or if entity has that
	 * component it will be replaced to all entities in range of first to last at once when a type is specified as a 
	 * generic parameter.
	 * <p>
	 * API NOTE: Only reason this method has name insertRange, because insert is ambisious for (int,int & Object...).
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 * @param first - First bound in entities storage.
	 * @param last - Second bound in entities storage.
	 * @param args - Component arguments.
	 */
	@JECSApi(since = "0.1.9")
	public final <C extends Component> void insertRange(Class<C> component, int first, int last, Object... args) {
		for(int i = 0; i < entities.size(); i++) {
			var entity = entities.get(i);
			if(i >= first && i <= last) {
				if(has(entity, component))
					replace(entity, component, args);
				else
					emplace(entity, component, args);
			}
		}
	}
	
	/**
	 * Assign the same component from instnace to all entities at once when a type is specified as a 
	 * generic parameter.
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 */
	@JECSApi(since = "0.1.9")
	@SuppressWarnings("unchecked")
	public final <C extends Component> void insert(C component) {
		for(var entity : entities) {
			if(has(entity, (Class<C>) component.getClass()))
				replace(entity, component);
			else
				emplace(entity, component);
		}
	}
	
	/**
	 * Assign the same component from instnace in range of first to last to all entities at once when a type is 
	 * specified as a generic parameter.
	 * 
	 * @param <C> - Component type.
	 * @param component - Component class.
	 */
	@JECSApi(since = "0.1.9")
	public final <C extends Component> void insert(C component, int first, int last) {
		for(int i = 0; i < entities.size(); i++) {
			var entity = entities.get(i);
			if(i >= first && i <= last) {
				if(has(entity, component))
					replace(entity, component);
				else
					emplace(entity, component);
			}
		}
	}
	
	/**
	 * Push the current raw components array to place-holder in ECS and you can use it from current ECS context and 
	 * get specific components, of right type <b>not use cast</b> syntax.
	 * 
	 * @param rawComponents - Raw array of objects representing the components.
	 * 
	 * @see #get(Object[], Class)
	 * @see #get(Class)
	 */
	@JECSApi(since = "0.1.8", funcDesc = "push components array to place-holder")
	public final void push(final Object[] rawComponents) {
		arr = rawComponents;
	}
	
	/**
	 * Pop the stored raw components array from place-holder in ECS. 
	 * <p>
	 * After this call you cannot this methods see at 'See Also' section. If try you will get {@link NullPointerException}.
	 * 
	 * @see #get(Object[], Class)
	 * @see #get(Class)
	 */
	@JECSApi(since = "0.1.8", funcDesc = "pop components array from place-holder")
	public final void pop() {
		arr = null;
	}
	
	/**
	 * Returns an iterator over the entities identifiers elements in this list 
	 * in proper sequence. 
	 */
	@JECSApi(since = "0.1.5")
	public final Iterator<EntityT> iterator() {
		return entities.iterator();
	}
	
	/**
	 * Returns an list iterator over the entities identifiers elements in this list 
	 * in proper sequence. 
	 */
	@JECSApi(since = "0.1.5")
	public final ListIterator<EntityT> listIterator() {
		return entities.listIterator();
	}
	
	/**
	 * Returns an reversed iterator over the entities identifiers elements in this list 
	 * in proper sequence. 
	 */
	@JECSApi(since = "0.1.5")
	public final ReversedIterator<EntityT> reversedIterator() {
		ReversedIterator<EntityT> reverseItr = new ReversedIterator<EntityT>(entities);
		return reverseItr;
	}
	
	/**
	 * Returns an reversed iterator list over the entities identifiers elements in this list 
	 * in proper sequence. 
	 */
	@JECSApi(since = "0.1.5")
	public final ReversedIteratorList<EntityT> reversedIteratorList() {
		ReversedIteratorList<EntityT> reverseListItr = new ReversedIteratorList<EntityT>(entities);
		return reverseListItr;
	}
	
	/**
	 * Remove all C components from all entities.
	 * 
	 * @param componentT - Component type.
	 */
	@JECSApi(since = "0.1.9")
	public <C extends Component> void clear(Class<C> componentT) {
		for(var entity : entities)
			if(has(entity, componentT)) 
				erase(entity, componentT);
	}
	
	/**
	 * Remove all mappings from this handle map.
	 */
	@JECSApi(since = "0.1.1")
	public void clear() {
		container.clear();
	}
	
	/**
	 * Returns true if this handle contains no entity mappings.
	 */
	@JECSApi(since = "0.1.1")
	public boolean empty() {
		return container.isEmpty();
	}
	
	/**
	 * Clear component sequence of input <code>entity</code>.
	 * 
	 * @param entity - A valid entiity.
	 */
	@JECSApi(since = "0.1.1")
	public void clear(EntityT entity) {
		removeAll(entity);
		
		if(!container.get(entity).isEmpty())
			clear(entity);
		
		container.get(entity).clear();
	}
	
	/**
	 * Returns the number of components contains in a entity.
	 * 
	 * @param entity - A valid entiity.
	 * @return number of components in a entity.
	 */
	@JECSApi(since = "0.1.1")
	public int size(EntityT entity) {
		return container.get(entity).size();
	}
	
	/**
	 * Returns the number of entities in a handle map.
	 * 
	 * @return number of entities in a handle map.
	 */
	@JECSApi(since = "0.1.1")
	public int size() {
		return container.size();
	}
	
    /**
     * Converts the entity object to identifier {@link NullEntity}.
     * 
     * @param entity - Type of entity identifier.
     * @return The null representation for the given identifier.
     */
	@SuppressWarnings({ "static-access" })
	@JECSApi(since = "0.1.8")
	public NullEntity<EntityT> nullEntity(EntityT entity) {
		return (NullEntity<EntityT>) NULL_ENTITY.nullEntity(entity);
	}
	
	/**
    * Create new temprary entity null identifier.
    * 
    * @param entity - Type of entity identifier.
    * @return The null representation for the given identifier.
    */
	@SuppressWarnings({ "static-access" })
	@JECSApi(since = "0.1.8")
	public NullEntity<EntityT> nullEntity() {
		return NULL_ENTITY.nullEntityFromNullConstnat();
	}
	
	/**
     * Converts the {@link NullEntity} identifier to normal entity.
     * 
     * @param entity - Type of {@link NullEntity} identifier.
     * @return The not null representation for the given identifier.
     */
	@SuppressWarnings({ "static-access" })
	@JECSApi(since = "0.1.8")
	public EntityT entity(NullEntity<EntityT> entity) {
		return NULL_ENTITY.entity(entity);
	}
	
	/**
	 * Checks if this entity is {@link NullEntity}.
	 * 
	 * @param entity - Type of entity identifier.
	 * @return False if the two elements differ, true otherwise.
	 */
	@SuppressWarnings("static-access")
	@JECSApi(since = "0.1.8")
	public boolean isNullEntity(EntityT entity) {
		return NULL_ENTITY.isNullEntity(entity);
	}
	
	/**
	 * Checks if this {@link NullEntity} acually null entity.
	 * 
	 * @param entity - Type of entity identifier.
	 * @return False if the two elements differ, true otherwise.
	 */
	@SuppressWarnings("static-access")
	@JECSApi(since = "0.1.8")
	public boolean isNullEntity(NullEntity<EntityT> entity) {
		return NULL_ENTITY.isNullEntity(entity);
	}
	
	/**
	 * Check entity identifier on valid is or not.
	 * 
	 * {@link NullEntity}'s always return false.
	 * 
	 * @param entity - Entity identifier.
	 * 
	 * @throws JECSException If entity is not valid.
	 */
	@JECSApi(since = "0.1.9")
	public boolean isValid(EntityT entity) { 
		if(entity == null)
			return false;
		
		if(NullEntity.class.isAssignableFrom(entity.getClass()))
			return false;
	
		return true;
	}
	
	/**
	 * Check entity identifier on valid is or not.
	 * 
	 * {@link NullEntity}'s always return false.
	 * 
	 * @param entity - Entity identifier.
	 * 
	 * @throws JECSException If entity is not valid.
	 */
	@JECSApi(since = "0.1.9")
	public boolean isValid(NullEntity<?> entity) { 
		return false;
	}
	
	/**
	 * Check entity identifier on valid is or not. (With exceptions)
	 * 
	 * @param entity - Entity identifier.
	 * @param msg - Message.
	 * 
	 * @throws JECSException If entity is not valid.
	 */
	@JECSApi(since = "0.1.1")
	private boolean validationCheck(EntityT entity, String msg) 
			throws JECSException {
		if(!Context.ctxRelease) { // Disable validationCheck for fast / release modes.
			final EntityT unvalid = setEntityT(-1); //uses Integer instead 'int' because generic type cast.
			boolean flag = (entity == null) || (entity == (EntityT) unvalid); 
			if(flag)
				throw new JECSException("Cannot " + msg + " unvalid entity.");
		}
		
		if(NullEntity.class.isAssignableFrom(entity.getClass()))
			throw new JECSException("NullEntity always not be valid. Convert it to normal entity identifier.");
			
		return true;
	}
	
	/**
	 * Returns a string representation of the this handle to Entity Component System. In general, the
	 * toString method returns a string that "textually represents" this object. The result should be
	 * a concise but informative epresentation that is easy for a person to read. It is recommended that 
	 * all subclasses override this method. 
	 */
	@Override
	@JECSApi(since = "0.1.4")
	public String toString() {
		return "JECSHandle "
				+ "[" +
				 	"\n  entities=" + entities + 
				 	"\n  container=" + container +
				 	"\n  pool=" + pool +
				 	"\n  packs=" + packs +
				  "]";
	}
	
	
	/**
	 * Returns a hash code value for the {@link BaseJECS}. This method is supported for the benefit of hash 
	 * tables such as those provided by java.util.HashMap. 
	 */
	@Override
	@JECSApi(since = "0.1.5")
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((container == null) ? 0 : container.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((packs == null) ? 0 : packs.hashCode());
		return result;
	}

	/**
	 * See {@link Object#equals(Object)}.
	 */
	@Override
	@JECSApi(since = "0.1.5")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		BaseJECS<EntityT, Component> other =  (BaseJECS<EntityT, Component>) obj;
		
		if (container == null) {
			if (other.container != null)
				return false;
		} else if (!container.equals(other.container))
			return false;
		
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		
		if (packs == null) {
			if (other.packs != null)
				return false;
		} else if (!packs.equals(other.packs))
			return false;
		
		if(arr == null) {
			if(other.arr != null)
				return false;
		} else if(!arr.equals(other.arr))
			return false;
		
		return true;
	}
	
	/**
	 * Return the context of this instance.
	 */
	@JECSApi(since = "0.1.6")
	public Context getContext() {
		return context;
	}

	@JECSApi(since = "0.1.*")
	@Override
	public void run() {

	}
	
	// Static methods using registy as accessors.
	
	/**
	 * Gets the entity attached to this instance by <code>C</code> component.
	 * <p>
	 * See {@link #get(C)}.
	 * 
	 * @param <C> - Component type.
	 * @param <EntityT> - Entity type.
	 * @param component - Its of instance of component.
	 * @param system - The current using system.
	 */
	@JECSApi(since = "0.1.9")
	public static <EntityT extends Number, C extends Object> EntityT toEntity(BaseJECS<EntityT, Object> system, C component) {
		if(system != null) return system.get((C) component);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
 	* The "traditional" way to measure time of JECS method invokation.
 	* 
	* @param system - The current using system.
 	* @param fn - Method where will called JECS method/s to measure time in milliseconds.
 	* @return Meausered time in milliseconds.
 	*/
	@JECSApi(since = "0.1.9")
	public static final  <EntityT extends Number, C extends Object> long elapsed(BaseJECS<EntityT, Object> system, ElapseFn fn) {
		if(system != null) return system.elapsed(fn);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
	 * The 'as' operator. Construct the array of component in easy text formatting.
	 * <p>
	 * Example when this can be usefull, its where need construct the array of different class types,
	 * and not write this auofull <b>new Class<<?>?>[] { ... }</b> syntax.
	 * 
	 * @param components - Sequence of component types.
	 */
	@JECSApi(since = "0.1.9")
	public static final Class<?>[] as(Class<?>... components) {
		Class<?>[] arr = new Class<?>[components.length];
		for(int i = 0; i < components.length; i++) 
			arr[i] = components[i];
		return arr;
	}
	
	/**
	 * Gets the entity attached to this instance by <code>C</code> component.
	 * <p>
	 * See {@link #get(Class)} or {@link #get(Object)}.
	 * @param <C> - component type.
	 * @param component - Its of instance of component.
	 */
	@JECSApi(since = "0.1.9")
	public static final <EntityT extends Number> EntityT 
			get(BaseJECS<EntityT, Object> system, Class<?> component) {
		if(system != null) return system.get(component);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
	 * Gets the component <code>C</code> from class type of that component.
	 * <p>
	 * {@link #get(Number, Class)}.
	 * 
	 * @see #get(Number, Class).
	 */
	@JECSApi(since = "0.1.9")
	public static final <EntityT extends Number> EntityT 
			get(BaseJECS<EntityT, Object> system, EntityT entity, Class<?> component) {
		if(system != null) return system.get(entity, component);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
	 * Return the group of existing components <code>?</code> from class type of that component.
	 * 
	 * See {@link #get(Number, Class...)}.
	 */
	@SafeVarargs
	@JECSApi(since = "0.1.9")
	public static final <EntityT extends Number, C extends Object> Object[] 
			get(BaseJECS<EntityT, Object> system, EntityT entity, Class<? extends C>... components) {
		if(system != null) return system.get(entity, components);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
	 * See {@link #each(Class, IEach)}.
	 */
	@JECSApi(since = "0.1.9")
	public static synchronized <EntityT extends Number, C extends Object> 
		void each(BaseJECS<EntityT, Object> system, Class<C> componentT, IEach<EntityT, C> funcImpl) {
		if(system != null) system.each(componentT, funcImpl);
		throw new JECSException("System is not valid or null.");
	}
	
	/**
	 * See {@link #each(Number)}.
	 */
	@JECSApi(since = "0.1.9", funcDesc = "range for each")
	public static <EntityT extends Number, C extends Object> ComponentSequence<C> 
		each(BaseJECS<EntityT, Object> system, EntityT entity) {
		if(system != null) return system.each(entity);
		throw new JECSException("System is not valid or null.");
	}
}
