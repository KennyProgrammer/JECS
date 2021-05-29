package example.component;

public class ScriptA extends ScriptComponent
{
	public ScriptA() {
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
