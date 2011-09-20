package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobileUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AccountsListActivity extends BaseActivity {
	private AccountListAdapter m_adapter;
	private ArrayList<Account> m_accounts;
	private ListView m_listview;
	private ViewGroup m_footer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_handler_error = Util.createErrorHandler(this);

		setContentView(R.layout.account_list_layout);

		m_listview = (ListView) findViewById(android.R.id.list);

		m_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AnzMobileUtil.logger.fine("Selected account + [" + position
						+ "]");
				startActivity(new Intent(AccountsListActivity.this,
						TransactionListActivity.class).putExtra(
						"account_index", position));
			}
		});

		m_footer = (ViewGroup) getLayoutInflater().inflate(R.layout.waiting_item_layout,
				null);
		m_listview.addFooterView(m_footer, null, false);

		// get the account list in background
		if (this.m_accounts == null) {
			runBackgroundTask();
		}

	}

	@Override
	protected void onResume() {
		if (m_adapter != null) {
			m_adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onBackgroundBegin() {
		m_footer.getChildAt(0).setVisibility(View.VISIBLE);
		Util.showWaitingDialog(this);
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		if (m_accounts != null) {
			m_adapter = new AccountListAdapter(this,
					R.layout.account_item_layout, m_accounts);
			m_listview.setAdapter(m_adapter);

		}

		m_footer.getChildAt(0).setVisibility(View.GONE);

		Util.dismissWaitingDialog();
	}
}