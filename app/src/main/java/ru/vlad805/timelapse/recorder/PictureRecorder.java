package ru.vlad805.timelapse.recorder;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import ru.vlad805.timelapse.Setting;
import ru.vlad805.timelapse.SettingsBundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class PictureRecorder implements IRecorder {

	private int mTotalSize = 0;
	private int mFrameCount = 0;
	private String mPath;
	private ArrayList<String> mPhotos;
	private long mStart;
	private static SettingsBundle mSettings;

	static {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public PictureRecorder(SettingsBundle settings, String name) {
		mSettings = settings;

		mPhotos = new ArrayList<>();
		mPath = settings.getPath() + File.separator + name;
		mStart = System.currentTimeMillis() / 1000;

		SmbFile f = getSmbFile(mPath);

		try
		{
			if (f.exists()) {
				throw new RuntimeException("Already exists");
			}
			f.mkdirs();
		} catch (SmbException e)
		{
			e.printStackTrace();
		}

	}

	private static SmbFile getSmbFile(String path) {
		try {
			//String url = "smb://192.168.1.50/backup_pi//" + path;
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, mSettings.getmSmbUsername(), mSettings.getmSmbPassword());
			return new SmbFile("smb://" + mSettings.getmSmbServer()+"//"+path, auth);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void stop() {
		try
		{
			String path = mPath + File.separator + "index.tlif";
			OutputStream os=null;
			if(mSettings.ismSmbUpload()) {
				SmbFile sum = getSmbFile(path);
				os = new SmbFileOutputStream(sum);
			}
			else {
				File sum = new File(path);
				os = new FileOutputStream(sum);
			}
			os.write(generateJSON().getBytes());
			os.close();

		}catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	public void addFrame(byte[] data)
	{
		mFrameCount++;

		mPhotos.add(mFrameCount + ".jpg");

		if(mSettings.ismSmbUpload()) {
			new Uploader(mPath,mFrameCount,data).execute();
		}
		else {
			File cur = new File(mPath + File.separator + mFrameCount + ".jpg");

			try (FileOutputStream fos = new FileOutputStream(cur)) {
				fos.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mTotalSize += cur.length();
		}
	}

	public int getFrameCount() {
		return mFrameCount;
	}

	public long getFileSize() {
		return mTotalSize;
	}

	private String generateJSON() throws JSONException {
		return new JSONObject()
				.put("meta",
						new JSONObject()
								.put("version", Setting.TLIF_VERSION)
								.put("date", mStart)
								.put("frames", getFrameCount())
				)
				.put("items", new JSONArray(mPhotos))
				.toString();
	}

	private class Uploader extends AsyncTask<String, Void, Long>
	{
		private final String path;
		private final int frameCount;
		private final byte[] data;

		public Uploader(String path, int frameCount, byte[] data) {

			this.path = path;
			this.frameCount = frameCount;
			this.data = data;
		}
		@Override
		protected Long doInBackground(String... params)
		{
			SmbFile cur = getSmbFile(path + File.separator + frameCount + ".jpg");

			try (SmbFileOutputStream fos = new SmbFileOutputStream(cur)) {
				fos.write(data);
			} catch (IOException e) {
				e.printStackTrace();
				return 0L;
			}

			try
			{
				return cur.length();
			} catch (SmbException e)
			{
				e.printStackTrace();
				return 0L;
			}
		}

		@Override
		protected void onPostExecute(Long length)
		{
			//Dangerous!
			PictureRecorder.this.mTotalSize+=length;
		}
	};

}
