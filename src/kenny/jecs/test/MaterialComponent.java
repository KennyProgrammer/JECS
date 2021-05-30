package kenny.jecs.test;

import org.joml.Vector4f;

public class MaterialComponent {
	public Vector4f color;

	public MaterialComponent(Vector4f color) {
		this.color = color;
	}
	
	public void func() {
		System.out.println("Function from MaterialComponent.");
	}
}
