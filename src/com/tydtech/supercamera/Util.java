/**
 * Create by zhangwuba 2014-1-2
 * 
 */

package com.tydtech.supercamera;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


import android.os.Environment;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;


public class Util {
	private static final String TAG = "superCamera.Util";
	
	public static final String SUPERCAMERA_PATH = Environment.getExternalStorageDirectory()
			.toString() + "/supercamera/";
	
	// Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 5;
	
	
	public static HashMap<String, String> getSupeCameraFilelist(File file) {

	        HashMap<String, String> fileList = new HashMap<String, String>();

	        getFileList(file, fileList);

	        return fileList;

	    }
	 
	 private static void getFileList(File path, HashMap<String, String> fileList){
	        if(path.isDirectory()){
	            File[] files = path.listFiles();
	            
	            if(null == files)
	                return;
	
	            for(int i = 0; i < files.length; i++){
	                getFileList(files[i], fileList);
	            }
	        }
	        else{
	            String filePath = path.getAbsolutePath();
	            String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
	            fileList.put(fileName, filePath);
	        }
	    }
	 
   
   public static class SDCardInfo {
	        public long total;
	        public long free;
	    }
	 
   public static SDCardInfo getMemSpace(){
		File pathFile = android.os.Environment.getExternalStorageDirectory();
		 try {
          android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
          long nTotalBlocks = statfs.getBlockCount();
          long nBlocSize = statfs.getBlockSize();
          long nAvailaBlock = statfs.getAvailableBlocks();

          SDCardInfo info = new SDCardInfo();
          info.total = nTotalBlocks * nBlocSize;
          info.free = nAvailaBlock * nBlocSize;                	
          Log.i(TAG, "zhangwuba ---------   total = " + info.total + " -- free = " + info.free);
          return info;
		 } catch (IllegalArgumentException e) {
          //Log.e(LOG_TAG, e.toString());
      }
		 return null;
	}
	
	
   public static void deleteOldFiles(){

		SDCardInfo info = getMemSpace();
		long free = info.free / 1024;
		long  alpa = 2048 * 1024;// * 1024; //2G
		
		File file  = new File(SUPERCAMERA_PATH);
		
		HashMap<String, String> fileList = getSupeCameraFilelist(file);
		//Iterator<String> ite = fileList.keySet().iterator();
		//while(ite.hasNext()){
			//String flist = fileList.get(ite.next());
			//Log.i(TAG, "zhangwuba ----  file = " + flist);
		//}
		
		ArrayList<HashMap.Entry<String,String>> sortList = new ArrayList<HashMap.Entry<String,String>>(
				fileList.entrySet());

		Collections.sort(sortList,new Comparator<HashMap.Entry<String,String>>(){

			@Override
			public int compare(Entry<String, String> arg0,
					Entry<String, String> arg1) {
				// TODO Auto-generated method stub
				 String path1 = arg0.getValue();
				 String path2 = arg1.getValue();
				 
				 File f1 = new File(path1);
				 File f2 = new File(path2);
				 
				 long time1 = f1.lastModified();
				 long time2 = f2.lastModified();
				 
				if(time2 - time1 > 0)
					return -1;
	
				return 1;
			}
			
		});
		
		 
	//Log.i(TAG, "zhangwuba --- alpa = " + alpa);
	 if(free < alpa){
		 //Log.i(TAG, "zhangwuba --- sortList.size() = ");
		if(sortList != null){
			Log.i(TAG, "zhangwuba --- sortList.size() = " + sortList.size());
			if(sortList.size() > 100){
				for(int i = 0; i < 100; i++){
					String old = sortList.get(i).getValue();
					File oldf = new File(old);
					oldf.delete();
				}
			}else{
				String old = sortList.get(0).getValue();
				File oldf = new File(old);
				oldf.delete();
			}
		}
	 }
   }
   
   public static void deleteAllFiles(){
	    File file  = new File(SUPERCAMERA_PATH);
		
		HashMap<String, String> fileList = getSupeCameraFilelist(file);
		Iterator<String> ite = fileList.keySet().iterator();
		while(ite.hasNext()){
			String flist = fileList.get(ite.next());
			File df = new File(flist);
			df.delete();
		}
   }
   
   public static int roundOrientation(int orientation, int orientationHistory) {
       boolean changeOrientation = false;
       if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
           changeOrientation = true;
       } else {
           int dist = Math.abs(orientation - orientationHistory);
           dist = Math.min( dist, 360 - dist );
           changeOrientation = ( dist >= 45 + ORIENTATION_HYSTERESIS );
       }
       if (changeOrientation) {
           return ((orientation + 45) / 90 * 90) % 360;
       }
       return orientationHistory;
   }
   
   public static int getDisplayRotation(WindowManager wm) {
       int rotation = wm.getDefaultDisplay()
               .getRotation();
       switch (rotation) {
           case Surface.ROTATION_0: return 0;
           case Surface.ROTATION_90: return 90;
           case Surface.ROTATION_180: return 180;
           case Surface.ROTATION_270: return 270;
       }
       return 0;
   }
	
	
}
