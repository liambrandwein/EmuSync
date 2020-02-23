package com.example.finalemucloud.configure;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.finalemucloud.R;

public class EmulatorConfigureFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
