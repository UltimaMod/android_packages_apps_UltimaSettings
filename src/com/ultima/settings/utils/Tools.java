package com.ultima.settings.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.stericson.RootTools.*;
import com.ultima.settings.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class Tools {
	public static String getSdCardPath(){
		String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
		return sdCard;
	}

	public static void dispatch(String[] tool){
		if(tool[0].contentEquals("reboot")){
			reboot();
		} else if(tool[0].contentEquals("recovery")){
			recovery();
		} else if(tool[0].contentEquals("download")){
			download();
		} else if(tool[0].contentEquals("bootloader")){
			bootloader();
		} else if(tool[0].contentEquals("hotboot")){
			hotboot();
		} else if(tool[0].contentEquals("restartsystemui")){
			rebootSystemUi();
		} else if(tool[0].contentEquals("restartlauncher")){
			rebootLauncher();
		} else if(tool[0].contentEquals("shell")){
			if(tool.length < 2){
				Log.e("Tools","Not enough parameters given for SHELL");
			} else {
				shell(tool[1]);
			}
		}
	}

	public static void dispatch(Context context, String[] tool){
		if(tool[0].contentEquals("reboot")){
			reboot(context);
		} else if(tool[0].contentEquals("recovery")){
			recovery(context);
		} else if(tool[0].contentEquals("download")){
			download(context);
		} else if(tool[0].contentEquals("bootloader")){
			bootloader(context);
		} else if(tool[0].contentEquals("hotboot")){
			hotboot();
		} else if(tool[0].contentEquals("restartsystemui")){
			rebootSystemUi();
		} else if(tool[0].contentEquals("restartlauncher")){
			rebootLauncher();
		} else if(tool[0].contentEquals("shell")){
			if(tool.length < 2){
				Log.e("Tools","Not enough parameters given for SHELL");
			} else {
				shell(tool[1]);
			}
		}
	}

	public static void reboot(){
		rebootPhone("now");
	}

	public static void reboot(Context context){
		rebootPhone(context ,"now");
	}

	public static void recovery(){
		rebootPhone("recovery");
	}

	public static void recovery(Context context){
		rebootPhone(context ,"recovery");
	}

	public static void download(){
		rebootPhone("download");
	}

	public static void download(Context context){
		rebootPhone(context ,"download");
	}

	public static void bootloader(){
		rebootPhone("bootloader");
	}

	public static void bootloader(Context context){
		rebootPhone(context ,"bootloader");
	}

	public static void rebootSystemUi(){
		shell("pkill -TERM -f com.android.systemui");
	}

	public static void rebootLauncher(){
		shell("pkill -TERM -f com.android.launcher3");
	}

	public static void hotboot(){
		shell("setprop ctl.restart surfaceflinger;setprop ctl.restart zygote");
	}

	public static String shell(String cmd) {
		String out = "";
		ArrayList<String> r = system(getSuBin(),cmd).getStringArrayList("out");
		for(String l: r){
			out += l+"\n";
		}
		return out;
	}

	public static String noneRootShell(String cmd) {
		String out = "";
		ArrayList<String> r = system("sh",cmd).getStringArrayList("out");
		for(String l: r){
			out += l+"\n";
		}
		return out;
	}

	public static boolean getRoot(){
		return RootTools.isAccessGiven();
	}

	public void setHardwareButtons(Object value, Context context){
		int ourValue = Integer.parseInt((String) value);
		new HardwareButtons(context).execute(ourValue);
	}

	public static void setHostname(String hostname){
		shell("setprop net.hostname " + hostname);
	}

	public static String getHostname(){
		return shell("getprop net.hostname");
	}

	private static void rebootPhone(String type){
		shell("reboot "+type);
	}

	private static void rebootPhone(Context context, String type) {
		try {
			((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(type);
		} catch (Exception e) {
			Log.e("Tools", "reboot '"+type+"' error: "+e.getMessage());
			shell("reboot "+type);
		}
	}

	public static void setBootanimation(String cmd){
		backupBootAnimation();
		shell("cp " + cmd + " /system/media/bootanimation.zip");
		shell("chmod 644 /system/media/bootanimation.zip");
	}


	public static void backupBootAnimation() {
		// Backup the existing boot animation, if a backup does not already exist
		if(!(new File("/system/media/bootanimation.zip.bak").exists())){
			shell("mount -o remount,rw /system");
			shell("cp /system/media/bootanimation.zip /system/media/bootanimation.zip.bak");
		}
	}

	public static void resetBootAnimation(){
		shell("mount -o remount,rw /system");

		if((new File("/system/media/bootanimation.zip.bak").exists())){
			shell("cp /system/media/bootanimation.zip.bak /system/media/bootanimation.zip");
			shell("rm /system/media/bootanimation.zip.bak");
			shell("chmod 644 /system/media/bootanimation.zip");
		}
	}

	public static void enableDisableBootAnimation(boolean value) {
		if (value){
			shell("setprop debug.sf.nobootanimation 0");
		}else{
			shell("setprop debug.sf.nobootanimation 1");
		}
	}

	private static boolean isUiThread(){
		return (Looper.myLooper() == Looper.getMainLooper());
	}

	private static String getSuBin(){
		if(new File("/system/xbin","su").exists()){
			return "/system/xbin/su";
		}
		if(RootTools.isRootAvailable()){
			return "su";
		}
		return "sh";
	}

	private static Bundle system(String shell, String command) {
		if (BuildConfig.DEBUG) {
			if (isUiThread()) {
				Log.e(shell,"Application attempted to run a shell command from the main thread");
			}
			Log.d(shell,"START");
		}

		ArrayList<String> res = new ArrayList<String>();
		ArrayList<String> err = new ArrayList<String>();
		boolean success = false;
		try {
			Process process = Runtime.getRuntime().exec(shell);
			DataOutputStream STDIN = new DataOutputStream(process.getOutputStream());
			BufferedReader STDOUT = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader STDERR = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			if (BuildConfig.DEBUG) Log.i(shell, command);
			STDIN.writeBytes(command + "\n");
			STDIN.flush();
			STDIN.writeBytes("exit\n");
			STDIN.flush();

			process.waitFor();
			if (process.exitValue() == 255) {
				if (BuildConfig.DEBUG) Log.e(shell,"SU was probably denied! Exit value is 255");
				err.add("SU was probably denied! Exit value is 255");
			}

			while (STDOUT.ready()) {
				String read = STDOUT.readLine();
				if (BuildConfig.DEBUG) Log.d(shell, read);
				res.add(read);
			}
			while (STDERR.ready()) {
				String read = STDERR.readLine();
				if (BuildConfig.DEBUG) Log.e(shell, read);
				err.add(read);
			}

			process.destroy();
			success = true;
			if(err.size() > 0){
				success = false;
			}
		} catch (IOException e) {
			if (BuildConfig.DEBUG) Log.e(shell,"IOException: "+e.getMessage());
			err.add("IOException: "+e.getMessage());
		} catch (InterruptedException e) {
			if (BuildConfig.DEBUG) Log.e(shell,"InterruptedException: "+e.getMessage());
			err.add("InterruptedException: "+e.getMessage());
		}
		if (BuildConfig.DEBUG) Log.d(shell,"END");
		Bundle r = new Bundle();
		r.putBoolean("success", success);
		r.putString("cmd", command);
		r.putString("binary", shell);
		r.putStringArrayList("out", res);
		r.putStringArrayList("error", err);
		return r;
	}


	private class HardwareButtons extends AsyncTask<Integer, Void, Void> {

		private Context mContext;

		private ProgressDialog mLoadingDialog;
		
		public HardwareButtons(Context context) {
			mContext = context;
		}

		@Override
		protected void onPreExecute(){
			mLoadingDialog = new ProgressDialog(mContext);
			mLoadingDialog.setIndeterminate(true);
			mLoadingDialog.setCancelable(false);
			mLoadingDialog.setMessage(mContext.getResources().getString(R.string.setting));
			mLoadingDialog.show();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			shell("mount -o rw,remount /system"); // Remount as readable
			if(params[0] == 0) { // Enable Controls
				shell("cp /system/jflte-gpe/buttons/stock /system/usr/keylayout/Generic.kl"); // Stock
			} else if(params[0] == 1) {
				shell("cp /system/jflte-gpe/buttons/disabled_buttons /system/usr/keylayout/Generic.kl"); // Disabled
			} else {
				shell("cp /system/jflte-gpe/buttons/menu_to_recents /system/usr/keylayout/Generic.kl"); // Menu to Recents				
			}
			shell("mount -o ro,remount /system"); // remount as read-only, for safety

			return null;
		}
		
		@Override
	    protected void onPostExecute(Void result) {
			mLoadingDialog.cancel();
			Toast.makeText(mContext, mContext.getResources().getString(R.string.buttons_set), Toast.LENGTH_LONG).show();
	        super.onPostExecute(result);
	    }
	}
}
