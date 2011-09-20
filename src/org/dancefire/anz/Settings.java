package org.dancefire.anz;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	public static final String PIN = "pin";
	public static final String USERID = "userid";
	public static final String PASSWORD = "password";

	private static SharedPreferences m_pref;

	public static SharedPreferences getSettings() {
		if (m_pref == null) {
			m_pref = PreferenceManager.getDefaultSharedPreferences(Apps.getContext());
		}
		return m_pref;
	}
	
	public static String getPIN() {
		return getSettings().getString(PIN, "");
	}
	
	public static void setPIN(String pin) {
		getSettings().edit().putString(PIN, pin).commit();
	}
	
	public static String getUserid() {
		return getSettings().getString(USERID, "");
	}
	
	public static void setUserid(String userid) {
		getSettings().edit().putString(USERID, userid).commit();
	}
	
	public static String getPassword() {
		return getSettings().getString(PASSWORD, "");
	}
	
	public static void setPassword(String password) {
		getSettings().edit().putString(PASSWORD, password).commit();
	}
}
