package com.example.thinkinviewsnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class MainActivity extends Activity {

	private final static String IMAGE_SAVE_FOLDER = "/sdcard/android_shotSnap/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		{
			File dir = new File(IMAGE_SAVE_FOLDER);
			if (dir.exists() && dir.isDirectory()) {
				String[] files = dir.list();
				for (String fileName : files) {
					new File(IMAGE_SAVE_FOLDER, fileName).delete();
				}
			}
		}

		// get all view png.
//		new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//
//				View decorView = MainActivity.this.getWindow().getDecorView();
//				saveViewTreeToFile(decorView);
//
//			}
//		}, 1000);
		
		// modify view attribute at runtime.
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					View view = MainActivity.this.findViewById(R.id.f1);
					
					
					ViewGroup parentViewGroup = (ViewGroup) view.getParent();
					LayoutParams lParams = view.getLayoutParams();
					lParams.width = 77;
					lParams.height = 88;
					
					
					view.requestLayout();
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			}
		}, 3000);

	}

	public void saveViewTreeToFile(View aView) {
		saveViewToFile(aView);

		if (aView instanceof ViewGroup) {

			ViewGroup vGroup = (ViewGroup) aView;

			for (int i = 0, len = vGroup.getChildCount(); i < len; i++) {
				View childView = vGroup.getChildAt(i);
				saveViewTreeToFile(childView);
			}
		}
	}

	public void saveViewToFile(View aView) {
		Bitmap bitmap = null;
		try {
			bitmap = loadBitmapFromView(aView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveBitmapToFile(bitmap);
	}

	public void saveBitmapToFile(Bitmap bitmap) {

		if (null == bitmap) {
			return;
		}

		try {
			FileOutputStream out = new FileOutputStream(IMAGE_SAVE_FOLDER + System.currentTimeMillis() + ".png");
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Bitmap shotSnap(View v) {
		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		v.draw(c);
		return b;
	}

	public Bitmap loadBitmapFromView(View v) throws Exception {
		if ((v instanceof View) && !(v instanceof ViewGroup)) {
			return shotSnap(v);
		} else {

			Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			// draw background
			Drawable backgroundDrawable = v.getBackground();
			if (null != backgroundDrawable) {
				backgroundDrawable.draw(c);
			}

			// draw content
			Class<? extends View> viewClass = v.getClass();

			Constructor<?>[] constructorArray = viewClass.getDeclaredConstructors();
			Constructor<?> defaultConstructor = null;

			for (Constructor<?> constructor : constructorArray) {
				Class<?>[] parameterTypes = constructor.getParameterTypes();
				if (1 == parameterTypes.length && parameterTypes[0].equals(Context.class)) {
					defaultConstructor = constructor;
					break;
				}
			}

			// System.out.println("default constructor:" + defaultConstructor);

			Object newInstance = defaultConstructor.newInstance(MainActivity.this);

			for (Field field : viewClass.getDeclaredFields()) {
				boolean originAccessible = field.isAccessible();
				field.setAccessible(true);
				// System.out.println("LETME Field[" + field.getName() + "," +
				// field.get(v) + "]");
				field.set(newInstance, field.get(v));
				// System.out.println("LETME Field[" + field.getName() + "," +
				// field.get(newInstance) + "]");
				field.setAccessible(originAccessible);
			}

			if (newInstance instanceof ViewGroup) {
				ViewGroup view = (ViewGroup) newInstance;
				view.removeAllViews();
			}

			if (newInstance instanceof View) {
				View view = (View) newInstance;

				view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
				view.invalidate();

				view.draw(c);
			}

			return b;

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
