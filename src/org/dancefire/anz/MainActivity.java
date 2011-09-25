package org.dancefire.anz;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity {
	private ArrayList<View> m_buttons;
	private ArrayList<Fragment> m_fragments;
	private Fragment m_current_fragment = null;

	static final int ID_ACCOUNTS = 0;
	static final int ID_TRANSFER = 1;
	static final int ID_PAYANYONE = 2;
	static final int ID_BPAY = 3;
	static final int NUM_FRAGMENTS = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		m_buttons = new ArrayList<View>();
		m_buttons.add(findViewById(R.id.button_accounts));
		m_buttons.add(findViewById(R.id.button_transfer));
		m_buttons.add(findViewById(R.id.button_payanyone));
		m_buttons.add(findViewById(R.id.button_bpay));

		for (int i = 0; i < m_buttons.size(); ++i) {
			setOnClickListener(m_buttons.get(i), i);
		}

		setCurrentItem(0);
	}

	private void setOnClickListener(View v, final int position) {
		if (v != null) {
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setCurrentItem(position);
				}
			});
		}
	}

	private void setCurrentItem(int index) {
		Fragment f = getItem(index);
		if (f != null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			if (m_current_fragment != null) {
				ft.remove(m_current_fragment);
			}
			ft.add(R.id.content, f);
			ft.commit();
			m_current_fragment = f;

			// Set current button's background
			for (int i = 0; i < m_buttons.size(); ++i) {

				if (i == index) {
					m_buttons.get(i).setBackgroundResource(
							R.drawable.pin_button_background_bright);
				} else {
					m_buttons.get(i).setBackgroundResource(
							R.drawable.pin_background);
				}
			}
			

		}
	}

	private int getCount() {
		return NUM_FRAGMENTS;
	}

	private Fragment getItem(int index) {
		if (index >= 0 && index <= getCount()) {
			if (m_fragments == null) {
				m_fragments = createFragmentList();
			}

			Fragment f = m_fragments.get(index);
			if (f == null) {
				switch (index) {
				case ID_ACCOUNTS:
					f = new AccountsListFragment();
					break;
				case ID_TRANSFER:
					f = new TransferFragment();
					break;
				case ID_PAYANYONE:
					f = new PayAnyoneFragment();
					break;
				case ID_BPAY:
					f = new BPayFragment();
					break;
				}
				m_fragments.set(index, f);
			}
			return f;
		} else {
			return null;
		}
	}

	private ArrayList<Fragment> createFragmentList() {
		ArrayList<Fragment> list = new ArrayList<Fragment>(getCount());
		for (int i = 0; i < getCount(); ++i) {
			list.add(null);
		}
		return list;
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
			startActivity(new Intent(this, SettingFragment.class));
			break;
		case R.id.menu_about:
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
