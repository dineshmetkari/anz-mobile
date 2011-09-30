package org.dancefire.anz;

import java.util.ArrayList;
import java.util.logging.Level;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobile;
import org.dancefire.anz.mobile.AnzMobileUtil;
import org.dancefire.anz.mobile.Formatter;
import org.dancefire.anz.mobile.PageErrorException;
import org.dancefire.anz.mobile.TransferData;
import org.dancefire.anz.mobile.TransferReceiptPage;

import android.os.Bundle;
import android.view.View;
import android.widget.Gallery;
import android.widget.TextView;

public class TransferFragment extends ConfirmFragment {
	static class ViewHolder {
		Gallery from;
		Gallery to;
		TextView amount;
	}

	private ArrayList<Account> m_accounts;
	private AccountListAdapter m_adapter_from;
	private AccountListAdapter m_adapter_to;
	private ViewHolder m_holder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.transfer_layout, container, false);

		m_holder = new ViewHolder();
		m_holder.from = (Gallery) v.findViewById(R.id.transfer_account_from);
		m_holder.to = (Gallery) v.findViewById(R.id.transfer_account_to);
		m_holder.amount = (TextView) v.findViewById(R.id.transfer_amount);

		m_holder.amount.setTypeface(Apps.getCardFont());

		setRequestOnClickListener(v, R.id.transfer_button);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		runBackgroundTask();
	}

	@Override
	protected void onBackgroundBegin() {
		//WaitingDialogFragment.showWaiting(getFragmentManager());
		Util.showPendingDialog(getFragmentManager());
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
		m_adapter_from = new AccountListAdapter(getActivity(),
				R.layout.account_item_layout, m_accounts);
		m_adapter_to = new AccountListAdapter(getActivity(),
				R.layout.account_item_layout, m_accounts);
	}

	@Override
	protected void onBackgroundEnd() {

		m_holder.from.setAdapter(m_adapter_from);
		m_holder.to.setAdapter(m_adapter_to);
		//Util.dismissWaitingDialog();
		//WaitingDialogFragment.hideWaiting();
		Util.dismissPendingDialog();
	}

	private TransferData getData(ViewHolder holder) {
		int from = holder.from.getSelectedItemPosition();
		int to = holder.to.getSelectedItemPosition();
		double amount = 0;

		String amount_text = holder.amount.getText().toString().trim();
		if (amount_text.length() > 0) {
			try {
				amount = Double.parseDouble(holder.amount.getText().toString());
			} catch (NumberFormatException ex) {
				AnzMobileUtil.logger.log(Level.SEVERE, "Cannot parse ["
						+ holder.amount.getText() + "] to double.", ex);
				toast("Invalid amount - '" + holder.amount.getText().toString()
						+ "'");
			}
		}

		String warning_message = "";
		if (from == to) {
			warning_message = "Cannot transfer within the same account. Please choose different accounts for transfer.";
		} else {
			if (amount <= 0.01) {
				warning_message = "Invalid transfer amount. The minimum amount is $0.01";
			} else if (amount > m_accounts.get(from).available_balance) {
				warning_message = "There is not sufficient funds ("
						+ Formatter.BALANCE_FORMAT.format(amount)
						+ ") in the from accounts.\n\n"
						+ Formatter.toString(m_accounts.get(from));
			}
		}

		if (warning_message.length() > 0) {
			toast(warning_message);
			return null;
		} else {
			TransferData data = new TransferData();
			data.from = from;
			data.to = to;
			data.amount = amount;

			return data;
		}
	}

	@Override
	protected void onCommit() {
		TransferData data = getData(m_holder);
		if (data != null) {
			showConfirm("Are you sure to transfer?",
					Formatter.toString(data, m_accounts));
		}
	}

	@Override
	public void onConfirm() {
		TransferData data = getData(m_holder);

		if (data != null) {
			AnzMobileUtil.logger.info("[onConfirm()]");
			AnzMobile bank = Apps.getBank();
			try {
				// Do Transfer
				ArrayList<Account> accounts = bank.getAccounts();
				TransferReceiptPage page = bank.transfer(
						accounts.get(data.from), accounts.get(data.to),
						data.amount);

				// Show receipt
				showReceipt("Transfer is successed.", Formatter.toString(page));
			} catch (PageErrorException ex) {
				showError("Transfer failed.", ex);
			}
		}
	}

	@Override
	public void onResume() {
		if (m_adapter_from != null) {
			m_adapter_from.notifyDataSetChanged();
		}
		if (m_adapter_to != null) {
			m_adapter_to.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	protected void onClear() {
		m_holder.amount.setText("");
	}

}
