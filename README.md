# JECS
![JECS](.github/Logo.png?raw=true "JECS - Java Entity Component System")

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
		JECS<Object> jecs = JECS.construct();
		
		// Create new entity identifier.
		int entity = jecs.create();
		// Emplace component to this entity.
		jecs.emplace(entity, Transform.class, 10.0f, 0.0f, 0.0f);
		// Emplace component with NULL args.
		jecs.emplace(entity, Data.class, JECS.NULL);
		
		// Check if this entity has a Transform Component class. 
		if(jecs.has(entity, Transform.class))
			for(int i = 0; i < 10; i++)
				update(jecs, entity);
		
		// Replace component with different data.
		jecs.replace(entity, Transform.class, 20.0f, 10.0f, 1.0f);
		// Returns the component from entity.
		jecs.get(entity, Transform.class);
		// Destroy this entity identifier.
		jecs.destroy(entity);
		
		JECS.deconstruct(jecs);
	}
	
	public static void update(JECS<Object> jecs, int entity) {
		// Iterate over all entities. 
		// Using Callback/Lamba.
		jecs.each((int entty, Object component) -> { /* ... */ });
		
		// Iterate over entities that has common component.
		jecs.each(Transform.class, (int entty, Transform component) -> { /* ... */ });
		
		// Invoke method 'method' from current entity Transform component.
		jecs.invoke(entity, Transform.class, "method");
		
		// Invoke method 'method' from current entity group of components.
		jecs.invokeEach(entity, new Class<?>[] {Transform.class, Data.class}, "method");
	}
}

```

## Requiements:
To use JECS you also need [LWJGL 3 Core](https://www.lwjgl.org/customize).
