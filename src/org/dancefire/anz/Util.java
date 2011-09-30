package org.dancefire.anz;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class Util {
	public static Handler createErrorHandler(final FragmentManager fm) {
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				BasicDialog d = BasicDialog.newMessageDialog(msg.getData().getString("title"), msg.getData().getString("message"));
				d.showDialog(fm);
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
	
	private static BasicDialog global_pending_dialog = null;
	private static ReentrantLock global_pending_dialog_lock = new ReentrantLock();
	
	public static void showPendingDialog(FragmentManager fm) {
		dismissPendingDialog();
		global_pending_dialog_lock.lock();
		global_pending_dialog = BasicDialog.newPendingDialog("Loading...");
		FragmentTransaction ft = fm.beginTransaction();
		Fragment f = fm.findFragmentByTag("waiting");
		if (f != null) {
			ft.remove(f);
		}
		ft.addToBackStack(null);
		global_pending_dialog.show(ft, "waiting");
		global_pending_dialog_lock.unlock();
	}

	public static void dismissPendingDialog() {
		global_pending_dialog_lock.lock();
		if (global_pending_dialog != null && global_pending_dialog.getDialog() != null) {
			Dialog d = global_pending_dialog.getDialog();
			d.cancel();
			global_pending_dialog = null;
		}
		global_pending_dialog_lock.unlock();
	}
	
	
}
