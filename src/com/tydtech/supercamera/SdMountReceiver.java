package com.tydtech.supercamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SdMountReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		context.stopService(new Intent(context, MyFloatViewServices.class));
	}

}
