package org.dancefire.anz;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class ConfirmActivity extends BaseActivity {
	protected static final int DIALOG_CONFIRM = 1;
	protected static final int DIALOG_RECEIPT = 2;
	protected static final int UPDATE = 1;
	protected static final int SHOW_RECEIPT = 1;

	protected Handler m_handler_update;
	protected Handler m_handler_finish;

	protected BackgroundTask m_background_task;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_handler_finish = new Handler() {
			public void handleMessage(android.os.Message msg) {
				onReceiptReturn();
			};
		};
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_CONFIRM:
		case DIALOG_RECEIPT:
			AlertDialog alert = (AlertDialog) dialog;
			alert.setTitle(args.getString("title"));
			alert.setMessage(args.getString("message"));
			AnzMobileUtil.logger.finer("[onPrepareDialog] \t title: "
					+ args.getString("title"));
			AnzMobileUtil.logger.finer("[onPrepareDialog] \t message: "
					+ args.getString("message"));
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, final Bundle args) {
		AlertDialog.Builder builder = new Builder(this);

		AnzMobileUtil.logger.finer("[onCreateDialog] \t title: "
				+ args.getString("title"));
		AnzMobileUtil.logger.finer("[onCreateDialog] \t message: "
				+ args.getString("message"));

		switch (id) {
		case DIALOG_CONFIRM:

			builder.setTitle(args.getString("title"))
					.setMessage(args.getString("message"))
					.setPositiveButton("Confirm", new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							AnzMobileUtil.logger.info("[onClick]");

							new BackgroundTask() {

								@Override
								protected void run() throws Throwable {
									onConfirm(args);
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
									Util.showWaitingDialog(ConfirmActivity.this);
								};
							}.sendEmptyMessageDelayed(0, 500);
						}
					})
					.setNegativeButton("Cancel", new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			break;
		case DIALOG_RECEIPT:
			builder.setTitle(args.getString("title"))
					.setMessage(args.getString("message"))
					.setPositiveButton(android.R.string.ok,
							new Dialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									m_handler_finish.sendEmptyMessage(0);
								}
							});
			break;
		}

		return builder.create();
	}

	protected void onCommit() {
	}

	protected void onConfirm(Bundle args) {
	}

	protected void onReceiptReturn() {
		AnzMobileUtil.logger.fine("ConfirmActivity.onReceipt()");
	}
	
	protected void showConfirm(String title, String message) {
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		showDialog(DIALOG_CONFIRM, args);
	}

	protected void showReceipt(String title, String message) {
		AnzMobileUtil.logger.fine("Show Receipt Activity.");
		Intent intent = new Intent(this, ReceiptActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("message", message);
		startActivityForResult(intent, SHOW_RECEIPT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		AnzMobileUtil.logger.fine("ConfirmActivity.onActivityResult()");
		if (requestCode == SHOW_RECEIPT) {
			AnzMobileUtil.logger.fine("\t Calling onReceipt()");
			onClear();
			onReceiptReturn();
		}
	}

	protected void setRequestOnClickListener(int resId) {
		findViewById(resId).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onCommit();
			}
		});
	}

	protected void onClear() {

	}
}
