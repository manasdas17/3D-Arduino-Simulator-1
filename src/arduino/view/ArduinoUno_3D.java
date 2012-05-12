package arduino.view;

import java.awt.Color;
import java.io.FileNotFoundException;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;


public class ArduinoUno_3D {
	
	public ArduinoUno_3D() { }
	
	
	public LED_3D[] Leds = new LED_3D[] {
			new LED_3D(0, -0.45f, 0.12f, -0.597f),
			new LED_3D(1, -0.45f, 0.12f, -0.547f),
			new LED_3D(2, -0.45f, 0.12f, -0.497f),
			new LED_3D(3, -0.45f, 0.12f, -0.447f),
			new LED_3D(4, -0.45f, 0.12f, -0.397f),
			new LED_3D(5, -0.45f, 0.12f, -0.347f),
			new LED_3D(6, -0.45f, 0.12f, -0.297f),
			new LED_3D(7, -0.45f, 0.12f, -0.247f),
			new LED_3D(8, -0.45f, 0.12f, -0.167f),
			new LED_3D(9, -0.45f, 0.12f, -0.117f),
			new LED_3D(10, -0.45f, 0.12f, -0.067f),
			new LED_3D(11, -0.45f, 0.12f, -0.017f),
			new LED_3D(12, -0.45f, 0.12f, 0.033f),
			new LED_3D(13, -0.45f, 0.12f, 0.083f)
	};

	public BranchGroup createArduino(/*URL fileName*/) {

		double creaseAngle = 60.0; //LIGHT ANGLE

		String filename = "3d/Arduino.obj";

		Scene s = null;
		BranchGroup objRoot = new BranchGroup();

		// Create a Transformgroup to scale all objects so they
		// appear in the scene.
		TransformGroup objScale = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setScale(0.7);
		objScale.setTransform(t3d);
		objRoot.addChild(objScale);

		// Create the transform group node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime. Add it to the
		// root of the subgraph.
		TransformGroup objTrans = new TransformGroup();
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objScale.addChild(objTrans);

		/*
		int flags = ObjectFile.RESIZE;
		if (!noTriangulate)
			flags |= ObjectFile.TRIANGULATE;
		if (!noStripify)
			flags |= ObjectFile.STRIPIFY;
		 */

		int flags = (ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY);

		ObjectFile f = new ObjectFile(flags, (float) (creaseAngle * Math.PI / 180.0));

		try {
			s = f.load(filename);
		} catch (FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		} catch (ParsingErrorException e) {
			System.err.println(e);
			System.exit(1);
		} catch (IncorrectFormatException e) {
			System.err.println(e);
			System.exit(1);
		}

		objTrans.addChild(s.getSceneGroup());

		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Set up the background
		Background bgNode = new Background(new Color3f(Color.white));
		bgNode.setApplicationBounds(bounds);
		objRoot.addChild(bgNode);

		Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f light2Direction = new Vector3f(-1.0f, -1.0f, -1.0f);

		DirectionalLight light2 = new DirectionalLight(light2Color,light2Direction);
		light2.setInfluencingBounds(bounds);
		objRoot.addChild(light2);

		for (int i = 0; i < this.Leds.length; i++)
			objRoot.addChild(this.Leds[i].getTransformGroup());
		
		return objRoot;

	}

}
