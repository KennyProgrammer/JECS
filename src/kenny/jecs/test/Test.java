package kenny.jecs.test;

import kenny.jecs.JECS;
import kenny.jecs.collection.Pair;
import kenny.jecs.collection.PairO;

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
	
	public static void defaultTest() {	
		JECS<Object> jecs = JECS.construct();
		
		// Create new entity identifier.
		int entity = jecs.create();
		// Emplace component to this entity.
		jecs.emplace(entity, Transform.class, 10.0f, 0.0f, 0.0f);
		// Emplace component with NULL args.
		jecs.emplace(entity, Data.class, JECS.NULL);
		
		boolean isNull = jecs.isNullEntity(entity);
		
		if(!isNull) {
			entity = jecs.nullEntity(entity);

			isNull = jecs.isNullEntity(entity);
			if(isNull) {
				entity = jecs.entity(entity);
			}
		}
		
		isNull = jecs.isNullEntity(entity);
		
		// Check if this entity has a Transform Component class. 
		if(jecs.has(entity, Transform.class))
			for(int i = 0; i < 10; i++)
				update(jecs, entity);
		
		// Replace component with different data.
		jecs.replaceOrEmplace(entity, Transform.class, 20.0f, 10.0f, 1.0f);
		
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
		jecs.each((int entty, Object component) -> { /* ... */});
		
		// Using range for
		for(Object component : jecs.each(entity)) {
			if(jecs.eqs(component, Transform.class)) {
				/* ... */
			}
			
			/* ... */
		}
		
		// Iterate over entities that has common component.
		jecs.each(Transform.class, (int entty, Transform component) -> { /* ... */ });
		
		// Invoke method 'method' from current entity Transform component.
		jecs.invoke(entity, Transform.class, "method");
		
		// Invoke method 'method' from current entity group of components.
		jecs.invokeEach(entity, new Class<?>[] {Transform.class, Data.class}, "method");
	}
}
