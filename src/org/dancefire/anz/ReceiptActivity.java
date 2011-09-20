package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ReceiptActivity extends BaseActivity {
	private TextView m_title;
	private TextView m_message;
	private Button m_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AnzMobileUtil.logger.fine("ReceiptActivity.onCreate()");
		setContentView(R.layout.receipt_layout);

		m_title = (TextView) findViewById(R.id.text_title);
		m_message = (TextView) findViewById(R.id.text_message);
		m_button = (Button) findViewById(R.id.button_ok);

		Intent intent = getIntent();
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
