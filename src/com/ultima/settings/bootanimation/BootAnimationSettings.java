package com.ultima.settings.bootanimation;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class BootAnimationSettings extends PreferenceActivity implements Constants
{
    CheckBoxPreference rebootAfterSelectionCheck;

    @SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        setTheme(Preferences.getTheme());
        
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings_bootanimation);

		rebootAfterSelectionCheck = (CheckBoxPreference) findPreference("boot_ani_reboot");
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	
		rebootAfterSelectionCheck.setChecked(Preferences.isRebootAfterSelection());
		
		rebootAfterSelectionCheck.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(Preferences.isRebootAfterSelection()){
					Preferences.setRebootAfterSelection(false);
				}else {
					Preferences.setRebootAfterSelection(true);
				}
				return false;
			}
		});	
	}
}
