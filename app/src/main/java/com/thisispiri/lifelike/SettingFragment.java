package com.thisispiri.lifelike;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class SettingFragment extends PreferenceFragment {
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
