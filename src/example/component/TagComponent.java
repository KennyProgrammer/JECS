package example.component;

import java.util.Random;

public class TagComponent extends Component
{
	public String tag = "Untagged";
	
	public TagComponent(String tag) 
	{
		this.tag = tag;
	}
	
	public TagComponent() {}
	
	public void printTag()
	{
		System.out.println(tag);
	}
	
	public void printTagAnd(String some, int a, boolean b, Random r)
	{
		System.out.println(tag + ", " + some + ", " + a + ", " + b + ", " + r.nextInt(43434) + ".");
	}
	
}