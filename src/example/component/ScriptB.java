package example.component;

public class ScriptB extends ScriptComponent
{
	public ScriptB() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(String msg, int id) 
	{
		System.out.println("OnCreate: " + getClass().getTypeName() + ", " + msg + ", " + id + ".");
	}
	
	@Override
	public void onUpdate() 
	{
		System.out.println("OnUpdate: " + getClass().getTypeName());
	}
}
