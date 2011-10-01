package org.dancefire.anz;

import java.util.logging.Level;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

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

	protected void onCommit() {
	}

	public void onConfirm() {
	}

	protected void onReceiptReturn() {
		AnzMobileUtil.logger.fine("ConfirmActivity.onReceipt()");
	}

	protected void showConfirm(String title, String message) {
		BasicDialog d = BasicDialog.newMessageDialog(title, message, new BasicDialog.OnClickListener() {
			
			@Override
			public void onClick() {
				AnzMobileUtil.logger.finer("[on]");
				Util.showPendingDialog(getFragmentManager());
				onConfirm();
			}
		}, new BasicDialog.DumpClickListener());
		d.showDialog(getFragmentManager());
	}

	protected void showReceipt(String title, String message) {
		AnzMobileUtil.logger.finer("[off]");
		Util.dismissPendingDialog();

		AnzMobileUtil.logger.fine("Show Receipt Activity.");
		
		BasicDialog d = BasicDialog.newMessageDialog(title, message, new BasicDialog.OnClickListener() {
			
			@Override
			public void onClick() {
				AnzMobileUtil.logger.fine("\t Calling onReceipt()");
				//onClear();
				onReceiptReturn();
			}
		}, null);
		d.showDialog(getFragmentManager());
	}

	protected void showError(String title, Throwable e) {
		AnzMobileUtil.logger.log(Level.SEVERE, e.getMessage(), e);
		showError(title, e.getMessage());
	}

	protected void showError(String title, String message) {
		AnzMobileUtil.logger.finer("[off]");
		Util.dismissPendingDialog();

		BasicDialog d = BasicDialog.newErrorDialog(title, message, new BasicDialog.DumpClickListener());
		d.showDialog(getFragmentManager());
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
