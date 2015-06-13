package com.ultima.settings.hostname;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.ultima.settings.utils.Root;


public class HostnameActivity extends Activity implements Constants
{
	public final String TAG = this.getClass().getSimpleName();

	private Button restoreBtn;
	private Button setBtn;
	private EditText inputBox;
	private EditText currentBox;
	private EditText originalBox;
	
	private enum Hostname {
	    GET, SET, ORIGINAL, RESTORE 
	}
	
	Hostname mHostname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
        setTheme(Preferences.getTheme());		
		setContentView(R.layout.hostname_main);		
		
		restoreBtn = (Button) findViewById(R.id.hostname_restore);
		setBtn = (Button) findViewById(R.id.hostname_setbtn);
		inputBox = (EditText) findViewById(R.id.hostname_tb);
		currentBox = (EditText) findViewById(R.id.curr_hostname);
		originalBox = (EditText) findViewById(R.id.hostname_orig_tb);
		currentBox.setMaxLines(1);
		originalBox.setMaxLines(1);

		final AlertDialog errorMessage = new AlertDialog.Builder(this).create();
        errorMessage.setTitle(getResources().getString(R.string.error));
        errorMessage.setButton(RESULT_OK, getResources().getString(R.string.ok),(DialogInterface.OnClickListener) null);
		
		if(!Preferences.getHostnameChanged()){
			Preferences.setHostnameChanged( true);
			new RunToolTask().execute(Hostname.ORIGINAL);
			Preferences.setHostname(Preferences.getOriginalHostname());
		} else {
			updateOriginal();
			updateCurrent();
		}
		
		restoreBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new RunToolTask().execute(Hostname.RESTORE);
			}
		});
		
		setBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String desiredHostname;
				desiredHostname = inputBox.getText().toString();
				
				if(desiredHostname.matches("^[\\w\\.-]{1,255}$")){
					new RunToolTask().execute(new Object[]{Hostname.SET, desiredHostname});
				} else { 
					errorMessage.setMessage(getResources().getString(R.string.hostname_incorrect_name));
					errorMessage.show();
				}			
			}
		});		
	}

	private void updateCurrent() {
		currentBox.setText(Preferences.getHostname());
	}

	private void updateOriginal(){
		originalBox.setText(Preferences.getOriginalHostname());
	}
	
	private class RunToolTask extends AsyncTask<Object, Void, Integer> {
		int resultValues = 0;
		@SuppressWarnings("null")
		@Override
		protected Integer doInBackground(Object... params) {
			Hostname hostname = (Hostname)params[0];
			
			switch(hostname){
			case GET:
				Preferences.setHostname(Root.getHostname());
				resultValues = 0;
				break;
			case SET:
				Root.setHostname((String)params[1]);
				Preferences.setHostname((String)params[1]);
				resultValues = 1;
				break;
			case ORIGINAL:
				Preferences.setOriginalHostname(Root.getHostname());
				Preferences.setHostname(Preferences.getOriginalHostname());
				resultValues = 2;
				break;
			case RESTORE:
				Root.setHostname(Preferences.getOriginalHostname());
				Preferences.setHostname(Root.getHostname());
				resultValues = 3;
				break;
			default:
				resultValues = (Integer) null;
				Log.d(TAG, "Incorrect Parameters passed in");
				break;
			}
			return resultValues;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result != 2){
				updateCurrent();
			} else {
				updateOriginal();
				updateCurrent();
			}
		}
	}
}
