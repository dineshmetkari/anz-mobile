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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Gallery;

public class PayAnyoneActivity extends ConfirmActivity {
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

	private static final int SELECTION_ACTION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payanyone_layout);

		m_holder = new ViewHolder();
		m_holder.from = (Gallery) findViewById(R.id.payanyone_account_from);
		m_holder.from_name = (EditText) findViewById(R.id.payanyone_from_name);
		m_holder.payee_name = (EditText) findViewById(R.id.payanyone_to_name);
		m_holder.payee_description = (EditText) findViewById(R.id.payanyone_to_description);
		m_holder.payee_bsb = (EditText) findViewById(R.id.payanyone_to_bsb);
		m_holder.payee_number = (EditText) findViewById(R.id.payanyone_to_number);
		m_holder.amount = (EditText) findViewById(R.id.payanyone_amount);
		m_holder.reference = (EditText) findViewById(R.id.payanyone_reference);
		m_holder.selection_button = findViewById(R.id.selection_button);

		//m_holder.payee_bsb.setTypeface(Apps.getCardFont());
		//m_holder.payee_number.setTypeface(Apps.getCardFont());
		m_holder.amount.setTypeface(Apps.getCardFont());

		runBackgroundTask();

		setRequestOnClickListener(R.id.payanyone_button);

	}

	@Override
	protected void onBackgroundBegin() {
		Util.showWaitingDialog(this);
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
		m_payanyone_accounts = Apps.getBank().getPayAnyoneAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		m_adapter = new AccountListAdapter(this, R.layout.account_item_layout,
				m_accounts);
		m_holder.from.setAdapter(m_adapter);

		if (m_holder.selection_button != null) {
			final ArrayList<String> names = new ArrayList<String>();
			names.add("<new payee>");
			for (PayAnyoneAccount account : m_payanyone_accounts) {
				names.add(Formatter.toString(account));
			}

			m_holder.selection_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PayAnyoneActivity.this,
							SelectionActivity.class);
					intent.putExtra("item_list", names);
					startActivityForResult(intent, SELECTION_ACTION);
				}
			});

			Util.dismissWaitingDialog();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECTION_ACTION) {
			if (resultCode == RESULT_OK) {
				int position = data.getIntExtra("selection", -1);
				setHolderValue(position);
			}
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
				notifyError("Pay Anyone is failed.", ex);
			}
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
	protected void onClear() {
		m_holder.amount.setText("");
	}

	private void setHolderValue(int index) {
		PayAnyoneAccount account = null;
		if (index > 0 && index <= m_payanyone_accounts.size()) {
			account = m_payanyone_accounts.get(index - 1);
		}
		setHolderValue(account);
	}

	private void setHolderValue(PayAnyoneAccount account) {
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
