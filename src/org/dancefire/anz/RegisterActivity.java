package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private EditText m_pin;
	private EditText m_userid;
	private EditText m_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_layout);

		m_pin = (EditText) findViewById(R.id.register_pin);
		m_userid = (EditText) findViewById(R.id.register_userid);
		m_password = (EditText) findViewById(R.id.register_password);
		
		findViewById(R.id.register_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						dispatch();
					}
				});
	}

	private void dispatch() {
		String warning_message = "";
		if (m_pin.length() != 4) {
			warning_message = "PIN should be 4 digits.";
		} else if (!AnzMobileUtil.isUseridValid(m_userid.getText().toString())) {
			warning_message = "CRN is not valid. It should contain 9, 15 or 16 digits.";
		} else if (!AnzMobileUtil.isPasswordValid(m_password.getText().toString())) {
			warning_message = "Password is not valid. It should contain 8 to 16 letters or digits";
		}

		if (warning_message.length() > 0) {
			Toast.makeText(this, warning_message, Toast.LENGTH_LONG);
		} else {
			// Save to preferences
			Settings.setPIN(m_pin.getText().toString());
			Settings.setUserid(m_userid.getText().toString());
			Settings.setPassword(m_password.getText().toString());
			// Go to main interface
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
	}
}
