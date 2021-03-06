package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;

public class FileApp extends AbstractApp {

	private World world;

	public int mFilePageIdx = 0;
	public int mPickState = 0;

	public FileApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1, World world) {
		super(animatables, callback, camera, ball1);
		this.world = world;
	}

	PickGroup[] mPickGroupFiles = new PickGroup[3];
	PickGroup[] mPickGroupFiles1 = new PickGroup[9];
	PickGroup[] mPickGroupFiles2 = new PickGroup[9];

	PickGroup[] mPickGroupDesks = new PickGroup[5];

	protected Object3D[] desks = null;
	protected Object3D[] files = null;
	protected Object3D[] files1 = null;
	protected Object3D[] files2 = null;

	private Map<String, Object3D> clickableFiles = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableDesks = new HashMap<String, Object3D>();

	SimpleVector getFilePos(int i, int x) {
		SimpleVector target = new SimpleVector();
		cam.getPosition(target);
		target.add(new SimpleVector(-3 - x, 0.8 * (i / 3 - 1),
				0.5 + 0.8 * (i % 3 - 1)));
		return target;
	}

	void resetFilePosition(Object3D[] files, int x) {
		for (int i = 0; i < 9; i++) {
			files[i].translate(getFilePos(i, x).calcSub(
					files[i].getTransformedCenter()));
			Log.e(TAG, "files[i]:" + files[i].getTransformedCenter());
		}
	}

	void file() {

		startFrom(desks, 5);
		startFrom(files1, 5);
		resetFilePosition(files1, 5);
		// startFrom(files2, 5);
		// resetFilePosition(files2, 5);

		mAnimatables.add(new TranslationAnimation("", SceneHelper
				.to1DArr(new Object3D[][] { desks, files1 }), new SimpleVector(
				5, 0, 0), null));

	}

	void startFrom(Object3D[] object3ds, int distance) {
		for (int i = 0; i < object3ds.length; i++) {
			object3ds[i].clearTranslation();
			object3ds[i].translate(-distance, 0, 0);
			object3ds[i].setVisibility(true);
		}
	}

	private void postInitFile() {
		for (int i = 0; i < mPickGroupFiles.length; i++) {
			mPickGroupFiles[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupDesks.length; i++) {
			mPickGroupDesks[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupFiles1.length; i++) {
			mPickGroupFiles1[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupFiles2.length; i++) {
			mPickGroupFiles2[i] = new PickGroup(1);
		}
		Object3D tmp;
		tmp = clickableDesks.get("f_trash");
		mPickGroupDesks[0].group[0] = tmp;

		tmp = clickableDesks.get("f_i_open");
		mPickGroupDesks[1].group[0] = tmp;

		tmp = clickableDesks.get("f_i_copy");
		mPickGroupDesks[2].group[0] = tmp;

		tmp = clickableDesks.get("f_i_cut");
		mPickGroupDesks[3].group[0] = tmp;

		tmp = clickableDesks.get("f_i_delete");
		mPickGroupDesks[4].group[0] = tmp;

		tmp = clickableFiles.get("f_book_b");
		mPickGroupFiles[0].group[0] = tmp;

		tmp = clickableFiles.get("f_book_s");
		mPickGroupFiles[1].group[0] = tmp;

		tmp = clickableFiles.get("f_b_folder");
		mPickGroupFiles[2].group[0] = tmp;

		desks = new Object3D[clickableDesks.size()];
		files = new Object3D[clickableFiles.size()];

		files1 = new Object3D[mPickGroupFiles1.length];
		files2 = new Object3D[mPickGroupFiles2.length];

		desks = clickableDesks.values().toArray(desks);
		files = clickableFiles.values().toArray(files);

		for (int i = 0; i < 9; i++) {
			files1[i] = mPickGroupFiles[1].group[0].cloneObject();
			files1[i].setTexture("dummy");
			mPickGroupFiles1[i].group[0] = files1[i];
			mPickGroupFiles1[i].oriPos[0] = getFilePos(i, 0);
			world.addObject(files1[i]);

			files2[i] = mPickGroupFiles[1].group[0].cloneObject();
			mPickGroupFiles2[i].group[0] = files2[i];
			mPickGroupFiles2[i].oriPos[0] = getFilePos(i, 0);
			world.addObject(files2[i]);
		}

	}

	private void initFile(String name, Object3D object3d) {
		object3d.setVisibility(false);
		// object3d.setAdditionalColor(new RGBColor(100, 100, 100));

		int end = -1;
		end = end == -1 ? name.indexOf("_Plane") : end;
		end = end == -1 ? name.indexOf("_Cube") : end;
		end = end == -1 ? name.indexOf("_Cylinder") : end;
		end = end == -1 ? name.indexOf("_OBJ") : end;

		String tname = name.substring(2, (end == -1 ? name.length() : end));

		Log.e(TAG, "nice: " + tname);

		if (tname.startsWith("f_b")) {
			object3d.getMesh().setLocked(true);
			// printTexture(tname, object3d);
			clickableFiles.put(tname, object3d);
		} else if (tname.startsWith("f_i_")) {
			SceneHelper.printTexture(tname, object3d);
			clickableDesks.put(tname, object3d);
		} else {
			clickableDesks.put(tname, object3d);
		}
	}

	private void toggleDesk(boolean on) {
		for (int i = 0; i < desks.length; i++) {
			desks[i].setVisibility(on);
		}
		for (int i = 0; i < files.length; i++) {
			files[i].setVisibility(on);
		}
		for (int i = 0; i < files1.length; i++) {
			files1[i].setVisibility(on);
		}
		for (int i = 0; i < files2.length; i++) {
			files2[i].setVisibility(on);
		}
	}

	@Override
	public void onPick() {
		// TODO
		pickList(mPickGroupDesks, 1, mPickGroupDesks.length);
		if (mFilePageIdx == 0) {
			pickList(mPickGroupFiles1, 0, mPickGroupFiles1.length);
		} else {
			pickList(mPickGroupFiles2, 0, mPickGroupFiles2.length);
		}
	}

	@Override
	public void initObj(String name, Object3D object3d) {
		initFile(name, object3d);
	}

	@Override
	public void postInitObj() {
		postInitFile();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShown() {
		// toggleDesk(true);
		file();
	}

	@Override
	public void onHide() {
		toggleDesk(false);
	}

	@Override
	public void onLeft() {
		// TODO scroll file left
		slideFile(true);
	}

	@Override
	public void onRight() {
		// TODO scroll file right
		slideFile(false);
	}

	@Override
	public void onUp() {
		// TODO go to home folder

	}

	@Override
	public void onDown() {
		// TODO go to up level

	}

	@Override
	public void onLongPress() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onDoubleTap() {
		// TODO
		// pick file
		// 1,2,3,4
		// 
		// -1,1,-1,1
		// -1,-1,1,1
		if (mPickState == 0) {

			Object3D[] files = mFilePageIdx == 0 ? files1 : files2;

			for (int i = 0; i < files.length; i++) {
				if (SceneHelper.isLookingAt(cam, ball1,
						files[i].getTransformedCenter()) > 0.99) {
					
					mPickState = 1;
					
					PickGroup group = null;
					SimpleVector tmp;
					for (int j = 1; j < mPickGroupDesks.length; j++) {
						group = mPickGroupDesks[j];
						tmp = getFilePos(i, 0);
						group.group[0].translate(tmp);
						group.oriPos[0] = tmp.calcAdd(new SimpleVector(1, (j%2*2-1)*0.2, (j/2*2-1)*0.2));
					}
					
					break;
				}
			}
		} else {
			PickGroup group = null;
			SimpleVector tmp = new SimpleVector(0,0,-1000);
			for (int j = 1; j < mPickGroupDesks.length; j++) {
				group = mPickGroupDesks[j];

				group.group[0].translate(tmp);
				group.oriPos[0] = null;
			}
			
		}

		// show the icons
		// pick icons and do the action

		// drag the files
		return false;
	}

	@Override
	public void onSingleTap() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpen(Bundle bundle) {
		onShown();
		scene.onAppReady();
	}

	@Override
	public void onClose(Runnable runnable) {
		scene.onHideObj(
				SceneHelper.to1DArr(new Object3D[][] { desks, files1, files2 }),
				false, runnable);
		scene.onAppClosed();

		scene.onSwitchMode(new ConnectionMode(1));
	}

	void openFileOnScene(String file) {

		Bundle bundle = new Bundle();
		bundle.putString("file", file);

		scene.onOpenApp(AppType.FILE_OPEN.ordinal(), bundle);
	}

	void slideFile(boolean slideLeft) {
		mFilePageIdx = (mFilePageIdx + 1) % 2;

		resetFilePosition(files1, 0);
		resetFilePosition(files2, 0);

		Object3D[] pre, cur;

		if (mFilePageIdx == 0) {
			pre = files2;
			cur = files1;
		} else {
			pre = files1;
			cur = files2;
		}

		slideList(slideLeft, pre, cur, 4, false, true);
	}

	@Override
	public boolean onToggleFullscreen() {
		// TODO Auto-generated method stub
		return false;
	}

}
