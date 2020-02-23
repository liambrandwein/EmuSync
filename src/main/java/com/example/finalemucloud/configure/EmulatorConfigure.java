package com.example.finalemucloud.configure;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceScreen;

import com.example.finalemucloud.R;

public class EmulatorConfigure extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new EmulatorConfigureFragment())
                .commit();
        setContentView(R.layout.settings_holder);

    }
}
