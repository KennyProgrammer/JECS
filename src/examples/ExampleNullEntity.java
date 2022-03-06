package examples;

import com.kenny.jecs.BaseJECS.JECS;

public class ExampleNullEntity {
	
	public class Transform {
		float pX, pY, pZ = 0.0f;
		float rX, rY, rZ = 0.0f;
		float sX, sY, sZ = 1.0f;
	}

	public static void main(String[] args) {
		
		// Create a system.
		JECS system = JECS.construct();
		
		// Create entity and attach Transform.
		int entity = system.create();
		system.emplace(entity, Transform.class);
		
		// Convert entity to null entity.
		var nullEntity = system.nullEntity(entity);
		
		// ===================================================
		// NOTE: After this you cannot modify state of entity.
		//====================================================
		if(system.isNullEntity(nullEntity)) {
			System.out.println("Entity " + nullEntity.intValue() + " is null.");
		}
		
		// Convert null entity to normal entity.
		system.entity(nullEntity);
		System.out.println("Entity " + entity + " is valid now.");	
		
		// Destroy entity.
		system.destroy(entity);
		
		// Destroy system.
		JECS.deconstruct(system);
	}
}
