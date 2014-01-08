/**
 * Create by zhangwuba 2014-1-2
 * 
 */
package com.tydtech.supercamera;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MyFloatViewServices extends Service {
	private static final String TAG = "SuperCamera.MyFloatViewServices";

	private FloatView mFloatView;
	
	private static final int MSG_DELETE = 0;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == MSG_DELETE){
				//new SuperCameraDeleteThread().start();
				boolean ret = Util.deleteOldFiles();
				if(!ret){
					sendBroadcast(new Intent("android.intent.action.super.ERRORS"));
					Toast.makeText(MyFloatViewServices.this, 
							getResources().getString(R.string.recorder_error_toast_no_space), 
							Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
	
		mFloatView = new FloatView(this);
		if(mFloatView != null){
			mFloatView.showFloatView();
		}
		
		
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("intent.action.supercamera.checkspace");
		registerReceiver(mCheckSpaceRecevier, filter);
	}

	public void onDestroy(){
		  super.onDestroy();
		  Log.i(TAG, "zhangwuba ---service onDestroy !!");
		  
		  if(mFloatView != null){
				mFloatView.stopVedioRecorder();
			}
		  
		  unregisterReceiver(mCheckSpaceRecevier);
	}
	
	@Override  
	public int onStartCommand(Intent intent, int flags, int startId) {  
		        // TODO Auto-generated method stub  
		   return super.onStartCommand(intent, START_STICKY, startId);  
	}
	
	
	
	private BroadcastReceiver mCheckSpaceRecevier = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mHandler.removeMessages(MSG_DELETE);
			mHandler.sendEmptyMessage(MSG_DELETE);
		}
		
	};
	
	private class SuperCameraDeleteThread extends Thread {
		@Override
		public void run() {
			Util.deleteOldFiles();
		}
	}
	

}
