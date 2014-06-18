package com.ultima.settings.utils;

import android.content.Context;

import com.ultima.settings.SettingsApplication;

public interface Constants {
	
	public static final String PREF_NAME = "RagnarSettings";
	public static Context CONTEXT = SettingsApplication.getContext();

	//Theme
	public static final String CURRENT_THEME = "current_theme";
	
	//Hostname
	public static final String HOSTNAME_STORE = "hostname_store";
	public static final String HOSTNAME_CHANGED = "hostname_changed";
	public static final String ORIGNAL_HOSTNAME = "original_hostname";
	
	//Boot Animation Chooser
	public static final String IS_BOOT_ENABLED = "is_boot_enabled";
	public static final String REBOOT_AFTER_SELECTION = "reboot_after_selection";
	public static final int BOOTANI_REQUEST_CODE = 6384;
	public static final int WALLPAPER_REQUEST_CODE = 6385;

}
