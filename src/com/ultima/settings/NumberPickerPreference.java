package com.ultima.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.ultima.settings.R;

public class NumberPickerPreference extends DialogPreference {

    private int mMin;
    private int mMax;

    private NumberPicker mNumberPicker;
    
    private ContentResolver cr;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        cr = context.getContentResolver();
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPreference, 0, 0);
        mMin = a.getInt(R.styleable.NumberPreference_min, 1);
        mMax = a.getInt(R.styleable.NumberPreference_max, 3);
        if(mMin<0 || mMin > mMax) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected View onCreateDialogView() {
        mNumberPicker = new android.widget.NumberPicker(getContext());
        return mNumberPicker;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle(getTitle())
            .setCancelable(true);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        NumberPicker p = (NumberPicker)view;
        p.setMinValue(mMin);
        p.setMaxValue(mMax);
        p.setValue(getPersistedInt(mMin));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        Settings.System.putInt(cr, this.getKey(), mNumberPicker.getValue());
    }

    @Override
    protected void onSetInitialValue (boolean restorePersistedValue, Object defaultValue) {
        int value = mMin;
        if (restorePersistedValue) {
            value = getPersistedInt(0);
        } else {
            Integer defVal = (Integer) defaultValue;
            if (defVal != null) {
                value = defVal;
            }
        }
        if(value < mMin) {
            value = mMin;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

}
