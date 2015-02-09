package com.ultima.settings.bootanimation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.GenerateORS;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Root;


public class BootAnimationActivity extends Activity implements Constants {
	
	public final String TAG = this.getClass().getSimpleName();

	private Context mContext;
	
	private TextView mFileInfo;
	private TextView mAvailablityStatus;
	private TextView mZipType;
	
	private Button mBrowseButton;
	private Button mSetAnimationButton;
	private Button mResetDefault;
	private Button mEnableDisable;
	private Button mClearButton;
	
	private AlertDialog mEnableDisableDialog;
	private AlertDialog mRebootDialog;
	
	private boolean mIsFlashable;

	private String mFile;
	private String mCurrentSelectedFile;
	private String mOk;
	private String mCancel;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(Preferences.getTheme());			
		setContentView(R.layout.bootani_main);
		
		mContext = this;

		mBrowseButton = (Button)findViewById(R.id.bootani_choose_file_button);
		mClearButton = (Button)findViewById(R.id.bootani_clear_file_button);
		
		mResetDefault = (Button)findViewById(R.id.bootani_reset_default_button);
		mEnableDisable = (Button)findViewById(R.id.bootani_boot_toggle_button);
		mSetAnimationButton = (Button)findViewById(R.id.bootani_set_animation_button);
		
		mFileInfo = (TextView)findViewById(R.id.bootani_selected_file);
		mAvailablityStatus = (TextView)findViewById(R.id.bootani_status_valid);
		mZipType = (TextView)findViewById(R.id.bootani_status_type);
		
		mOk = getResources().getString(R.string.ok);
		mCancel = getResources().getString(R.string.cancel);

		setDefaults();
		setOnClickListeners();
		
	}

	private void setOnClickListeners() {
		mBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showChooser();
			}
		});
		
		mClearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDefaults();
			}
		});

		mResetDefault.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Root.resetBootAnimation();
				if(Preferences.getBootaniRebootSelection()){
					showRebootDialog();
				}
			}
		});

		mEnableDisable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showEnableDialog();
			}
		});


		mSetAnimationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mCurrentSelectedFile.equals(null)){
					if(mIsFlashable){
						// We should only be here if the button is enabled anyway
						new GenerateORS(mContext, mCurrentSelectedFile).execute();
					} else {
						Root.setBootanimation(mCurrentSelectedFile);
						Toast.makeText(BootAnimationActivity.this, 
								getResources().getString(R.string.bootani_set_animation), Toast.LENGTH_LONG).show();
						mSetAnimationButton.setEnabled(false);
						if(Preferences.getBootaniRebootSelection()){
							showRebootDialog();
						} else {
							recreate();
						}
					}
				}
			}
		});
	}
	
	private void setDefaults(){
		
		mBrowseButton.setEnabled(true);
		mClearButton.setEnabled(false);
		mSetAnimationButton.setEnabled(false);
		
		// Default to no file selected
		mFileInfo.setText(getResources().getString(R.string.bootani_no_file_selected));
		// N/A Text
		String notAvailable = getString(R.string.not_available);
		
		// Availability status	    
	    String availStatusText = getResources().getString(R.string.bootani_title) + ":" + " <font color='#ff4444'>" + notAvailable + "</font>";
		mAvailablityStatus.setText(Html.fromHtml(availStatusText), TextView.BufferType.SPANNABLE);		
		
		// Type Status
		String fileTypeStatusText = getResources().getString(R.string.bootani_zip_type) + ":" + " <font color='#ff4444'>" + notAvailable + "</font>";
		mZipType.setText(Html.fromHtml(fileTypeStatusText), TextView.BufferType.SPANNABLE);		

		mCurrentSelectedFile = null;
		
		if(Preferences.getBootAniCustom()){
			mResetDefault.setText(getResources().getString(R.string.bootani_restore_default));
			mResetDefault.setEnabled(true);
		} else {
			mResetDefault.setText(getResources().getString(R.string.bootani_already_default));
			mResetDefault.setEnabled(false);
		}
	}

	private void showChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		startActivityForResult(intent, BOOTANI_REQUEST_CODE);
	}
	
	private void showInfo() {
		AlertDialog builder = new AlertDialog.Builder(this).create();
		builder.setCancelable(true);
		builder.setTitle(getResources().getString(R.string.information));
		builder.setMessage(getResources().getString(R.string.bootani_help));
		builder.show();
	}

	public void showRebootDialog() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		mRebootDialog = builder.create();

		builder.setTitle(getResources().getString(R.string.reboot));
		builder.setMessage(getResources().getString(R.string.reboot_device_question));

		builder.setNegativeButton(mCancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mRebootDialog.dismiss();			
			}
		});

		builder.setPositiveButton(mOk, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mRebootDialog.dismiss();
				Root.reboot();
			}
		});

		builder.show(); 
	}

	public void showEnableDialog() {

		final CharSequence[] items={getResources().getString(R.string.enabled), 
				getResources().getString(R.string.disabled)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mEnableDisableDialog = builder.create();
		int currentSelection;

		if(Preferences.getBootAniEnabled()){
			currentSelection = 0;
		}else{
			currentSelection = 1;
		}

		builder.setTitle(getResources().getString(R.string.enable_disable_question));
		builder.setSingleChoiceItems(items, currentSelection, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which){						
				if (getResources().getString(R.string.enabled).equals(items[which]))	{		
					Preferences.setIsBootaniEnabled(true);
				}				
				if (getResources().getString(R.string.disabled).equals(items[which])){
					Preferences.setIsBootaniEnabled(false);
				}				  	
			}
		});

		builder.setNegativeButton(mCancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mEnableDisableDialog.dismiss();
			}
		});

		builder.setPositiveButton(mOk, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which){
				mEnableDisableDialog.dismiss();
				Root.enableDisableBootAnimation(Preferences.getBootAniEnabled());
				if(Preferences.getBootaniRebootSelection()){
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
				mFile = data.getData().getPath().toString();
				//Show selected file
				mFileInfo.setText(mFile);
				mFileInfo.setTextColor(getResources().getColor(R.color.white));
				mBrowseButton.setEnabled(false);
				mClearButton.setEnabled(true);
				
				String valid = getResources().getString(R.string.bootani_valid);
				String fileTypeStatusText = "";
				String availStatusText = "";

				if(Root.noneRootShell("unzip -l " + mFile + " | grep -ci desc.txt").trim().equals("1")){
					mIsFlashable = false;
					mSetAnimationButton.setEnabled(true);
					// Availability status	    
				    availStatusText = getResources().getString(R.string.bootani_title) + ":" + " <font color='#009688'>" + valid + "</font>";					
					// Type Status
					String type = getResources().getString(R.string.bootani_animation_type);
					fileTypeStatusText = getResources().getString(R.string.bootani_zip_type) + ":" + " <font color='#009688'>" + type + "</font>";
				} else if(!(Root.noneRootShell("unzip -l " + mFile + " | grep -ci META-INF").trim().equals("0"))){
					mIsFlashable = true;
					// Availability status	    
				    availStatusText = getResources().getString(R.string.bootani_title) + ":" + 
					" <font color='#009688'>" + valid + "</font>";
				    
				    String type = getResources().getString(R.string.bootani_flashable_type);
				    
					if(Preferences.getBootAniORS()){
						mSetAnimationButton.setEnabled(true);		
						// Type Status			
						fileTypeStatusText = getResources().getString(R.string.bootani_zip_type) + ":" + 
						" <font color='#009688'>" + type + "</font>";
					} else {
						mSetAnimationButton.setEnabled(false);		
						// Type Status
						fileTypeStatusText = getResources().getString(R.string.bootani_zip_type) + ":" + 
						" <font color='#009688'>" + type + "</font> " +
						"<font color='#ff4444'>" + getResources().getString(R.string.bootani_ors_is_disabled) + "</font>";
					}
				} else {
					mIsFlashable = false;
					String invalid = getResources().getString(R.string.bootani_invalid);
				    availStatusText = getResources().getString(R.string.bootani_title) + ":" + 
					" <font color='#ff4444'>" + invalid + "</font>";
					fileTypeStatusText = getResources().getString(R.string.bootani_zip_type) + ":" + 
					" <font color='#ff4444'>" + invalid + "</font>";
				}

				mAvailablityStatus.setText(Html.fromHtml(availStatusText), TextView.BufferType.SPANNABLE);
				mZipType.setText(Html.fromHtml(fileTypeStatusText), TextView.BufferType.SPANNABLE);	

				//The currently selected file.
				mCurrentSelectedFile = mFile;
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
			showInfo();
			return true;
		default:
			return super.onOptionsItemSelected(item);			
		}
	}
}
