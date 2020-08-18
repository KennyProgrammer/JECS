package example.component;

public class TransformComponent extends Component
{
	public Vector3f position, rotation, scale;
	
	public TransformComponent(Vector3f position, Vector3f rotation, Vector3f scale) 
	{
		this.position = new Vector3f(position.x, position.y, position.z);
		this.rotation = new Vector3f(rotation.x, rotation.y, rotation.z);
		this.scale = new Vector3f(scale.x, scale.y, scale.z);
	}
	
	public TransformComponent() {}
}