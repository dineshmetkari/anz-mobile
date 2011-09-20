package org.dancefire.anz;

import java.util.List;

import org.dancefire.anz.mobile.Account;
import org.dancefire.anz.mobile.Formatter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * The Account list adapter
 * 
 */
class AccountListAdapter extends ArrayAdapter<Account> {
	static class ViewHolder {
		TextView account_name;
		TextView account_number;
		TextView current_balance;
		TextView available_balance;
		TextView current_balance_label;
		TextView available_balance_label;
	}

	private LayoutInflater m_inflater;
	private List<Account> m_data;
	private int m_resid;

	public AccountListAdapter(Context context, int textViewResourceId,
			List<Account> objects) {
		super(context, textViewResourceId, objects);
		m_inflater = LayoutInflater.from(context);
		m_resid = textViewResourceId;
		m_data = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = m_inflater.inflate(m_resid, null);
			holder = new ViewHolder();
			holder.account_name = (TextView) convertView
					.findViewById(R.id.text_account_name);
			holder.account_number = (TextView) convertView
					.findViewById(R.id.text_account_number);
			holder.current_balance = (TextView) convertView
					.findViewById(R.id.text_current_balance);
			holder.available_balance = (TextView) convertView
					.findViewById(R.id.text_available_balance);
			holder.current_balance_label = (TextView) convertView
					.findViewById(R.id.text_label_current_balance);
			holder.available_balance_label = (TextView) convertView
					.findViewById(R.id.text_label_available_balance);

			Typeface font = Apps.getCardFont();
			holder.account_name.setTypeface(font);
			holder.account_number.setTypeface(font);
			holder.current_balance.setTypeface(font);
			holder.available_balance.setTypeface(font);
			holder.current_balance_label.setTypeface(font);
			holder.available_balance_label.setTypeface(font);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Account account = m_data.get(position);
		holder.account_name.setText(account.name);
		if (account.number.length() == 16 && !account.number.contains(" ")) {
			// 16-digits credit card number, insert space for each 4-digits
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < account.number.length(); ++i) {
				sb.append(account.number.charAt(i));
				if (i > 0 && i < (account.number.length() - 1) && (i % 4) == 3) {
					sb.append(' ');
				}
			}
			holder.account_number.setText(sb.toString());
		} else {
			holder.account_number.setText(account.number);
		}

		holder.current_balance.setText(Formatter.BALANCE_FORMAT
				.format(account.current_balance));
		holder.available_balance.setText(Formatter.BALANCE_FORMAT
				.format(account.available_balance));

		Formatter.setTextColorByAmountDark(holder.current_balance,
				account.current_balance);
		Formatter.setTextColorByAmountDark(holder.available_balance,
				account.available_balance);

		return convertView;
	}
}