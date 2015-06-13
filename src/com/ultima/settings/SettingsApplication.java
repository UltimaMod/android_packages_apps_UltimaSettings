package com.ultima.settings;

import com.ultima.settings.utils.Constants;

import android.app.Application;
import android.content.Context;

public class SettingsApplication extends Application implements Constants {
	
	private static Context mContext;

    public SettingsApplication() {
    	mContext = this;
    }

    public static Context getContext() {
        return mContext;
    }
}

