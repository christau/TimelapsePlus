package ru.vlad805.timelapse.recorder;

import ru.vlad805.timelapse.MotionJpegGenerator;

import java.io.File;

public class VideoRecorder implements IRecorder {
	private int mFrameCount = 0;
	private MotionJpegGenerator mVideo = null;

	public VideoRecorder(String path, String filename, int width, int height, double framerate) {
		try {
			mVideo = new MotionJpegGenerator(new File(path + File.separator + filename), width, height, framerate, 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			mVideo.finishAVI();
			mVideo.fixAVI(mFrameCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addFrame(byte[] data) {
		mFrameCount++;
		try {
			mVideo.addFrame(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getFrameCount() {
		return mFrameCount;
	}

	public long getFileSize() {
		return mVideo.getFile().length();
	}
}
