package com.ultima.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ultima.settings.colorpicker.ColorPickerPreference;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Tools;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SettingsActivity extends Activity implements Constants {
	private static Context mContext = CONTEXT;
	private static Context mActivityContext;
	private static AlertDialog aDialog;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getTheme());
    	Tools.getRoot(); //Check for root, so that checking later doesn't slow down the action
        super.onCreate(savedInstanceState);
        mActivityContext = this;
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
		case R.id.change_theme:
			showThemeDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	 @Override
		public void onDestroy()	{
			super.onDestroy();
			if (aDialog != null) {
				aDialog.dismiss();
				aDialog = null;
			}
		}
    
    public void showThemeDialog() {
    	int currentThemeSelection = Preferences.getCurrentTheme();
		final CharSequence[] items={"Holo Light", "Holo Light Dark Actionbar", "Holo Dark"};
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		aDialog = builder.create();

		builder.setTitle("Choose a Theme");
		builder.setSingleChoiceItems(items, currentThemeSelection, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {		

				if ("Holo Light".equals(items[which])){			
					Preferences.setTheme(0);
				}				
				if ("Holo Light Dark Actionbar".equals(items[which])){
					Preferences.setTheme(1);
				}
				if ("Holo Dark".equals(items[which])){
					Preferences.setTheme(2);
				}
				aDialog.dismiss();	    	
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				aDialog.dismiss();				
			}
		});
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				aDialog.dismiss();
				restartActivity();
			}
		});
		builder.show();
	}
            
    private void restartActivity() {
		Intent i = new Intent(mContext, SettingsActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.finish();
		startActivity(i);
	}
    
    public boolean appInstalled(String uri)	{
		PackageManager pm = getPackageManager();
		boolean app_installed = false;
		try	{
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		}
		catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed ;
	}
    
    public static class PrefsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    	ContentResolver cr;
    	private static final String LCAT = "PrefsFragment";

    	private static String ROMCFG_FOLDER;
    	private static String MODCFG_FOLDER;
    	private int batteryLevelLowSelection, batteryLevelMidSelection;
        String lowBatterySummary, midBatterySummary;
        Preference lowBattPref, midBattPref;
    	
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            ROMCFG_FOLDER = getResources().getString(R.string.romcfg_folder);
            MODCFG_FOLDER = getResources().getString(R.string.modcfg_folder);
            cr = getActivity().getContentResolver();
            initPrefs();
            disablePrefs();
            lowBatterySummary = getString(R.string.battery_level_low_summary);
            midBatterySummary = getString(R.string.battery_level_mid_summary);

            lowBattPref = findPreference("battery_level_low");
            setLowBattSummary();
            
            lowBattPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showBatteryLowLevelDialog();
                    return false;
                }
            });
            
            midBattPref = findPreference("battery_level_mid");
            setMidBattSummary();
            
            midBattPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showBatteryMidLevelDialog();
                    return false;
                }
            });
            Preference resetBatteryDefault = findPreference("battery_reset_defaults");
            resetBatteryDefault.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showBatteryResetDialog();
                    return false;
                }
            });
        }

        private void setLowBattSummary(){
            int currentValue = getSettingInt(cr, "status_bar_battery_low_level", 4);
            String text = lowBatterySummary + " " + currentValue + "%";
            lowBattPref.setSummary(text);
        }

        private void setMidBattSummary(){
            String text;
            int currentValue = getSettingInt(cr, "status_bar_battery_mid_level", 15);
            text = midBatterySummary + " " + currentValue + "%";
            midBattPref.setSummary(text);
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
    		} 
    		
    		return true;
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
        	} else if(type.equals("MultiSelectListPreference")){
        		dispatchMultiSelectList((MultiSelectListPreference) item, (Set<String>) value);
        	} else if(type.equals("SwitchPreference")){
        		dispatchSwitch((SwitchPreference) item, value);
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
        		setSettingBoolean(cr,item.getKey(),(Boolean) value);
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
        		setSettingBoolean(cr,item.getKey(),(Boolean) value);
        		//Log.d(LCAT, "Setting in Settings: "+item.getKey()+" => "+value);
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
        	PreferenceScreen settingsRoot = (PreferenceScreen) findPreference("settings_root"); // First Settings page

			//Remove Advanced Display from settings if it's not installed
			if(!((SettingsActivity) getActivity()).appInstalled("com.cyanogenmod.settings.device")){
			    PreferenceCategory cat = (PreferenceCategory) findPreference("crt_category");
				Preference pref1 = (Preference) findPreference("activity;com.cyanogenmod.settings.device;com.cyanogenmod.settings.device.DisplaySettings");
				cat.removePreference(pref1);
			}
        }
        
        public void showBatteryLowLevelDialog() {
            int current = getSettingInt(cr, "status_bar_battery_low_level", 4);
            int currentSelection = 0;
            if(current == 4){
                currentSelection = 0;
            } else if (current == 10){
                currentSelection = 1;
            } else {
                currentSelection = 2;
            }
            final CharSequence [] items={ "4", "10", "14" };
            AlertDialog.Builder builder=new AlertDialog.Builder(mActivityContext);
            aDialog = builder.create();

            builder.setTitle("Select a Value");
            builder.setSingleChoiceItems(items, currentSelection, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {        

                    if ("4".equals(items[which])){         
                        batteryLevelLowSelection = 4;
                    }               
                    if ("10".equals(items[which])){
                        batteryLevelLowSelection = 10;
                    }
                    if ("14".equals(items[which])){
                        batteryLevelLowSelection = 14;
                    }
                    aDialog.dismiss();          
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    aDialog.dismiss();              
                }
            });
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSettingInt(cr, "status_bar_battery_low_level", batteryLevelLowSelection);
                    setLowBattSummary();
                }
            });
            builder.show();
        }
        
        public void showBatteryResetDialog() {
            AlertDialog.Builder builder=new AlertDialog.Builder(mActivityContext);
            aDialog = builder.create();

            builder.setTitle("Are you sure?");
            builder.setMessage("Are you sure you want to reset the battery options to their defaults?");
            
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    aDialog.dismiss();              
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSettingInt(cr, "status_bar_battery_low_level", 4);
                    setLowBattSummary();
                    setSettingInt(cr, "status_bar_battery_mid_level", 15);
                    setMidBattSummary();
                    setSettingInt(cr, "status_bar_battery_percent_color", 0xFF000000);
                    setSettingInt(cr, "status_bar_battery_norm_color", 0xFFFFFFFF);
                    setSettingInt(cr, "status_bar_battery_back_color", 0x66FFFFFF);
                    setSettingInt(cr, "status_bar_battery_charge_color", 0xFFFFFFFF);
                    setSettingInt(cr, "status_bar_battery_bolt_color", 0xB2000000);
                    setSettingInt(cr, "status_bar_battery_low_color", 0xFFF3300);
                    setSettingInt(cr, "status_bar_battery_mid_color", 0xFFF3300);
                    setSettingInt(cr, "status_bar_battery_percent_color", 0xFF000000);
                }
            });
            builder.show();
        }
        
        public void showBatteryMidLevelDialog() {
            int current = getSettingInt(cr, "status_bar_battery_mid_level", 15);
            int currentSelection = 0;
            if(current == 15){
                currentSelection = 0;
            } else if (current == 30){
                currentSelection = 1;
            } else {
                currentSelection = 2;
            }
            final CharSequence [] items={ "15", "30", "45" };
            AlertDialog.Builder builder=new AlertDialog.Builder(mActivityContext);
            aDialog = builder.create();

            builder.setTitle("Select a Value");
            builder.setSingleChoiceItems(items, currentSelection, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {        

                    if ("15".equals(items[which])){         
                        batteryLevelMidSelection = 15;
                    }               
                    if ("30".equals(items[which])){
                        batteryLevelMidSelection = 30;
                    }
                    if ("45".equals(items[which])){
                        batteryLevelMidSelection = 45;
                    }
                    aDialog.dismiss();          
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    aDialog.dismiss();              
                }
            });
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSettingInt(cr, "status_bar_battery_mid_level", batteryLevelMidSelection);
                    setMidBattSummary();
                }
            });
            builder.show();
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
        	} else if(type.equals("MultiSelectListPreference")){
        		initMultiSelectList((MultiSelectListPreference) item);
        	} else if(type.equals("SwitchPreference")){
        		initSwitch((SwitchPreference) item);
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
            if (item.getValue() == null){
                item.setValueIndex(0);
                item.setSummary(item.getEntry());
            }
        	item.setValue(getSettingString(cr, item.getKey()));       	
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
        		item.setOnPreferenceChangeListener(this);
        		boolean isChecked = getSettingBoolean(cr,item.getKey());
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
        		item.setOnPreferenceChangeListener(this);
        		boolean isChecked = getSettingBoolean(cr,item.getKey());
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
    	
    	public static boolean getSettingBoolean(ContentResolver contentResolver, String setting) {
    		//Log.d(LCAT, "getSettingInt called: "+setting);
    		if(setting.startsWith("romcfg") || setting.startsWith("modcfg")){
        		return getFileBoolean(setting);
        	}
    		try {
    			final ContentResolver cr = contentResolver;
    			return android.provider.Settings.System.getInt(cr, setting) > 0;
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
