import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import example.component.Component;
import example.component.RendererComponent;
import example.component.TagComponent;
import example.component.TransformComponent;
import example.component.Vector3f;
import kenny.jecs.JECSException;
import kenny.jecs.JECSHandle;

public class Main 
{
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, JECSException
	{
		new Main().init();
	}
	
	public void init() throws JECSException
	{	
		JECSHandle<Component> system = JECSHandle.construct();
		{
			int invalidEntity = 32342324;
			int entity = system.create();
			int entity2 = system.create();
		
			
			//int v = system.destroy(entity, entity2);
			
			system.emplace(entity, TagComponent.class, "TagComponent");
			system.emplace(entity, TransformComponent.class, new Vector3f(1, 2, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
			system.emplace(entity, RendererComponent.class, true);
			
			system.replace(invalidEntity, RendererComponent.class, true);
			
			system.replace(entity, new Class[] {RendererComponent.class, TagComponent.class}, 
					new Object[] {new Object[] {false}, new Object[] {"Replaced TagComponent"}});
			
			System.out.println(system.get(entity, TagComponent.class).tag);
			//System.out.println(system.get(entity, TransformComponent.class).scale.x);
			//
			//system.erase(entity, TagComponent.class, RendererComponent.class);
			
			boolean f = system.has(entity, TagComponent.class);
			boolean f2 = system.has(entity, TransformComponent.class);
			boolean f3 = system.has(entity, RendererComponent.class);
			//System.out.println(f + "" + f2 + "" + f3);
			
			//boolean f = system.has(entity, TagComponent.class, TransformComponent.class, RendererComponent.class);
			
			system.replaceOrEmplace(entity, TagComponent.class, "Replace or Emplace TagComponent");
			System.out.println(system.get(entity, TagComponent.class).tag);
			
			system.replaceOrEmplace(invalidEntity, new Class[] {RendererComponent.class, TagComponent.class}, 
					new Object[] {new Object[] {false}, new Object[] {"Replace or Emplace TagComponent 2"}});
			
			boolean f4 = system.has(invalidEntity, TagComponent.class);
			
			System.out.println(f4 + "" + system.get(invalidEntity, TagComponent.class).tag);
		
			boolean f5 = system.any(invalidEntity, TransformComponent.class, TagComponent.class);
			boolean f6 = system.any(entity2, TransformComponent.class, TagComponent.class, RendererComponent.class);
			
			System.out.println(f5 + " " + f6);
			
			system.erase(entity, TagComponent.class);
			
			//var componentGet = system.getOrEmplace(entity, TagComponent.class);
			var componentEmplace = system.getOrEmplace(entity, TagComponent.class, "GetOrEmplace: Emplace Taggiy! (*_*)");
			System.out.println(componentEmplace.tag);
			
			TagComponent taggiy = system.get(entity, TagComponent.class);
			
			system.invoke(entity, TagComponent.class, "printTag");
			system.invoke(entity, TagComponent.class, "printTagAnd", "TagComponent", 5, true, new Random());
			//taggiy.printTagAnd("TagComponent", 5, true, new Random());
		}
		system = JECSHandle.deconstruct(system);

	
		/*
		JECSHandleOld<Object> j = new JECSHandleOld<Object>();
		
		int entity = j.create();
		
		j.emplace(new TagComponent("hgujrhgh"));
		System.out.println(j.get(TagComponent.class).tag);
		*/
		
		
	}
}