/**
 * Create by zhangwuba 2014-1-2
 * 
 */


package com.tydtech.supercamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("zwb", "zhangwuba ---  BootCompleteReceiver superCamera");
		Intent service = new Intent(context,MyFloatViewServices.class);  
		context.startService(service);
	}

}
