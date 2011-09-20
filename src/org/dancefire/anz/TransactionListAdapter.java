package org.dancefire.anz;

import java.util.List;

import org.dancefire.anz.mobile.Formatter;
import org.dancefire.anz.mobile.Transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionListAdapter extends ArrayAdapter<Transaction> {
	static class ViewHolder {
		TextView date;
		TextView description;
		TextView amount;
	}
	
	private LayoutInflater m_inflater;
	private List<Transaction> m_data;
	private int m_resid;
	
	public TransactionListAdapter(Context context, int textViewResourceId,
			List<Transaction> objects) {
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
			holder.date = (TextView) convertView.findViewById(R.id.text_transaction_date);
			holder.description = (TextView) convertView.findViewById(R.id.text_transaction_description);
			holder.amount = (TextView) convertView.findViewById(R.id.text_transaction_amount);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Transaction transaction = m_data.get(position);
		holder.date.setText(Formatter.DATE_FORMAT.format(transaction.date));
		holder.description.setText(transaction.description);
		holder.amount.setText(Formatter.AMOUNT_FORMAT.format(transaction.amount));

		Formatter.setTextColorByAmountLight(holder.amount, transaction.amount);

		return convertView;
	}
}
