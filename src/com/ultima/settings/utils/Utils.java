package com.ultima.settings.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.ultima.settings.SettingsApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
}
