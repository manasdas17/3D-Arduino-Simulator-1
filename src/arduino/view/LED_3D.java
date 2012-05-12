package arduino.view;
import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Box;

public class LED_3D {

	Appearance cubeAppearance;
	TransformGroup tfg;
	Material m;
	int pinNo;
	
	public LED_3D(int pinNo, float x, float y, float z) {
		this.pinNo = pinNo;
		this.create(x, y, z);
	}

	public void create(float x, float y, float z) {
		
		this.createAppearance();
		
		Box box = new Box(0.015f, 0.015f, 0.015f, cubeAppearance);
				
		TransformGroup tg = new TransformGroup();
		Transform3D transform = new Transform3D();
		Vector3f vector = new Vector3f(x, y, z);
		transform.setTranslation(vector);
		tg.setTransform(transform);
		tg.addChild(box);
		
		this.tfg = tg;
		
		//rootGroup.addChild(tg);
	}
	
	public TransformGroup getTransformGroup() {
		return this.tfg;
	}

	void createAppearance() {
		cubeAppearance = new Appearance();
		cubeAppearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		
		Color3f ambientColour = new Color3f();
		ambientColour.set(Color.GRAY);
		Color3f emissiveColour = new Color3f(0.0f, 1.0f, 0.0f);
		Color3f specularColour = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f diffuseColour = new Color3f();
		diffuseColour.set(Color.GRAY);

		float shininess = 20.0f;
		m = new Material(ambientColour, emissiveColour,diffuseColour, specularColour, shininess);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		cubeAppearance.setMaterial(m);
		
	}
	
	public void switchLedOn() {
		Color3f ambientColour = new Color3f();
		ambientColour.set(Color.GREEN);
		Color3f diffuseColour = new Color3f();
		diffuseColour.set(Color.GREEN);
		m.setDiffuseColor(diffuseColour);
		cubeAppearance.setMaterial(m);
		
	}
	
	public void switchLedOff() {
	
		Color3f ambientColour = new Color3f();
		ambientColour.set(Color.GRAY);
		Color3f diffuseColour = new Color3f();
		diffuseColour.set(Color.GRAY);
		m.setDiffuseColor(diffuseColour);
		cubeAppearance.setMaterial(m);
	
	}
}
