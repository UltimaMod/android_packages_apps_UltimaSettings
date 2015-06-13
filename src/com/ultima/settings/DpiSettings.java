package com.ultima.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ultima.settings.utils.Constants;
import com.ultima.settings.utils.Preferences;
import com.ultima.settings.utils.Root;
import com.ultima.settings.utils.Utils;

public class DpiSettings extends Activity implements Constants {

	private TextView mSeekbarShowValue;
	private int mOffset;
	private int mSeekBarProgressValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTheme(Preferences.getTheme());			
		setContentView(R.layout.dpi_settings_main);

		mSeekbarShowValue = (TextView) findViewById(R.id.seekbar_value);
		TextView currentDpiTv = (TextView) findViewById(R.id.currentSummary);
		SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
		Button apply = (Button) findViewById(R.id.apply);
		Button reset = (Button) findViewById(R.id.reset_default);

		int currentDPI = Integer.parseInt(Utils.getProp("ro.sf.lcd_density"));
		mOffset = 360;

		currentDpiTv.setText(Integer.toString(currentDPI));

		mSeekbarShowValue.setText(Integer.toString(currentDPI));

		seekbar.setMax(12);
		seekbar.setProgress((currentDPI - mOffset) / 10);	
		mSeekBarProgressValue = 0;
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mSeekbarShowValue.setText(Integer.toString((mSeekBarProgressValue * 10) + mOffset));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mSeekBarProgressValue = progress; 
			}
		});

		apply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setDpi((mSeekBarProgressValue * 10) + mOffset);				
			}
		});

		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setDpi(480);			
			}
		});

	}

	private void setDpi(int value) {
		Root.shell("mount -o remount,rw /system");
		Root.shell("sed -i 's/.*ro.sf.lcd_density.*/ro.sf.lcd_density=" + value + "/g' /system/build.prop; echo $?");
		Root.shell("mount -o remount,ro /system");
		Toast.makeText(this, getResources().getString(R.string.dpi_ok), Toast.LENGTH_LONG).show();
	}
}
