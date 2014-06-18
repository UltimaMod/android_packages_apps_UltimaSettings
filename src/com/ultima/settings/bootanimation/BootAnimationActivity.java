package com.ultima.settings.bootanimation;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Tools;


public class BootAnimationActivity extends Activity implements Constants {

	TextView fileInfo;
	TextView animationStatus;
	Button browseButton;
	Button setAnimationButton;
	Button resetDefault;
	Button enableDisable;
	Button clearButton;
	String currentSelectedFile;

	AlertDialog enableDisableDialog;
	AlertDialog rebootDialog;
	boolean info_page;
	
	String mPath;
	String mSdRoot = Environment.getExternalStorageDirectory().getPath();
	File mFile;
	File mFilename;
	String mExtractLocation;
	File mExtractFolder;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(Preferences.getTheme());			
		setContentView(R.layout.bootani_main);
		setupActionBar();
		
		

		browseButton = (Button)findViewById(R.id.choose_file);
		clearButton = (Button)findViewById(R.id.clear_file);
		
		resetDefault = (Button)findViewById(R.id.reset_default);
		enableDisable = (Button)findViewById(R.id.enable_disable_boot);
		setAnimationButton = (Button)findViewById(R.id.set_animation);
		
		fileInfo = (TextView)findViewById(R.id.file_information);
		animationStatus = (TextView)findViewById(R.id.StatusTextAnimation);
		
		setDefaults();
		

		browseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showChooser();
			}
		});
		
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Tools.noneRootShell("rm -rf " +  mExtractFolder);
				setDefaults();
			}
		});

		resetDefault.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Tools.resetBootAnimation();
				if(Preferences.isRebootAfterSelection()){
					if(Preferences.isRebootAfterSelection()){
						showRebootDialog();
					}
				}
			}
		});

		enableDisable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showEnableDialog();
			}
		});


		setAnimationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!currentSelectedFile.equals(null)){
					Tools.setBootanimation(currentSelectedFile);
				
					setAnimationButton.setEnabled(false);
				}
				Toast.makeText(BootAnimationActivity.this, 
						"Animation Set... Reboot to view it", Toast.LENGTH_LONG).show();
				if(Preferences.isRebootAfterSelection()){
					showRebootDialog();
				}
				recreate();
			}
		});
		
	}
	
	private void setDefaults(){
		Tools.noneRootShell("rm -rf " + mSdRoot + getString(R.string.boot_animation_extraction_folder));
		browseButton.setEnabled(true);
		clearButton.setEnabled(false);
		setAnimationButton.setEnabled(false);
		fileInfo.setText("No File Selected");
		animationStatus.setText("Boot Animation: N/A");
		animationStatus.setTextColor(getResources().getColor(R.color.holo_red_light));
		currentSelectedFile = null;
		
		if((new File("/system/media/bootanimation.zip.bak").exists())){
			resetDefault.setText("Restore Default");
			resetDefault.setEnabled(true);
		}
		else{
			resetDefault.setText("Already Default");
			resetDefault.setEnabled(false);
		}
	}

	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void showChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		startActivityForResult(intent, BOOTANI_REQUEST_CODE);
	}

	public void showRebootDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		rebootDialog = builder.create();

		builder.setTitle("Reboot");
		builder.setMessage("Reboot your device?");

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				rebootDialog.dismiss();			
			}
		});

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				rebootDialog.dismiss();
				Tools.noneRootShell("rm -rf " + mSdRoot + getString(R.string.boot_animation_extraction_folder));
				Tools.reboot();
			}
		});

		builder.show(); 
	}

	public void showEnableDialog() {
		final CharSequence[] items={"Enabled","Disabled"};
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		enableDisableDialog = builder.create();
		int currentSelection;

		if(Preferences.isBootAnimationEnabled()){
			currentSelection = 0;
		}else{
			currentSelection = 1;
		}

		builder.setTitle("Enable or Disable");
		builder.setSingleChoiceItems(items, currentSelection, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which){						
				if ("Enabled".equals(items[which]))	{		
					Preferences.setBootAnimationStatus(true);
				}				
				if ("Disabled".equals(items[which])){
					Preferences.setBootAnimationStatus(false);
				}				  	
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				enableDisableDialog.dismiss();
			}
		});

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which){
				enableDisableDialog.dismiss();
				Tools.enableDisableBootAnimation(Preferences.isBootAnimationEnabled());
				if(Preferences.isRebootAfterSelection()){
					showRebootDialog();
				}
			}
		});
		builder.show(); 
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BOOTANI_REQUEST_CODE:	
			// If the file selection was successful
			if (resultCode == RESULT_OK) {

				//Full file path
				mPath = data.getData().getPath().toString();
				//Filename
				mFile = new File("" + mPath);
				//Filename without the extension
				mFilename = new File(mFile.getName().substring(0, mFile.getName().length() - 4));
				//Boot animations folder
				mExtractLocation = mSdRoot + getResources().getString(R.string.boot_animation_extraction_folder);
				//File for the newly created unzipped archive
				mExtractFolder = new File(mExtractLocation + mFilename);
				//Create the dirs to unzip to
				mExtractFolder.mkdirs();
				//Perform the unzip
				Tools.noneRootShell("unzip " + mPath + " -d" + mExtractLocation + mFilename);
				//Show selected file
				fileInfo.setText(mPath);
				fileInfo.setTextColor(getResources().getColor(R.color.white));
				browseButton.setEnabled(false);
				clearButton.setEnabled(true);
				
				
				if(new File(mExtractFolder + "/desc.txt").exists())
				{
					setAnimationButton.setEnabled(true);
					animationStatus.setText("Boot Animation: Valid");
					animationStatus.setTextColor(getResources().getColor(R.color.holo_green_light));
				}
				else{
					animationStatus.setText("Boot Animation: Invalid");
				}
				Tools.shell("rm -rf " + mSdRoot + "/ROMControl/BootAnimations/temp");

				currentSelectedFile = mPath;
			} 
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bootani_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.file_chooser_settings:
			Intent intent = new Intent(this, BootAnimationSettings.class);
			startActivity(intent);
			return true;
		case R.id.file_choose_help:
			setContentView(R.layout.bootani_info);
			info_page = true;
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}
	}

	public void onBackPressed() {
		if(info_page){
			info_page = false;
			setContentView(R.layout.bootani_main);
			this.recreate();
		} else {
			super.onBackPressed();
		}
	}

}
