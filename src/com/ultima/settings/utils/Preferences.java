package com.ultima.settings.utils;

import com.ultima.settings.R;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences implements Constants{
	
	private static Context mContext = CONTEXT;
	
    private Preferences() {
    }
    
	public static boolean isHostnameChanged(){
		return getPrefs().getBoolean(HOSTNAME_CHANGED, false);
	}
	
	public static boolean isRebootAfterSelection(){
		return getPrefs().getBoolean(REBOOT_AFTER_SELECTION, true);
	}
	
	public static boolean isBootAnimationEnabled(){
		return getPrefs().getBoolean(IS_BOOT_ENABLED, true);
	}
	
	public static void setTheme(int value){
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putInt(CURRENT_THEME, value);
		editor.commit();
	}
	
	public static void setHostname(String value) {
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString(HOSTNAME_STORE, value);
		editor.commit();
	}
	
	public static void setHostnameChanged(boolean value){
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putBoolean(HOSTNAME_CHANGED, value);
		editor.commit();
	}
	
	public static void setOriginalHostname(String value){
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putString(ORIGNAL_HOSTNAME, value);
		editor.commit();
	}
	
	public static void setRebootAfterSelection(boolean value){
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putBoolean(REBOOT_AFTER_SELECTION, value);
		editor.commit();
	}
	
	public static void setBootAnimationStatus(boolean status){
		SharedPreferences.Editor editor = getPrefs().edit();
		editor.putBoolean(IS_BOOT_ENABLED, status);
		editor.commit();
	}
	
	public static int getTheme()
	{		
		switch(getCurrentTheme())
		{
		case 0:
			return R.style.RagnarTheme_Light;
		case 1:
			return R.style.RagnarTheme_Light_Dark_Actionbar;
		case 2:
			return R.style.RagnarTheme_Dark;
		default:
			return R.style.RagnarTheme_Dark;
		}
	}
	
	public static int getCurrentTheme(){
		return getPrefs().getInt(CURRENT_THEME, 2); // #2 is the Dark Theme
	}
	
	public static String getHostname(){
		return getPrefs().getString(HOSTNAME_STORE, "");
	}
	
	public static String getOriginalHostname(){
		return getPrefs().getString(ORIGNAL_HOSTNAME, "");
	}
	
	private static SharedPreferences getPrefs() {
        return mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
