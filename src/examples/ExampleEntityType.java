package examples;

import com.kenny.jecs.BaseJECS.JECS;
import com.kenny.jecs.BaseJECS.JECSGeneric;

public class ExampleEntityType {
	
	public static void main(String[] args) {
		
		// Create a system with short type.
		JECSGeneric<Short> system = JECS.construct(Short.class);
		
		// Create entity and print its value.
		short entity = system.create();
		System.out.println("Short entity: " + entity);
		
		system.destroy(entity);
		JECS.deconstruct(system);
		
		// ==========================================================
		
		// Create a system with long type.
		JECSGeneric<Long> system2 = JECS.construct(Long.class);

		// Create entity and print its value.
		long entity2 = system2.create();
		System.out.println("Long entity: " + entity2);
		
		system2.destroy(entity2);
		JECS.deconstruct(system2);
	}
}
