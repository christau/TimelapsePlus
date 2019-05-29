package ru.vlad805.timelapse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.Editable;

@SuppressWarnings("UnusedReturnValue")
public class SettingsBundle {

	private Context mContext;

	private int mWidth = 0;
	private int mHeight = 0;
	private int mDelay;
	private int mFPS;
	private int mInterval;
	private int mRecordMode;
	private int mHandler;

	private boolean mRemoteControl;

	private String mBalance = "";
	private String mEffect = "";
	private String mFlash = "";
	private String mPath = null;
	private int mQuality;

	private int mIntro;
	private boolean mSmbUpload;
	private String mSmbUsername;
	private String mSmbPassword;
	private String mSmbServer;

	public SettingsBundle(Context context) {
		mContext = context;
	}

	public SettingsBundle load() {
		SharedPreferences settings = mContext.getSharedPreferences(Setting.NAME, Context.MODE_PRIVATE);
		mEffect = settings.getString(Setting.EFFECT, "");
		mWidth = settings.getInt(Setting.WIDTH, 0);
		mHeight = settings.getInt(Setting.HEIGHT, 0);
		mBalance = settings.getString(Setting.WHITE_BALANCE, "");
		mDelay = settings.getInt(Setting.DELAY, 3000);
		mInterval = settings.getInt(Setting.INTERVAL, 5000);
		mFPS = settings.getInt(Setting.FPS, 25);
		mQuality = settings.getInt(Setting.QUALITY, 70);
		mFlash = settings.getString(Setting.FLASH_MODE, "");
		mPath = settings.getString(Setting.WORK_DIRECTORY, Environment.getExternalStorageDirectory().getAbsolutePath() + "/TimelapseDir/");
		mIntro = settings.getInt(Setting.INTRO, 0);
		mRecordMode = settings.getInt(Setting.RECORD_MODE, Setting.RecordMode.VIDEO);
		mHandler = settings.getInt(Setting.HANDLER, 0);
		try
		{
			mRemoteControl = settings.getBoolean(Setting.REMOTE_CONTROL, false);
			mSmbUpload = settings.getBoolean(Setting.SMB_UPLOAD, false);

		}catch (Exception e){

		}
		mSmbUsername = settings.getString(Setting.SMB_USERNAME, "");
		mSmbPassword = settings.getString(Setting.SMB_PASSWORD, "");
		mSmbServer = settings.getString(Setting.SMB_SERVER, "");
		return this;
	}
	
	public SettingsBundle save() {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(Setting.NAME, Context.MODE_PRIVATE).edit();
		editor.putString(Setting.EFFECT, mEffect);
		editor.putInt(Setting.WIDTH, mWidth);
		editor.putInt(Setting.HEIGHT, mHeight);
		editor.putString(Setting.WHITE_BALANCE, mBalance);
		editor.putInt(Setting.DELAY, mDelay);
		editor.putInt(Setting.INTERVAL, mInterval);
		editor.putInt(Setting.FPS, mFPS);
		editor.putInt(Setting.QUALITY, mQuality);
		editor.putString(Setting.FLASH_MODE, mFlash);
		editor.putString(Setting.WORK_DIRECTORY, mPath);
		editor.putInt(Setting.INTRO, mIntro);
		editor.putInt(Setting.RECORD_MODE, mRecordMode);
		editor.putInt(Setting.HANDLER, mHandler);
		editor.putBoolean(Setting.REMOTE_CONTROL, mRemoteControl);
		editor.putBoolean(Setting.SMB_UPLOAD, mSmbUpload);
		editor.putString(Setting.SMB_PASSWORD, mSmbPassword);
		editor.putString(Setting.SMB_USERNAME, mSmbUsername);
		editor.putString(Setting.SMB_SERVER, mSmbServer);
		editor.apply();
		return this;
	}

	
	
	public int getDelay() {
		return mDelay;
	}

	public int getFPS() {
		return mFPS;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getInterval() {
		return mInterval;
	}

	public int getIntro() {
		return mIntro;
	}

	public int getQuality() {
		return mQuality;
	}

	public int getRecordMode() {
		return mRecordMode;
	}

	public int getImageHandler() {
		return mHandler;
	}

	public String getFlashMode() {
		return mFlash;
	}

	public String getBalance() {
		return mBalance;
	}

	public String getEffect() {
		return mEffect;
	}

	public String getPath() {
		return mPath;
	}

	public boolean hasRemoteControl() {
		return mRemoteControl;
	}

	
	
	public SettingsBundle setBalance(String balance) {
		mBalance = balance;
		return this;
	}
	
	public SettingsBundle setDelay(int delay) {
		mDelay = toRange(delay, 0, Integer.MAX_VALUE);
		return this;
	}

	public SettingsBundle setEffect(String effect) {
		mEffect = effect;
		return this;
	}

	public SettingsBundle setFPS(int fps) {
		mFPS = toRange(fps, 15, 60);
		return this;
	}

	public SettingsBundle setWidth(int width) {
		mWidth = toRange(width, 100, 8000);
		return this;
	}

	public SettingsBundle setHeight(int height) {
		mHeight = toRange(height, 100, 8000);
		return this;
	}

	public SettingsBundle setInterval(int interval) {
		mInterval = toRange(interval, 100, 3600000);
		return this;
	}

	public SettingsBundle setIntro(int intro) {
		mIntro = intro;
		return this;
	}

	public SettingsBundle setPath(String path) {
		mPath = path;
		return this;
	}

	public SettingsBundle setQuality(int quality) {
		mQuality = toRange(quality, 0, 100);
		return this;
	}

	public SettingsBundle setFlashMode(String mode) {
		mFlash = mode;
		return this;
	}

	public SettingsBundle setRecordMode(int mode) {
		mRecordMode = mode;
		return this;
	}

	public SettingsBundle setImageHandler(int handler) {
		mHandler = handler;
		return this;
	}

	public SettingsBundle setRemoteControl(boolean state) {
		mRemoteControl = state;
		return this;
	}

	private int toRange(int value, int min, int max) {
		return Math.min(max, Math.max(min, value));
	}

	public void setSmbUpload(boolean checked)
	{
		mSmbUpload = checked;
	}

	public boolean ismSmbUpload()
	{
		return mSmbUpload;
	}

	public void setSmbUsername(String username)
	{
		mSmbUsername=username;
	}

	public String getmSmbUsername()
	{
		return mSmbUsername;
	}

	public void setSmbPassword(String password)
	{
		mSmbPassword = password;
	}

	public String getmSmbPassword()
	{
		return mSmbPassword;
	}

	public void setSmbServer(String server)
	{
		mSmbServer = server;
	}

	public String getmSmbServer()
	{
		return mSmbServer;
	}
}
