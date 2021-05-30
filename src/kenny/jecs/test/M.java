package kenny.jecs.test;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import kenny.jecs.JECS;

import static kenny.jecs.JECS.*;

public class M 
{
	public class A {
		public class B {
			public class C {
				public class MaterialComponent
				{
					public Vector4f color;

					public MaterialComponent() {
						this.color = new Vector4f();
					}
					
					public MaterialComponent(Vector4f color) {
						this.color = color;
					}
					
					public MaterialComponent(Vector4f color, Integer i) {
						this(color);
					}
					
					public void func() {
						System.out.println("Function from MaterialComponent.");
					}
				}
			}
		}		
	}
	
	public static class TransformComponent
	{
		public Vector3f translation, rotation, scale;

		public TransformComponent(Vector3f translation, Vector3f rotation, Vector3f scale) {
			this.translation = translation;
			this.rotation = rotation;
			this.scale = scale;
		}
		
		public TransformComponent(Vector3f translation) {
			this(translation, new Vector3f(), new Vector3f());
		}
		
		public void func() {
			System.out.println("Function from TransformComponent.");
		}
	}
	
	public static void main(String[] args) {
		
		Test.defaultTest();
		System.exit(0);

		JECS<Object> jecs = JECS.construct(true);
		
		List<Integer> objects = new ArrayList<>();
		objects.add(jecs.create());
		objects.add(jecs.create());
		objects.add(jecs.create());
		
		
		int object = objects.get(0);
		
		// Emplace component by reference.
		//long fn1 = jecs.elapsed(() -> {jecs.emplace(object, new TransformComponent(new Vector3f(1, 0, 1)));});
		//System.out.println("Time: " + (double)fn1 + " ms");
		
		// Emplace component by type and args.
		//long fn2 = jecs.elapsed(() -> {jecs.emplace(object, M.A.B.C.MaterialComponent.class, new Vector4f(2, 3, 2, 1));});
		//System.out.println("Time: " + (double)fn2 + " ms");
				
		// Emplace component by type and args.
		///long fn3 = jecs.elapsed(() -> {jecs.emplace(object, MaterialComponent.class, new Vector4f(2, 3, 2, 1));});
		//System.out.println("Time: " + (double)fn3 + " ms");
		
		// Emplace component by reference.
		//jecs.emplace(object, new TransformComponent(new Vector3f(1, 0, 1)));

		// Emplace component by type and args.
		//jecs.emplace(object, M.A.B.C.MaterialComponent.class, new Vector4f(2, 3, 2, 1));
		//jecs.emplace(object, MaterialComponent.class, new Vector4f(2, 3, 2, 1));
		
		//Object[] components = jecs.emplace(object, new Class<?>[]{MaterialComponent.class, TransformComponent.class}, 
				//new Object[] {new Vector4f(1, 1, 3333, 1)}, new Object[] {new Vector3f(1, 0, 1)});
		
		Object[] components = jecs.emplace(object, new Class<?>[] {MaterialComponent.class, TransformComponent.class}, 
				new Object[] {new Vector4f(1, 3, 3, 1)}, new Object[] {new Vector3f(1, 1, 2)});
		
		((MaterialComponent)components[0]).color.x = 0;
		((TransformComponent)components[1]).scale.x = 5;
		
		boolean hasMaterial = jecs.has(object, MaterialComponent.class);
		boolean hasTransform = jecs.has(object, TransformComponent.class);
		boolean hasAll = jecs.has(object, MaterialComponent.class, TransformComponent.class);
		
		jecs.invoke(object, MaterialComponent.class, "func");
		jecs.invoke(object, TransformComponent.class, "func");
		
		jecs.emplace(object, ScriptA.class, 5, 10.0F, 20.0D, 1L);
		
		jecs.emplace(object, ScriptB.class, NULL);
		
		jecs.replace(object, ScriptA.class, NULL);
		
		jecs.replace(object, new Class<?>[] {ScriptA.class, ScriptB.class}, new Object[] { NULL }, new Object[] { NULL });
		
		jecs.replaceOrEmplace(object, ScriptB.class, NULL);
		
		jecs.invokeEach(object, ScriptComponent.class, "onCreate");
		
		if(hasAll && hasMaterial && hasTransform) {
			
			jecs.erase(object, MaterialComponent.class);
			jecs.erase(object, TransformComponent.class);
			
			hasMaterial = jecs.has(object, MaterialComponent.class);
			hasTransform = jecs.has(object, TransformComponent.class);
			hasAll = jecs.has(object, MaterialComponent.class, TransformComponent.class);
			
			if(!hasAll) {
				jecs.destroy(object);
				
				boolean contains = jecs.contains(object);
				
				if(!contains) {
					JECS.deconstruct(jecs);
				}
			}
		}
	}
}
