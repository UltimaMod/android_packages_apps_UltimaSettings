package com.ultima.settings.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class UltimaListPreference extends ListPreference {
    int mDefaultValue;
    
    public UltimaListPreference(Context context) {
        super(context);
        init(context, null);
    }

    public UltimaListPreference(Context context, AttributeSet attrs) {
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
