package examples;

import com.kenny.jecs.BaseJECS.JECS;

public class ExampleGroupsAndViews {
	
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
	
	public class Color {
		float r, g, b, a = 0.0f;
		
		public Color(float r, float g, float b, float a) {
			this.r = r; 
			this.g = g; 
			this.b = b; 
			this.a = a;
		}
	}

	public static void main(String[] args) {
		
		// Create a system.
		JECS system = JECS.construct();
		
		// Create entity and attach Transform and Color.
		int entity = system.create();
		system.emplace(entity, Transform.class, 0.0f, 0.0f, 0.0f);
		system.emplace(entity, Color.class, 0.0f, 0.0f, 0.0f, 1.0f);

		// Create view and iterate over it.
		var view = system.view(Transform.class);
		for(int e : view) {
			Transform t = view.get(e, Transform.class);
			System.out.println("[Transform] Position: " + t.pX + ", " + t.pY + ", " + t.pZ + 
					", Rotation: " + t.rX + ", " + t.rY + ", " + t.rZ + ", Scale: " + t.sX + ", " + t.sY + ", " + t.sZ);
		}
		
		// Create group and iterate over it.
		var group = system.group(Transform.class, Color.class);
		for(int e : group) {
			
			// Using regular get:
			Object[] components = group.get(e, Transform.class, Color.class);
			Transform transform = (Transform) components[0];
			Color     color     = (Color) components[1];
			
			// Print initial values.
			System.out.println("[Transform] Position: " + transform.pX + ", " + transform.pY + ", " + transform.pZ);
			System.out.println("[Color]: " + color.r + ", " + color.g + ", " + color.b + ", " + color.a);
			
			// Change component values:
			system.replace(e, Transform.class, 2.0f, 5.0f, 0.0f);
			system.replace(e, Color.class, 0.0f, 1.0f, 1.0f, 1.0f);
			
			// Or using getPair:
			var pair = group.getPair(e, Transform.class, Color.class);
			transform = pair.first;
			color    = pair.second;
			
			// Print updated values.
			System.out.println("[Transform] Position: " + transform.pX + ", " + transform.pY + ", " + transform.pZ);
			System.out.println("[Color]: " + color.r + ", " + color.g + ", " + color.b + ", " + color.a);
		}
		
		// Destroy system.
		JECS.deconstruct(system);
	}
}
