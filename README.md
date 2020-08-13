# JECS

<code>JECS</code> or <b>Java Entity-Component-System API</b> this is a small system that holds all entity identifiers in a single object 
as well as their component identifiers. It makes it easy to create entities, put a component in it, and when it is not needed, it is also
easy to delete it without leaving the object in memory.
<p>
This is not the best solution for sorting entities and components around the world, I am  sure there are better solutions in other languages
like C++ and Entt, but I have not seen similar systems in Java.
<p>
<b>Entity</b> The entity is a general purpose object. Usually, it only consists of a unique id. They "tag every coarse gameobject as a 
separate item". Implementations typically use a plain integer for this. <p>
<b>Component</b> the raw data for one aspect of the object, and how it interacts with the world. "Labels the Entity as possessing this 
particular aspect". Implementations typically use structs, classes, or associative arrays. <p>
<b>System</b> "Each System runs continuously (as though each System had its own private thread) and performs global actions on every 
Entity that possesses a Component of the same aspect as that System."

## Example: 
```java
JECSHandle<Object> jecs = JECSHandle.construct();

int entity = jecs.create(); 
jecs.emplace(entity, TagComponent.class, "First Entity"); 
jecs.emplace(entity, TransformComponent.class, new Vector3(0,0,0),  new Vector3(0,0,0), new Vector3(1,1,1)); 
	
while(jecs.has(TransformComponent.class)) 
{
  TransformComponent t = jecs.get(entity, TransformComponent.class);
	t.position.add(0.5f, 0.1f, 0.0f);
	System.out.println(t.position);
}

jecs.erase(entity, TagComponent.class, TransformComponent.class);

jecs = JECSHandle.deconstruct(jecs);
```
