package com.ultima.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ultima.settings.colorpicker.ColorPickerPreference;
import com.ultima.settings.preferences.UltimaListPreference;
import com.ultima.settings.preferences.UltimaSwitchPreference;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Tools;
import com.ultima.settings.utils.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SettingsActivity extends Activity implements Constants {

	private static Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(Preferences.getTheme());
		Tools.getRoot(); //Check for root, so that checking later doesn't slow down the action
		super.onCreate(savedInstanceState);
		mContext = this;
		if (savedInstanceState == null)
			getFragmentManager().beginTransaction().replace(android.R.id.content,new PrefsFragment()).commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static class PrefsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
		ContentResolver cr;
		private static final String LCAT = "PrefsFragment";

		private static String ROMCFG_FOLDER;
		private static String MODCFG_FOLDER;

		private AlertDialog aDialog;
		private Preference rebootDialog;

		private int rebootChoice;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//Add preferences - made them modular, so they're easier to layout
			addPreferencesFromResource(R.xml.preferences_battery);
			addPreferencesFromResource(R.xml.preferences_bootanimation);
			addPreferencesFromResource(R.xml.preferences_hostname);
			addPreferencesFromResource(R.xml.preferences_network_meter);
			addPreferencesFromResource(R.xml.preferences_statusbar);

			addPreferencesFromResource(R.xml.preferences_misc);

			ROMCFG_FOLDER = getResources().getString(R.string.romcfg_folder);
			MODCFG_FOLDER = getResources().getString(R.string.modcfg_folder);
			cr = getActivity().getContentResolver();
			initPrefs();
			disablePrefs();

			rebootDialog = (Preference) findPreference("reboot_dialog");
			rebootDialog.setOnPreferenceClickListener(this);
		}

		public boolean onPreferenceChange(Preference item, Object newValue){
			String type = item.getClass().getSimpleName();
			Log.d(LCAT, "onPreferenceChange: "+type+" "+item.getKey()+" > "+newValue);
			dispatchItem(item,newValue);

			return true;
		}

		public boolean onPreferenceClick(Preference item) {
			if(item.getKey().startsWith("runactivity_")){
				String localActivity = item.getKey().substring(new String("runactivity_").length());
				Log.w(LCAT, "Run Local Activity: "+localActivity);
				getActivity().startActivity(new Intent().setClassName(getActivity().getApplicationInfo().packageName, getActivity().getApplicationInfo().packageName+"."+localActivity));
			} else if(item.getKey().startsWith("runexternal_")){
				String externalActivity = item.getKey().substring(new String("runexternal_").length());
				Log.w(LCAT, "Run External Activity: "+externalActivity);
				getActivity().startActivity(new Intent(Intent.ACTION_MAIN).setClassName(externalActivity.substring(0, externalActivity.lastIndexOf(".")), externalActivity));
			} else if(item.getKey().startsWith("activity;")){
				String[] data = item.getKey().substring(9).split(";");
				if(data.length != 2){
					Log.e(LCAT, "Wrong Params Given: "+item.getKey().substring(9));
					return false;
				}
				Log.w(LCAT, "Run External Activity: "+data[0]+"/"+data[1]);
				getActivity().startActivity(new Intent(Intent.ACTION_MAIN).setClassName(data[0], data[1]));
			} else if(item.getKey().startsWith("tool#")){
				String[] data = item.getKey().substring(5).split("#");
				if(data.length < 1){
					Log.e(LCAT, "Wrong Tool Params Given: "+item.getKey().substring(5));
					return false;
				}
				new RunToolTask().execute(new Object[]{getActivity(),data});
			} else if(item == rebootDialog){
				showRebootDialog();
			}
			return true;
		}

		public void showRebootDialog() {
			final CharSequence[] items={getResources().getString(R.string.reboot), getResources().getString(R.string.recovery)};
			AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
			aDialog = builder.create();

			builder.setTitle(getResources().getString(R.string.reboot_options));

			builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {		

					if (getResources().getString(R.string.reboot).equals(items[which])){			
						rebootChoice = 0;
					}				
					if (getResources().getString(R.string.recovery).equals(items[which])){
						rebootChoice = 1;
					}
					aDialog.dismiss();	    	
				}
			});

			builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					aDialog.dismiss();				
				}
			});
			builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (rebootChoice == 0){
						Toast.makeText(mContext, getResources().getString(R.string.rebooting), Toast.LENGTH_LONG).show();
						Tools.shell("reboot");
					}				
					if (rebootChoice == 1){
						Toast.makeText(mContext, getResources().getString(R.string.rebooting), Toast.LENGTH_LONG).show();
						Tools.shell("reboot recovery");
					}
					aDialog.dismiss();			
				}
			});
			builder.show();
		}

		class RunToolTask extends AsyncTask<Object, Void, Void> {
			@Override
			protected Void doInBackground(Object... params) {
				Tools.dispatch((Context) params[0], (String[]) params[1]);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Toast.makeText(getActivity(), "Executed", Toast.LENGTH_SHORT).show();
			}
		}

		@SuppressWarnings("unchecked")
		private void dispatchItem(Preference item, Object value){
			String type = item.getClass().getSimpleName();
			if(type.equals("CheckBoxPreference")){
				dispatchCheckbox((CheckBoxPreference) item, value);
			} else if(type.equals("EditTextPreference")){
				dispatchText((EditTextPreference) item, value);
			} else if(type.equals("ListPreference")){
				dispatchList((ListPreference) item, value);
			} else if(type.equals("UltimaListPreference")){
				dispatchList((UltimaListPreference) item, value);
			} else if(type.equals("MultiSelectListPreference")){
				dispatchMultiSelectList((MultiSelectListPreference) item, (Set<String>) value);
			} else if(type.equals("SwitchPreference")){
				dispatchSwitch((SwitchPreference) item, value);
			} else if(type.equals("UltimaSwitchPreference")){
				dispatchSwitch((UltimaSwitchPreference) item, value);
			} else if(type.equals("RingtonePreference")){
				dispatchRingtone((RingtonePreference) item, value);
			} else if(type.equals("ColorPickerPreference")){
				dispatchColorPicker((ColorPickerPreference) item, value);
			} else if(type.equals("SeekBarPreference")){
				dispatchSeekBar(item, value);
			} else {
				Log.e(LCAT, "Need to implement: "+type);
			}
		}

		public String getRealPathFromURI(Uri contentUri) {

			// can post image
			String [] proj={MediaColumns.DATA};
			@SuppressWarnings("deprecation")
			Cursor cursor = this.getActivity().managedQuery( contentUri,
					proj, // Which columns to return
					null,       // WHERE clause; which rows to return (all rows)
					null,       // WHERE clause selection arguments (none)
					null); // Order-by clause (ascending by name)
			int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();

			return cursor.getString(column_index);
		}

		private void dispatchSeekBar(Preference item, Object value){
			//Log.d(LCAT, "Dispatching dispatchSeekBar: "+item);
			if(item != null){
				setSettingInt(cr,item.getKey(),(Integer) value);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
			}
		}

		private void dispatchRingtone(RingtonePreference item, Object value){
			//Log.d(LCAT, "Dispatching List: "+item);
			if(item != null){
				String path = getRealPathFromURI(Uri.parse((String) value));

				setSettingString(cr,item.getKey(),path);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+path);
			}
		}

		private void dispatchSwitch(SwitchPreference item, Object value){
			//Log.d(LCAT, "Dispatching Switch: "+item);
			if(item != null){
				setSettingBoolean(cr, item.getKey(),(Boolean) value);
				//Log.d(LCAT, "Setting in Switch: "+item.getKey()+" => "+value);
			}
		}

		private void dispatchSwitch(UltimaSwitchPreference item, Object value){
			//Log.d(LCAT, "Dispatching Switch: "+item);
			if(item != null){
				setSettingBoolean(cr, item.getKey(),(Boolean) value);
				//Log.d(LCAT, "Setting in Switch: "+item.getKey()+" => "+value);
			}
		}

		private void dispatchList(ListPreference item, Object value){
			//Log.d(LCAT, "Dispatching List: "+item);
			if(item != null){
				setSettingString(cr,item.getKey(),(String) value);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
				int id = 0;
				for (int i = 0; i < item.getEntryValues().length; i++)
				{
					if(item.getEntryValues()[i].equals((String)value)){
						id = i;
						break;
					}
				}
				item.setSummary(item.getEntries()[id]);

				disableListItems(item);
			}
		}

		private void dispatchList(UltimaListPreference item, Object value){
			//Log.d(LCAT, "Dispatching List: "+item);
			if(item != null){
				setSettingString(cr,item.getKey(),(String) value);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
				int id = 0;
				for (int i = 0; i < item.getEntryValues().length; i++)
				{
					if(item.getEntryValues()[i].equals((String)value)){
						id = i;
						break;
					}
				}
				item.setSummary(item.getEntries()[id]);

				disableListItems(item);
			}
		}

		private void dispatchMultiSelectList(MultiSelectListPreference item, Set<String> value){
			//Log.d(LCAT, "Dispatching MultiSelectList: "+item);
			if(item != null){
				String val = "";
				Iterator<String> iterator = value.iterator();
				while(iterator.hasNext()){
					String v = iterator.next();
					val += v;
					if(iterator.hasNext()){
						val += ",";
					}
				}
				setSettingString(cr,item.getKey(),val);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+val);
			}
		}

		private void dispatchColorPicker(ColorPickerPreference item, Object value){
			//Log.d(LCAT, "Dispatching EditText: "+item);
			if(item != null){
				String key = item.getKey();
				if(key.startsWith("argb;")){
					String[] prefs = key.substring(5).split(";");
					if(prefs.length != 4){
						Log.e(LCAT,"color prefs wrong size! : "+prefs.length);
						return;
					}
					String key1 = prefs[0];
					int val1 = Color.alpha((Integer) value);
					Log.d(LCAT,"ARGBS color prefs alpha: "+key1+" > "+val1);
					if (key1.length() > 0) setSettingInt(cr,key1,val1);
					String key2 = prefs[1];
					int val2 = Color.red((Integer) value);
					Log.d(LCAT,"ARGBS color prefs red: "+key2+" > "+val2);
					if (key2.length() > 0) setSettingInt(cr,key2,val2);
					String key3 = prefs[2];
					int val3 = Color.green((Integer) value);
					Log.d(LCAT,"ARGBS color prefs green: "+key3+" > "+val3);
					if (key3.length() > 0) setSettingInt(cr,key3,val3);
					String key4 = prefs[3];
					int val4 = Color.blue((Integer) value);
					Log.d(LCAT,"ARGBS color prefs blue: "+key4+" > "+val4);
					if (key4.length() > 0) setSettingInt(cr,key4,val4);

				} else if(key.startsWith("argbf;")){
					String[] prefs = key.substring(6).split(";");
					if(prefs.length != 4){
						Log.e(LCAT,"color prefs wrong size! : "+prefs.length);
						return;
					}
					String key1 = prefs[0];
					float val1 = Color.alpha((Integer) value) / 255.f;
					Log.d(LCAT,"ARGBFS color prefs alpha: "+key1+" > "+val1);
					if (key1.length() > 0) setSettingFloat(cr,key1,val1);
					String key2 = prefs[1];
					float val2 = Color.red((Integer) value) / 255.f;
					Log.d(LCAT,"ARGBFS color prefs red: "+key2+" > "+val2);
					if (key2.length() > 0) setSettingFloat(cr,key2,val2);
					String key3 = prefs[2];
					float val3 = Color.green((Integer) value) / 255.f;
					Log.d(LCAT,"ARGBFS color prefs green: "+key3+" > "+val3);
					if (key3.length() > 0) setSettingFloat(cr,key3,val3);
					String key4 = prefs[3];
					float val4 = Color.blue((Integer) value) / 255.f;
					Log.d(LCAT,"ARGBFS color prefs blue: "+key4+" > "+val4);
					if (key4.length() > 0) setSettingFloat(cr,key4,val4);

				} else if(key.startsWith("argb_")){
					key = key.substring(5);
					String val = getRGB((Integer) value,true);
					Log.d(LCAT,"ARGB color prefs: "+key+" > "+val);
					setSettingString(cr,key,val);
				} else if(key.startsWith("rgb_")){
					key = key.substring(4);
					String val = getRGB((Integer) value,false);
					Log.d(LCAT,"RGB color prefs: "+key+" > "+val);
					setSettingString(cr,key,val);
				} else {
					setSettingInt(cr,key,(Integer) value);
				}
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
			}
		}

		private String getRGB(int color, boolean hasAlpha){
			int red = Color.red(color);
			int green = Color.green(color);
			int blue = Color.blue(color);
			int alpha = Color.alpha(color);
			String out = "#";
			if(hasAlpha){
				out += ((alpha < 17)?"0":"")+Integer.toHexString(alpha);
			}
			out += ((red < 17)?"0":"")+Integer.toHexString(red)+((green < 17)?"0":"")+Integer.toHexString(green)+((blue < 17)?"0":"")+Integer.toHexString(blue);
			return out;
		}


		private void dispatchText(EditTextPreference item, Object value){
			//Log.d(LCAT, "Dispatching EditText: "+item);
			if(item != null){
				setSettingString(cr,item.getKey(),(String) value);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
			}
		}

		private void dispatchCheckbox(CheckBoxPreference item, Object value){
			//Log.d(LCAT, "Dispatching Checkbox: "+item);
			if(item != null){
				setSettingBoolean(cr, item.getKey(),(Boolean) value);
				//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
			}
			if (item.getKey().equals("touch_key_backlight")) {
				Tools tools = new Tools();
				tools.setBacklightValue(value);
			}
			if (item.getKey().equals("on_screen_controls")) {
				Tools tools = new Tools();
				tools.setOnScreenControls(value);
			}
		}

		private void initPrefs(){
			int items = this.getPreferenceScreen().getPreferenceCount();
			for(int i = 0; i < items; i++){
				Preference item = this.getPreferenceScreen().getPreference(i);
				initItem(item);
			}
		}

		private void disablePrefs(){

			//Remove Advanced Display from settings if it's not installed
			if(!(Utils.appInstalled("com.cyanogenmod.lockclock"))){
				Preference pref1 = (Preference) findPreference("activity;com.cyanogenmod.lockclock;com.cyanogenmod.lockclock.preference.Preferences");
				pref1.setEnabled(false);
				pref1.setSummary(R.string.lock_clock_not_installed);
			}
			
			//			
			//            //Remove 4G option for non-4G phones
			//            if(!Utils.doesPropExist("ro.product.name", "jfltexx")){
			//                UltimaCheckboxPreference preference = (UltimaCheckboxPreference) findPreference("system_pref_show_4g");
			//                PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("screen_signal_icons");
			//                preferenceScreen.removePreference(preference);
			//            }
		}

		private void disableListItems(ListPreference item){

			//            if(item.getKey().equals("system_pref_battery_style")){
			//                // Remove the battery 40% setting if we're not using the battery icon
			//                ColorPickerPreference pref40pc = 
			//                        (ColorPickerPreference) findPreference("status_bar_battery_percent_color_forty");
			//                UltimaSwitchPreference prefBatterypc = 
			//                        (UltimaSwitchPreference) findPreference("status_bar_show_battery_percent");
			//                ColorPickerPreference prefBatterypcColor = 
			//                        (ColorPickerPreference) findPreference("status_bar_battery_percent_color");
			//                int setting = Settings.System.getInt(
			//                        mContext.getContentResolver(), "system_pref_battery_style", 0);
			//                if(setting != 0){                 
			//                    pref40pc.setEnabled(false);
			//                    pref40pc.setSummary("Only available for battery icon");
			//                } else {
			//                    pref40pc.setEnabled(true);
			//                    pref40pc.setSummary(R.string.battery_text_percent_colour_40_summary);
			//                }
			//                
			//                if(setting == 3){
			//                    prefBatterypc.setEnabled(false);
			//                    prefBatterypc.setSummary("Only available for battery text only option");
			//                    prefBatterypcColor.setEnabled(false);
			//                    prefBatterypcColor.setSummary("Only available for battery text only option");
			//                } else {
			//                    prefBatterypc.setEnabled(true);
			//                    prefBatterypc.setSummary(R.string.battery_percent_summary);
			//                    prefBatterypcColor.setEnabled(true);
			//                    prefBatterypcColor.setSummary(R.string.battery_text_colour_summary);
			//                }
			//            }
		}


		private void initItem(Preference item){
			String type = item.getClass().getSimpleName();
			//Log.d(LCAT, "Found: "+type);
			if(type.equals("PreferenceCategory")){
				initCategory((PreferenceCategory) item);
			} else if(type.equals("PreferenceScreen")){
				initScreen((PreferenceScreen) item);
			} else if(type.equals("CheckBoxPreference")){
				initCheckbox((CheckBoxPreference) item);
			} else if(type.equals("EditTextPreference")){
				initText((EditTextPreference) item);
			} else if(type.equals("ListPreference")){
				initList((ListPreference) item);
			} else if(type.equals("UltimaListPreference")){
				initList((UltimaListPreference) item);
			} else if(type.equals("MultiSelectListPreference")){
				initMultiSelectList((MultiSelectListPreference) item);
			} else if(type.equals("SwitchPreference")){
				initSwitch((SwitchPreference) item);
			} else if(type.equals("UltimaSwitchPreference")){
				initSwitch((UltimaSwitchPreference) item);
			} else if(type.equals("RingtonePreference")){
				initRingtone((RingtonePreference) item);
			} else if(type.equals("ColorPickerPreference")){
				initColorPicker((ColorPickerPreference) item);
			} else if(type.equals("SeekBarPreference")){
				initSeekBar(item);
			} else if(type.equals("Preference") && (item.getKey().startsWith("tool#") || item.getKey().startsWith("runactivity_") || item.getKey().startsWith("runexternal_") || item.getKey().startsWith("activity;"))){
				initActivityShortcut(item);
			} else {
				Log.e(LCAT, "Need to implement: "+type);
			}
		}

		private void initActivityShortcut(Preference item){
			item.setOnPreferenceClickListener(this);
		}

		private void initSeekBar(Preference item){
			item.setOnPreferenceChangeListener(this);
		}

		private void initColorPicker(ColorPickerPreference item){
			item.setOnPreferenceChangeListener(this);
			String key = item.getKey();
			if(key.startsWith("argb;")){
				String[] prefs = key.substring(5).split(";");
				if(prefs.length != 4){
					Log.e(LCAT,"color prefs wrong size! : "+prefs.length);
					return;
				}
				String key1 = prefs[0];
				int val1 = getSettingInt(cr, key1, 1);
				String key2 = prefs[1];
				int val2 = getSettingInt(cr, key2);
				String key3 = prefs[2];
				int val3 = getSettingInt(cr, key3);
				String key4 = prefs[3];
				int val4 = getSettingInt(cr, key4);
				int color = Color.argb(val1, val2, val3, val4);
				item.setInitialColor(color);
			} else if(key.startsWith("argbf;")){
				String[] prefs = key.substring(6).split(";");
				if(prefs.length != 4){
					Log.e(LCAT,"color prefs wrong size! : "+prefs.length);
					return;
				}
				String key1 = prefs[0];
				float val1 = getSettingFloat(cr, key1, 1);
				String key2 = prefs[1];
				float val2 = getSettingFloat(cr, key2);
				String key3 = prefs[2];
				float val3 = getSettingFloat(cr, key3);
				String key4 = prefs[3];
				float val4 = getSettingFloat(cr, key4);
				int color = Color.argb(Integer.valueOf((int) (val1 * 255)), Integer.valueOf((int) (val2 * 255)), Integer.valueOf((int) (val3 * 255)), Integer.valueOf((int) (val4 * 255)));
				item.setInitialColor(color);
			} else if(key.startsWith("argb_")){
				key = key.substring(5);
				String theColor = getSettingString(cr,key);
				if(theColor == null) { theColor = "#FF33B5E5"; }
				int color = Color.parseColor(theColor);
				item.setInitialColor(color);
			} else if(key.startsWith("rgb_")){
				key = key.substring(4);
				String theColor = getSettingString(cr,key);
				if(theColor == null) { theColor = "#33B5E5"; }
				int color = Color.parseColor(theColor);
				item.setAlphaSliderEnabled(true);
				item.setInitialColor(color);
			} else {
				item.setInitialColor(getSettingInt(cr, key));
			}
		}

		private void initRingtone(RingtonePreference item){
			item.setOnPreferenceChangeListener(this);
		}

		private void initList(ListPreference item){
			item.setOnPreferenceChangeListener(this);
			item.setValue(getSettingString(cr, item.getKey()));     	      	
		}

		private void initList(UltimaListPreference item){
			item.setOnPreferenceChangeListener(this);
			item.setValue(getSettingString(cr, item.getKey())); 
			if (item.getValue() == null ||
					"%s".equals(item.getSummary())){
				item.setValueIndex(item.getDefaultValue());
				item.setSummary(item.getEntry());
			}                   
		}

		private void initMultiSelectList(MultiSelectListPreference item){
			item.setOnPreferenceChangeListener(this);
			String initial = getSettingString(cr, item.getKey());
			if(initial != null){
				String[] ival = initial.split(",", 0);
				Set<String> fval = new HashSet<String>();
				int il = ival.length;
				for(int i = 0; i < il; i++){
					fval.add(ival[i]);
				}
				item.setValues(fval);
			}
		}

		private void initText(EditTextPreference item){
			item.setOnPreferenceChangeListener(this);
			item.setText(getSettingString(cr, item.getKey()));
		}

		private void initSwitch(SwitchPreference item){
			//Log.d(LCAT, "Initializin Switch: "+item);
			if(item != null){
				int defaultValue = 0;
				String[] prefs = item.getKey().substring(0).split(";");
				String key = prefs[0];
				if (prefs.length >= 2){
					defaultValue = Integer.parseInt(prefs[1]);
				}
				item.setOnPreferenceChangeListener(this);
				boolean isChecked = getSettingBoolean(cr, key, defaultValue);
				item.setChecked(isChecked);
				//Log.d(LCAT, "Setting Switch: "+item.getKey()+" => "+isChecked);
			}
		}

		private void initSwitch(UltimaSwitchPreference item){
			//Log.d(LCAT, "Initializin Switch: "+item);
			if(item != null){
				int defaultValue = item.getDefaultValue();
				item.setOnPreferenceChangeListener(this);
				boolean isChecked = getSettingBoolean(cr, item.getKey(), defaultValue);
				item.setChecked(isChecked);
				//Log.d(LCAT, "Setting Switch: "+item.getKey()+" => "+isChecked);
			}
		}

		private void initCategory(PreferenceCategory category){
			int items = category.getPreferenceCount();
			for(int i = 0; i < items; i++){
				Preference item = category.getPreference(i);
				initItem(item);
			}
		}

		private void initScreen(PreferenceScreen category){
			int items = category.getPreferenceCount();
			for(int i = 0; i < items; i++){
				Preference item = category.getPreference(i);
				initItem(item);
			}
		}

		private void initCheckbox(CheckBoxPreference item){
			//Log.d(LCAT, "Initializin Checkbox: "+item);
			if(item != null){
				int defaultValue = 0;
				String[] prefs = item.getKey().substring(0).split(";");
				String key = prefs[0];
				if (prefs.length >= 2){
					defaultValue = Integer.parseInt(prefs[1]);
				}
				item.setOnPreferenceChangeListener(this);
				boolean isChecked = getSettingBoolean(cr,key, defaultValue);
				item.setChecked(isChecked);
				//Log.d(LCAT, "Setting CheckBox: "+item.getKey()+" => "+isChecked);
			}
		}

		public static boolean setSettingInt(ContentResolver contentResolver, String setting, int value) {
			//Log.d(LCAT, "setSettingInt called: "+setting+":"+value);
			try {
				final ContentResolver cr = contentResolver;
				boolean result = android.provider.Settings.System.putInt(cr, setting, value);
				cr.notifyChange(Uri.parse("content://settings/system/"+setting), null);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static boolean setSettingFloat(ContentResolver contentResolver, String setting, float value) {
			//Log.d(LCAT, "setSettingInt called: "+setting+":"+value);
			try {
				final ContentResolver cr = contentResolver;
				boolean result = android.provider.Settings.System.putFloat(cr, setting, value);
				cr.notifyChange(Uri.parse("content://settings/system/"+setting), null);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static boolean setSettingBoolean(ContentResolver contentResolver, String setting, boolean value) {
			//Log.d(LCAT, "setSettingInt called: "+setting+":"+value);
			if(setting.startsWith("romcfg") || setting.startsWith("modcfg")){
				return setFileBoolean(setting,value);
			}
			try {
				final ContentResolver cr = contentResolver;
				boolean result = android.provider.Settings.System.putInt(cr, setting, value?1:0);
				cr.notifyChange(Uri.parse("content://settings/system/"+setting), null);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static int getSettingInt(ContentResolver contentResolver, String setting) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getInt(cr, setting);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		public static int getSettingInt(ContentResolver contentResolver, String setting, int def) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getInt(cr, setting, def);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		public static float getSettingFloat(ContentResolver contentResolver, String setting) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getFloat(cr, setting);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		public static float getSettingFloat(ContentResolver contentResolver, String setting, float def) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getFloat(cr, setting, def);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		public static boolean getSettingBoolean(ContentResolver contentResolver, String setting, int def) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			if(setting.startsWith("romcfg") || setting.startsWith("modcfg")){
				return getFileBoolean(setting);
			}
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getInt(cr, setting, def) > 0;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static boolean getSettingBoolean(ContentResolver contentResolver, String setting) {
			//Log.d(LCAT, "getSettingInt called: "+setting);
			if(setting.startsWith("romcfg") || setting.startsWith("modcfg")){
				return getFileBoolean(setting);
			}
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getInt(cr, setting, 0) > 0;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static boolean setSettingString(ContentResolver contentResolver, String setting, String value) {
			//Log.d(LCAT, "setSettingString called: "+setting+":"+value);
			try {
				final ContentResolver cr = contentResolver;
				boolean result = android.provider.Settings.System.putString(cr, setting, value);
				cr.notifyChange(Uri.parse("content://settings/system/"+setting), null);
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public static String getSettingString(ContentResolver contentResolver, String setting) {
			//Log.d(LCAT, "getSettingString called: "+setting);
			try {
				final ContentResolver cr = contentResolver;
				return android.provider.Settings.System.getString(cr, setting);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}



		private static boolean setFileBoolean(String setting, boolean value) {
			boolean isMod = setting.startsWith("modcfg");
			boolean isReverse = isMod?setting.startsWith("modcfgreverse_"):setting.startsWith("romcfgreverse_");
			String fileName = "";
			if(isMod){
				if(isReverse){
					fileName = setting.replace("modcfgreverse_", "");
				} else {
					fileName = setting.replace("modcfg_", "");
				}
			} else {
				if(isReverse){
					fileName = setting.replace("romcfgreverse_", "");
				} else {
					fileName = setting.replace("romcfg_", "");
				}
			}

			Log.d(LCAT,"Set FileBoolean: value: "+value+" filname: "+fileName+" mod: "+isMod+" reverse: "+isReverse);
			new File(isMod?MODCFG_FOLDER:ROMCFG_FOLDER).mkdirs();

			File file = new File((isMod?MODCFG_FOLDER:ROMCFG_FOLDER)+fileName);
			boolean create = value;

			if(isReverse){
				create = create?false:true;
			}

			if(create){
				try{
					Log.d(LCAT,"Set FileBoolean: Creating "+file.getPath());
					file.createNewFile();
				}catch(Exception e){
					Log.e(LCAT,"Set FileBoolean: Creating "+file.getPath()+" ERROR: "+e.getMessage());
					e.printStackTrace();
				}
			} else {
				try{
					Log.d(LCAT,"Set FileBoolean: Deleting "+file.getPath());
					file.delete();
				}catch(Exception e){
					Log.e(LCAT,"Set FileBoolean: Deleting "+file.getPath()+" ERROR: "+e.getMessage());
					e.printStackTrace();
				}
			}
			return getFileBoolean(setting);
		}

		private static boolean getFileBoolean(String setting) {
			boolean isMod = setting.startsWith("modcfg");
			boolean isReverse = isMod?setting.startsWith("modcfgreverse_"):setting.startsWith("romcfgreverse_");
			String fileName = "";
			if(isMod){
				if(isReverse){
					fileName = setting.replace("modcfgreverse_", "");
				} else {
					fileName = setting.replace("modcfg_", "");
				}
			} else {
				if(isReverse){
					fileName = setting.replace("romcfgreverse_", "");
				} else {
					fileName = setting.replace("romcfg_", "");
				}
			}

			File file = new File((isMod?MODCFG_FOLDER:ROMCFG_FOLDER)+fileName);
			boolean exists = file.exists();

			Log.d(LCAT,"Get FileBoolean: filname: "+fileName+" mod: "+isMod+" reverse: "+isReverse+" exists: "+exists);
			return isReverse?!exists:exists;
		}
	}


}
