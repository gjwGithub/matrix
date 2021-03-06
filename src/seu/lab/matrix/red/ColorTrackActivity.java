package seu.lab.matrix.red;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import seu.lab.matrix.R;
import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;
import seu.lab.matrix.red.SimpleCameraBridge.DefaultCvCameraViewListener2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class ColorTrackActivity extends CardboardActivity implements
		CardboardView.StereoRenderer, OnRemoteChangeListener{
	private static final String TAG = "SimpleColorBlobDetectionActivity";

	private static boolean isInit = false;
	
	private static float ballDistance = 5f;

	private FrameBuffer fb = null;
	private World world = null;
	private Light sun = null;
	private Object3D plane = null;
	private Object3D cube = null;
	private Object3D ball1 = null;
	private Object3D ball2 = null;

	private RGBColor back = new RGBColor(50, 50, 100);
	private float[] mAngles = new float[3];
	
	private SimpleVector origin = new SimpleVector(0,0,5);

	Point point = new Point();
	
	private SimpleCameraBridge mOpenCvCameraView;

	public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					mOpenCvCameraView.enableView();
				}
					break;
				default: {
					super.onManagerConnected(status);
				}
					break;
			}
		}
	};

	DefaultCvCameraViewListener2 cvCameraViewListener2 = null;

	private boolean test;
	 
	public ColorTrackActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
		
		mOpenCvCameraView = new SimpleCameraBridge(getApplicationContext(), -1, this);
		cvCameraViewListener2 = mOpenCvCameraView.new DefaultCvCameraViewListener2();
		mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener2);
		mOpenCvCameraView.surfaceCreated(null);
		
		if(!isInit){
			// Create a texture out of the icon...:-)
			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 64, 64));
			TextureManager.getInstance().addTexture("texture", texture);
			isInit = true;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
		
		mOpenCvCameraView.surfaceChanged(null, 0, 0, 0);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		
		mOpenCvCameraView.surfaceDestroyed(null);
	}
	
	@Override
	public void onDrawEye(Eye eye) {
		
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public void onFinishFrame(Viewport arg0) {

	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		headTransform.getEulerAngles(mAngles, 0);
		
		Camera cam = world.getCamera();
		cam.lookAt(cube.getTransformedCenter());
		cam.rotateY(mAngles[1]);
		cam.rotateZ(0 - mAngles[2]);
		cam.rotateX(mAngles[0]);

		SimpleVector camDir = new SimpleVector();
		cam.getDirection(camDir);
		
		camDir.x = camDir.x * ballDistance;
		camDir.y = camDir.y * ballDistance;
		camDir.z = camDir.z * ballDistance;
		
		SimpleVector originInballView = new SimpleVector(camDir);
		originInballView.x = -originInballView.x;
		originInballView.y = -originInballView.y;
		originInballView.z = -originInballView.z;
		
		ball1.clearTranslation();
		ball1.clearRotation();
		
		ball1.translate(camDir);

		if(point != null){
			ball1.setRotationPivot(originInballView);
			ball1.rotateAxis(cam.getUpVector(), (float)(0.5f*point.x));
			ball1.rotateAxis(cam.getSideVector(), (float)(0.5f*point.y));
		}
		
		ball2.clearRotation();
		ball2.setRotationPivot(origin);
		ball2.rotateY(0.5f);
		ball2.rotateX(0.5f);
		
//		if(points.size() > 1){
//			ball2.setRotationPivot(origin);
//			ball2.rotateY((float)(0.5f*points.get(1).x));
//			ball2.rotateZ((float)(-0.5f*points.get(1).y));
//		}
	}

	@Override
	public void onRendererShutdown() {
		
	}

	@Override
	public void onSurfaceChanged(int w, int h) {
		if (fb != null) {
			fb.dispose();
		}

		fb = new FrameBuffer(w, h);
		world = new World();
		world.setAmbientLight(20, 20, 20);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);

		cube = Primitives.getCube(1);
		cube.translate(0, 0, -10);
		cube.calcTextureWrapSpherical();
		cube.setTexture("texture");
		cube.strip();
		cube.build();
		world.addObject(cube);
		
		ball1 = Primitives.getSphere(0.2f);
		ball1.translate(0, 0, -5);
		ball1.calcTextureWrapSpherical();
		ball1.setAdditionalColor(new RGBColor(100, 0, 0));
		ball1.strip();
		ball1.build();
		world.addObject(ball1);

		ball2 = Primitives.getSphere(0.2f);
		ball2.translate(0, 0, -5);
		ball2.calcTextureWrapSpherical();
		ball2.setAdditionalColor(new RGBColor(0, 100, 0));
		ball2.strip();
		ball2.build();
		world.addObject(ball2);
		
		plane = Primitives.getCube(30);
		plane.translate(0, 32, 0);
		plane.calcTextureWrapSpherical();
		plane.setTexture("texture");
		plane.strip();
		plane.build();
		world.addObject(plane);

		Camera cam = world.getCamera();
		cam.lookAt(origin);
		
		SimpleVector sv = new SimpleVector();
		sv.set(cube.getTransformedCenter());
		sv.y -= 100;
		sv.z -= 100;
		sun.setPosition(sv);
		MemoryHelper.compact();
	}

	@Override
	public void onSurfaceCreated(EGLConfig config) {
		world = new World();
	}

	@Override
	public void onMove(Point p) {
		Log.e(TAG, "remote : onMove x:"+p.x + " y: "+p.y);
		point = p;
	}

	@Override
	public void onClick() {
		Log.e(TAG, "remote : onClick");
		if(test){
			test = false;
			ball1.setAdditionalColor(new RGBColor(0, 100, 0));
		}else {
			test = true;
			ball1.setAdditionalColor(new RGBColor(0, 0, 100));
		}
	}

	@Override
	public void onPress(Point p) {
		Log.e(TAG, "remote : onPress");
		
		ball1.setAdditionalColor(new RGBColor(0, 100, 0));

	}

	@Override
	public void onRaise(Point p) {
		Log.e(TAG, "remote : onRaise");
		
		ball1.setAdditionalColor(new RGBColor(0, 0, 100));

	}

}
