package main;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import arduino.Arduino;
import arduino.view.ArduinoUno_3D;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class Gui extends JFrame {
	
	JPanel jp_top = new JPanel();
	JPanel jp_bottom = new JPanel();
	//JApplet ja = new JApplet();
	JPanel jp_3dspace = new JPanel();
	JMenuBar menubar = new JMenuBar();
	
	ArduinoUno_3D ArduinoModel;
	Arduino a;
	
	/// Control \\\\
	JButton b_run = new JButton("Run");
	JButton b_stop = new JButton("Stop");
	
	
	/// 3D \\\
	Canvas3D c3d;
	SimpleUniverse u;
	ViewingPlatform viewingPlatform;
	PlatformGeometry pg;
	BoundingSphere bounds;
	
	
	public Gui() {
		init();
	}
	
	public void init() {
		initGUI();
		init3D();
		createArduinoModel();
		a = new Arduino(this.ArduinoModel);
	}
	
	public void initGUI() {

		this.setSize(1024, 768);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		///////////////////// Menu \\\\\\\\\\\\\\\\\\\\\\\\\\\\

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenu menu1 = new JMenu("Edit");
		menuBar.add(menu1);
		JMenu menu2 = new JMenu("View");
		menuBar.add(menu2);
		JMenu menu3 = new JMenu("Simulation");
		menuBar.add(menu3);
		JMenu menu4 = new JMenu("Components");
		menuBar.add(menu4);
		JMenu menu5 = new JMenu("Help");
		menuBar.add(menu5);
		
		/*
		JButton buttonPlay = new JButton("Play", playButtonIcon);
		menuBar.add(buttonPlay);
		JButton buttonStop = new JButton("Stop", stopButtonIcon);
		menuBar.add(buttonStop);
		JButton buttonPause = new JButton("Pause", pauseButtonIcon);
		menuBar.add(buttonPause);
		*/

		JMenuItem item = new JMenuItem("New File");
		item.addActionListener(new MenuActionListener());
		menu.add(item);

		JMenuItem item2 = new JMenuItem("About");
		item.addActionListener(null);
		menu5.add(item2);
		
		JMenuItem item3 = new JMenuItem("Help");
		item.addActionListener(null);
		menu5.add(item3);
		
		JMenuItem item4 = new JMenuItem("Add Components");
		item.addActionListener(null);
		menu4.add(item4);
		
		JMenuItem item5 = new JMenuItem("Ground Source");
		item.addActionListener(null);
		menu4.add(item5);
		
		JMenuItem item6 = new JMenuItem("VCC, LED, SERVO, etc.");
		item.addActionListener(null);
		menu4.add(item6);
		
		JMenuItem item7 = new JMenuItem("Start Simulation");
		item.addActionListener(null);
		menu3.add(item7);
		
		JMenuItem item8 = new JMenuItem("Pause Simulation");
		item.addActionListener(null);
		menu3.add(item8);
		
		JMenuItem item9 = new JMenuItem("End Simulation");
		item.addActionListener(null);
		menu3.add(item9);
		
		JMenuItem item10 = new JMenuItem("I/O Graphs");
		item.addActionListener(null);
		menu3.add(item10);
		
		this.setJMenuBar(menuBar);
		//////////////////////////////////////////////////////////////////////////////////
		
		final Thread arduinoRunner = new Thread() {
			public void run() {
				a.run();
			}
		};
		
		
		b_run.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				arduinoRunner.start();
			}
		});
		jp_top.add(this.b_run);
		
		b_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				arduinoRunner.stop();
			}
		});
		jp_top.add(this.b_stop);
		
		
		this.add(jp_top, BorderLayout.NORTH);
		
		/*
		Container ja_content = ja.getContentPane();
		ja_content.setBackground(Color.BLACK);
		ja_content.add(new JMenuBar());
		ja_content.setLayout(new FlowLayout()); 
		ja_content.add(new JButton("Play"));
		ja_content.add(new JButton("Pause"));
		ja_content.add(new JButton("Stop"));
		ja.setVisible(true);
		this.add(ja);
		*/
		
		this.jp_3dspace.setBackground(Color.GRAY);
		this.jp_3dspace.setLayout(new BorderLayout());
		//this.jp_3dspace.add(new JButton("Play"));
		//this.jp_3dspace.add(new JButton("Pause"));
		//this.jp_3dspace.add(new JButton("Stop"));
		
		this.c3d = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		this.jp_3dspace.add("Center", this.c3d);
		this.add(this.jp_3dspace);
		
		jp_bottom.add(new JLabel("Bottom JPanel"));
		this.add(jp_bottom, BorderLayout.SOUTH);
		
		this.setVisible(true);
		
	}
	
	public void init3D() {
		
		this.u = new SimpleUniverse(this.c3d);
		this.viewingPlatform = u.getViewingPlatform();
		this.pg = new PlatformGeometry();

		
		viewingPlatform.setPlatformGeometry(pg);
		
		viewingPlatform.setNominalViewingTransform();

		// Add Mouse Interaction
		OrbitBehavior orbit = new OrbitBehavior(this.c3d,OrbitBehavior.REVERSE_ALL);
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0,0.0), 100.0);
		orbit.setSchedulingBounds(bounds);
		viewingPlatform.setViewPlatformBehavior(orbit);
		
	}
	
	public void createArduinoModel(/*URL fileName*/) {
		
		/*
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
*/
		/*
		int flags = ObjectFile.RESIZE;
		if (!noTriangulate)
			flags |= ObjectFile.TRIANGULATE;
		if (!noStripify)
			flags |= ObjectFile.STRIPIFY;
		*/
	/*	
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

		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

		// Set up the background
		Background bgNode = new Background(new Color3f(Color.white));
		bgNode.setApplicationBounds(bounds);
		objRoot.addChild(bgNode);
		
		Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f light2Direction = new Vector3f(-1.0f, -1.0f, -1.0f);

		DirectionalLight light2 = new DirectionalLight(light2Color,light2Direction);
		light2.setInfluencingBounds(bounds);
		objRoot.addChild(light2);

		
		objRoot.addChild(new LED_3D(0).create(-0.45f, 0.12f, -0.597f));
		objRoot.addChild(new LED_3D(1).create(-0.45f, 0.12f, -0.547f));
		objRoot.addChild(new LED_3D(2).create(-0.45f, 0.12f, -0.497f));
		objRoot.addChild(new LED_3D(3).create(-0.45f, 0.12f, -0.447f));
		objRoot.addChild(new LED_3D(4).create(-0.45f, 0.12f, -0.397f));
		objRoot.addChild(new LED_3D(5).create(-0.45f, 0.12f, -0.347f));
		objRoot.addChild(new LED_3D(6).create(-0.45f, 0.12f, -0.297f));
		objRoot.addChild(new LED_3D(7).create(-0.45f, 0.12f, -0.247f));
		
		objRoot.addChild(new LED_3D(8).create(-0.45f, 0.12f, -0.167f));
		objRoot.addChild(new LED_3D(9).create(-0.45f, 0.12f, -0.117f));
		objRoot.addChild(new LED_3D(10).create(-0.45f, 0.12f, -0.067f));
		objRoot.addChild(new LED_3D(11).create(-0.45f, 0.12f, -0.017f));
		objRoot.addChild(new LED_3D(12).create(-0.45f, 0.12f, 0.033f));
		objRoot.addChild(new LED_3D(13).create(-0.45f, 0.12f, 0.083f));
		
		*/
		
		this.u.addBranchGraph((ArduinoModel = new ArduinoUno_3D()).createArduino());
		
		
	}
	
	class MenuActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			//System.out.println("Selected: " + e.getActionCommand());
	    }
	}
	

}
