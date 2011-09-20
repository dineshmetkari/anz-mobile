package org.dancefire.anz;

import java.util.ArrayList;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.AnzMobile;
import org.dancefire.anz.mobile.BPayAccount;
import org.dancefire.anz.mobile.BPayData;
import org.dancefire.anz.mobile.BPayReceiptPage;
import org.dancefire.anz.mobile.Formatter;
import org.dancefire.anz.mobile.PageErrorException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Gallery;

public class BPayActivity extends ConfirmActivity {
	static class ViewHolder {
		private Gallery from;
		private EditText from_name;
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

	private static final int SELECTION_ACTION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bpay_layout);

		m_holder = new ViewHolder();
		m_holder.from = (Gallery) findViewById(R.id.bpay_account_from);
		m_holder.from_name = (EditText) findViewById(R.id.bpay_from_name);
		m_holder.biller_code = (EditText) findViewById(R.id.bpay_biller_code);
		m_holder.biller_name = (EditText) findViewById(R.id.bpay_biller_name);
		m_holder.biller_reference = (EditText) findViewById(R.id.bpay_biller_reference);
		m_holder.biller_description = (EditText) findViewById(R.id.bpay_biller_description);
		m_holder.amount = (EditText) findViewById(R.id.bpay_amount);
		m_holder.selection_button = findViewById(R.id.selection_button);

		m_holder.amount.setTypeface(Apps.getCardFont());
		
		setRequestOnClickListener(R.id.bpay_button);

		runBackgroundTask();
	}

	@Override
	protected void onBackgroundBegin() {
		Util.showWaitingDialog(this);
	}

	@Override
	protected void onBackground() throws Throwable {
		m_accounts = Apps.getBank().getAccounts();
		m_billers = Apps.getBank().getBPayAccounts();
	}

	@Override
	protected void onBackgroundEnd() {
		m_adapter = new AccountListAdapter(this, R.layout.account_item_layout,
				m_accounts);
		m_holder.from.setAdapter(m_adapter);

		if (m_holder.selection_button != null) {
			final ArrayList<String> names = new ArrayList<String>();
			names.add("<new biller>");
			for (BPayAccount biller : m_billers) {
				names.add(Formatter.toString(biller));
			}

			m_holder.selection_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BPayActivity.this,
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
		biller.code = holder.biller_code.getText().toString();
		biller.description = holder.biller_description.getText().toString();
		biller.reference = holder.biller_reference.getText().toString();

		String from_name = holder.from_name.getText().toString();

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
			data.from_name = from_name;
			data.to = biller;
			data.amount = amount;
			return data;
		}
	}

	@Override
	protected void onCommit() {
		BPayData data = getData(m_holder);
		if (data != null) {
			showConfirm("Are you sure to pay?", Formatter.toString(data));
		}
	}

	protected void onConfirm(Bundle args) {
		BPayData data = getData(m_holder);
		if (data != null) {
			AnzMobile bank = Apps.getBank();
			try {
				// Do BPay
				Account account_from = bank.getAccount(data.from);
				BPayReceiptPage page = bank.payBills(account_from,
						data.from_name, data.to, data.amount);

				// Show Receipt
				showReceipt("Pay Bill is successed.", Formatter.toString(page));
			} catch (PageErrorException ex) {
				notifyError("Pay Bill is failed.", ex);
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
		BPayAccount account = null;
		if (index > 0 && index <= m_billers.size()) {
			account = m_billers.get(index - 1);
		}
		setHolderValue(account);
	}

	private void setHolderValue(BPayAccount biller) {
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
