package com.thisispiri.lifelike.andr;

import android.preference.PreferenceFragment;
import android.os.Bundle;

import com.thisispiri.lifelike.R;

public class SettingFragment extends PreferenceFragment {
	@Override public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
