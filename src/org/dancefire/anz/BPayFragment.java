package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobile;
import org.dancefire.anz.mobile.AnzMobileUtil;
import org.dancefire.anz.mobile.BPayAccount;
import org.dancefire.anz.mobile.BPayData;
import org.dancefire.anz.mobile.BPayReceiptPage;
import org.dancefire.anz.mobile.Formatter;
import org.dancefire.anz.mobile.PageErrorException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Gallery;

public class BPayFragment extends ConfirmFragment {
	static class ViewHolder {
		private Gallery from;
		private EditText biller_name;
		private EditText biller_code;
		private EditText biller_reference;
		private EditText biller_description;
		private EditText amount;
		private View selection_button;
	}

	private ArrayList<Account> m_accounts;
	private ArrayList<BPayAccount> m_billers;
	private AccountListAdapter m_adapter;
	private ViewHolder m_holder;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.bpay_layout, container, false);

		m_holder = new ViewHolder();
		m_holder.from = (Gallery) v.findViewById(R.id.bpay_account_from);
		m_holder.biller_code = (EditText) v.findViewById(R.id.bpay_biller_code);
		m_holder.biller_name = (EditText) v.findViewById(R.id.bpay_biller_name);
		m_holder.biller_reference = (EditText) v
				.findViewById(R.id.bpay_biller_reference);
		m_holder.biller_description = (EditText) v
				.findViewById(R.id.bpay_biller_description);
		m_holder.amount = (EditText) v.findViewById(R.id.bpay_amount);
		m_holder.selection_button = v.findViewById(R.id.selection_button);

		m_holder.amount.setTypeface(Apps.getCardFont());

		setRequestOnClickListener(v, R.id.bpay_button);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (m_accounts == null || m_billers == null) {
			runBackgroundTask();
		} else {
			onBackgroundEnd();
		}
		// onLoadSavedState(savedInstanceState);
	}

	@Override
	protected void onBackgroundBegin() {
		Util.showPendingDialog(getFragmentManager());
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
		m_billers = Apps.getBank().getBPayAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		m_adapter = new AccountListAdapter(getActivity(),
				R.layout.account_item_layout, m_accounts);
		m_holder.from.setAdapter(m_adapter);

		if (m_holder.selection_button != null) {
			final ArrayList<String> names = new ArrayList<String>();
			names.add("<new biller>");
			if (m_billers != null) {
				for (BPayAccount biller : m_billers) {
					names.add(Formatter.toString(biller));
				}
			}

			AnzMobileUtil.logger.fine("setSelectionOnClickListener()");
			setSelectionOnClickListener(m_holder.selection_button, names);

			Util.dismissPendingDialog();
		}
	}

	// private void onLoadSavedState(Bundle inState) {
	// if (inState != null) {
	// AnzMobileUtil.logger.fine("onLoadSavedState()");
	// m_holder.biller_name.setText(inState.getString("biller_name"));
	// m_holder.biller_code.setText(inState.getString("biller_code"));
	// m_holder.biller_description.setText(inState
	// .getString("biller_description"));
	// m_holder.biller_reference.setText(inState
	// .getString("biller_reference"));
	// m_holder.amount.setText(inState.getString("amount_text"));
	// m_holder.from.setSelection(inState.getInt("from"));
	// }
	// }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		AnzMobileUtil.logger.fine("onSaveInstanceState()");

		outState.putString("biller_name", m_holder.biller_name.getText()
				.toString());
		outState.putString("biller_code", m_holder.biller_code.getText()
				.toString());
		outState.putString("biller_description", m_holder.biller_description
				.getText().toString());
		outState.putString("biller_reference", m_holder.biller_reference
				.getText().toString());
		outState.putInt("from", m_holder.from.getSelectedItemPosition());
		outState.putString("amount_text", m_holder.amount.getText().toString());
	}

	protected BPayData getData(ViewHolder holder) {
		int from = holder.from.getSelectedItemPosition();

		double amount = 0;

		String amount_text = holder.amount.getText().toString().trim();
		if (amount_text.length() > 0) {
			try {
				amount = Double.parseDouble(holder.amount.getText().toString());
			} catch (NumberFormatException ex) {
				notifyError("Cannot parse [" + m_holder.amount.getText()
						+ "] to double.", ex);
				toast("Invalid amount.");
			}
		}

		BPayAccount biller = new BPayAccount();
		biller.name = holder.biller_name.getText().toString();
		biller.code = holder.biller_code.getText().toString();
		biller.description = holder.biller_description.getText().toString();
		biller.reference = holder.biller_reference.getText().toString();

		String warning_message = "";
		if (amount <= 0.01) {
			warning_message = "Invalid transfer amount. The minimum amount is $0.01";
		} else if (amount > m_accounts.get(from).available_balance) {
			warning_message = "There is not sufficient funds ("
					+ Formatter.BALANCE_FORMAT.format(amount)
					+ ") in the from accounts.\n\n"
					+ Formatter.toString(m_accounts.get(from));
		} else if (biller.code.length() == 0 || biller.reference.length() == 0) {
			warning_message = "Biller code and the reference should not be empty.";
		}

		if (warning_message.length() > 0) {
			toast(warning_message);
			return null;
		} else {
			BPayData data = new BPayData();
			data.from = from;
			data.to = biller;
			data.amount = amount;
			return data;
		}
	}

	@Override
	protected void onCommit() {
		BPayData data = getData(m_holder);
		if (data != null) {
			showConfirm("Are you sure to pay?",
					Formatter.toString(data, m_accounts));
		}
	}

	@Override
	public void onConfirm() {
		BPayData data = getData(m_holder);
		if (data != null) {
			AnzMobile bank = Apps.getBank();
			try {
				// Do BPay
				Account account_from = bank.getAccount(data.from);
				BPayReceiptPage page = bank.payBills(account_from, data.to,
						data.amount);

				// Show Receipt
				if (page.hasError()) {
					showError("Pay Bill is failed.", page.getErrorString());
				} else {
					showReceipt("Pay Bill is successed.",
							Formatter.toString(page));
				}
			} catch (PageErrorException ex) {
				showError("Pay Bill is failed.", ex);
			}

		}
	}

	@Override
	public void onResume() {
		AnzMobileUtil.logger.fine("onResume()");
		if (m_adapter != null) {
			m_adapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onClear() {
		m_holder.amount.setText("");
	}

	@Override
	protected void onSelectionItemSelected(int position) {
		super.onSelectionItemSelected(position);
		AnzMobileUtil.logger.fine("onItemSelected(" + position + ")");

		BPayAccount biller = null;
		if (position > 0 && position <= m_billers.size()) {
			biller = m_billers.get(position - 1);
		}

		if (biller != null) {
			m_holder.biller_name.setText(biller.name);
			m_holder.biller_code.setText(biller.code);
			m_holder.biller_description.setText(biller.description);
			m_holder.biller_reference.setText(biller.reference);
		} else {
			m_holder.biller_name.setText("");
			m_holder.biller_code.setText("");
			m_holder.biller_description.setText("");
			m_holder.biller_reference.setText("");
		}
	}
}
