package com.ultima.settings.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class UltimaCheckBoxPreference extends CheckBoxPreference {    
    int mDefaultValue;

    public UltimaCheckBoxPreference(Context context) {
        super(context, null, 0);
        init(context, null);
    }
    
    public UltimaCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDefaultValue = attrs.getAttributeIntValue(null, "defaultValue", 0);
    }

    public int getDefaultValue(){
        return mDefaultValue;
    }
}