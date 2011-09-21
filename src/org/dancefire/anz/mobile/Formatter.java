package org.dancefire.anz.mobile;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.http.NameValuePair;
import org.htmlcleaner.TagNode;

import android.graphics.Color;
import android.widget.TextView;

public class Formatter {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"dd/MM/yyyy");
	public static final DecimalFormat BALANCE_FORMAT = new DecimalFormat(
			"$##0.00");
	public static final DecimalFormat WEB_BALANCE_FORMAT = new DecimalFormat(
			"#,##0.00");
	public static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat(
			"##0.00");

	public static String toString(TagNode node) {
		return AnzMobileUtil.parser.getInnerHtml(node);
	}

	public static String toString(Form form) {
		StringBuilder sb = new StringBuilder();
		if (form != null) {
			sb.append("\t \t [Form] \t " + form.action + "\n");
			for (NameValuePair p : form.params) {
				sb.append("\t \t \t " + p.getName() + " = \t [" + p.getValue()
						+ "]\n");
			}
		} else {
			sb.append("\t \t [Form] is null.\n");
		}
		return sb.toString();
	}

	public static String toString(Object object) {
		return object.toString();
	}

	public static <T> String toString(ArrayList<T> list) {
		StringBuilder sb = new StringBuilder();
		if (list != null) {
			sb.append("\t \t [ArrayList]\n");
			for (T o : list) {
				sb.append("\t \t \t " + toString(o));
			}
		} else {
			sb.append("\t \t [ArrayList] is null.\n");
		}
		return sb.toString();
	}

	public static String toString(Account account) {
		StringBuilder sb = new StringBuilder();
		sb.append(account.name + "\n");
		sb.append(account.number + "\n");
		sb.append("Current Balance: \t"
				+ Formatter.BALANCE_FORMAT.format(account.current_balance)
				+ "\n");
		sb.append("Available Balance: \t"
				+ Formatter.BALANCE_FORMAT.format(account.available_balance)
				+ "");
		return sb.toString();
	}

	public static String toString(PayAnyoneAccount account) {
		StringBuilder sb = new StringBuilder();
		sb.append(account.account_name + " (" + account.description + ")\n");
		sb.append(account.bsb + " " + account.number + "");
		return sb.toString();
	}

	public static String toString(BPayAccount account) {
		StringBuilder sb = new StringBuilder();
		sb.append(account.name + " - " + account.description + "\n");
		sb.append("Code     : " + account.code + "\n");
		sb.append("Reference: " + account.reference);
		return sb.toString();
	}

	public static String toString(Page page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[PAGE] \t " + page.url + "\n");
		sb.append("\t [View Accounts] \t " + page.link_accounts + "\n");
		sb.append("\t [Transfer Funds] \t " + page.link_transfer + "\n");
		sb.append("\t [Pay Anyone] \t " + page.link_payanyone + "\n");
		sb.append("\t [Pay Bills] \t " + page.link_bpay + "\n");
		sb.append("\t [Back] \t " + page.link_back + "\n");
		sb.append("\t [Logout] \n " + toString(page.logout_form));
		return sb.toString();
	}

	public static String toString(TransferPage page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[TransferPage]\n");
		sb.append(toString((Page) page));
		sb.append("\t [TransferForm] \n " + toString(page.form));
		return sb.toString();
	}

	public static String toString(TransferConfirmPage page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[TransferConfirmPage]\n");
		sb.append(toString((Page) page));
		sb.append("\t \t [From] \t " + page.from + "\n");
		sb.append("\t \t [To] \t " + page.to + "\n");
		sb.append("\t \t [Amount] \t " + page.amount + "\n");
		sb.append(toString(page.form));
		return sb.toString();
	}

	public static String toString(TransferReceiptPage page, boolean isDebug) {
		if (isDebug) {
			if (page == null) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("[TransferResultPage]\n");
			sb.append(toString((Page) page));
			sb.append("\t \t [From] \t " + page.from + "\n");
			sb.append("\t \t [To] \t " + page.to + "\n");
			sb.append("\t \t [Amount] \t " + page.amount + "\n");
			sb.append("\t \t [Date] \t " + page.date + "\n");
			sb.append("\t \t [Lodge] \t " + page.lodgement_number + "\n");
			sb.append("\t \t [Receipt] \t " + page.receipt_number + "\n");

			return sb.toString();
		} else {
			return toString(page);
		}
	}

	public static String toString(TransferReceiptPage page) {
		if (page == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("From   : " + page.from + "\n");
		sb.append("To     : " + page.to + "\n");
		sb.append("Amount : " + Formatter.BALANCE_FORMAT.format(page.amount)
				+ "\n\n");
		sb.append("Receipt #   : " + page.receipt_number + "\n");
		sb.append("Lodgement # : " + page.lodgement_number + "\n");
		return sb.toString();
	}

	public static String toString(PayAnyonePage page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[PayAnyonePage page]\n");
		sb.append(toString((Page) page));
		sb.append("\t [PayAnyone] \n " + toString(page.form));
		sb.append(toString(page.payee_list));
		sb.append("\t \t [Daily limit] \t " + page.daily_limit + "\n");
		return sb.toString();
	}

	public static String toString(PayAnyoneConfirmPage page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[PayAnyoneConfirmPage page]\n");
		sb.append(toString((Page) page));
		sb.append("\t [ConfirmForm] \n " + toString(page.form));
		sb.append("\t \t [From] \t [" + page.from + "]\t[" + page.from_name
				+ "]\n");
		sb.append("\t \t [ To ] \t " + toString(page.to));
		sb.append("\t \t [Amount] \t [" + page.amount + "]\n");
		sb.append("\t \t [Ref] \t [" + page.reference + "]\n");
		return sb.toString();

	}

	public static String toString(PayAnyoneReceiptPage page) {
		if (page == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("From   : " + page.from + " (" + page.from_name + ")\n\n");
		sb.append("To     :\n" + Formatter.toString(page.to) + "\n\n");
		sb.append("Amount : " + Formatter.BALANCE_FORMAT.format(page.amount)
				+ "\n");
		sb.append("Reference        : " + page.reference + "\n\n");
		sb.append("Receipt #   : " + page.receipt_number + "\n");
		sb.append("Lodgement # : " + page.lodgement_number + "\n");
		return sb.toString();
	}

	public static String toString(BPayReceiptPage page) {
		StringBuilder sb = new StringBuilder();
		sb.append("From   : " + page.from + "\n\n");
		sb.append("To     : " + Formatter.toString(page.to) + "\n\n");
		sb.append("Amount : " + Formatter.BALANCE_FORMAT.format(page.amount)
				+ "\n\n");
		sb.append("Receipt #   : " + page.receipt_number + "\n");
		sb.append("Lodgement # : " + page.lodgement_number + "");
		return sb.toString();
	}

	public static String toString(PayAnyoneData data,
			ArrayList<Account> accounts) {
		StringBuilder sb = new StringBuilder();

		sb.append("Amount    : " + Formatter.BALANCE_FORMAT.format(data.amount)
				+ "\n\n");
		sb.append("From      :\n" + Formatter.toString(accounts.get(data.from))
				+ "\n\n");
		sb.append("To        :\n" + Formatter.toString(data.to) + "\n\n");
		sb.append("Reference :\n" + data.reference);

		return sb.toString();
	}

	public static String toString(TransferData data, ArrayList<Account> accounts) {
		StringBuilder sb = new StringBuilder();

		sb.append("Amount : " + Formatter.BALANCE_FORMAT.format(data.amount)
				+ "\n\n");
		sb.append("From   :\n" + Formatter.toString(accounts.get(data.from))
				+ "\n\n");
		sb.append("To     :\n" + Formatter.toString(accounts.get(data.to))
				+ "\n");

		return sb.toString();
	}

	public static String toString(BPayData data, ArrayList<Account> accounts) {
		StringBuilder sb = new StringBuilder();

		sb.append("Amount : " + Formatter.BALANCE_FORMAT.format(data.amount)
				+ "\n\n");
		sb.append("From   :\n" + Formatter.toString(accounts.get(data.from))
				+ "\n\n");
		sb.append("To     :\n" + Formatter.toString(data.to) + "");

		return sb.toString();
	}

	public static double parseAmount(String text) {
		String s = text.trim();
		if (s.startsWith("$")) {
			try {
				return Formatter.WEB_BALANCE_FORMAT.parse(s.substring(1))
						.doubleValue();
			} catch (ParseException ex) {
				AnzMobileUtil.logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		return 0;
	}

	public static void setTextColorByAmountDark(TextView view, double amount) {
		if (amount <= 0) {
			view.setTextColor(Color.RED);
		} else {
			view.setTextColor(Color.WHITE);
		}
	}

	public static void setTextColorByAmountLight(TextView view, double amount) {
		if (amount <= 0) {
			view.setTextColor(Color.RED);
		} else {
			view.setTextColor(Color.BLACK);
		}
	}

}
