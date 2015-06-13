package com.ultima.settings.utils;

import android.content.Context;
import android.os.Environment;

import com.ultima.settings.SettingsApplication;

public interface Constants {
	
	public static final String PREF_NAME = "RagnarSettings";
	public static Context CONTEXT = SettingsApplication.getContext();
	public static String SDCARD = Environment.getExternalStorageDirectory().getPath();

	//Theme
	public static final String CURRENT_THEME = "current_theme";
	
	//Hostname
	public static final String HOSTNAME_STORE = "hostname_store";
	public static final String HOSTNAME_CHANGED = "hostname_changed";
	public static final String ORIGNAL_HOSTNAME = "original_hostname";
	
	//Boot Animation Chooser
	public static final String BOOTANI_IS_BOOT_ENABLED = "bootani_is_boot_enabled";
	public static final String BOOTANI_REBOOT_AFTER_SELECTION = "bootani_reboot_after_selection";
	public static final String BOOTANI_USE_ORS = "bootani_use_open_recovery_script";
	public static final String BOOTANI_WIPE_CACHE = "bootani_wipe_cache";
	public static final int BOOTANI_REQUEST_CODE = 6384;
	
	public static final String KEY_CABC = "cabc";
    public static final String KEY_MDNIE_SCENARIO = "mdnie_scenario";
    public static final String KEY_MDNIE_MODE = "mdnie_mode";
    public static final String KEY_MDNIE_NEGATIVE = "mdnie_negative";
    public static final String KEY_MDNIE_OUTDOOR = "mdnie_outdoor";

}
