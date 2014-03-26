package com.ultima.settings.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class UltimaCheckboxPreference extends CheckBoxPreference {    
    int mDefaultValue;

    public UltimaCheckboxPreference(Context context) {
        super(context, null, 0);
        init(context, null);
    }
    
    public UltimaCheckboxPreference(Context context, AttributeSet attrs) {
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
