package org.dancefire.anz;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		addTab("account_list", "Accounts", android.R.drawable.ic_menu_agenda,
				AccountsListActivity.class);
		addTab("transfer", "Transfer", android.R.drawable.ic_menu_rotate,
				TransferActivity.class);
		addTab("payanyone", "Pay Anyone", android.R.drawable.ic_menu_myplaces,
				PayAnyoneActivity.class);
		addTab("bpay", "BPay", R.drawable.bpay, BPayActivity.class);

		getTabHost().setCurrentTab(0);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:
			// Logout
			Apps.getBank().logout();
			finish();
			break;
		case R.id.menu_setting:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		case R.id.menu_about:
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void addTab(String tag, String indicator, int resid, Class<?> cls) {
		TabHost host = getTabHost();
		TabHost.TabSpec spec = getTabHost().newTabSpec(tag)
				.setIndicator(indicator, getResources().getDrawable(resid))
				.setContent(new Intent(this, cls));
		host.addTab(spec);
	}

}
