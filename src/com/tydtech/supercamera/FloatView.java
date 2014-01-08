/**
 * Create by zhangwuba 2014-1-2
 * 
 */


package com.tydtech.supercamera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class FloatView extends View {
	private static final String TAG = "SuperCamera.FloatView";
	
	private  WindowManager mWindoManager;  
	private  WindowManager.LayoutParams mParams;  
	
	private NotificationManager mNotificationManager;
	private static final int NOTIFICATION_ID = 1003;
	
	private Context mContext;
	
	private CameraPreview mSurfaceView;
	
	private Camera mCamera;
	private MediaRecorder mMediaRecorder = null;
	private CamcorderProfile mProfile;
	
	private MyOrientationEventListener mOrientationListener;
	    // The degrees of the device rotated clockwise from its natural orientation.
	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	    // The orientation compensation for icons and thumbnails. Ex: if the value
	    // is 90, the UI components should be rotated 90 degrees counter-clockwise.
	private int mOrientationCompensation = 0;
	    // The orientation compensation when we start recording.
    private int mOrientationCompensationAtRecordStart;


	
	private File mFile;
	
	private static final int MSG_START_RECORDER = 0;
	
	private Handler mHandle = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == MSG_START_RECORDER){
				startVideoRecording();
			}
		}
	};

	public FloatView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		
		mWindoManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		mOrientationListener = new MyOrientationEventListener(mContext);

	}
	
	public void showFloatView(){
		
		CameraOpenThread cameraOpenThread = new CameraOpenThread();
	    cameraOpenThread.start();
	    
		mParams = new WindowManager.LayoutParams();
		mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; 
		mParams.format = PixelFormat.RGBA_8888;
		mParams.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; 
		
		mParams.width = 1;//200;
		mParams.height = 1;//200;
		mParams.x = 0;
		mParams.y = 0;
		mParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
		
		mSurfaceView = new CameraPreview(mContext);
		
		mWindoManager.addView(mSurfaceView, mParams);
		
		try{
		    cameraOpenThread.join();
		    if(mCamera == null){
		    	return;
		    }
		}catch(InterruptedException e){
			//return;
		}
		
		Thread startPreviewThread = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                startPreview();
	            }
	        });
	    startPreviewThread.start();
		
		
		 // Make sure preview is started.
        try {
            startPreviewThread.join();
            if(mCamera == null){
		    	return;
		    }
        } catch (InterruptedException ex) {
            // ignore
        }
        
        mProfile = CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH);
		
		showNotification();
		
		
		mHandle.removeMessages(MSG_START_RECORDER);
		mHandle.sendEmptyMessageDelayed(MSG_START_RECORDER, 2000);
	}
	
	
	protected class CameraOpenThread extends Thread {
        @Override
        public void run() {
            openCamera();
        }
    }
	
	private void openCamera(){
		try{
			mCamera = Camera.open();
			Camera.Parameters params = mCamera.getParameters();
			mCamera.setParameters(params);
		}catch(RuntimeException e){
			Log.d(TAG, "zhangwuba openCamera fail");
			mCamera = null;
		}
	}
	
	private void startPreview(){
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(mSurfaceView.getHolder());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//mCamera.setDisplayOrientation(180);
			setCameraDisplayOrientation(0,mCamera);
			mCamera.startPreview();
		}
	}
	
	private void stopPreview(){
		if(mCamera != null){
			mCamera.stopPreview();
		 }
	}
	
	private void startVideoRecording(){
		
		Intent intent = new Intent("intent.action.supercamera.checkspace");
	    mContext.sendBroadcast(intent);
	     
		mCamera.unlock();
		//if(mMediaRecorder != null){
			initializeRecorder();
		//}
		mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
		
		mMediaRecorder.setMaxDuration(1000*60*3);
		
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start(); 
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mMediaRecorder.setOnInfoListener(mRecorderInfoListen);
		mMediaRecorder.setOnErrorListener(mRecorderErrorLsiten);
		
	}
	
	private void stopVideoRecording(){
		if(mMediaRecorder != null){
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.setOnInfoListener(null);
			mMediaRecorder.setOnErrorListener(null);
		}
		 if(mMediaRecorder != null){
			 mMediaRecorder.release();
		 }
		 
	}
	
	private void initializeRecorder(){
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setCamera(mCamera);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setOutputFormat(mProfile.fileFormat);
		mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
		mMediaRecorder.setVideoSize(mProfile.videoFrameWidth,
                mProfile.videoFrameHeight);
        mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        mMediaRecorder.setVideoEncoder(mProfile.videoCodec);
        mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
        mMediaRecorder.setAudioChannels(mProfile.audioChannels);
        mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
        mMediaRecorder.setAudioEncoder(mProfile.audioCodec);
        
        String path = Util.getSuperCameraVideoPath();
        mFile = new File(path);
		if(!mFile.exists()){
			mFile.mkdirs();
		}
		long currentTime = System.currentTimeMillis();
		Date date = new Date(currentTime);
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat(mContext.getResources().getString(R.string.video_file_name_format));
		String uniqueOutFile = path + "/super-" + dateFormat.format(date) + ".mp4";
		File outFile = new File(mFile,uniqueOutFile);
		
		mMediaRecorder.setOutputFile(uniqueOutFile);
	}
	
	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

		 SurfaceHolder mHolder;  
		 
		public CameraPreview(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			mHolder = getHolder();  
            mHolder.addCallback(this); 
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			//initVedioRecorder(mHolder);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			// TODO Auto-generated method stub
			releaseVedioRecorder();
		}
		
	}
	
	public void setCameraDisplayOrientation(int cameraId , android.hardware.Camera camera) {
	     android.hardware.Camera.CameraInfo info =
	              new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId , info );
	      int rotation = mWindoManager.getDefaultDisplay().getRotation();
	      int degrees = 0 ;
	      
	      switch (rotation) {
	          case Surface.ROTATION_0 : degrees = 0 ; break ;
	          case Surface.ROTATION_90 : degrees = 90 ; break ;
	          case Surface.ROTATION_180 : degrees = 180 ; break ;
	          case Surface.ROTATION_270 : degrees = 270 ; break ;
	      }
	 
	      int result ;
	      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = ( info.orientation + degrees ) % 360 ;
	         result = ( 360 - result ) % 360 ;   // compensate the mirror
	      } else {   // back-facing
	         result = ( info.orientation - degrees + 360 ) % 360 ;
	      }
	     Log.i(TAG, "zhangwuba ----  degrees = " + degrees + " info.orientation = " + info.orientation);
	     Log.i(TAG, "zhangwuba ----  setCameraDisplayOrientation = " + result);
	     camera.setDisplayOrientation(270);
	  }
	
	
	public void stopVedioRecorder(){
		
		//releaseVedioRecorder();
		mNotificationManager.cancel(NOTIFICATION_ID);
		mHandle.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub

				if(mSurfaceView != null){
					mSurfaceView.setVisibility(GONE);
					//mWindoManager.removeView(mSurfaceView);
					//mSurfaceView = null;
				}
			}
		}, 1000);
	}
	public void releaseVedioRecorder(){
		if(mMediaRecorder != null){
			mMediaRecorder.stop();
			mMediaRecorder.reset();
			mMediaRecorder.setOnInfoListener(null);
		}
		
		 if(mCamera != null){
			mCamera.stopPreview();
		 }
		 
		 if(mMediaRecorder != null){
			 mMediaRecorder.release();
		 }
		 
		 if(mCamera != null){
			 mCamera.release();  
			 mCamera = null;
		 }
	}
	
	OnInfoListener mRecorderInfoListen = new OnInfoListener() {
		
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			// TODO Auto-generated method stub
			if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
				Log.i(TAG,"zhangwuba --- dution retach");
				stopVideoRecording();
				mHandle.removeMessages(MSG_START_RECORDER);
				mHandle.sendEmptyMessage(MSG_START_RECORDER);
				//releaseVedioRecorder();
				//initVedioRecorder(mSurfaceView.getHolder());
			}
		}
	};
	
	OnErrorListener mRecorderErrorLsiten = new OnErrorListener() {
		
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			// TODO Auto-generated method stub
			Log.i(TAG,"zhangwuba --- mRecorderErrorLsiten what = " + what);
			mNotificationManager.cancel(NOTIFICATION_ID);
			Toast.makeText(mContext, 
					mContext.getString(R.string.recorder_error_toast), 
					Toast.LENGTH_LONG).show();
			mContext.sendBroadcast(new Intent("android.intent.action.super.ERRORS"));
		}
	};
	
	private void showNotification(){
		@SuppressWarnings("deprecation")
		Notification notification = new Notification();//new Notification(R.drawable.ic_stat_notify_vi, 
				//mContext.getString(R.string.notification),System.currentTimeMillis());
		notification.icon = R.drawable.ic_stat_notify_vi;
		notification.tickerText = "Recording!";
		notification.setLatestEventInfo(mContext, "Recording", "Recording", null);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private class  MyOrientationEventListener extends OrientationEventListener{

		public MyOrientationEventListener(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onOrientationChanged(int orientation) {
			// TODO Auto-generated method stub
			if (orientation == ORIENTATION_UNKNOWN)
                return;
			int newOrientation = Util.roundOrientation(orientation,
	                    mOrientation);
			if (mOrientation != newOrientation) {
	                mOrientation = newOrientation;
	            }
			 
			int orientationCompensation = (mOrientation + Util
	                    .getDisplayRotation(mWindoManager)) % 360;

			 if (mOrientationCompensation != orientationCompensation) {
	                mOrientationCompensation = orientationCompensation;
			 }


		}
		
	}

}
