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

public class AccountsListFragment extends BaseFragment {
	private AccountListAdapter m_adapter;
	private ArrayList<Account> m_accounts;
	private ListView m_listview;
	private ViewGroup m_footer;

	public View onCreateView(android.view.LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		AnzMobileUtil.logger.fine("AccountListFragment.onCreateView()");
		m_handler_error = Util.createErrorHandler(getFragmentManager());

		View v = inflater.inflate(R.layout.account_list_layout, container,
				false);

		m_listview = (ListView) v.findViewById(android.R.id.list);

		m_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AnzMobileUtil.logger.fine("Selected account + [" + position
						+ "]");

				if (true) {
					Intent intent = new Intent(getActivity(),
							TransactionListActivity.class);
					intent.putExtra("index", position);
					startActivity(intent);
				} else {
					TransactionListFragment f = TransactionListFragment
							.newInstance(position);
					getFragmentManager().beginTransaction().replace(getId(), f)
							.addToBackStack(null).commit();
				}
			}
		});

		m_footer = (ViewGroup) inflater.inflate(R.layout.waiting_item_layout,
				null);
		m_listview.addFooterView(m_footer, null, false);

		return v;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		AnzMobileUtil.logger.fine("AccountListFragment.onActivityCreated()");
		AnzMobileUtil.logger.fine("m_accounts: " + this.m_accounts
				+ "\n m_adapter: " + m_adapter);
		// get the account list in background
		if (this.m_accounts == null) {
			runBackgroundTask();
		} else {
			showAccounts();
		}
	}

	@Override
	public void onResume() {
		AnzMobileUtil.logger.fine("AccountListFragment.onResume()");
		if (m_adapter != null) {
			m_adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onBackgroundBegin() {
		m_footer.getChildAt(0).setVisibility(View.VISIBLE);
		Util.showPendingDialog(getFragmentManager());
	}

	@Override
	protected void onBackground() throws Throwable {
		AnzMobileUtil.logger.fine("AccountListFragment.onBackground()");
		m_accounts = Apps.getBank().getAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		AnzMobileUtil.logger.fine("AccountListFragment.onBackgroundEnd()");

		showAccounts();

		Util.dismissPendingDialog();
	}

	private void showAccounts() {
		if (m_accounts != null) {
			m_adapter = new AccountListAdapter(getActivity(),
					R.layout.account_item_layout, m_accounts);
			m_listview.setAdapter(m_adapter);
			m_footer.getChildAt(0).setVisibility(View.GONE);
		}
	}
}