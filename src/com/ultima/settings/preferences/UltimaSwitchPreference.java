package com.ultima.settings.preferences;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class UltimaSwitchPreference extends SwitchPreference {
    int mDefaultValue;

    public UltimaSwitchPreference(Context context) {
        super(context, null, 0);
        init(context, null);
    }
    
    public UltimaSwitchPreference(Context context, AttributeSet attrs) {
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
