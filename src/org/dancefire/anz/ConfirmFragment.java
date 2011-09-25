package org.dancefire.anz;

import java.util.logging.Level;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class ConfirmFragment extends BaseFragment {
	static interface IConfirm {
		void showReceipt(String title, String message);

		void showError(String title, String message);

		void showError(String title, Throwable e);
	}

	protected static final int DIALOG_CONFIRM = 1;
	protected static final int DIALOG_RECEIPT = 2;

	protected static final int ACTION_RECEIPT = 0x10;
	protected static final int ACTION_ERROR = 0x11;

	protected Handler m_handler_update;
	protected Handler m_handler_finish;

	protected BackgroundTask m_background_task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_handler_finish = new Handler() {
			public void handleMessage(android.os.Message msg) {
				onReceiptReturn();
			};
		};
	}

	protected void showDialog(final Bundle args) {
		AlertDialog.Builder builder = new Builder(getActivity());

		AnzMobileUtil.logger.finer("[showDialog] \t title: "
				+ args.getString("title"));
		AnzMobileUtil.logger.finer("[showDialog] \t message: "
				+ args.getString("message"));

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
								Util.showWaitingDialog(getActivity());
							};
						}.sendEmptyMessageDelayed(0, 500);
					}
				}).setNegativeButton("Cancel", new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		builder.create().show();
	}

	protected void onCommit() {
	}

	protected void onConfirm(Bundle args) {
	}

	protected void onReceiptReturn() {
		AnzMobileUtil.logger.fine("ConfirmActivity.onReceipt()");
	}

	protected void showConfirm(String title, String message) {
		ConfirmDialogFragment f = ConfirmDialogFragment.newInstance(title, message);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		f.show(ft, "confirm");
	}

	protected void showReceipt(String title, String message) {
		AnzMobileUtil.logger.fine("Show Receipt Activity.");
		ReceiptFragment f = ReceiptFragment.newInstance(title, message,
				ReceiptFragment.RECEIPT_MESSAGE);
		f.setOnReturnListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AnzMobileUtil.logger.fine("\t Calling onReceipt()");
				onClear();
				onReceiptReturn();
			}
		});

		getFragmentManager().beginTransaction().replace(getId(), f).commit();
	}

	protected void showError(String title, Throwable e) {
		AnzMobileUtil.logger.log(Level.SEVERE, e.getMessage(), e);
		showError(title, e.getMessage());
	}

	protected void showError(String title, String message) {
		getFragmentManager()
				.beginTransaction()
				.replace(
						getId(),
						ReceiptFragment.newInstance(title, message,
								ReceiptFragment.ERROR_MESSAGE)).commit();
	}

	protected void setRequestOnClickListener(View v, int resId) {
		v.findViewById(resId).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						onCommit();
					}
				});
	}

	protected void onClear() {

	}
}
