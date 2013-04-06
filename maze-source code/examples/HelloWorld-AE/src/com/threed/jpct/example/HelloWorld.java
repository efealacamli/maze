package com.threed.jpct.example;

import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.threed.jpct.Camera;
import com.threed.jpct.CollisionEvent;
import com.threed.jpct.CollisionListener;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

/**
 * A simple demo. This shows more how to use jPCT-AE than it shows how to write
 * a proper application for Android. It includes basic activity management to
 * handle pause and resume...
 * 
 * @author EgonOlsen
 * 
 */
public class HelloWorld extends Activity implements SensorEventListener {

	// Used to handle pause and resume...
	private static HelloWorld master = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private World world2= null;
	private World menu = null;
	private RGBColor back = new RGBColor(50, 50, 100);
	private RGBColor back2 = new RGBColor(100, 0, 0);

	private float touchTurn = 0;
	private float touchTurnUp = 0;
	private float xAcc;
	private float yAcc;
	private float zAcc;

	// motion parameters
	private final float FACTOR_FRICTION = 0.5f; // imaginary friction on the screen
	private final float GRAVITY = 9.8f; // acceleration of gravity
	private float mAx; // acceleration along x axis
	private float mAy; // acceleration along y axis
	private float mAz; // acceleration along z axis
	private final float mDeltaT = 0.5f; // imaginary time interval between each acceleration updates

    private SimpleVector move=new SimpleVector(0,0,0);
    private SimpleVector move2;
	private SimpleVector ellipsoid = new SimpleVector(5, 5, 5);
	
	private float xpos = -1;
	private float ypos = -1;

	private Object3D cube = null;
	private Object3D cone = null;
	private Object3D cone2 = null;
	private Object3D cone3 = null;
	private Object3D cone4 = null;
	private Object3D sphere = null;
	private Object3D plane = null;
	private Object3D box = null;
	private Object3D box2 = null;
	private Object3D box3 = null;
	private Object3D box4 = null;
	private Object3D box5 = null;
	private Object3D box6 = null;
	private Object3D box7 = null;
	private Object3D box8 = null;
	private Object3D box9 = null;
	private Object3D box10 = null;
	private Object3D box11 = null;
	private Object3D box12 = null;
	private Object3D plane2= null;
	private Object3D plane3= null;
	private Object3D plane4 = null;
	private int levelNumber = 1;
	private int fps = 0;
	private int currentLevelNumber = 0;

	private Light sun = null;
	private Light sun2 = null;
	
	private SensorManager sensorManager;
	
	boolean restartGame = false;
	boolean nextLevel = false;
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		mGLView = new GLSurfaceView(getApplication());

		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Accelerometer Deneme bölgesi baþlangýcý
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}

		mAx = Math.signum(xAcc) * Math.abs(xAcc);
		mAy = Math.signum(yAcc) * Math.abs(yAcc);
		mAz = Math.signum(zAcc) * Math.abs(zAcc);
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		xAcc = values[0];
		yAcc = values[1];
		zAcc = values[2];

		
	}	
	
	//Accelerometer Deneme bölgesi bitiþi-iyi günler


	public boolean onTouchEvent(MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			xpos = -1;
			ypos = -1;
			touchTurn = 0;
			touchTurnUp = 0;
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();

			touchTurn = xd / -100f;
			touchTurnUp = yd / -100f;
			return true;
		}

		try {
			Thread.sleep(15);
		} catch (Exception e) {
			// No need for this...
		}

		return super.onTouchEvent(me);
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);

			if (master == null) {

				world = new World();
				world.setAmbientLight(20, 20, 20);
				
				menu = new World();
				menu.setAmbientLight(20, 20, 20);
				
				world2 = new World();
				world2.setAmbientLight(20, 20, 20);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);
				
				sun2 = new Light(world);
				sun2.setIntensity(250, 250, 250);

				// Create a texture out of the icon...:-)
				Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.icon)), 64, 64));
				TextureManager.getInstance().addTexture("texture", texture);
				
				Texture texture2 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.wood1)), 256, 256));
				TextureManager.getInstance().addTexture("texture2", texture2);
				
				Texture texture3 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.wood2)), 128, 128));
				TextureManager.getInstance().addTexture("texture3", texture3);
				
				Texture texture4 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.wood3)), 128, 128));
				TextureManager.getInstance().addTexture("texture4", texture4);
				
				Texture texture5 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.finish)), 64, 64));
				TextureManager.getInstance().addTexture("texture5", texture5);
				
				Texture texture6 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.metal)), 64, 64));
				TextureManager.getInstance().addTexture("texture6", texture6);
				
				Texture texture7 = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.raw.black)), 128, 128));
				TextureManager.getInstance().addTexture("texture7", texture7);


//				cube = Primitives.getCube(10);
//				cube.calcTextureWrapSpherical();
//				cube.setTexture("texture");
//				cube.strip();
//				cube.build();
//				cube.translate(-140,80,0);
//				world.addObject(cube);
				
				
				if(levelNumber==1)
				{
				sphere = Primitives.getSphere(5);
				sphere.calcTextureWrapSpherical();
				sphere.setTexture("texture");
				sphere.strip();
				sphere.build();
				sphere.translate(-50,0,-10);
				world.addObject(sphere);
				
				sphere.setCollisionMode(Object3D.COLLISION_CHECK_SELF);				
				
				plane = Primitives.getPlane(1, 200);
				plane.calcTextureWrapSpherical();
				plane.setTexture("texture4");
				plane.strip();
				plane.build();
				plane.translate(-100,0,0);
				world.addObject(plane);
				
				plane2 = Primitives.getPlane(1, 200);
				plane2.calcTextureWrapSpherical();
				plane2.setTexture("texture4");
				plane2.strip();
				plane2.build();
				plane2.translate(200,0,0);
				world.addObject(plane2);
				plane2.addParent(plane);
				
				//Finish hole
				cone = Primitives.getCone(8);
				cone.calcTextureWrapSpherical();
				cone.setTexture("texture5");
				cone.strip();
				cone.build();
				cone.translate(-40,-40,2.5f);
				cone.rotateX((float) (Math.PI * .5f));
				world.addObject(cone);
				cone.addParent(plane);
				cone.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
				cone.addCollisionListener(new CollisionListener() {
					
					@Override
					public boolean requiresPolygonIDs() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void collision(CollisionEvent ce) {
						// TODO Auto-generated method stub
						Log.d("PickCollision", "colision vaaaarrrrrrrrrrr");
						nextLevel = true;
					}
				});
						
				//Hole2
				cone2 = Primitives.getCone(8);
				cone2.calcTextureWrapSpherical();
				cone2.setTexture("texture7");
				cone2.strip();
				cone2.build();
				cone2.translate(230,-70,2.5f);
				cone2.rotateX((float) (Math.PI * .5f));
				world.addObject(cone2);
				cone2.addParent(plane);
				cone2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
				cone2.addCollisionListener(new CollisionListener() {
					
					@Override
					public boolean requiresPolygonIDs() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void collision(CollisionEvent ce) {
						// TODO Auto-generated method stub
						Log.d("PickCollision", "dustun, oyun yeniden baslayacak");
						//finish();
						restartGame = true;
						
					}
				});
				
				
				cone3 = Primitives.getCone(8);
				cone3.calcTextureWrapSpherical();
				cone3.setTexture("texture7");
				cone3.strip();
				cone3.build();
				cone3.translate(90,-70,2.5f);
				cone3.rotateX((float) (Math.PI * .5f));
				world.addObject(cone3);
				cone3.addParent(plane);
				cone3.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
				cone3.addCollisionListener(new CollisionListener() {
					
					@Override
					public boolean requiresPolygonIDs() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void collision(CollisionEvent ce) {
						// TODO Auto-generated method stub
						Log.d("PickCollision", "dustun, oyun yeniden baslayacak");
						//finish();
						restartGame = true;
						
					}
				});
				
				cone4 = Primitives.getCone(8);
				cone4.calcTextureWrapSpherical();
				cone4.setTexture("texture7");
				cone4.strip();
				cone4.build();
				cone4.translate(125,55,2.5f);
				cone4.rotateX((float) (Math.PI * .5f));
				world.addObject(cone4);
				cone4.addParent(plane);
				cone4.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
				cone4.addCollisionListener(new CollisionListener() {
					
					@Override
					public boolean requiresPolygonIDs() {
						// TODO Auto-generated method stub
						return false;
					}
					
					@Override
					public void collision(CollisionEvent ce) {
						// TODO Auto-generated method stub
						Log.d("PickCollision", "dustun, oyun yeniden baslayacak");
						//finish();
						restartGame = true;
						
					}
				});
				//left boundary
				box = Primitives.getBox(5, 19);
				box.calcTextureWrapSpherical();
				box.setTexture("texture2");
				box.strip();
				box.build();
				box.translate(-90,0,-5);
				box.rotateY((float) (Math.PI * .25f));
				world.addObject(box);
				box.addParent(plane);
				
				//up boundary
				box2 = Primitives.getBox(5, 20);
				box2.calcTextureWrapSpherical();
				box2.setTexture("texture2");
				box2.strip();
				box2.build();
				box2.translate(0,-100,-5);
				box2.rotateY((float) (Math.PI * .25f));
				box2.rotateZ((float) (Math.PI * .5f));
				world.addObject(box2);
				box2.addParent(plane);
				
				box7 = Primitives.getBox(5, 20);
				box7.calcTextureWrapSpherical();
				box7.setTexture("texture2");
				box7.strip();
				box.build();
				box7.translate(200,-100,-5);
				box7.rotateY((float) (Math.PI * .25f));
				box7.rotateZ((float) (Math.PI * .5f));
				world.addObject(box7);
				box7.addParent(plane);
				
				//right boundary
				box3 = Primitives.getBox(5, 19);
				box3.calcTextureWrapSpherical();
				box3.setTexture("texture2");
				box3.strip();
				box3.build();
				box3.translate(290,0,-5);
				box3.rotateY((float) (Math.PI * .25f));
				world.addObject(box3);
				box3.addParent(plane);
				
				//down boundary
				box4 = Primitives.getBox(5, 20);
				box4.calcTextureWrapSpherical();
				box4.setTexture("texture2");
				box4.strip();
				box4.build();
				box4.translate(0,100,-5);
				box4.rotateY((float) (Math.PI * .25f));
				box4.rotateZ((float) (Math.PI * .5f));
				world.addObject(box4);
				box4.addParent(plane);
				//wall
				box8 = Primitives.getBox(5, 20);
				box8.calcTextureWrapSpherical();
				box8.setTexture("texture2");
				box8.strip();
				box8.build();
				box8.translate(200,100,-5);
				box8.rotateY((float) (Math.PI * .25f));
				box8.rotateZ((float) (Math.PI * .5f));
				world.addObject(box8);
				box8.addParent(plane);
	
//				box5 = Primitives.getBox(5, 8);
//				box5.calcTextureWrapSpherical();
//				box5.setTexture("texture2");
//				box5.strip();
//				box5.build();
//				box5.translate(55,55,-5);
//			//	box5.rotateY((float) (Math.PI * .25f));
//				world.addObject(box5);
//				box5.addParent(plane);
//				

				box5 = Primitives.getBox(5, 8);
				box5.calcTextureWrapSpherical();
				box5.setTexture("texture2");
				box5.strip();
				box5.build();
				box5.translate(55,55,-5);
				box5.rotateY((float) (Math.PI * .25f));
				world.addObject(box5);
				box5.addParent(plane);
				
				box6 = Primitives.getBox(5, 8);
				box6.calcTextureWrapSpherical();
				box6.setTexture("texture2");
				box6.strip();
				box6.build();
				box6.translate(10,-55,-5);
				box6.rotateY((float) (Math.PI * .25f));
				world.addObject(box6);
				box6.addParent(plane);
				
				box6 = Primitives.getBox(5, 8);
				box6.calcTextureWrapSpherical();
				box6.setTexture("texture2");
				box6.strip();
				box6.build();
				box6.translate(10,-55,-5);
				box6.rotateY((float) (Math.PI * .25f));
				world.addObject(box6);
				box6.addParent(plane);
				
				box10 = Primitives.getBox(5, 8);
				box10.calcTextureWrapSpherical();
				box10.setTexture("texture2");
				box10.strip();
				box10.build();
				box10.translate(200,-55,-5);
				box10.rotateY((float) (Math.PI * .25f));
				world.addObject(box10);
				box10.addParent(plane);
		
				box12 = Primitives.getBox(5, 8);
				box12.calcTextureWrapSpherical();
				box12.setTexture("texture2");
				box12.strip();
				box12.build();
				box12.translate(120,-55,-5);
				box12.rotateY((float) (Math.PI * .25f));
				world.addObject(box12);
				box12.addParent(plane);
				
				
				box11 = Primitives.getBox(5, 8);
				box11.calcTextureWrapSpherical();
				box11.setTexture("texture2");
				box11.strip();
				box11.build();
				box11.translate(175,55,-5);
				box11.rotateY((float) (Math.PI * .25f));
				world.addObject(box11);
				box11.addParent(plane);
				
				
				plane.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				Config.collideOffset = 250;
				plane.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				plane2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				//Config.collideOffset = 250;
				//plane2.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box2.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box3.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box3.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box4.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box4.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box5.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box5.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box6.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				box6.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
				box7.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				box8.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				box10.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				box11.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				box12.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
				
				}
				
//			if(nextLevel)
//			{
//				sphere = Primitives.getSphere(5);
//				sphere.calcTextureWrapSpherical();
//				sphere.setTexture("texture");
//				sphere.strip();
//				sphere.build();
//				sphere.translate(-50,0,-10);
//				world2.addObject(sphere);
//				
//				sphere.setCollisionMode(Object3D.COLLISION_CHECK_SELF);				
//				
//				plane = Primitives.getPlane(1, 200);
//				plane.calcTextureWrapSpherical();
//				plane.setTexture("texture4");
//				plane.strip();
//				plane.build();
//				plane.translate(-100,0,0);
//				world2.addObject(plane);
//				
//				plane2 = Primitives.getPlane(1, 200);
//				plane2.calcTextureWrapSpherical();
//				plane2.setTexture("texture4");
//				plane2.strip();
//				plane2.build();
//				plane2.translate(200,0,0);
//				world2.addObject(plane2);
//				plane2.addParent(plane);
//				
//				//Finish hole
//				cone = Primitives.getCone(8);
//				cone.calcTextureWrapSpherical();
//				cone.setTexture("texture5");
//				cone.strip();
//				cone.build();
//				cone.translate(-60,75,2.5f);
//				cone.rotateX((float) (Math.PI * .5f));
//				world2.addObject(cone);
//				cone.addParent(plane);
//				cone.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
//				cone.addCollisionListener(new CollisionListener() {
//					
//					@Override
//					public boolean requiresPolygonIDs() {
//						// TODO Auto-generated method stub
//						return false;
//					}
//					
//					@Override
//					public void collision(CollisionEvent ce) {
//						// TODO Auto-generated method stub
//						Log.d("PickCollision", "colision vaaaarrrrrrrrrrr");
//						nextLevel = true;
//					}
//				});
//						
//				//Hole
//				cone2 = Primitives.getCone(8);
//				cone2.calcTextureWrapSpherical();
//				cone2.setTexture("texture7");
//				cone2.strip();
//				cone2.build();
//				cone2.translate(150,20,2.5f);
//				cone2.rotateX((float) (Math.PI * .5f));
//				world2.addObject(cone2);
//				cone2.addParent(plane);
//				cone2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);	//TODO self yapip kendi check for collision yapmayi dene
//				cone2.addCollisionListener(new CollisionListener() {
//					
//					@Override
//					public boolean requiresPolygonIDs() {
//						// TODO Auto-generated method stub
//						return false;
//					}
//					
//					@Override
//					public void collision(CollisionEvent ce) {
//						// TODO Auto-generated method stub
//						Log.d("PickCollision", "dustun, oyun yeniden baslayacak");
//						//finish();
//						restartGame = true;
//						
//					}
//				});
//				
//				//left boundary
//				box = Primitives.getBox(5, 19);
//				box.calcTextureWrapSpherical();
//				box.setTexture("texture2");
//				box.strip();
//				box.build();
//				box.translate(-90,0,-5);
//				box.rotateY((float) (Math.PI * .25f));
//				world2.addObject(box);
//				box.addParent(plane);
//				
//				//up boundary
//				box2 = Primitives.getBox(5, 20);
//				box2.calcTextureWrapSpherical();
//				box2.setTexture("texture2");
//				box2.strip();
//				box2.build();
//				box2.translate(0,-100,-5);
//				box2.rotateY((float) (Math.PI * .25f));
//				box2.rotateZ((float) (Math.PI * .5f));
//				world2.addObject(box2);
//				box2.addParent(plane);
//				
//				box7 = Primitives.getBox(5, 20);
//				box7.calcTextureWrapSpherical();
//				box7.setTexture("texture2");
//				box7.strip();
//				box.build();
//				box7.translate(200,-100,-5);
//				box7.rotateY((float) (Math.PI * .25f));
//				box7.rotateZ((float) (Math.PI * .5f));
//				world2.addObject(box7);
//				box7.addParent(plane);
//				
//				//right boundary
//				box3 = Primitives.getBox(5, 19);
//				box3.calcTextureWrapSpherical();
//				box3.setTexture("texture2");
//				box3.strip();
//				box3.build();
//				box3.translate(290,0,-5);
//				box3.rotateY((float) (Math.PI * .25f));
//				world2.addObject(box3);
//				box3.addParent(plane);
//				
//				//down boundary
//				box4 = Primitives.getBox(5, 20);
//				box4.calcTextureWrapSpherical();
//				box4.setTexture("texture2");
//				box4.strip();
//				box4.build();
//				box4.translate(0,100,-5);
//				box4.rotateY((float) (Math.PI * .25f));
//				box4.rotateZ((float) (Math.PI * .5f));
//				world2.addObject(box4);
//				box4.addParent(plane);
//				
//				box8 = Primitives.getBox(5, 20);
//				box8.calcTextureWrapSpherical();
//				box8.setTexture("texture2");
//				box8.strip();
//				box8.build();
//				box8.translate(70,100,-5);
//				box8.rotateY((float) (Math.PI * .25f));
//				box8.rotateZ((float) (Math.PI * .5f));
//				world2.addObject(box8);
//				box8.addParent(plane);
//	
//				box5 = Primitives.getBox(5, 8);
//				box5.calcTextureWrapSpherical();
//				box5.setTexture("texture2");
//				box5.strip();
//				box5.build();
//				box5.translate(100,25,-5);
//				box5.rotateY((float) (Math.PI * .25f));
//				world2.addObject(box5);
//				box5.addParent(plane);
//				
//				box10 = Primitives.getBox(5, 8);
//				box10.calcTextureWrapSpherical();
//				box10.setTexture("texture2");
//				box10.strip();
//				box10.build();
//				box10.translate(200,-55,-5);
//				box10.rotateY((float) (Math.PI * .25f));
//				world2.addObject(box10);
//				box10.addParent(plane);
//				
//				
//				plane.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				Config.collideOffset = 250;
//				plane.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				plane2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				//Config.collideOffset = 250;
//				//plane2.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box2.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box3.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box3.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box4.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box4.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box5.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box5.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box6.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
////				Config.collideOffset = 250;
////				box6.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
//				box7.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				box8.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//				box10.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
//
//			}
				Camera cam = world.getCamera();
				cam.moveCamera(Camera.CAMERA_MOVEOUT, 320);
				//cam.lookAt(plane.getTransformedCenter());
				

				SimpleVector sv = new SimpleVector(0,-200,-200);
				sv.set(sv);
				sun.setPosition(sv);
				
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = HelloWorld.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {
			if (touchTurn != 0) {
				//cube.rotateY(touchTurn);
				//box2.rotateZ(touchTurn);
				//plane.rotateY(touchTurn);
				//plane.translate(touchTurn,0,0);
				touchTurn = 0;
			}

			if (touchTurnUp != 0) {
				//cube.rotateX(touchTurnUp);
				//box2.rotateZ(touchTurnUp);
				//plane.rotateX(touchTurnUp);
				//plane.translate(0,touchTurnUp,0);
				touchTurnUp = 0;
			}
			
//			cone.rotateX(xAcc / 100f);
//			cone.rotateY(yAcc / 100f);
			
			mAz = (float) (mAz * 0.01);
			
			//check collision
			SimpleVector move2 = new SimpleVector(mAy, mAx, 1);	//TODO z-Acc eklenecek
			//SimpleVector trsn = sphere.checkForCollisionEllipsoid(move2, ellipsoid, 5);
			SimpleVector trsn=sphere.checkForCollisionSpherical(move2, 5);
			sphere.translate(trsn);			
	
			//rotation of sphere
			SimpleVector planeNormal = new SimpleVector(0,0,1);	//move2'deki ucuncu parametre
			SimpleVector rotationVector = planeNormal.calcCross(trsn);
			SimpleVector axis = rotationVector.normalize();
			float angle = trsn.length() / 5;
			sphere.rotateAxis(axis, angle);

			
			//plane.rotateX(-xAcc * 0.01f);
			//plane.rotateY(-yAcc * 0.01f);
			//sphere.rotateX((float) (-mAx * 0.1));
			//sphere.rotateY((float) (-mAy * 0.1));			
			//Log.d("MyApp", "xAcc" + xAcc);
			//Log.d("MyApp", "mAx" + mAx);
			//Log.d("MyApp", "yAcc" + yAcc);
			//Log.d("MyApp", "mAcc" + mAx);
			


			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();
			
			if(nextLevel){
				fb.clear(back2);
				world2.renderScene(fb);
				world2.draw(fb);
				fb.display();
			
			}
			if(restartGame){
				fb.clear(back2);
				menu.renderScene(fb);
				menu.draw(fb);
				fb.display();
			}
			
			if (System.currentTimeMillis() - time >= 1000) {
				//Logger.log(fps + "fps");
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
