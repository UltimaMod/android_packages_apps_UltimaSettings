package com.ultima.settings.receivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Utils;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
    	String action  = bootintent.getAction();
    	if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			ContentResolver cr = context.getContentResolver();
			try {
				Utils.writeValue(
						context.getResources().getString(R.string.mdnie_mode_sysfs_file), 
						Integer.toString(Settings.System.getInt(cr, Constants.KEY_MDNIE_MODE)));
				Utils.writeValue(
						context.getResources().getString(R.string.mdnie_scenario_sysfs_file), 
						Integer.toString(Settings.System.getInt(cr, Constants.KEY_MDNIE_SCENARIO)));
				Utils.writeValue(
						context.getResources().getString(R.string.mdnie_negative_sysfs_file), 
						Integer.toString(Settings.System.getInt(cr, Constants.KEY_MDNIE_NEGATIVE)));
				Utils.writeValue(
						context.getResources().getString(R.string.mdnie_outdoor_sysfs_file), 
						Integer.toString(Settings.System.getInt(cr, Constants.KEY_MDNIE_OUTDOOR)));
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
			}	
		} 	
    }
}
