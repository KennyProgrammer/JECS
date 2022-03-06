package examples;

import com.kenny.jecs.BaseJECS.JECS;

public class ExampleBasics {
	
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
		
		// Iterate over entity, get component and print its data.
		system.each((e) -> {
			Transform t = system.get(e, Transform.class);
			System.out.println("[Transform] Position: " + t.pX + ", " + t.pY + ", " + t.pZ + 
					", Rotation: " + t.rX + ", " + t.rY + ", " + t.rZ + ", Scale: " + t.sX + ", " + t.sY + ", " + t.sZ);
		});
		
		// Remove component from entity.
		system.erase(entity, Transform.class);
		
		// Destroy entity.
		system.destroy(entity);
		
		// Destroy system.
		JECS.deconstruct(system);
	}
}
