package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class BaseFragment extends Fragment {
	protected Handler m_handler_error;

	private static final int SELECTION_ACTION = 0x123;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_handler_error = Util.createErrorHandler(getFragmentManager());
	}
	
	protected void notifyError(String title, String message) {
		Util.notifyError(m_handler_error, title, message);
	}

	protected void notifyError(String title, Throwable e) {
		Util.notifyError(m_handler_error, title, e);
	}

	protected void toast(String text) {
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
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

	protected void setSelectionOnClickListener(View v,
			final ArrayList<String> list) {
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AnzMobileUtil.logger.fine("Selection Button:onClick()");
				if (true) {
					Intent intent = new Intent(getActivity(),
							SelectionActivity.class);
					intent.putExtra("list", list);
					startActivityForResult(intent, SELECTION_ACTION);
//				} else {
//					SelectionFragment f = SelectionFragment.newInstance(list);
//					f.setOnItemClickListner(new OnItemClickListener() {
//
//						@Override
//						public void onItemClick(AdapterView<?> parent,
//								View view, int position, long id) {
//							AnzMobileUtil.logger
//									.fine("Received onItemClick event");
//							AnzMobileUtil.logger
//									.fine("Selection:onItemSelected()");
//							onSelectionItemSelected(position);
//						}
//					});
//					f.setTargetFragment(BaseFragment.this, 0);
//					getFragmentManager().beginTransaction().add(getId(), f)
//							.addToBackStack(null).commit();
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		AnzMobileUtil.logger.fine("onActivityResult(): requestCode: " + requestCode
				+ ", resultCode: " + resultCode);
		if (requestCode == SELECTION_ACTION
				&& resultCode == Activity.RESULT_OK) {
			int position = data.getIntExtra("position", -1);
			AnzMobileUtil.logger.fine("position: " + position);
			onSelectionItemSelected(position);
		}
	}

	protected void onSelectionItemSelected(int position) {

	}
}
