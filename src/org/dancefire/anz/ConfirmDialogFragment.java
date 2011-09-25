package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;

public class ConfirmDialogFragment extends DialogFragment {
	
	public static interface OnConfirmListener {
		void onConfirm(Bundle args);
	}
	
	private String m_title;
	private String m_message;
	private OnConfirmListener m_confirm_listener;
	
	public static ConfirmDialogFragment newInstance(String title, String message) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		ConfirmDialogFragment f = new ConfirmDialogFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_title = getArguments().getString("title");
		m_message = getArguments().getString("message");
		m_confirm_listener = null;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity()).setTitle(m_title)
		.setMessage(m_message)
		.setPositiveButton("Confirm", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AnzMobileUtil.logger.info("[onClick]");

				new BackgroundTask() {

					@Override
					protected void run() throws Throwable {
						if (m_confirm_listener == null) {
							((OnConfirmListener)getActivity()).onConfirm(getArguments());
						} else {
							m_confirm_listener.onConfirm(getArguments());
						}
					}

					@Override
					protected void onEnd() {
						AnzMobileUtil.logger.info("[off]");
						Util.dismissWaitingDialog();
					};
				}.start();

				new Handler() {
					public void handleMessage(android.os.Message msg) {
						AnzMobileUtil.logger.info("[on]");
						Util.showWaitingDialog(getActivity());
					};
				}.sendEmptyMessageDelayed(0, 500);
			}
		}).setNegativeButton("Cancel", new Dialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).create();
	}
	
	public void setOnConfirmListener(OnConfirmListener listner) {
		this.m_confirm_listener = listner;
	}
}
