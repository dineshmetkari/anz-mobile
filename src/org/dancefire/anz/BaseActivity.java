package org.dancefire.anz;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class BaseActivity extends Activity {
	protected Handler m_handler_error;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_handler_error = Util.createErrorHandler(this);
	}

	protected void notifyError(String title, String message) {
		Util.notifyError(m_handler_error, title, message);
	}

	protected void notifyError(String title, Throwable e) {
		Util.notifyError(m_handler_error, title, e);
	}

	protected void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
	
	protected void onBackgroundBegin() {

	}

	protected void onBackground() throws Throwable {

	}

	protected void onBackgroundEnd() {

	}

	protected void runBackgroundTask() {
		new BackgroundTask() {

			@Override
			protected void run() throws Throwable {
				onBackground();
			}

			@Override
			protected void onBegin() {
				onBackgroundBegin();
			}

			@Override
			protected void onEnd() {
				onBackgroundEnd();
			}

			@Override
			protected void onError(Throwable e) {
				notifyError("Error", e);
			}
		}.start();
	}
}
