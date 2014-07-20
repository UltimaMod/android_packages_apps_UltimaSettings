package com.ultima.settings.bootanimation;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;

public class BootAnimationSettings extends PreferenceActivity implements Constants
{
	public final String TAG = this.getClass().getSimpleName();
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        setTheme(Preferences.getTheme());
        
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(Constants.PREF_NAME);
        PreferenceManager.setDefaultValues(this, R.xml.settings_bootanimation, false);
        addPreferencesFromResource(R.xml.settings_bootanimation);

	}
}
