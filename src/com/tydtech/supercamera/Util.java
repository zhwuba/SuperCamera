/**
 * Create by zhangwuba 2014-1-2
 * add support external sd --2014-1-7
 * 
 */

package com.tydtech.supercamera;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    public static final long MEM_SPACE_BETA = 2048 * 1024;// * 1024; //2G
    
    public static final long MEM_SPACE_LOW_BETA = 100 * 1024;//
    
    
    public static String getExternalSdPath(){
    	String externalSdPath = null;
    	
    	Runtime runtime = Runtime.getRuntime();
    	try {
			Process proc = runtime.exec("mount");
			InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while((line = br.readLine()) != null){
            	if(line.contains("/storage/sdcard1")){
            		externalSdPath = "/storage/sdcard1";
            		break;
            	}
            }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return externalSdPath;
    	
    }
    
    public static boolean isExternalSdMount(){
    	if(getExternalSdPath() != null){
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public static String getSuperCameraVideoPath(){
    	String path = getExternalSdPath();
    	if(path == null){
    		path = SUPERCAMERA_PATH;
    	}else{
    		path = path + "/supercamera/";
    	}
    	return path;
    }
	
	
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
	 
   public static SDCardInfo getMemSpace(String path){
		//File pathFile = android.os.Environment.getExternalStorageDirectory();
		 try {
          android.os.StatFs statfs = new android.os.StatFs(path);
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
	
	
   public static boolean deleteOldFiles(){
	    boolean ret = true;
	   try{
        String path = getExternalSdPath();
        if(path == null){
        	path = android.os.Environment.getExternalStorageDirectory().getPath();
        }
        
        Log.d(TAG, "zhangwuba ----- deleteOldFiles path = " + path);
        
		SDCardInfo info = getMemSpace(path);
		long free = info.free / 1024;
		
		if(free < MEM_SPACE_LOW_BETA){
			return false;
		}
		
		File file  = new File(getSuperCameraVideoPath());
		
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
	 if(free < MEM_SPACE_BETA){
		 //Log.i(TAG, "zhangwuba --- sortList.size() = ");
		if(sortList != null){
			Log.i(TAG, "zhangwuba --- sortList.size() = " + sortList.size());
			if(sortList.size() > 100){
				for(int i = 0; i < 100; i++){
					String old = sortList.get(i).getValue();
					File oldf = new File(old);
					oldf.delete();
				}
			}else if(sortList.size() > 10){
				String old = sortList.get(0).getValue();
				File oldf = new File(old);
				oldf.delete();
			}
		}
	  }
	 }catch (Exception e) {
		// TODO: handle exception
		 ret = false;
	}
	     return ret;
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
		
		String sdPath = getExternalSdPath();
		if(sdPath != null){
			sdPath = sdPath + "/supercamera/";
			 File filesd  = new File(sdPath);
				
				HashMap<String, String> fileListsd = getSupeCameraFilelist(filesd);
				Iterator<String> itesd = fileListsd.keySet().iterator();
				while(itesd.hasNext()){
					String flist = fileListsd.get(itesd.next());
					File df = new File(flist);
					df.delete();
				}
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
