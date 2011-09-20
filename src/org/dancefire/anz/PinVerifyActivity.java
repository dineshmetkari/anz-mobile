package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobile;
import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class PinVerifyActivity extends Activity {

	AnzMobile m_bank = null;
	private TextView m_pin_text;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setView() {
		setContentView(R.layout.pin_layout);
		this.m_pin_text = (TextView) findViewById(R.id.pin_text);

		// Add buttons
		// TODO: should adjust for tablet
		TableLayout table = (TableLayout) findViewById(R.id.pin_table);

		LinearLayout.LayoutParams frame_lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		table.setLayoutParams(frame_lp);
		table.setStretchAllColumns(true);

		TableLayout.LayoutParams table_lp = new TableLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		table_lp.gravity = Gravity.CENTER;
		table_lp.weight = 1;

		TableRow.LayoutParams row_lp = new TableRow.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		row_lp.gravity = Gravity.CENTER;
		row_lp.weight = 1;

		for (int r = 0; r < 4; ++r) {
			TableRow row = new TableRow(this);
			for (int b = 0; b < 3; ++b) {
				int n = r * 3 + b;
				View button = generateButton(n);
				row.addView(button);// , row_lp);
			}
			table.addView(row, table_lp);
		}
	}

	private void verify(String pin) {
		if (pin.equals(Settings.getPIN())) {
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		} else {
			// Pin is wrong
			Toast.makeText(this, "PIN is wrong, please try it again.",
					Toast.LENGTH_SHORT).show();
			AnzMobileUtil.logger.info("Input:[" + pin + "], Correct:["
					+ Settings.getPIN() + "]");
			this.m_pin_text.setText("");
		}
	}

	private void buttonPressed(int button_index) {
		String pin = this.m_pin_text.getText().toString();

		switch (BUTTON_TYPES[button_index]) {
		case BUTTON_NUMBER:
			if (pin.length() == 4) {
				// check the pin and go to account list activity if it's
				// correct.
				Toast.makeText(this, "PIN's length should be 4.",
						Toast.LENGTH_SHORT).show();
			} else if (pin.length() < 4) {
				pin = pin + BUTTON_TITLES[button_index];
			}

			this.m_pin_text.setText(pin);

			if (pin.length() == 4) {
				verify(pin);
			}
			break;
		case BUTTON_DEL:
			if (pin.length() > 0) {
				pin = pin.substring(0, pin.length() - 1);
				this.m_pin_text.setText(pin);
			}
			break;
		case BUTTON_REGISTER:
			// go to register page
			startActivity(new Intent(this, RegisterActivity.class));
			finish();
			break;
		}
	}

	private enum ButtonType {
		BUTTON_NUMBER, BUTTON_REGISTER, BUTTON_DEL
	}

	private static String[] BUTTON_TITLES = { "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "Reg", "0", "Del" };

	private static String[] BUTTON_SUBTITLES = { "", "ABC", "DEF", "GHI",
			"JKL", "MNO", "PQRS", "TUV", "WXYZ", "", "", "" };

	private static ButtonType[] BUTTON_TYPES = { ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_NUMBER, ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_NUMBER, ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_NUMBER, ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_NUMBER, ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_REGISTER, ButtonType.BUTTON_NUMBER,
			ButtonType.BUTTON_DEL };

	private static final int BUTTON_ZERO_INDEX = 10;
	private static final int BUTTON_DEL_INDEX = 11;
	private static final int BUTTON_REG_INDEX = 9;

	/**
	 * Generate a button by specified index
	 * 
	 * @param button_index
	 * @return
	 */
	private View generateButton(final int button_index) {
		View button = getLayoutInflater().inflate(R.layout.pin_button_layout,
				null);

		TextView text_title = (TextView) button.findViewById(R.id.title);
		text_title.setTypeface(Apps.getCardFont());

		TextView text_subtitle = (TextView) button.findViewById(R.id.subtitle);
		text_title.setText(BUTTON_TITLES[button_index]);
		String subtitle = BUTTON_SUBTITLES[button_index];
		if (subtitle.length() > 0) {
			text_subtitle.setText(subtitle);
		} else {
			text_subtitle.setVisibility(View.GONE);
		}

		button.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonPressed(button_index);
			}
		});
		return button;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			buttonPressed(BUTTON_ZERO_INDEX);
			return true;
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			buttonPressed(keyCode - KeyEvent.KEYCODE_1);
			return true;
		case KeyEvent.KEYCODE_DEL:
			buttonPressed(BUTTON_DEL_INDEX);
			return true;
		case KeyEvent.KEYCODE_AT:
			buttonPressed(BUTTON_REG_INDEX);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
