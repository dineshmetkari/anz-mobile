package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobile;
import org.dancefire.anz.mobile.AnzMobileUtil;
import org.dancefire.anz.mobile.Formatter;
import org.dancefire.anz.mobile.PageErrorException;
import org.dancefire.anz.mobile.PayAnyoneAccount;
import org.dancefire.anz.mobile.PayAnyoneData;
import org.dancefire.anz.mobile.PayAnyoneReceiptPage;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Gallery;

public class PayAnyoneFragment extends ConfirmFragment {
	static class ViewHolder {
		private Gallery from;
		private EditText from_name;
		private EditText payee_name;
		private EditText payee_description;
		private EditText payee_bsb;
		private EditText payee_number;
		private EditText amount;
		private EditText reference;
		private View selection_button;
	}

	private ArrayList<Account> m_accounts;
	private ArrayList<PayAnyoneAccount> m_payanyone_accounts;
	private AccountListAdapter m_adapter;
	private ViewHolder m_holder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.payanyone_layout, container, false);

		m_holder = new ViewHolder();
		m_holder.from = (Gallery) v.findViewById(R.id.payanyone_account_from);
		m_holder.from_name = (EditText) v
				.findViewById(R.id.payanyone_from_name);
		m_holder.payee_name = (EditText) v.findViewById(R.id.payanyone_to_name);
		m_holder.payee_description = (EditText) v
				.findViewById(R.id.payanyone_to_description);
		m_holder.payee_bsb = (EditText) v.findViewById(R.id.payanyone_to_bsb);
		m_holder.payee_number = (EditText) v
				.findViewById(R.id.payanyone_to_number);
		m_holder.amount = (EditText) v.findViewById(R.id.payanyone_amount);
		m_holder.reference = (EditText) v
				.findViewById(R.id.payanyone_reference);
		m_holder.selection_button = v.findViewById(R.id.selection_button);

		// m_holder.payee_bsb.setTypeface(Apps.getCardFont());
		// m_holder.payee_number.setTypeface(Apps.getCardFont());
		m_holder.amount.setTypeface(Apps.getCardFont());

		setRequestOnClickListener(v, R.id.payanyone_button);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (m_accounts == null || m_payanyone_accounts == null) {
			runBackgroundTask();
		} else {
			onBackgroundEnd();
		}
	}

	@Override
	protected void onBackgroundBegin() {
		Util.showWaitingDialog(getActivity());
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
		m_payanyone_accounts = Apps.getBank().getPayAnyoneAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		m_adapter = new AccountListAdapter(getActivity(),
				R.layout.account_item_layout, m_accounts);
		m_holder.from.setAdapter(m_adapter);

		if (m_holder.selection_button != null) {
			final ArrayList<String> names = new ArrayList<String>();
			names.add("<new payee>");
			for (PayAnyoneAccount account : m_payanyone_accounts) {
				names.add(Formatter.toString(account));
			}

			setSelectionOnClickListener(m_holder.selection_button, names);

			Util.dismissWaitingDialog();
		}
	}

	private PayAnyoneData getData(ViewHolder holder) {
		int from = holder.from.getSelectedItemPosition();

		double amount = 0;

		String amount_text = holder.amount.getText().toString().trim();
		if (amount_text.length() > 0) {
			try {
				amount = Double.parseDouble(holder.amount.getText().toString());
			} catch (NumberFormatException ex) {
				AnzMobileUtil.logger.severe("Cannot parse ["
						+ holder.amount.getText() + "] to double.");
				toast("Invalid amount.");
			}
		}

		PayAnyoneAccount payee = new PayAnyoneAccount();
		payee.account_name = holder.payee_name.getText().toString();
		payee.description = holder.payee_description.getText().toString();
		payee.bsb = holder.payee_bsb.getText().toString();
		payee.number = holder.payee_number.getText().toString();

		String from_name = holder.from_name.getText().toString();
		String reference = holder.reference.getText().toString();

		String warning_message = "";
		if (amount <= 0.01) {
			warning_message = "Invalid transfer amount. The minimum amount is $0.01";
		} else if (amount > m_accounts.get(from).available_balance) {
			warning_message = "There is not sufficient funds ("
					+ Formatter.BALANCE_FORMAT.format(amount)
					+ ") in the from accounts.\n\n"
					+ Formatter.toString(m_accounts.get(from));
		} else if (payee.account_name.length() == 0 || payee.bsb.length() == 0
				|| payee.number.length() == 0) {
			warning_message = "Account name, BSB and account number should not be empty.";
		}

		if (warning_message.length() > 0) {
			toast(warning_message);
			return null;
		} else {
			PayAnyoneData data = new PayAnyoneData();
			data.from = from;
			data.from_name = from_name;
			data.to = payee;
			data.reference = reference;
			data.amount = amount;

			return data;
		}
	}

	@Override
	protected void onCommit() {
		PayAnyoneData data = getData(m_holder);
		if (data != null) {
			showConfirm("Are you sure to transfer?",
					Formatter.toString(data, m_accounts));
		}
	}

	@Override
	protected void onConfirm(Bundle args) {
		PayAnyoneData data = getData(m_holder);

		if (data != null) {

			AnzMobile bank = Apps.getBank();
			try {
				// Do Pay Anyone
				Account account_from = bank.getAccount(data.from);
				PayAnyoneReceiptPage page = bank.payAnyone(account_from,
						data.from_name, data.to, data.amount, data.reference);

				// Show Receipt
				showReceipt("Pay Anyone is successed.",
						Formatter.toString(page));
			} catch (PageErrorException ex) {
				showError("Pay Anyone is failed.", ex);
			}
		}
	}

	@Override
	public void onResume() {
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
		PayAnyoneAccount account = null;
		if (position > 0 && position <= m_payanyone_accounts.size()) {
			account = m_payanyone_accounts.get(position - 1);
		}

		if (account != null) {
			m_holder.payee_name.setText(account.account_name);
			m_holder.payee_description.setText(account.description);
			m_holder.payee_bsb.setText(account.bsb);
			m_holder.payee_number.setText(account.number);
		} else {
			m_holder.payee_name.setText("");
			m_holder.payee_description.setText("");
			m_holder.payee_bsb.setText("");
			m_holder.payee_number.setText("");
		}
	}
}
