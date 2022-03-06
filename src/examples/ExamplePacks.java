package examples;

import com.kenny.jecs.BaseJECS.JECS;

public class ExamplePacks {
	
	public class Transform {
		float pX, pY, pZ = 0.0f;
		float rX, rY, rZ = 0.0f;
		float sX, sY, sZ = 1.0f;
		
		public Transform(float x, float y, float z) {
			pX = x; 
			pY = y; 
			pZ = z;
		}
		
		public void func() {
			System.out.println("Func from Transform!");
		}
	}
	
	public class Color {
		float r, g, b, a = 0.0f;
		
		public Color(float r, float g, float b, float a) {
			this.r = r; 
			this.g = g; 
			this.b = b; 
			this.a = a;
		}
		
		public void func() {
			System.out.println("Func from Color!");
		}
	}
	
	public class Damage {
		float value = 0.0f;
		
		public Damage(float value) {
			this.value = value;
		}
		
		public void func() {
			System.out.println("Func from Damage and its value is " + value + "!");
		}
	}
	
	public static void main(String[] args) {
		
		// Create a system.
		JECS system = JECS.construct();
		
		// Create entity and attach Transform.
		int entity = system.create();
		system.emplace(entity, Transform.class, 0.0f, 0.0f, 0.0f);
		system.emplace(entity, Color.class, 0.0f, 0.0f, 0.0f, 1.0f);
		system.emplace(entity, Damage.class, 50.0f);
		
		// Create a pack.
		system.createPack(entity, Transform.class, Color.class);
		
		// Get the pack && invoke functions from each compoennt
		var pack = system.pack(entity, 0); 
		system.invokeEachPack(pack, "func");
		
		// Create a second pack.
		system.createPack(entity, Color.class, Damage.class);
		
		// Get the second pack && invoke functions from each compoennt
		var pack2 = system.pack(entity, 1); 
		system.invokeEachPack(pack2, "func");
		
		// Destroy system.
		JECS.deconstruct(system);
	}
}
