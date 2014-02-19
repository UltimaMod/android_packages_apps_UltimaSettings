package com.ultima.settings.hostname;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ultima.settings.R;
import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Tools;


public class HostnameActivity extends Activity implements Constants
{

	Context mContext = CONTEXT;
    
    Button restoreBtn;
    Button setBtn;
    EditText inputBox;
    EditText currentBox;
    EditText originalBox;
    
    RunToolTask toolTask = new RunToolTask();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        setTheme(Preferences.getTheme());
		
		setContentView(R.layout.hostname_main);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		restoreBtn = (Button) findViewById(R.id.hostname_restore);
		setBtn = (Button) findViewById(R.id.hostname_setbtn);
		inputBox = (EditText) findViewById(R.id.hostname_tb);
		currentBox = (EditText) findViewById(R.id.curr_hostname);
		originalBox = (EditText) findViewById(R.id.hostname_orig_tb);
		
		inputBox.setMaxLines(1);
		
		currentBox.setFocusable(false);
		currentBox.setFocusableInTouchMode(false);
		currentBox.setClickable(false);
		currentBox.setMaxLines(1);
		
		originalBox.setFocusable(false);
		originalBox.setFocusableInTouchMode(false);
		originalBox.setClickable(false);
		originalBox.setMaxLines(1);
		
		final AlertDialog errorMessage = new AlertDialog.Builder(this).create();
        errorMessage.setTitle("Error");
        errorMessage.setButton(RESULT_OK, "Ok",(DialogInterface.OnClickListener) null);
		
		if(!Preferences.isHostnameChanged()){
			Preferences.setHostnameChanged( true);
			toolTask.execute("original_hostname");
			Preferences.setHostname(Preferences.getOriginalHostname());
		}
		else {
			updateOriginal();
			updateCurrent();
		}
		
		restoreBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toolTask.execute("restore_hostname");
			}
		});
		
		setBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String desiredHostname;
				desiredHostname = inputBox.getText().toString();
				
				if(desiredHostname.matches("^[\\w\\.-]{1,255}$")){
					toolTask.execute(new String[]{"set_hostname", desiredHostname});
				}
				else{
					errorMessage.setMessage("The hostname may only contain letters (a-zA-Z), numbers (0-9) and . or - as per a proper hostname.\n\nIt cannot contain any special characters and must be shorter than 255 characters.");
					errorMessage.show();
				}			
			}
		});
		
	}

	private void updateCurrent() {
		currentBox.setText(Preferences.getHostname());
	}
	
	private void refresh(){
		updateCurrent();
		restartActivity();
	}
	
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void updateOriginal(){
		originalBox.setText(Preferences.getOriginalHostname());
	}
	
    private void restartActivity() {
		Intent i = new Intent(mContext, HostnameActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.finish();
		this.startActivity(i);
	}
	
	private class RunToolTask extends AsyncTask<String, Void, Void> {
		int resultValues = 0;
		@Override
		protected Void doInBackground(String... params) {
			if(params[0].equals("get_hostname") ){
				Preferences.setHostname(Tools.getHostname());
				resultValues = 0;
			} else if (params[0].equals("set_hostname")){
				Tools.setHostname(params[1]);
				Preferences.setHostname(params[1]);
				resultValues = 1;
			} else if (params[0].equals("original_hostname")){
				Preferences.setOriginalHostname(Tools.getHostname());
				Preferences.setHostname(Preferences.getOriginalHostname());
				resultValues = 2;
			} else if(params[0].equals("restore_hostname")){
				Tools.setHostname(Preferences.getOriginalHostname());
				Preferences.setHostname(Tools.getHostname());
				resultValues = 3;
			}
			else{
				Log.d("Error", "Incorrect Parameters passed in");
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(resultValues == 2){
				updateOriginal();
				updateCurrent();
				refresh();
			} else {
				refresh();
			}
		}
	}
}
