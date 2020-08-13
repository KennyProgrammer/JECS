package example;

import component.Component;
import component.RendererComponent;
import component.TagComponent;
import component.TransformComponent;
import component.Vector3f;
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
			int entity = system.create();
			system.emplace(entity, TagComponent.class, "TagComponent");
			system.emplace(entity, TransformComponent.class, new Vector3f(1, 2, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1));
			system.emplace(entity, RendererComponent.class, true);
			
			System.out.println(system.get(entity, TagComponent.class).tag);
			System.out.println(system.get(entity, TransformComponent.class).scale.x);
			//
			//system.erase(entity, TagComponent.class, RendererComponent.class);
			
			boolean f = system.has(entity, TagComponent.class);
			boolean f2 = system.has(entity, TransformComponent.class);
			boolean f3 = system.has(entity, RendererComponent.class);
			//System.out.println(f + "" + f2 + "" + f3);
			
			//boolean f = system.has(entity, TagComponent.class, TransformComponent.class, RendererComponent.class);
			
			System.out.println(f + "");
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