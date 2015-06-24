package com.ultima.settings.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.ultima.settings.SettingsApplication;

import android.content.Context;
import android.content.pm.PackageManager;

public class Utils {
   
	private static Context mContext = SettingsApplication.getContext();
   
   public static Boolean doesPropExist(String propName, String propValue) {
        boolean valid = false;

        try {
            Process process = Runtime.getRuntime().exec("getprop");
            BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) 
            {
                if(line.contains("[" + propName +"]: [" + propValue + "]")){
                    valid = true;
                }
            }
        } 
        catch (IOException e){
            e.printStackTrace();
        }
        return valid;
    }
    
    public static boolean appInstalled(String uri) {
        PackageManager pm = mContext.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed ;
    }

    /**
     * Write a string value to the specified file.
     * 
     * @param filename The filename
     * @param value The value
     */
    public static void writeValue(String filename, String value) {
        Root root = new Root();
		root.setMdnieControls(value, filename);
        
    }
    
public static void writeValue(String filename, Boolean value) {
        String strValue = "0";
        if(value) {
        	strValue = "1";
        }
        Root root = new Root();
		root.setMdnieControls(strValue, filename);
    }

public static String getProp(String propName) {
	Process p = null;
	String result = "";
	try {
		p = new ProcessBuilder("/system/bin/getprop", propName).redirectErrorStream(true).start();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while ((line=br.readLine()) != null) {
			result = line;
		}
		br.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return result;
}
}
