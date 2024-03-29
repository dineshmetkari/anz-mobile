package org.dancefire.anz;

import java.util.logging.Level;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobileUtil;
import org.dancefire.anz.mobile.PageErrorException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class TransactionListFragment extends BaseFragment {
	private TransactionListAdapter m_adapter;
	private Account m_account;
	private ListView m_listview;
	private ViewGroup m_header;
	private ViewGroup m_footer;
	private TransactionType m_current_type = TransactionType.TRANSACTION_NORMAL;
	private boolean m_running = false;
	private int m_index;

	public static TransactionListFragment newInstance(int index) {
		Bundle args = new Bundle();
		args.putInt("index", index);
		TransactionListFragment f = new TransactionListFragment();
		f.setArguments(args);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get the transaction list of specified account
		m_index = getArguments().getInt("index", -1);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.transaction_list_layout, container, false);

		m_listview = (ListView) v.findViewById(android.R.id.list);

		m_header = (ViewGroup) inflater.inflate(R.layout.waiting_item_layout, null);
		m_footer = (ViewGroup) inflater.inflate(R.layout.waiting_item_layout, null);
		m_listview.addHeaderView(m_header);
		m_listview.addFooterView(m_footer);
		m_header.getChildAt(0).setVisibility(View.GONE);
		m_footer.getChildAt(0).setVisibility(View.GONE);

		// Scroll to the bottom then load more.
		m_listview.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				boolean show_more = firstVisibleItem + visibleItemCount
						+ visibleItemCount > totalItemCount;
				if (show_more && m_account.current_page != null
						&& m_account.current_page.link_more != null
						&& m_account.current_page.link_more.length() > 0) {
					load_transaction(TransactionType.TRANSACTION_MORE);
				}
			}
		});

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Util.showPendingDialog(getFragmentManager());
		load_transaction(TransactionType.TRANSACTION_NORMAL);
	}

	@Override
	public void onResume() {
		if (m_adapter != null) {
			m_adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	private static enum TransactionType {
		TRANSACTION_NORMAL, TRANSACTION_TODAY, TRANSACTION_MORE
	};

	@Override
	protected void onBackgroundBegin() {
		m_running = true;
		switch (m_current_type) {
		case TRANSACTION_TODAY:
			m_header.getChildAt(0).setVisibility(View.VISIBLE);
			break;
		case TRANSACTION_MORE:
		case TRANSACTION_NORMAL:
			m_footer.getChildAt(0).setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	protected void onBackgroundEnd() {
		if (getActivity() == null) {
			return;
		}
		
		if (m_adapter != null) {
			m_adapter.notifyDataSetChanged();
		}
		switch (m_current_type) {
		case TRANSACTION_TODAY:
			m_header.getChildAt(0).setVisibility(View.GONE);
			break;
		case TRANSACTION_MORE:
		case TRANSACTION_NORMAL:
			m_footer.getChildAt(0).setVisibility(View.GONE);
			break;
		}

		Util.dismissPendingDialog();

		m_running = false;
		if (m_current_type == TransactionType.TRANSACTION_NORMAL) {
			m_adapter = new TransactionListAdapter(getActivity(),
					R.layout.transaction_item_layout, m_account.transactions);
			m_listview.setAdapter(m_adapter);
			load_transaction(TransactionType.TRANSACTION_TODAY);
		}
	}

	@Override
	protected void onBackground() throws Throwable {
		if (m_index >= 0) {
			m_account = Apps.getBank().getAccount(m_index);
			try {
				switch (m_current_type) {
				case TRANSACTION_NORMAL:
					Apps.getBank().getTransactions(m_account);
					break;
				case TRANSACTION_TODAY:
					try {
						Apps.getBank().getTodayTransaction(m_account);
					} catch (PageErrorException ex) {
						AnzMobileUtil.logger
								.log(Level.WARNING, "Exception", ex);
					}
					break;
				case TRANSACTION_MORE:
					Apps.getBank().getMoreTransactions(m_account);
					break;
				}
			} catch (final PageErrorException e) {
				notifyError("Cannot retrieve transactions.", e);
			}
		} else {
			toast("Selected invalid account.");
			getFragmentManager().popBackStack();
		}
	}

	private void load_transaction(TransactionType type) {
		if (!m_running) {
			m_current_type = type;
			runBackgroundTask();
		}
	}

}
