package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobile;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

public class Apps extends Application {
	private static Context m_context;
	private static AnzMobile m_bank = null;
	private static Typeface m_credit_card_font = null;

	@Override
	public void onCreate() {
		m_context = getApplicationContext();
		super.onCreate();
	}

	public static Context getContext() {
		return m_context;
	}

	public static AnzMobile getBank() {
		if (m_bank == null || !m_bank.isValid()) {
			String userid = Settings.getUserid();
			String password = Settings.getPassword();
			m_bank = new AnzMobile(userid, password);
		}
		return m_bank;
	}

	public static Typeface getCardFont() {
		if (m_credit_card_font == null) {
			m_credit_card_font = Typeface.createFromAsset(Apps.getContext()
					.getAssets(), "halter.ttf");
		}

		return m_credit_card_font;
	}
}
