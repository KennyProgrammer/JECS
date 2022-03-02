# JECS
![JECS](.github/Logo.png?raw=true "JECS - Entity Component System")

<code>JECS</code> or <b>Java Entity Component System</b> this is a small system that holds all entity identifiers in a single object 
as well as their component identifiers. It makes it easy to create entities, put a component in it, and when it is not needed, it is also
easy to delete it without leaving the object in memory.
   
This is not the best solution for sorting entities and components around the world. This is a library written using the same template and style as 
`Entt` written in C++. Only `JECS` is just trying to repeat what `Entt` can, and has a similar syntax. But the implementation is completely 
different. Therefore, if you need speed, then use memory, if not, then try `JECS`.

# Documentation

* [What is Entity Component System](#what-is-entity-component-system)
* [Example](#example)
* [System design](#system-design)
  * [User-side control](#user-side-control)
  * [Memory usage](#memory-usage)
  * [Generic and Reflection](#generic-and-reflection)
  * [Entity Identifier](#entity-identifier)
  * [Pools](#pools)
* [The System, Entity, and Components](#the-system-entity-and-components)
* [Sorting](#sorting)
* [Iteration over entities and components](#iteration-over-entities-and-components)
* [Invokation](#invokation)
* [Views and Groups](#views-and-groups)
   * [View](#view)
   * [Group](#group)
* [Packs](#packs)
   * [Each using pack or iteration](#each-using-pack-or-iteration)
* [Other usefull utilities](#other-usefull-utilities)
   * [Null Entity](#null-entity)
   * [Entity from Component](#entity-from-component)
   * [The As operator](#the-as-operator)
   * [Callbacks](#callbacks)
   * [Profiling](#profiling)
   * [Context](#context)

# What is Entity Component System
Entity Component System (ECS) - is a software architectural pattern mostly used on video game development for the storage of game world objects. An ECS follows the pattern of "entities" with "components" of data.   
   
An ECS follows the principle of composition over inheritance, meaning that every entity is defined not by a "type", but by the components that are associated with it. The 
design of how components relate to entities depend upon the Entity Component System being used.   
**Entity** The entity is a general purpose object. Usually, it only consists of a unique id. They "tag every coarse gameobject as a 
separate item". Implementations typically use a plain integer for this.   
**Component** the raw data for one aspect of the object, and how it interacts with the world. "Labels the Entity as possessing this 
particular aspect". Implementations typically use structs, classes, or associative arrays.   
**System** "Each System runs continuously (as though each System had its own private thread) and performs global actions on every 
Entity that possesses a Component of the same aspect as that System."
	
# Example: 
```java
package kenny.jecs.test;
import kenny.jecs.JECS;

public class Test {
	// Components
	class Transform {
		public float x, y, z;
		
		public Transform() {} // Can be used with NULL constant.
		public Transform(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void method() {}; // Method to be invoked by JECS.
	}
	
	class Data {
		public Data() {}
		
		public void method() { 
			System.out.println("From Data component."); 
		};
	}
	
	public static void main() {	
		JECS<Object> system = JECS.construct();
		
		// Create new entity identifier.
		int entity = jecs.create();
		// Emplace component to this entity.
		system.emplace(entity, Transform.class, 10.0f, 0.0f, 0.0f);
		// Emplace component with NULL args.
		system.emplace(entity, Data.class, JECS.NULL_ARGS);
		
		// Check if this entity has a Transform Component class. 
		if(system.has(entity, Transform.class))
			for(int i = 0; i < 10; i++)
				update(jecs, entity);
		
		// Replace component with different data.
		system.replace(entity, Transform.class, 20.0f, 10.0f, 1.0f);
		// Returns the component from entity.
		system.get(entity, Transform.class);
		// Destroy this entity identifier.
		system.destroy(entity);
		
		JECS.deconstruct(jecs);
	}
	
	public static void update(JECS<Object> jecs, int entity) {
		// Iterate over all entities. 
		// Using Callback/Lamba.
		system.each((int entty, Object component) -> { /* ... */ });
		
		// Iterate over entities that has common component.
		system.each(Transform.class, (int entty, Transform component) -> { /* ... */ });
		
		// Invoke method 'method' from current entity Transform component.
		systemsystem.invoke(entity, Transform.class, "method");
		
		// Invoke method 'method' from current entity group of components.
		system.invokeEach(entity, JECS.as(Transform.class, Data.class), "method");
	}
}

```

# System design
The design of the creation of the system was quite simple and convenient and easily fit object-oriented programming in Java. To do this, 
a good solution was simply to have one object that would create, delete and manipulate entities and components, and controll all system.
Initializing ECS instance simply by using `JECS.construct` method:
```java
JECS system = JECS.construct();
```

Then after you finally finish using the system use `JECS.deconstruct` method:
```java
JECS.deconstruct(system);
```

## User-side control
By this concept, it means that the system will not manipulate the basics of your cycles and user code or remove components without your 
knowledge. It is based on various containers or pools whether pairs of objects that in one way or another will be associated with your
entity. The scheme of this system is very simple, create some kind of entity and tie components to it that will be stored in different 
containers depending on use.

## Memory usage
Since this library is written in Java and is just trying to repeat the functionality of `ENTT`, it will not take into account that a lot of 
memory can be used for all the work in some cases. Again, as mentioned earlier, everything will depend only on how and for what the user will
use `JECS`. If you are really looking for something that will consume less memory, I advise you to use `ENTT` written in C++.

## Generic and Reflection
`JECS` is fully exploits the concept of generics. This means that each component will initially not be fully defined and the user will need to manually 
convert to the desired type or use the built-in methods in the system.   
It is also worth mentioning that the library uses Java reflection to search and determine the type of components and their compatibility during creation,
deletion during runtime, which may also not affect performance well, but this is not critical, usually the difference is only a few nanoseconds.

## Entity Identifier
For all entities in this system we use a by default simple `int` type or `Integer` type for iteration. When initial desined witch type use for entity identifier first choice
was `long` but at end of the day that is not nessessary. But now JECS has a way to create system using `long` type for larges systems, or a `short` or `Short` for really
small systems, but its not effitient.

## Pools
Pools of components are a sort of specialized version of a sparse set. Each pool contains all the instances of a single component type and all the entities to 
which it's assigned.

# The System, Entity, and Components
A system stores and manages entities or entity indetifiers and allows to user add components to it, that will be stored in pool or list.
Class `JECS` let user create a normal system with default entity idenfier type as `int` also knows as `int32` or `Integer`.   
   
The class generic `JECSGeneric<EntityT>` lets users decide what's the preferred type to represent an entity. By default system will use entity `int` because this
more than enogth to hold all entites, or if you make a small system you can use `short` as identifies (bad choice), or for really large systems `long` as entity
identifier.   
   
Note all examples will use keyword `var`, because entity and components types can be different. The system allows create and destroy entities:
```java

// Create new empty entity with no component attached and return identifier.
var entity = system.create();

// Destroy entity with all components.
system.destroy(entity);

```
The create method has also array version and custom user identifier version. And the same with destroying.
```java

// Create 100 new entities and returns identifiers in EntityT[].
var entities = system.insert(100);

// And destruction.
system.destroy(entities);

```
At the case if you wan't keep the entity identifier in system, but removing/releasing components you can use:
```java

// Remove all components from the entity but keep entity in system.
system.release(entity);
```
Components can be assigned to or removed from entities at any time. As for the entities, the registry offers a set of methods to use to work with components.   
`JECS` support two ways to giving or constructing component to a entity, by **instance**, or by **class-type** its when method has parameter `Class<C>`. So in
that case you accessing to class type by ```Component.class```.
   
To add component to entity use `emplace` method that with initial overload accepts entity identifer and component instance. Basically is emplace component 
with to that entity. All components in Java will connect with `Object` and that means that component should not the null when assigned to entity.   
```java
system.emplace(entity, new TransformComponent());
```
Or another very usefull overload of method `emplace` that accept entity identifier, component type, and arguments to call propely constructor of that component
instance.
```java
var transform = system.emplace(entity, TransformComponent.class, position, rotation, scale);
// ...

transform.position.x = 0.5f;
transform.scale = new Vector3(1, 1, 1);

```
You can also create components without specifying any constructor, since in this case the system will call the default invisible constructor.
Also on other side `insert` works with ranges and can be used to inserting component or component of the same type to all entites or to specific
entites in range.
```java

// Insert to all entities component with default constructor.
system.insert(TransformComponent.class);

// Insert to all entities component with default constructor in range from 0 to 20.
system.insert(TransformComponent.class, 0, 20);
```
Or from user-define initialized instance:
```java
system.insert(new TransformComponent(p, r, s));
```
If an entity already has a component of a given type, you cannot add a component of the same type, because the library does not support this move. 
But you can use `replace` methods to update a component of a given type and use `patch` to change the data of the component in-place.
```java

// Replace the component instance by new constructing from arguments.
system.replace(entity, TransformComponent.class, new Vector3f(1.0f, 1.0f, 1.0f));

// Update the component data.
system.patch(entity, TransformComponent.class, (component) -> {
	component.position.x = 5.0f;
});
```
If you don't know if a given entity has this component or not, then you can use `replaceOrEmplace` for this case.
```java
system.replaceOrEmplace(entity, TransformComponent.class, new Vector3f(10.0f, 12.0f, 13.0f));
```
The `all` and `any` methods may also be useful if in doubt about whether or not an entity has all the components in a set or any of them:
```java

// Returns true if one of given components exist.
boolean any = system.any(entity, TransformComponent.class, SpriteComponent.class);

 // Returns true if all of given components exist.
boolean all = system.all(entity, TransformComponent.class, SpriteComponent.class);
```
If you need to remove a component from the entity being used, method `erase` do it:
```java
system.erase(entity, TransformComponent.class);
```
But it throws a exception if component not found, to avoid this use `remove` instead:
```java
system.remove(entity, TransformComponent.class);
```
The `clear` method works similarly and can be used to either:
 - To erase all components of one type from all entities:
   ```java
   system.clear(TransformComponent.class);
   ```
 - To clear all system entities and components:
   ```java
   system.clear();
   ```

And then the most obvious thing is to get the components of the entity using the system:
```java

// Returns the component from entity of the given type.
var transform = system.get(entity,  TransformComponent.class);

// Returns the components in raw array of Object[].
var components = system.get(entity, TransformComponent.class, SpriteRenderer.class, Material.class);

```
To get component type from raw array of Object[] you need to cast each component to that type. Is not safe that can be
situation that you dont know if component presents in specific entity. For that case you can use `push`, `pop` and
`getArr` methods:
```java

// Get components.
var components = system.get(entity, TransformComponent.class, SpriteRenderer.class);

// Push the array to system temprary array, and retrive components from it using getArr.
system.push(components);
var transform = system.getArr(TransformComponent.class);
var spriteRenderer = system.getArr(SpriteRenderer.class);
system.pop();

```
Basically `get` methods return component only then when it actually exist in entity, otherwise it cause exception. To avoid
this use `tryGet`. In this case if component not exist its just return null and not cause any exceptions.
```java
var material =  registry.tryGet(entity, Material.class);
```
# Sorting
`JECS` also supports sorting for the entities by using specific callback method `ISort`.
```java
system.sort((ISort<EntityT>)(a, b) -> {
   return a - b;
});
```
Or use the another parallelSort algorithm:
```java
system.parallelSort(sort);
```
This will sort the entities by their value, this is very useful when you need to display or iterate the entities by their value. 
Unfortunately, it also possible to sort the entities by component component order passing in `ISortC` only one type because the library does not 
support holding multiple instances of a component with the same type. I hope that this will be implemented in the future, although for many cases it is not necessary.
```java
system.sort(TransformComponent.class, (ISortC<TransformComponent>)(l, r) -> {
	return (int) (l.translation.x - r.translation.x);
});
```

# Iteration over entities and components

`JECS` can get access directlly to entities and components and iterate it by one of methods:
 - `each(Class<C> componentT, IEach<EntityT, C> funcImpl)`. This method iterate over each entity including all components of and all sub-components
extended, implemented, inherted from it at runtime. Each uses `IEach` functional interface as additional parameter. Its allows to add 
additional properties inside <code>each</code> function for specific entity and components.
```java
system.each(ComponentBase.class, (entity, component) -> {
	component.printName();
});
```
 - `each(IEachE<EntityT> funcImpl)`. This method iterate over each entity. Each uses `IEachE` functional interface as additional parameter. Its 
allows to add additional properties inside `each` method for specific entity. 
```java
system.each((entity) -> {
	...
});
```
 - `each(IEachC<EntityT, C> funcImpl)`. This method iterate over each entity including all components which are contains at each entity 
in runtime. Each uses `IEachC` functional interface as additional parameter. Its allows to add additional properties inside `each` function 
for specific entity and components. <b>Remember</b> that if you want to call methods, functions, or parameters of a special
component, you must first check that this type of component exists at all and then cast to specific type to invoke content. 
```java
system.each((entity, anyComponent) -> {
	if(system.eqs(ComponentAny.class, anyComponent))
		((ComponentAny) anyComponent).printObj(toString());
});
```
# Invokation

`JECS` allow to call method from its component in runtime, because in Java each Component, is single `java.lang.Object`.
- `invoke(EntityT entity, Class<C> componentT, String funcName,	Object... funcArgs)`. This method invoke function/method from entity 
component at runtime. This method is usually not the best and fastest, but it is very effective and useful when the reference to the component 
is unknown and only the type is known.
```java
// Invoke method 'method' from current entity Transform component.
system.invoke(entity, TransformComponent.class, "method");
```
- `invokeEach(EntityT entity, Class<?>[] componentTs, String funcName, Object... funcArgs)`. This method invoke function/method from entity for 
each components at runtime. If at least one component doesen't have method name with method arguments this method  throws one of system exceptions,
`NoSuchMethodException` or `InvocationTargetException`.   
This method is usually not the best and fastest, but it is very effective and useful when the reference to the component is unknown 
and only the type is known.
```java
// Invoke method 'method' from current entity group of components.
system.invokeEach(entity, JECS.as(TransformComponent.class, Material.class), "method");
```
# Views and Groups
The View and the Group are used for one purpose to get direct access to and their entities from given components and so that something 
can be done with them. In the `JECS` library difference with Group that is this method is faster when user wan't to iterate over all
entities with one type component, but not with all types, that is increase iteration perfomance and in finally should be faster.   
 - Therefore, use `Views` when you need to go through one type of component and all its entities.
 - And use the `Groups` when you need to iterate several different types of components and their entities.   
   
I want to say that this may be an approximation of the concept taken from `ENTT`, but the implementation of these methods is completely 
different, so `ENTT` mainly focuses on memory and memory ordering, and `JECS` makes it possible to use the same concepts in Java.

## View
Creates the view by checking one type component on all entities, and if some entity has given component its put to view pool,
otherwise that entity will be skipped.   
   
A view returns all entities and only the entities that have at least the given components.
```java
var view = system.view(TransformComponent.class);
for(var entity : view) {

  // Get component from view.
  var transform = view.get(entity, TransformComponent.class);
}
```

## Group
Creates the group by checking all typed compoents on all entities, and if some entity has given components its put to group pool, 
otherwise that entity will be skipped. A groups returns all entities and only the entities that have at least the given components.
Groups share instnaces to the underlying data structures/classes of the context that generated them. By context means current system.   

Lifetime of a group must not overcome that of the context that generated it. In any other case, attempting to use a group results in 
undefined behavior or `NullPointerException` or `JECSException`.
```java
var group = system.group(TransformComponent.class, Material.class);
for(var entity : group) {

   // Get component from group.
  var transform = group.get(entity, TransformComponent.class);

  // Or with two components.
  var components = group.getPair(entity, TransformComponent.class, Material.class);
  components.first = ...
  components.second = ...

  // Or with multiple components.
  var components = group.get(entity, TransformComponent.class, Material.class, SpriteRenderer.class);
}
```
# Packs
**Pack** is a specific set or group, where you can store components of entity. Pack should be used when you need to 
group certain components of an entity and iterate them separately, regardless of the main sequence of components. 
This gives a small performance gain, but when an entity has many components, the gain is significantly felt.   
   
I also want to note that when components are placed in a pack, they lose their connection with their entity. This 
is also done in the case of performance. Technically, these component references will be associated with the entity, 
but not in the pack. To get a pack of entity and iterate over components, you must specify the exact entity that the
pack was originally created from.
   
To create a pack of component use `createPack` method, that accept the entity identifier and components types. When pack
will created it place current pack to zero pack map.
```java
// Create entity and emplace components.
var entity = system.create();
system.empalce(TransformComponent.class, position, rotation, scale);
system.emplace(Material.class, new Vector4f(1.0f, 1.0f, 1.0f, 1.0);

// Create a pack.
system.createPack(entity, TransformComponent.class, Material.class);
```
To get a current created pack, use `pack` method:
```java
system.pack(entity, 0);
```
If at least one of compoenent will not contains in that entity returns null.
## Each using pack or iteration
Iterates over pack with index with specific components, and invoke method name with method arguments of each components
from pack. This method is identical to `invokeEach` but with better perfomance. This does not mean that it will 
always need to be used and `invokeEach` will be forgotten. InvokeEach should be used when an entity has several components, and 
`invokeEachPack` is better for entities with a large number of components.
```java

// Get the pack && invoke functions from each compoennt
var pack = system.pack(entity, 0); 
system.invokeEachPack(pack, "func"); 

// Or with arguments...
system.invokeEachPack(pack, "myMethod", new String("MyMethod"), 2003, true); 
```
If you don wan't to use the packs, you also can achive the same result follow this code snippet below:
```java
var components = system.get(entity, Script1.class, Script2.class);
for(var component : components)
 ((ScriptComponent)component).myMethod();
```

# Other usefull utilities
Other interesting utility methods and classes that helps you with entities and components.

## Null Entity
The `NullEntity` class type models the concept of null entity.   
   
This means that as long as the entity is null, we cannot delete, add, or edit its components. This is still a beta 
concept of null entity and will still look for its application in the future. But for now main reason to use null entity
is wrap entity identifer to class and block its interaction, its means adding, removing components and removing identifier.

Following expressions 100% returns false:
```java
// By static NULL_ENTITY constant.
system.isValid(JECS.NULL_ENTITY);

// or by method that generates temprary null identifier.
system.isValid(system.nullEntity());

```
The type of the null entity is special and should not be used for any purpose other than defining the null entity itself. However, there exist 
conversions from the null entity to identifiers of any allowed type:
```java

// Convert the entity to null entity.
var entity = system.create();
var nullEntity = system.nullEntity(entity);

// Convert the null entity to normal.
system.entity(nullEntity);

// Checks if input identifier is null entity.
system.isNullEntity(entity);
```
And finally null entities is not equals to 0. Most closest variant is `JECS.NULL_ENTITY` or `system.nullEntity()`.

## Entity from Component
Sometimes the user needs to get the entity identifier from the instance of the component.    
For this case we can use static method `JECS.toEntity` that accept system and component instnace, or use in-build to system itself
method `get` that accept only component instance.
```java
var entity = JECS.toEntity(system, userComponent);
```
## The As operator
The `as` operator is just another alternative to `new Class<?>[] { ... }`.    
Balance this with what you do otherwise its can little slow you program. 
```java
var components = JECS.as(TransformComponent.class, Material.class);
```
## Callbacks
The system is designed in such a way that listeners are sewn into it / in other words, simple methods or callbacks that the user can call
at different stages when working with the entity.   
Suppose we have a simple entity that we are creating, and we need to track the moment when it is created or perform a number of other 
operations after its creation on the system side.
To do that we need to call overload method `create` that accept functional interface `CreateI`:
```java
var entity = system.create((entity) -> { System.out.println("Entity " + entity + " created!")} );
```
And what it actually will does its printing message to the console imediatlly after assigned entity to system container. And in addition
to the usual output of messages, we can do anything in this callback, naturally within reason, so as not to slow down the system.    
Another example if we iterate over each entity and its components and we need track it:
```java
system.each((entity) -> {
	// do something here
});
```
Here a list with all callbacks that can be perfomed on system methods:   
  - `CreateI`  using for `system.create(CreateI)`   
  - `DestroyI` using for `system.destroy(DestroyI)`   
  - `EachCI`   using for `system.each(Class, EachCI)`   
  - `EachEI`   using for `system.each(EachEI)`   
  - `EachI`    using for `system.each(Class, EachI)`    

There are also some limitations on what a callbacks can and cannot do:
  - Removing the component from within the body of a callback that observes the construction or update of instances of a given type isn't allowed.
  - Assigning and removing components from within the body of a callbacks that observes the destruction of instances of a given type should be avoided. It can lead to undefined behavior in some cases. This type of callbacks is intended to provide users with an easy way to perform cleanup and nothing more.

## Profiling
Sometimes it is necessary to check how much a particular method takes time to execute, this is useful both for me as a developer and for users who 
use the system.    
The system has a built-in static `JECS.elapsed` method that accepts system and the `ElapsedFn` callback, or a non-static `elapsed` method that accepts only `ElapsedFn`.
```java
// It will measure how long it takes to call the create method.
JECS.elapsed(() -> { var entity = system.create(); });
```

## Context
It is also worth noting that each system under the hood creates an instance of its context and holds. In the future, you will be able to change its state, while this idea is still in development.
Basically when use one of `JECS.construct` methods its creates new context for system and append it to list of systems (global contexts). You can share it in the future.

