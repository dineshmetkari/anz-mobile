package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BasicDialog extends DialogFragment {
	public interface OnClickListener {
		void onClick();
	}

	public static class DumpClickListener implements OnClickListener {
		@Override
		public void onClick() {
		}
	}

	private String m_title;
	private String m_message;
	private int m_type;
	private OnClickListener m_positive_listener = null;
	private OnClickListener m_negative_listener = null;

	public static final int DIALOG_MESSAGE = 0;
	public static final int DIALOG_ERROR = 1;
	public static final int DIALOG_WAITING = 2;

	public static BasicDialog newMessageDialog(String title, String message) {
		return newInstance(DIALOG_MESSAGE, title, message,
				new DumpClickListener(), null);
	}

	public static BasicDialog newMessageDialog(String title, String message,
			OnClickListener positive_listener, OnClickListener negative_listener) {
		return newInstance(DIALOG_MESSAGE, title, message, positive_listener,
				negative_listener);
	}

	public static BasicDialog newErrorDialog(String title, String message,
			OnClickListener positive_listener) {
		return newInstance(DIALOG_ERROR, title, message, positive_listener,
				null);
	}

	public static BasicDialog newPendingDialog(String title) {
		return newInstance(DIALOG_WAITING, title, null, null, null);
	}

	public static BasicDialog newInstance(int type, String title,
			String message, OnClickListener positive_listener,
			OnClickListener negative_listener) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		args.putInt("type", type);
		BasicDialog f = new BasicDialog();
		f.setArguments(args);
		f.setOnPositiveListener(positive_listener);
		f.setOnNegativeListener(negative_listener);
		return f;
	}

	public void showDialog(FragmentManager fm) {
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag("dialog");
		if (f != null) {
			ft.remove(f);
		}
		ft.addToBackStack(null);
		this.show(ft, "dialog");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (m_type != DIALOG_WAITING) {
			setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_NoTitleBar_Fullscreen);
		}
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AnzMobileUtil.logger.finer("CBaseDialog.onCreateDialog()");
		m_title = getArguments().getString("title");
		m_message = getArguments().getString("message");
		m_type = getArguments().getInt("type");

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		if (m_type == DIALOG_WAITING) {
			View v = inflater.inflate(R.layout.waiting_item_layout, null);
			builder.setView(v);
			builder.setTitle(m_title);
		} else {
			View v = inflater.inflate(R.layout.basic_dialog_layout, null);

			((TextView) v.findViewById(android.R.id.title)).setText(m_title);
			((TextView) v.findViewById(android.R.id.message))
					.setText(m_message);

			Button positive_button = (Button)v.findViewById(R.id.button_ok);
			
			if (m_positive_listener == null) {
				positive_button.setVisibility(View.GONE);
			}else{
				int resid;
				if (m_negative_listener == null) {
					resid = android.R.string.ok;
				} else {
					resid = android.R.string.yes;
				}
				positive_button.setVisibility(View.VISIBLE);
				positive_button.setText(resid);
				positive_button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						dismiss();

						new BackgroundTask() {

							@Override
							protected void run() throws Throwable {
								m_positive_listener.onClick();
							}
						}.start();
					}
				});
			}

			Button negative_button = (Button)v.findViewById(R.id.button_cancel);
			if (m_negative_listener != null && m_type == DIALOG_MESSAGE) {
				negative_button.setVisibility(View.VISIBLE);
				negative_button.setText(android.R.string.cancel);
				negative_button.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						m_negative_listener.onClick();
						dismiss();
					}
				});
			} else {
				negative_button.setVisibility(View.GONE);
			}
			
			builder.setView(v);
		}

		return builder.create();
	}

	public void setOnPositiveListener(OnClickListener listener) {
		this.m_positive_listener = listener;
	}

	public void setOnNegativeListener(OnClickListener listener) {
		this.m_negative_listener = listener;
	}
}
