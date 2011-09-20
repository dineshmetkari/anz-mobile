package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ANZMobileActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrance_layout);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check configuration
		String pin = Settings.getPIN();
		String userid = Settings.getUserid();
		String password = Settings.getPassword();
		
		if (pin.length() != 4
				|| !org.dancefire.anz.mobile.AnzMobileUtil.isUseridValid(userid)
				|| !org.dancefire.anz.mobile.AnzMobileUtil.isPasswordValid(password)) {
			AnzMobileUtil.logger.warning("pin: [" + pin + "] \t userid: [" + userid
					+ "] \t password: [" + password + "]");
			// Call Registration Activity
			startActivity(new Intent(this, RegisterActivity.class));
		} else {
			startActivity(new Intent(this, PinVerifyActivity.class));
		}
		finish();
	}
}