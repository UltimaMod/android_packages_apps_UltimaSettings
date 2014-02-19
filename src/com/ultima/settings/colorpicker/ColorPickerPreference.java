package com.ultima.settings.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.ultima.settings.R;

public class ColorPickerPreference extends DialogPreference {
	
	final View view;
	final View viewHue;
	final ColorPickerView viewSatVal;
	final ImageView viewCursor;
	final View viewOldColor;
	final View viewNewColor;
	final EditText rgbValue;
	final ImageView viewTarget;
	final ViewGroup viewContainer;
	final CheckBox alphaCheck;
	final SeekBar alphaSlider;
	final float[] currentColorHsv = new float[3];
	private int mColor;
	
	public ColorPickerPreference(Context context) {
		this(context, null);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs) {
		//this(context, attrs, R.attr.colorPickerPreferenceStyle);
		this(context, attrs, android.R.attr.editTextPreferenceStyle);
	}

	public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		view = LayoutInflater.from(context).inflate(R.layout.colorpicker_dialog, null);
		viewHue = view.findViewById(R.id.colorpicker_viewHue);
		viewSatVal = (ColorPickerView) view.findViewById(R.id.colorpicker_viewSatBri);
		viewCursor = (ImageView) view.findViewById(R.id.colorpicker_cursor);
		viewOldColor = view.findViewById(R.id.colorpicker_warnaLama);
		viewNewColor = view.findViewById(R.id.colorpicker_warnaBaru);
		viewTarget = (ImageView) view.findViewById(R.id.colorpicker_target);
		viewContainer = (ViewGroup) view.findViewById(R.id.colorpicker_viewContainer);
		alphaCheck = (CheckBox) view.findViewById(R.id.opacityActive);
		//if(alphaCheck != null) alphaCheck.setChecked(true);
		alphaSlider = (SeekBar) view.findViewById(R.id.opacityValue);
		rgbValue = (EditText) view.findViewById(R.id.color_rgb);
		if(rgbValue != null) rgbValue.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	try {
            		int color = Color.parseColor(s.toString());
            		mColor = color;
            		alphaSlider.setProgress(Color.alpha(color));
            		Color.colorToHSV(color, currentColorHsv);
            		viewNewColor.setBackgroundColor(color);
            		viewSatVal.setHue(getHue());
            		moveCursor();
            		moveTarget();
            	}catch(Exception e){
            		Log.d("TextColor","Error: "+e.getMessage()+": "+s.toString());
            	}
            }
			public void afterTextChanged(Editable s) {}
        });
		
		if(viewHue != null) viewHue.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getY();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredHeight()) y = viewHue.getMeasuredHeight() - 0.001f; // to avoid looping from end to start.
					float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
					if (hue == 360.f) hue = 0.f;
					setHue(hue);

					// update view
					viewSatVal.setHue(getHue());
					moveCursor();
					viewNewColor.setBackgroundColor(getColor());
					rgbValue.setText(getRGB(getColor()));

					return true;
				}
				return false;
			}
		});
		if(viewSatVal != null) viewSatVal.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float x = event.getX(); // touch event are in dp units.
					float y = event.getY();

					if (x < 0.f) x = 0.f;
					if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
					if (y < 0.f) y = 0.f;
					if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

					setSat(1.f / viewSatVal.getMeasuredWidth() * x);
					setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

					// update view
					moveTarget();
					viewNewColor.setBackgroundColor(getColor());
					rgbValue.setText(getRGB(getColor()));

					return true;
				}
				return false;
			}
		});
		if(alphaCheck != null) alphaCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				setColor(getColor());
			}
		});
		if(alphaSlider != null) alphaSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				setColor(getColor());
			}
			public void onStartTrackingTouch(SeekBar arg0) {}
			public void onStopTrackingTouch(SeekBar arg0) {}
		});
		// move cursor & target on first draw
		ViewTreeObserver vto = view.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				moveCursor();
				moveTarget();
				view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}
	
	public void disableAlpha(boolean disable){
		if(alphaCheck != null) alphaCheck.setChecked(!disable);
		if(alphaCheck != null) alphaCheck.setVisibility(disable?View.GONE:View.VISIBLE);
		if(alphaSlider != null) alphaSlider.setVisibility(disable?View.GONE:View.VISIBLE);
	}
	
	@Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        View picker = view;
        setColor(getColor());
        ViewParent oldParent = picker.getParent();
        if (oldParent != v) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(picker);
            }
        }
    }
	
	protected View onCreateDialogView(){
		return view;
	}
	
	protected boolean needInputMethod() {
        return false;
    }
	
	protected void onAddColorPickerToDialogView(View dialogView, View picker) {
        ViewGroup container = (ViewGroup) dialogView;
        if (container != null) {
        	container.addView(picker, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
	
	protected void moveCursor() {
		float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
		if (y == viewHue.getMeasuredHeight()) y = 0.f;
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
		layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		;
		layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		;
		viewCursor.setLayoutParams(layoutParams);
	}

	protected void moveTarget() {
		float x = getSat() * viewSatVal.getMeasuredWidth();
		float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
		layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
		layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
		viewTarget.setLayoutParams(layoutParams);
	}

	private int getColor() {
		int color = Color.HSVToColor(currentColorHsv);
		if(alphaCheck != null && alphaCheck.isChecked()){
			color = Color.HSVToColor(alphaSlider.getProgress(),currentColorHsv);
		}
		mColor = color;
		return mColor;
	}
	
	public void setColor(int color) {
		mColor = color;
        persistInt(color);
        if(alphaSlider != null) alphaSlider.setProgress(Color.alpha(color));
		Color.colorToHSV(color, currentColorHsv);
		if(viewNewColor != null) viewNewColor.setBackgroundColor(color);
		if(rgbValue != null) rgbValue.setText(getRGB(color));
		if(viewSatVal != null) viewSatVal.setHue(getHue());
		moveCursor();
		moveTarget();
	}
	
	public void setInitialColor(int color){
		setColor(color);
		if(viewOldColor != null) viewOldColor.setBackgroundColor(color);
	}
	
	private String getRGB(int color){
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		int alpha = Color.alpha(color);
		String out = "#";
		if(alphaCheck != null && alphaCheck.isChecked()){
			out += ((alpha < 17)?"0":"")+Integer.toHexString(alpha);
		}
		out += ((red < 17)?"0":"")+Integer.toHexString(red)+((green < 17)?"0":"")+Integer.toHexString(green)+((blue < 17)?"0":"")+Integer.toHexString(blue);
		return out;
	}
	
	private float getHue() {
		return currentColorHsv[0];
	}

	private float getSat() {
		return currentColorHsv[1];
	}

	private float getVal() {
		return currentColorHsv[2];
	}

	private void setHue(float hue) {
		currentColorHsv[0] = hue;
	}

	private void setSat(float sat) {
		currentColorHsv[1] = sat;
	}

	private void setVal(float val) {
		currentColorHsv[2] = val;
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            int value = getColor();
            if (callChangeListener(value)) {
                setColor(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, mColor);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    	int state = Integer.parseInt(getPersistedInt(mColor)+"");
    	int def = 0;
    	if(defaultValue != null){
    		def = (Integer) defaultValue;
    	}
        setColor(restoreValue ? state : def);
    }
    
    
    
    
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.color = getColor();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        viewOldColor.setBackgroundColor(myState.color);
        setColor(myState.color);
    }
    
    private static class SavedState extends BaseSavedState {
        int color;
        
        public SavedState(Parcel source) {
            super(source);
            color = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(color);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    
}
