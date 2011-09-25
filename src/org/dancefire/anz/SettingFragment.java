package org.dancefire.anz;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingFragment extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
	}
}
