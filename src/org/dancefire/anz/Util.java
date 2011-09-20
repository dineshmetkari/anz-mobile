package org.dancefire.anz;

import java.util.logging.Level;

import org.dancefire.anz.mobile.AnzMobileUtil;
import org.dancefire.anz.mobile.Page;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class Util {
	public static AlertDialog createAlert(final Activity activity,
			String title, String message) {
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle(title).setMessage(message).setCancelable(false)
				.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				});
		return builder.create();
	}

	public static AlertDialog createAlert(Activity activity, String title,
			Page page) {
		String message;
		if (page == null || !page.hasError()) {
			message = "";
		} else {
			message = page.getErrorString();
		}
		return createAlert(activity, title, message);
	}

	public static Handler createErrorHandler(final Activity activity) {
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Util.createAlert(activity, msg.getData().getString("title"),
						msg.getData().getString("message")).show();
			};
		};

		return handler;
	}

	public static void notifyError(Handler handler, String title,
			String message) {
		Message m = handler.obtainMessage();
		m.getData().putString("title", title);
		m.getData().putString("message", message);
		handler.sendMessage(m);
	}
	
	public static void notifyError(Handler handler, String title, Throwable e) {
		AnzMobileUtil.logger.log(Level.SEVERE, e.getMessage(),
				e);
		notifyError(handler, title, e.getMessage());
	}

	public static AlertDialog createWaitingDialog(Activity activity) {
		AlertDialog.Builder builder = new Builder(activity);
		View v = activity.getLayoutInflater().inflate(
				R.layout.waiting_item_layout, null);
		builder.setTitle("Loading...").setView(v);
		return builder.create();
	}
	
	private static AlertDialog waiting_dialog = null;
	public static void showWaitingDialog(Activity activity) {
		//	dismiss the old one before initiate a new one.
		dismissWaitingDialog();
		
		waiting_dialog = createWaitingDialog(activity);
		waiting_dialog.show();
	}
	
	public static void dismissWaitingDialog() {
		if (waiting_dialog != null) {
			waiting_dialog.dismiss();
			waiting_dialog = null;
		}
	}
}
