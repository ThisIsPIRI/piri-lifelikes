package com.thisispiri.lifelike;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SettingActivity extends AppCompatActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
	}
}
