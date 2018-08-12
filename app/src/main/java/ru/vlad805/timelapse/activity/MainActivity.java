package ru.vlad805.timelapse.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import permissions.dispatcher.*;
import ru.vlad805.timelapse.*;
import ru.vlad805.timelapse.R;
import ru.vlad805.timelapse.ui.*;

import java.util.*;

@SuppressWarnings("deprecation")
@RuntimePermissions
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

	private static final String TAG = "TimeLapse";

	private LinearLayout mRoot;

	private SettingsUtils mControls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holder);

		mRoot = findViewById(R.id.camera_options_root);
		mControls = new SettingsUtils();

		MainActivityPermissionsDispatcher.showSettingsWithPermissionCheck(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@NeedsPermission({
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.WAKE_LOCK
	})
	void showSettings() {
		Camera c = CameraUtils.openCamera(0);

		SharedPreferences sp = getSharedPreferences(Const.NAME_PREFERENCES, Context.MODE_PRIVATE);
		try {
			mControls
					.add(Const.CATEGORY_VIDEO, new CameraOptionHeaderView(this, R.string.settingHeaderVideo))
					.add(Const.OPTION_RECORD_TYPE, getSpinnerRecordType())
					.add(Const.OPTION_VIDEO_RESOLUTION, getSpinnerSizes(CameraUtils.pictureSizesForCameraParameters(c.getParameters())))
					.add(Const.OPTION_VIDEO_FPS,
							new InputNumberCameraOptionView(this, R.string.settingVideoFps, sp.getInt(String.valueOf(Const.OPTION_VIDEO_FPS), 20))
									.setValidator(data -> 15 <= data && data <= 60)
					)
					.add(Const.OPTION_QUALITY,
							new InputNumberCameraOptionView(this, R.string.settingImageQuality, sp.getInt(String.valueOf(Const.OPTION_QUALITY), 95))
									.setValidator(data -> 1 <= data && data <= 100)
					)
					.add(Const.CATEGORY_CAPTURING, new CameraOptionHeaderView(this, R.string.settingHeaderTimelapse))
					.add(Const.OPTION_CAPTURE_DELAY,
							new InputNumberCameraOptionView(this, R.string.settingVideoDelay, sp.getInt(String.valueOf(Const.OPTION_CAPTURE_DELAY), 1000))
									.setValidator(data -> 0 <= data)
					)
					.add(Const.OPTION_CAPTURE_INTERVAL,
							new InputNumberCameraOptionView(this, R.string.settingVideoCaptureInterval, sp.getInt(String.valueOf(Const.OPTION_CAPTURE_INTERVAL), 5000))
									.setValidator(data -> 50 <= data && data <= 180000)
					)
					.add(Const.OPTION_RECORD_PATH,
							new InputTextCameraOptionView(
									this,
									R.string.settingVideoPath,
									Environment.getExternalStorageDirectory().getAbsolutePath() + "/TimelapsePlus/" + System.currentTimeMillis())
					)

					.add(Const.CATEGORY_CAMERA, new CameraOptionHeaderView(this, R.string.settingHeaderCamera));
			if (CameraUtils.cameraSupportsFlash(c)) {
				List<String> modes = CameraUtils.getFlashModes(c);
				if (modes != null) {
					mControls.add(Const.OPTION_CAPTURE_FLASH, new SelectCameraOptionView(this, R.string.settingImageFlash, modes));
				}
			}
			
			mControls
					.add(Const.CATEGORY_EXTRA, new CameraOptionHeaderView(this, R.string.settingHeaderExtra))

					.add(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_DATETIME, new CheckboxCameraOptionView(this, R.string.settingImageStoreDateTime, sp.getBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_DATETIME), false)))
					.add(Const.OPTION_PH_DT_TEXT_SIZE,
							new InputNumberCameraOptionView(this, R.string.settingProcessingHandlerDateTimeTextSize, sp.getInt(String.valueOf(Const.OPTION_PH_DT_TEXT_SIZE), 5))
									.setValidator(data -> 1 <= data && data <= 20)
					)
					.add(Const.OPTION_PH_DT_ALIGN, getSpinnerDateTimeTextAlign())
					.add(Const.OPTION_PH_DT_TEXT_COLOR, new InputColorCameraOptionView(this, R.string.settingProcessingHandlerDateTimeColorText, sp.getInt(String.valueOf(Const.OPTION_PH_DT_TEXT_COLOR), Color.WHITE)))
					.add(Const.OPTION_PH_DT_BACK_COLOR, new InputColorCameraOptionView(this, R.string.settingProcessingHandlerDateTimeColorBack, sp.getInt(String.valueOf(Const.OPTION_PH_DT_BACK_COLOR), Color.BLACK)))

					.add(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_ALIGN, new CheckboxCameraOptionView(this, R.string.settingImageStoreAlign, sp.getBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_ALIGN), false)))
					.add(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_GEOTRACK, new CheckboxCameraOptionView(this, R.string.settingImageStoreLocation, sp.getBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_GEOTRACK), false)));

			mControls.setOnChangeListener(what -> {
				switch (what) {
					case Const.OPTION_RECORD_TYPE:
						mControls.toggle(Const.OPTION_VIDEO_FPS, mControls.<SelectCameraOptionView>get(what).getResultIndex() == Const.RECORD_TYPE_MP4);
						break;

					case Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_DATETIME:
						boolean state = mControls.<CheckboxCameraOptionView>get(what).getResult();
						mControls.toggle(Const.OPTION_PH_DT_TEXT_SIZE, state);
						mControls.toggle(Const.OPTION_PH_DT_ALIGN, state);
						mControls.toggle(Const.OPTION_PH_DT_TEXT_COLOR, state);
						mControls.toggle(Const.OPTION_PH_DT_BACK_COLOR, state);
						break;

					case Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_GEOTRACK:
						if (mControls.<CheckboxCameraOptionView>get(what).getResult()) {
							MainActivityPermissionsDispatcher.checkLocationWithPermissionCheck(this);
						}
						break;
				}
			});

		} finally {
			c.release();
		}

		View[] views = mControls.getViews();
		for (View v : views) {
			mRoot.addView(v);
		}
	}

	private ICameraOptionView getSpinnerSizes(@Nullable List<Camera.Size> sizes) {
		if (sizes == null) { return null; }
		List<String> sizeArray = new ArrayList<>();
		for (Camera.Size size : sizes) {
			sizeArray.add(size.width + "x" + size.height);
		}
		return new SelectCameraOptionView(this, R.string.settingImageSize, sizeArray);
	}

	private ICameraOptionView getSpinnerRecordType() {
		List<String> sizeArray = new ArrayList<>();
		sizeArray.add(Const.RECORD_TYPE_MP4, "MP4");
		sizeArray.add(Const.RECORD_TYPE_JPG, "JPG");
		return new SelectCameraOptionView(this, R.string.settingVideoMode, sizeArray);
	}

	private ICameraOptionView getSpinnerDateTimeTextAlign() {
		List<String> items = Arrays.asList(getResources().getStringArray(R.array.settingProcessingHandlerDateTimeAlignValues));
		return new SelectCameraOptionView(this, R.string.settingProcessingHandlerDateTimeAlign, items);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_save:
				savePreferences();
				break;

			case R.id.menu_start:
				goToCaptureActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void savePreferences() {
		try {
			PreferenceBundle b = mControls.toBundle();

			SharedPreferences sp = getSharedPreferences(Const.NAME_PREFERENCES, Context.MODE_PRIVATE);
			Editor editor = sp.edit();

			editor
					.putString(String.valueOf(Const.OPTION_VIDEO_RESOLUTION), b.getResolution().toString())
					.putInt(String.valueOf(Const.OPTION_RECORD_TYPE), b.getRecordType())
					.putInt(String.valueOf(Const.OPTION_VIDEO_FPS), b.getFps())
					.putInt(String.valueOf(Const.OPTION_QUALITY), b.getQuality())
					.putInt(String.valueOf(Const.OPTION_CAPTURE_DELAY), b.getDelay())
					.putInt(String.valueOf(Const.OPTION_CAPTURE_INTERVAL), b.getInterval())
					//.putString(String.valueOf(Const.OPTION_RECORD_PATH), b.getPath())
					.putString(String.valueOf(Const.OPTION_CAPTURE_FLASH), b.getFlashMode())
					.putBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_DATETIME), b.hasProcessingHandler(Const.PROCESSING_HANDLER_DATETIME))
					.putBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_ALIGN), b.hasProcessingHandler(Const.PROCESSING_HANDLER_ALIGN))
					.putBoolean(String.valueOf(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_GEOTRACK), b.hasProcessingHandler(Const.PROCESSING_HANDLER_GEOTRACK))
					.putInt(String.valueOf(Const.OPTION_PH_DT_TEXT_SIZE), b.getDateTimeTextSize())
					.putInt(String.valueOf(Const.OPTION_PH_DT_ALIGN), b.getDateTimeAlign())
					.putInt(String.valueOf(Const.OPTION_PH_DT_BACK_COLOR), b.getDateTimeColorBack())
					.putInt(String.valueOf(Const.OPTION_PH_DT_TEXT_COLOR), b.getDateTimeColorText());

			editor.apply();
		} catch (IllegalOptionValue e) {
			showToastInvalidValue(e);
		}
	}

	private void goToCaptureActivity() {
		try {
			PreferenceBundle b = mControls.toBundle();

			Intent i = new Intent(this, TestActivity.class);
			i.putExtra(Intent.EXTRA_STREAM, b);
			startActivity(i);
		} catch (IllegalOptionValue e) {
			showToastInvalidValue(e);
		}
	}

	private void showToastInvalidValue(IllegalOptionValue e) {
		@StringRes int resId = R.string.optionInvalidDefault;

		switch (e.getWhat()) {
			case Const.OPTION_VIDEO_FPS: resId = R.string.optionInvalidFps; break;
			case Const.OPTION_QUALITY: resId = R.string.optionInvalidQuality; break;
			case Const.OPTION_CAPTURE_DELAY: resId = R.string.optionInvalidDelay; break;
			case Const.OPTION_CAPTURE_INTERVAL: resId = R.string.optionInvalidInterval; break;
			case Const.OPTION_PH_DT_TEXT_SIZE: resId = R.string.optionInvalidProcessingHandlerDateTimeTextSize; break;
			default:
				e.printStackTrace();
		}

		Toast.makeText(MainActivity.this, resId, Toast.LENGTH_SHORT).show();
	}

	@NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
	void checkLocation() {

	}

	@OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
	void onLocationPermissionDenied() {
		mControls.<CheckboxCameraOptionView>get(Const.OPTION_PROCESSING_HANDLERS + Const.PROCESSING_HANDLER_GEOTRACK).setup(false);
	}

	// Annotate a method which is invoked if the user doesn't grant the permissions
	@OnPermissionDenied({
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.WAKE_LOCK
	})
	void showDeniedForCamera() {
		Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show();
	}

	// Annotates a method which is invoked if the user
	// chose to have the device "never ask again" about a permission
	@OnNeverAskAgain({
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.WAKE_LOCK
	})
	void showNeverAskForCamera() {
		Toast.makeText(this, "never ask", Toast.LENGTH_SHORT).show();
	}
}
