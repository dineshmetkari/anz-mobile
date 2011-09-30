package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ReceiptFragment extends BaseFragment {
	
	private TextView m_banner;
	private TextView m_title;
	private TextView m_message;
	private Button m_button;
	private int m_message_type;
	
	private OnClickListener m_click_listner;

	public static final int RECEIPT_MESSAGE = 1;
	public static final int ERROR_MESSAGE = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		AnzMobileUtil.logger.fine("ReceiptActivity.onCreate()");
		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.receipt_layout, container, false);

		m_banner = (TextView) layout.findViewById(R.id.banner_title);
		m_title = (TextView) layout.findViewById(R.id.text_title);
		m_message = (TextView) layout.findViewById(R.id.text_message);
		m_button = (Button) layout.findViewById(R.id.button_ok);

		Bundle args = getArguments();
		m_message_type = args.getInt("type", RECEIPT_MESSAGE);
		if (m_message_type == ERROR_MESSAGE) {
			m_banner.setText("Error");
		} else {
			m_banner.setText("Receipt");
		}

		m_title.setText(args.getString("title"));
		m_message.setText(args.getString("message"));

		m_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AnzMobileUtil.logger.fine("ReceiptActivity.Button.onClick()");
				getFragmentManager().popBackStack();
				if (m_click_listner != null) {
					m_click_listner.onClick(v);
				}
			}
		});

		return layout;
	}
	
	public void setOnReturnListener(View.OnClickListener listener) {
		this.m_click_listner = listener;
	}

	public static ReceiptFragment newInstance(String title, String message) {
		return newInstance(title, message, RECEIPT_MESSAGE);
	}

	public static ReceiptFragment newInstance(String title, String message,
			int type) {
		Bundle args = new Bundle();
		args.putInt("type", type);
		args.putString("title", title);
		args.putString("message", message);
		ReceiptFragment fragment = new ReceiptFragment();
		fragment.setArguments(args);
		return fragment;
	}
}
