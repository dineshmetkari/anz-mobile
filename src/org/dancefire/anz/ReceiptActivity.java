package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ReceiptActivity extends BaseActivity {
	private TextView m_banner;
	private TextView m_title;
	private TextView m_message;
	private Button m_button;
	private int m_message_type;
	
	public static final int RECEIPT_MESSAGE = 1;
	public static final int ERROR_MESSAGE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AnzMobileUtil.logger.fine("ReceiptActivity.onCreate()");
		setContentView(R.layout.receipt_layout);

		m_banner = (TextView) findViewById(R.id.banner_title);
		m_title = (TextView) findViewById(R.id.text_title);
		m_message = (TextView) findViewById(R.id.text_message);
		m_button = (Button) findViewById(R.id.button_ok);

		Intent intent = getIntent();
		m_message_type = intent.getIntExtra("type", RECEIPT_MESSAGE);
		if (m_message_type == ERROR_MESSAGE) {
			m_banner.setText("Error");
		} else {
			m_banner.setText("Receipt");
		}
		
		m_title.setText(intent.getStringExtra("title"));
		m_message.setText(intent.getStringExtra("message"));

		m_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AnzMobileUtil.logger.fine("ReceiptActivity.Button.onClick()");
				setResult(RESULT_OK);
				finish();
			}
		});
	}
}
