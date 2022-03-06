package examples;

import java.util.Random;

import com.kenny.jecs.BaseJECS.JECS;
import com.kenny.jecs.funcs.ISort;
import com.kenny.jecs.funcs.ISortC;

public class ExampleSorting {
	
	public class Transform {
		float pX, pY, pZ = 0.0f;
		float rX, rY, rZ = 0.0f;
		float sX, sY, sZ = 1.0f;
		
		public Transform(float x, float y, float z) {
			pX = x; 
			pY = y; 
			pZ = z;
		}
	}

	public static void main(String[] args) {
		
		// Create a system.
		JECS system = JECS.construct();
		
		// Create 10 entities, and insert for each of them Transform components with different
		// z values.
		var entities = system.insert(10);
		for(var entity : entities) 
			system.emplace(entity, Transform.class, 0.0f, 0.0f, (float) new Random().nextInt());
		
		System.out.println("Entities before sorting: ");
		for(var entity : entities) 
			System.out.println(entity);
		
		// Sort entities by identifier.
		system.sort((ISort<Integer>)(a, b) -> {
			  return a - b;
		});
		
		var sortedEntities = system.getAll(Transform.class);
		System.out.println("Entities after sorting: ");
		for(var entity : sortedEntities) 
			System.out.println(entity);
		
		// Sort entities by Transform component z value.
		system.sort(Transform.class, (ISortC<Transform>)(l, r) -> {
			return (int) (r.pZ - l.pZ);
		});
		
		var sortedEntities2 = system.getAll(Transform.class);
		System.out.println("Entities after sorting (by Transform component Z value): ");
		for(var entity : sortedEntities2) 
			System.out.println(entity);
		
		// Destroy system.
		JECS.deconstruct(system);
	}
}
