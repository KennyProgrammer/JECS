
import component.ScriptA;
import component.ScriptB;
import component.ScriptComponent;
import kenny.jecs.JECSException;
import kenny.jecs.JECSHandle;

public class Main3
{
	
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, JECSException
	{
		new Main3().init();
	}
	
	public void init() throws JECSException
	{	
		JECSHandle<Object> system = JECSHandle.construct();
		{
			int scriptableGameObject = system.create();
			system.emplace(scriptableGameObject, ScriptA.class);
			system.emplace(scriptableGameObject, ScriptB.class);	
			System.out.println("Has: " + system.has(scriptableGameObject, ScriptA.class, ScriptB.class));
			
			boolean flag = true;
			
			while(flag)
			{
				//system.get(scriptableGameObject, ScriptA.class).onUpdate();
				//system.get(scriptableGameObject, ScriptB.class).onUpdate();
				
				system.invokeEach(scriptableGameObject, new Class<?>[] {ScriptB.class, ScriptA.class}, 
						"onUpdate");	
			}
		}
		system = JECSHandle.deconstruct(system);
	}
}