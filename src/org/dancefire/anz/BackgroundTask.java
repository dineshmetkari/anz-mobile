package org.dancefire.anz;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public abstract class BackgroundTask {
	protected Handler m_handler_finished;

	protected static final int BACKGROUND_ACTION = 1;

	public BackgroundTask() {
		m_handler_finished = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == BACKGROUND_ACTION) {
					onEnd();
				}
			}
		};
	}

	protected void onEnd() {
	}

	protected void onBegin() {
	}

	protected void onError(Throwable e) {
	}

	protected abstract void run() throws Throwable;

	public void start() {
		onBegin();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					run();
				} catch (Throwable e) {
					onError(e);
				} finally {
					m_handler_finished.sendEmptyMessage(BACKGROUND_ACTION);
				}
				return null;
			}
		}.execute();
	}
}
