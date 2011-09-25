package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.AnzMobileUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectionActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnzMobileUtil.logger.fine("SelectionActivity.onCreate()");
		
		if (savedInstanceState == null) {
			ArrayList<String> list = getIntent()
					.getStringArrayListExtra("list");
			if (list != null) {
				AnzMobileUtil.logger.fine("SelectionActivity.onCreate(): starting fragment...");
				SelectionFragment f = SelectionFragment.newInstance(list);
				f.setOnItemClickListner(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						AnzMobileUtil.logger.fine("SelectionActivity.onItemClick(" + position + ")");
						Intent data = new Intent();
						data.putExtra("position", position);
						setResult(RESULT_OK, data);
						finish();
					}
				});
				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, f).commit();
			} else {
				finish();
			}
		}
	}
}
