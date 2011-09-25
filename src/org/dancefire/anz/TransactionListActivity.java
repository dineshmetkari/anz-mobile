package org.dancefire.anz;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class TransactionListActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// Initialization
			int position = getIntent().getIntExtra("index", -1);

			if (position >= 0) {
				TransactionListFragment f = TransactionListFragment
						.newInstance(position);
				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, f).commit();
			} else {
				finish();
			}
		}
	}
}
