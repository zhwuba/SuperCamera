/**
 * superCamera MainActivity
 * Create by zhangwuba 2014-1-2
 * 
 * 
 */

package com.tydtech.supercamera;



import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {
	private static final String TAG = "SuperCamera.MainActivity";
	
	private static final int[] mainAction = {
		R.drawable.record, R.drawable.stop, R.drawable.setting
	};
	private static final int[] mainActionTitile = {
		R.string.action_start, R.string.action_stop, R.string.action_delete
	};
	
	private Handler mHandler = new Handler();
	

	private GridView mGridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        mGridView = (GridView)findViewById(R.id.main_action);
        mGridView.setAdapter(new MainActionAdapter(this));
        mGridView.setOnItemClickListener(this);
        
        //Intent service = new Intent(this,MyFloatViewServices.class);  
        //this.startService(service);
    }

   public void onBackPressed(){
	   super.onBackPressed();
	   //stopService(new Intent(this, MyFloatViewServices.class));
   }
   
   private class MainActionAdapter extends BaseAdapter {
	
	private Context mContext;
	public MainActionAdapter(Context c){
		mContext = c;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mainAction.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		 Log.i(TAG, "zhangwuba getItemId!!!! == " + arg0);
		return mainAction[arg0];
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if(arg1 == null){
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			arg1 = inflater.inflate(R.layout.action_item, null);
		}
		
		ImageView icon = (ImageView)arg1.findViewById(R.id.actionItemImageView);
		if(icon != null){
			icon.setImageResource(mainAction[arg0]);
		}
		
		TextView titile = (TextView)arg1.findViewById(R.id.actionItemName);
		if(titile != null){
			titile.setText(mainActionTitile[arg0]);
		}
		
		return arg1;
	}
	   
   }

   @Override
   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	// TODO Auto-generated method stub
	 //Log.i(TAG, "zhangwuba onItemClick == " + arg3);
	if(arg3 == mainAction[0]){
		Intent service = new Intent(this,MyFloatViewServices.class);  
        this.startService(service);
		 //Log.i(TAG, "zhangwuba start!!!!");
	}else if(arg3 == mainAction[1]){
		 //Log.i(TAG, "zhangwuba stop!!!!");
		 stopService(new Intent(this, MyFloatViewServices.class));
	}else if(arg3 == mainAction[2]){
		 Util.deleteAllFiles();
	}
   }
 
    
}
