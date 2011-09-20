package org.dancefire.anz.mobile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.htmlcleaner.TagNode;

import android.text.Html;

public class Parser {

	private static final String URL_LOGIN = "https://www.anz.com/inetbank/mobiledevice/login.asp";
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"dd/MM/yyyy");

	/**
	 * Get login form
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public static Form getLoginForm() throws PageErrorException {
		// Get the page
		TagNode node = AnzMobileUtil.getNode(URL_LOGIN, URL_LOGIN);
		if (node != null) {
			// Parse the page
			Form form = AnzMobileUtil.parseForm(node, "loginForm", URL_LOGIN);
			// return the result
			return form;
		} else {
			return null;
		}
	}

	/**
	 * Login and return the AccountsPage object
	 * 
	 * @param userid
	 * @param password
	 * @return the AccountsPage which represents the information on the Account
	 *         list page
	 * @throws PageErrorException
	 */
	public static AccountsPage login(String userid, String password,
			String url, ArrayList<NameValuePair> params)
			throws PageErrorException {
		// Prepare url
		url = url + "&jsEnabled=true";
		// Prepare params
		userid = AnzMobileUtil.cleanUserid(userid);
		ArrayList<NameValuePair> new_params = new ArrayList<NameValuePair>();
		for (NameValuePair item : params) {
			if (item.getName().equals("CorporateSignonCorpId")) {
				new_params.add(new BasicNameValuePair(item.getName(), userid));
			} else if (item.getName().equals("CorporateSignonPassword")) {
				new_params
						.add(new BasicNameValuePair(item.getName(), password));
			} else if (item.getName().equals("USERID")) {
				new_params.add(new BasicNameValuePair(item.getName(), userid));
			} else if (item.getName().equals("PIN")) {
				new_params
						.add(new BasicNameValuePair(item.getName(), password));
			} else if (item.getName().equals("EWFBUTTON")) {
				String buttonValue = "Modify";
				new_params.add(new BasicNameValuePair(item.getName()
						+ buttonValue, buttonValue));
			} else {
				new_params.add(item);
			}
		}

		// Get the page
		AnzMobileUtil.logger.info("<< URL_LOGIN >>");
		TagNode node = AnzMobileUtil.getNode(url, URL_LOGIN, new_params);
		// return the parsed result
		AccountsPage accounts_page = parseAccountsPage(node, url);
		return accounts_page;
	}

	public static void logout(Page page) {
		if (page != null && page.logout_form != null) {
			AnzMobileUtil.logger.info("<< Logout >>");
			try {
				AnzMobileUtil.getNode(page.logout_form.action, page.url,
						page.logout_form.params);
			} catch (PageErrorException ex) {
				AnzMobileUtil.logger.warning(ex.toString());
			}
		}
	}

	private static final String LINK_NAME_ACCOUNTS = "View Accounts";
	private static final String LINK_NAME_TRANSFER = "Transfer Funds";
	private static final String LINK_NAME_PAYMENTS = "Make Payments";
	private static final String LINK_NAME_BPAY = "Pay Bills";
	private static final String LINK_NAME_BACK = "Back";
	private static final String NAME_CURRENT_BALANCE = "Current Balance";
	private static final String NAME_AVAILABLE_FUNDS = "Available Funds";

	/**
	 * Parse the Account page and return the AccountsPage object
	 * 
	 * @param page
	 * @return
	 * @throws PageErrorException
	 */
	private static AccountsPage parseAccountsPage(TagNode node, String url)
			throws PageErrorException {
		AccountsPage page = new AccountsPage();
		page.url = url;

		// Get function links
		parseFunctionLinks(node, page);

		// Search links of accounts
		TagNode[] account_link_list = node.getElementsByAttValue("class",
				"clickItem", true, false);
		page.accounts = new ArrayList<Account>();
		for (TagNode link : account_link_list) {
			TagNode account_name_node = link.findElementByAttValue("class",
					"primary", true, false);
			if (account_name_node != null) {
				Account acct = new Account();
				acct.name = account_name_node.getText().toString().trim();
				acct.link = AnzMobileUtil.getLink(
						link.findElementByName("a", false).getAttributeByName(
								"href"), page.url);
				TagNode[] balance_nodes = link.getElementsByName("tr", true);
				for (TagNode b : balance_nodes) {
					TagNode d = b.findElementByAttValue("class", "desc", true,
							false);
					if (d != null) {
						TagNode a = b.findElementByAttValue("class", "amount",
								true, false);
						if (a != null) {
							String amount_text = a.getText().toString().trim()
									.substring(1);
							double amount = 0;
							try {
								amount = Formatter.WEB_BALANCE_FORMAT.parse(
										amount_text).doubleValue();
							} catch (ParseException e) {
								AnzMobileUtil.logger.log(Level.WARNING,
										e.getMessage(), e);
							}
							String desc = d.getText().toString().trim();
							if (NAME_CURRENT_BALANCE.equals(desc)) {
								acct.current_balance = amount;
							} else if (NAME_AVAILABLE_FUNDS.equals(desc)) {
								acct.available_balance = amount;
							}
						}
					}
				}
				page.accounts.add(acct);
				AnzMobileUtil.logger.fine("Account: [" + acct.name + "] "
						+ acct.current_balance + "/" + acct.available_balance);
				AnzMobileUtil.logger.fine("\t " + acct.link);
			}
		}
		return page;
	}

	/**
	 * Parse the function links, such as 'View Accounts', 'Transfer' and
	 * 'Payments'
	 * 
	 * @param node
	 * @param page
	 * @throws PageErrorException
	 */
	private static void parseFunctionLinks(TagNode node, Page page)
			throws PageErrorException {
		TagNode[] linklist = node.getElementsByName("a", true);
		// Get function links
		for (TagNode link : linklist) {
			// Link of functions
			TagNode[] images = link.getElementsByName("img", true);
			for (TagNode img : images) {
				AnzMobileUtil.logger.finer("[img.alt] ["
						+ img.getAttributeByName("alt") + "]");
				if (LINK_NAME_ACCOUNTS.equals(img.getAttributeByName("alt"))) {
					page.link_accounts = AnzMobileUtil.getLink(
							link.getAttributeByName("href"), page.url);
					break;
				} else if (LINK_NAME_TRANSFER.equals(img
						.getAttributeByName("alt"))) {
					page.link_transfer = AnzMobileUtil.getLink(
							link.getAttributeByName("href"), page.url);
					break;
				} else if (LINK_NAME_PAYMENTS.equals(img
						.getAttributeByName("alt"))) {
					page.link_payanyone = AnzMobileUtil.getLink(
							link.getAttributeByName("href"), page.url);
					break;
				} else if (LINK_NAME_BPAY.equals(img.getAttributeByName("alt"))) {
					page.link_bpay = AnzMobileUtil.getLink(
							link.getAttributeByName("href"), page.url);
					break;
				} else if (LINK_NAME_BACK.equals(img.getAttributeByName("alt"))) {
					page.link_back = AnzMobileUtil.getLink(
							link.getAttributeByName("href"), page.url);
					break;
				}
			}
		}
		// Get Logout form
		try {
			page.logout_form = AnzMobileUtil.parseForm(node, "headerFrm",
					page.url);
		} catch (PageErrorException ex) {
			AnzMobileUtil.logger.log(Level.WARNING, "Cannot find Logout form.",
					ex);
		}
		// Get error message
		parseErrorMessage(node, page);
	}

	private static void fillMessage(ArrayList<String> list, TagNode[] nodes) {
		for (TagNode n : nodes) {
			String message = Html.fromHtml(n.getText().toString()).toString()
					.trim();
			if (message.length() > 0) {
				list.add(message);
			}
		}
	}

	/**
	 * Parse the error and warning message in the anz mobile page
	 * 
	 * @param node
	 * @param page
	 *            - fill the error_messages[]. Default it will be empty.
	 * @throws PageErrorException
	 */
	private static void parseErrorMessage(TagNode node, Page page)
			throws PageErrorException {
		page.error_messages = new ArrayList<String>();

		fillMessage(page.error_messages,
				node.getElementsByAttValue("class", "warningText", true, false));
		fillMessage(page.error_messages,
				node.getElementsByAttValue("class", "errorText", true, false));

		if (page.hasError()) {
			if (page.link_accounts != null || page.link_back != null
					|| page.logout_form != null) {
				// Only warning on the page, nothing important.
				AnzMobileUtil.logger.warning(page.getErrorString());
			} else {
				throw new PageErrorException(page);
			}
		}
	}

	/**
	 * Get Accounts list page
	 * 
	 * @param current_page
	 * @return
	 * @throws PageErrorException
	 */
	public static AccountsPage getAccountsPage(Page current_page)
			throws PageErrorException {
		if (current_page instanceof AccountsPage) {
			// Not necessary to reload the page if current one the same page
			// request.
			return (AccountsPage) current_page;
		}

		AnzMobileUtil.logger.info("<< Accounts Page >>");
		TagNode node = AnzMobileUtil.getNode(current_page.link_accounts,
				current_page.url.toString());
		AccountsPage page = parseAccountsPage(node, current_page.url);
		return page;
	}

	/**
	 * Get and parse the Transaction page in generic
	 * 
	 * @param link
	 *            - The page link, which could be normal transaction, today's
	 *            transaction or more transactions.
	 * @param account
	 *            - The transaction of account
	 * @param context
	 *            - Context page which contain the transaction page link.
	 * @return the parsed TransactionPage object
	 * @throws PageErrorException
	 */
	private static TransactionPage getGenericTransactionPage(String link,
			Account account, Page context) throws PageErrorException {
		TagNode node = AnzMobileUtil.getNode(link, context.url);
		TransactionPage page = new TransactionPage();
		page.account = account;
		page.url = link;

		// Parse function links
		parseFunctionLinks(node, page);

		// Parse transactions
		parseTransactions(node, page);

		// Parse links of today transaction and more
		parseMoreLink(node, page);

		return page;

	}

	/**
	 * Normal Transaction
	 * 
	 * @param account
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static TransactionPage getTransactionPage(Account account,
			Page context) throws PageErrorException {
		AnzMobileUtil.logger.info("<< Transaction Page >>");
		return getGenericTransactionPage(account.link, account, context);
	}

	/**
	 * More transaction
	 * 
	 * @param account
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static TransactionPage getMoreTransactionPage(Account account,
			TransactionPage context) throws PageErrorException {
		AnzMobileUtil.logger.info("<< More Transaction Page >>");
		return getGenericTransactionPage(context.link_more, account, context);
	}

	/**
	 * Today's transaction
	 * 
	 * @param account
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static TransactionPage getTodayTransactionPage(Account account,
			TransactionPage context) throws PageErrorException {
		AnzMobileUtil.logger.info("<< Today Transaction Page >>");
		return getGenericTransactionPage(context.link_today, account, context);
	}

	/**
	 * For some transaction pages, there are links of today or more
	 * transactions. This function will parse the links.
	 * 
	 * @param node
	 *            - The transaction page in TagNode object
	 * @param page
	 *            - The page object which will be filled the links.
	 */
	private static void parseMoreLink(TagNode node, TransactionPage page) {
		TagNode[] list = node.getElementsByAttValue("class", "go_forward",
				true, false);
		// get link of today transactions
		for (TagNode t : list) {
			if (t.getText().indexOf("today") > 0) {
				page.link_today = AnzMobileUtil.getLink(
						t.getAttributeByName("href"), page.url);
				AnzMobileUtil.logger.fine("[Today] " + page.link_today);
			}
		}
		// link of more
		TagNode more = node.findElementByAttValue("class",
				"more load_more clickItem", true, false);
		if (more != null) {
			page.link_more = AnzMobileUtil.getLink(
					more.getAttributeByName("href"), page.url);
			AnzMobileUtil.logger.fine("[More] " + page.link_more);
		}
	}

	/**
	 * The function will parse the transactions one by one from the node object,
	 * and put them to the page object.
	 * 
	 * @param node
	 * @param page
	 */
	private static void parseTransactions(TagNode node, TransactionPage page) {
		TagNode[] list = node.getElementsByAttValue("class", "clickItem", true,
				false);
		page.transactions = new ArrayList<Transaction>();
		for (TagNode t : list) {
			Transaction tran = new Transaction();
			// Date
			TagNode date_node = t.findElementByAttValue("class", "floatLeft",
					true, false);
			if (date_node != null) {
				try {
					tran.date = DATE_FORMATTER.parse(
							date_node.getText().toString().trim()).getTime();
				} catch (ParseException ex) {
					AnzMobileUtil.logger
							.log(Level.WARNING, ex.getMessage(), ex);
				}
			} else {
				tran.date = Calendar.getInstance().getTimeInMillis();
			}
			// Amount and direction
			String[] lines = t
					.findElementByAttValue("class", "amount", true, false)
					.getText().toString().trim().split("\n");
			for (String s : lines) {
				s = s.trim();
				if (s.startsWith("$")) {
					// amount
					try {
						tran.amount = Formatter.WEB_BALANCE_FORMAT.parse(
								s.substring(1)).doubleValue();
					} catch (ParseException ex) {
						AnzMobileUtil.logger.log(Level.WARNING,
								ex.getMessage(), ex);
					}
				} else if (s.equals("cr")) {
					// amount is positive
				} else if (s.equals("dr")) {
					// amount should be negative
					tran.amount = -tran.amount;
				}
			}
			// Description
			tran.description = t
					.findElementByAttValue("class", "desc", true, false)
					.getText().toString().trim().replaceAll(" +", " ");
			page.transactions.add(tran);

			// Debug output
			AnzMobileUtil.logger.fine("[" + DATE_FORMATTER.format(tran.date)
					+ "] (" + tran.amount + ") " + "\t [" + tran.description
					+ "]");
		}
	}

	/**
	 * Get the transfer funds between accounts form and the account number.
	 * 
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static TransferPage getTransferPage(Page context)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< Transfer Page >>");
		TagNode node = AnzMobileUtil
				.getNode(context.link_transfer, context.url);
		TransferPage page = new TransferPage();
		page.url = context.link_transfer;

		// parse the functions links
		parseFunctionLinks(node, page);

		// parse the transfer form
		page.form = AnzMobileUtil.parseForm(node, "frmInitiateFundTransfer",
				context.url);

		// get account numbers by read the span node with id "drAcctNo"
		// or
		// "crAcctNo"
		TagNode acct_nums_node = node.findElementByAttValue("id", "drAcctNo",
				true, false);
		page.account_numbers = acct_nums_node.getText().toString().trim()
				.split("\\|");
		// Debug
		AnzMobileUtil.logger.fine("[Transfer Form] : " + page.form.action);
		for (NameValuePair p : page.form.params) {
			AnzMobileUtil.logger.fine("\t " + p.getName() + " = ["
					+ p.getValue() + "]");
		}
		for (String n : page.account_numbers) {
			AnzMobileUtil.logger.fine("  [" + n + "]");
		}
		return page;
	}

	private static final String TRANSFER_FORM_DR = "FundsTransferSelfDrAccountDetails";
	private static final String TRANSFER_FORM_CR = "FundsTransferSelfCrAccountDetails";
	private static final String TRANSFER_FORM_AMOUNT = "Txn_Amt";

	/**
	 * Transfer funds between accounts
	 * 
	 * @param context
	 * @param from
	 *            - account index
	 * @param to
	 *            - account index
	 * @param amount
	 *            - amount of the money
	 * @return transfer confirm page
	 * @throws PageErrorException
	 */
	public static TransferConfirmPage transfer(TransferPage context, int from,
			int to, double amount) throws PageErrorException {
		AnzMobileUtil.logger.info("<< Transfer Confirm Page >>");
		// prepare the post params
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		for (NameValuePair p : context.form.params) {
			if (p.getName().equals(TRANSFER_FORM_DR)) {
				params.add(new BasicNameValuePair(p.getName(), Integer
						.toString(from)));
			} else if (p.getName().equals(TRANSFER_FORM_CR)) {
				params.add(new BasicNameValuePair(p.getName(), Integer
						.toString(to)));
			} else if (p.getName().equals(TRANSFER_FORM_AMOUNT)) {
				params.add(new BasicNameValuePair(p.getName(),
						Formatter.AMOUNT_FORMAT.format(amount)));
			} else {
				params.add(p);
			}
		}

		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				params);

		TransferConfirmPage page = new TransferConfirmPage();
		page.url = context.form.action;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse the confirm form
		page.form = AnzMobileUtil.parseForm(node, "FundsTransferConfirm",
				context.url);
		// parse confirmation information
		TagNode[] list = node.getElementsByName("li", true);
		for (TagNode li : list) {
			TagNode label_node = li.findElementByName("label", true);
			if (label_node != null) {
				// has label
				String label = label_node.getText().toString().trim()
						.toLowerCase();
				TagNode text_node = li.findElementByName("div", true);
				if (text_node != null) {
					// has div
					String text = text_node.getText().toString().trim();
					if (label.equals("from")) {
						page.from = text;
					} else if (label.equals("to")) {
						page.to = text;
					} else if (label.equals("amount")) {
						page.amount = Double.parseDouble(text.substring(1));
					}

				}
			}
		}
		return page;
	}

	/**
	 * Confirm the transfer and get the result.
	 * 
	 * @param context
	 * @return the information on the page like date, lodgement number, receipt
	 *         number, etc.
	 * @throws PageErrorException
	 */
	public static TransferReceiptPage getTransferReceiptPage(
			TransferConfirmPage context) throws PageErrorException {
		AnzMobileUtil.logger.info("<< Transfer Receipt Page >>");
		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				context.form.params);
		TransferReceiptPage page = new TransferReceiptPage();
		page.url = context.form.action;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse confirmation information
		ArrayList<NameValuePair> list = parseLabelValue(node);
		for (NameValuePair p : list) {
			if (p.getName().equals("from")) {
				page.from = p.getValue();
			} else if (p.getName().equals("to")) {
				page.to = p.getValue();
			} else if (p.getName().equals("amount")) {
				page.amount = Double.parseDouble(p.getValue().substring(1));
			} else if (p.getName().equals("date")) {
				page.date = p.getValue();
			} else if (p.getName().equals("lodgement number")) {
				page.lodgement_number = p.getValue();
			} else if (p.getName().equals("receipt number")) {
				page.receipt_number = p.getValue();
			}
		}
		return page;
	}

	private static final String PATTERN_PAYANYONE_DAILY_LIMIT = "[^<]*\\$([0-9,]+)[^<]*";
	private static final String PATTERN_PAYANYONE_PAYEE = "benefDetails\\[([0-9]+)\\]\\[0\\] = \"([^\"]+)\";\\s+benefDetails\\[[0-9]+\\]\\[1\\] = \"([^\"]+)\";\\s+benefDetails\\[[0-9]+\\]\\[2\\] = \"([^\"]+)\";\\s+benefDetails\\[[0-9]+\\]\\[3\\] = \"([^\"]+)\";\\s+";
	private static final Pattern REGEX_PAYANYONE_PAYEE = Pattern
			.compile(PATTERN_PAYANYONE_PAYEE);

	/**
	 * get PayAnyone page
	 * 
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static PayAnyonePage getPayAnyonePage(Page context)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< PayAnyone Page >>");
		TagNode node = AnzMobileUtil.getNode(context.link_payanyone,
				context.url);
		PayAnyonePage page = new PayAnyonePage();
		page.url = context.link_payanyone;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse the form
		page.form = AnzMobileUtil.parseForm(node, "frmInitiatePayAnyone",
				page.url);

		// parse the daily limit
		TagNode[] plist = node.getElementsByName("p", true);
		for (TagNode p : plist) {
			String text = p.getText().toString().trim();
			if (text.indexOf("Pay Anyone limit") >= 0) {
				// Found the limit sentence.
				text = text.replaceFirst(PATTERN_PAYANYONE_DAILY_LIMIT, "$1");
				text = text.replaceAll(",", "");
				page.daily_limit = Double.parseDouble(text);
				break;
			}
		}

		// parse Payee list
		TagNode[] jslist = node.getElementsByName("script", true);
		for (TagNode js_node : jslist) {
			String js = js_node.getText().toString().trim();
			if (js.indexOf("var benefDetails") >= 0) {
				page.payee_list = new ArrayList<PayAnyoneAccount>();
				Matcher matcher = REGEX_PAYANYONE_PAYEE.matcher(js);
				while (matcher.find()) {
					PayAnyoneAccount account = new PayAnyoneAccount();
					account.bsb = matcher.group(2);
					account.number = matcher.group(3);
					account.account_name = matcher.group(4);
					account.description = matcher.group(5);
					if (account.bsb != null && account.bsb.length() > 0
							&& account.number != null
							&& account.number.length() > 0
							&& account.account_name != null
							&& account.account_name.length() > 0) {
						// Only load valid payee
						page.payee_list.add(account);
					}
				}
				if (page.payee_list.size() > 0) {
					AnzMobileUtil.logger.info("Found PayAnyone accounts.");
				}
			}
		}

		if (page.payee_list == null || page.payee_list.size() == 0) {
			AnzMobileUtil.logger
					.warning("Cannot find Pay Anyone account list in this <script>");
			if (jslist.length > 0) {
				for (TagNode js_node : jslist) {
					AnzMobileUtil.logger.finest("[Script Content]\n"
							+ Formatter.toString(js_node.getParent()));
				}
			} else {
				AnzMobileUtil.logger.finest("\n[Page Content]\n"
						+ Formatter.toString(node));
			}
		}

		return page;
	}

	/**
	 * Post PayAnyone form and get the confirm page
	 * 
	 * @param context
	 * @param from
	 * @param from_name
	 * @param payee
	 * @param amount
	 * @param reference
	 * @return
	 * @throws PageErrorException
	 */
	public static PayAnyoneConfirmPage getPayAnyoneConfirmPage(
			PayAnyonePage context, int from, String from_name,
			PayAnyoneAccount payee, double amount, String reference)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< PayAnyone Confirm Page >>");
		// Prepare the post form
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		for (NameValuePair p : context.form.params) {
			String name = p.getName();
			if (name.equals("userName")) {
				params.add(new BasicNameValuePair(name, from_name));
			} else if (name.equals("pymtDrAccDetails")) {
				params.add(new BasicNameValuePair(name, Integer.toString(from)));
			} else if (name.equals("payeeAcctName")) {
				params.add(new BasicNameValuePair(name, payee.account_name));
			} else if (name.equals("payeeBSB")) {
				params.add(new BasicNameValuePair(name, payee.bsb));
			} else if (name.equals("payeeAcid")) {
				params.add(new BasicNameValuePair(name, payee.number));
			} else if (name.equals("payeeName")) {
				params.add(new BasicNameValuePair(name, payee.description));
			} else if (name.equals("Txn_Amt")) {
				params.add(new BasicNameValuePair(name,
						Formatter.WEB_BALANCE_FORMAT.format(amount)));
			} else if (name.equals("txnMemo")) {
				params.add(new BasicNameValuePair(name, reference));
			} else if (name.equals("cpayListIndex")) {
				// without assign this, it will report error and reload
				// PayAnyonePage
				params.add(new BasicNameValuePair(name, "-1"));
			} else {
				params.add(p);
			}
		}

		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				params);
		PayAnyoneConfirmPage page = new PayAnyoneConfirmPage();
		page.url = context.form.action;
		parseFunctionLinks(node, page);
		page.form = AnzMobileUtil.parseForm(node, "PayAnyOneConfirm", page.url);

		// parse the information
		ArrayList<NameValuePair> li_list = parseLabelValue(node);
		page.to = new PayAnyoneAccount();
		for (NameValuePair p : li_list) {
			if (p.getName().equals("from")) {
				page.from = p.getValue();
			} else if (p.getName().equals("your / business name")) {
				page.from_name = p.getValue();
			} else if (p.getName().equals("to")) {
				page.to.description = p.getValue();
			} else if (p.getName().equals("account name")) {
				page.to.account_name = p.getValue();
			} else if (p.getName().equals("account bsb")) {
				page.to.bsb = p.getValue();
			} else if (p.getName().equals("account number")) {
				page.to.number = p.getValue();
			} else if (p.getName().equals("message / reference")) {
				page.reference = p.getValue();
			} else if (p.getName().equals("amount")) {
				try {
					page.amount = Formatter.WEB_BALANCE_FORMAT.parse(
							(p.getValue().trim().substring(1))).doubleValue();
				} catch (ParseException e) {
					page.amount = 0;
					AnzMobileUtil.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return page;
	}

	/**
	 * Confirm the PayAnyone payment and get the result.
	 * 
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static PayAnyoneReceiptPage getPayAnyoneResultPage(
			PayAnyoneConfirmPage context) throws PageErrorException {
		AnzMobileUtil.logger.info("<< PayAnyone Result Page >>");
		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				context.form.params);
		PayAnyoneReceiptPage page = new PayAnyoneReceiptPage();
		page.url = context.form.action;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse confirmation information
		ArrayList<NameValuePair> list = parseLabelValue(node);
		page.to = new PayAnyoneAccount();
		for (NameValuePair p : list) {
			if (p.getName().equals("from")) {
				page.from = p.getValue();
			} else if (p.getName().equals("your / business name")) {
				page.from_name = p.getValue();
			} else if (p.getName().equals("to")) {
				page.to.description = p.getValue();
			} else if (p.getName().equals("account name")) {
				page.to.account_name = p.getValue();
			} else if (p.getName().equals("account bsb")) {
				page.to.bsb = p.getValue();
			} else if (p.getName().equals("account number")) {
				page.to.number = p.getValue();
			} else if (p.getName().equals("message / reference")) {
				page.reference = p.getValue();
			} else if (p.getName().equals("amount")) {
				try {
					page.amount = Formatter.WEB_BALANCE_FORMAT.parse(
							p.getValue().trim().substring(1)).doubleValue();
				} catch (ParseException e) {
					page.amount = 0;
					AnzMobileUtil.logger.log(Level.WARNING, e.getMessage(), e);
				}
			} else if (p.getName().equals("date")) {
				page.date = p.getValue();
			} else if (p.getName().equals("lodgement number")) {
				page.lodgement_number = p.getValue();
			} else if (p.getName().equals("receipt number")) {
				page.receipt_number = p.getValue();
			}
		}
		return page;
	}

	private static final String PATTERN_BPAY_PAYEE = "benefDetails\\[([0-9]+)\\]\\[0\\] = \"([^\"]+)\";\\s+benefDetails\\[[0-9]+\\]\\[1\\] = \"([^\"]+)\";\\s+benefDetails\\[[0-9]+\\]\\[2\\] = \"([^\"]+)\";";
	private static final Pattern REGEX_BPAY_PAYEE = Pattern
			.compile(PATTERN_BPAY_PAYEE);

	/**
	 * get BPay page
	 * 
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static BPayPage getBPayPage(PayAnyonePage context)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< BPay Page >>");
		TagNode node = AnzMobileUtil.getNode(context.link_bpay, context.url);
		BPayPage page = new BPayPage();
		page.url = context.link_bpay;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse the form
		page.form = AnzMobileUtil.parseForm(node, "frmpaybills", page.url);

		// parse Payee list
		TagNode[] jslist = node.getElementsByName("script", true);
		for (TagNode js_node : jslist) {
			String js = js_node.getText().toString().trim();
			if (js.indexOf("var benefDetails") >= 0) {
				page.payee_list = new ArrayList<BPayAccount>();
				Matcher matcher = REGEX_BPAY_PAYEE.matcher(js);
				while (matcher.find()) {
					BPayAccount account = new BPayAccount();
					account.code = matcher.group(2);
					account.reference = matcher.group(3);
					account.description = matcher.group(4);
					if (account.code != null && account.code.length() > 0
							&& account.reference != null
							&& account.reference.length() > 0
							&& account.description != null
							&& account.description.length() > 0) {
						// Only load valid payee
						page.payee_list.add(account);
					}
				}
				if (page.payee_list.size() > 0) {
					AnzMobileUtil.logger.info("Found BPay accounts");
				}
			}
		}

		if (page.payee_list == null || page.payee_list.size() == 0) {
			AnzMobileUtil.logger
					.warning("Cannot find BPay account list in this <script>");
			if (jslist.length > 0) {
				for (TagNode js_node : jslist) {
					AnzMobileUtil.logger.finest("[Script Content]\n"
							+ Formatter.toString(js_node.getParent()));
				}
			} else {
				AnzMobileUtil.logger.finest("\n[Page Content]\n"
						+ Formatter.toString(node));
			}
		}

		// Parse BPay biller's name
		TagNode billers_name = node.findElementByAttValue("name",
				"cpayListIndex1", true, false);
		if (billers_name != null && page.payee_list != null) {
			TagNode[] options = billers_name.getElementsByName("option", true);
			if (options.length - 1 != page.payee_list.size()) {
				AnzMobileUtil.logger.warning("Names count ("
						+ (options.length - 1)
						+ ") is not equal to billers count("
						+ page.payee_list.size() + ").");
			} else {
				for (int i = 0; i < page.payee_list.size(); ++i) {
					String[] contents = options[i + 1].getText().toString()
							.split("\n");
					if (contents.length > 0) {
						page.payee_list.get(i).name = contents[0].trim();
					} else {
						page.payee_list.get(i).name = "";
					}
				}
			}
		} else {
			AnzMobileUtil.logger.warning("Cannot find biller's name.");
			AnzMobileUtil.logger.finest("[Page Content]\n"
					+ Formatter.toString(node));
		}

		return page;
	}

	/**
	 * Post BPay form and get the confirm page
	 * 
	 * @param context
	 * @param from
	 * @param from_name
	 * @param payee
	 * @param amount
	 * @return
	 * @throws PageErrorException
	 */
	public static BPayConfirmPage getBPayConfirmPage(BPayPage context,
			int from, String from_name, BPayAccount payee, double amount)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< BPay Confirm Page >>");
		// Prepare the post form
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		for (NameValuePair p : context.form.params) {
			String name = p.getName();
			if (name.equals("userName")) {
				params.add(new BasicNameValuePair(name, from_name));
			} else if (name.equals("pymtDrAccDetails")) {
				params.add(new BasicNameValuePair(name, Integer.toString(from)));
			} else if (name.equals("Biller_cd")) {
				params.add(new BasicNameValuePair(name, payee.code));
			} else if (name.equals("Consumer_cd")) {
				params.add(new BasicNameValuePair(name, payee.reference));
			} else if (name.equals("Nick_name")) {
				params.add(new BasicNameValuePair(name, payee.description));
			} else if (name.equals("Txn_Amt")) {
				params.add(new BasicNameValuePair(name,
						Formatter.WEB_BALANCE_FORMAT.format(amount)));
			} else if (name.equals("cpayListIndex1")) {
				// without assign this, it will report error and reload
				// BPayPage
				params.add(new BasicNameValuePair(name, "-1"));
			} else {
				params.add(p);
			}
		}

		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				params);
		BPayConfirmPage page = new BPayConfirmPage();
		page.url = context.form.action;
		parseFunctionLinks(node, page);
		page.form = AnzMobileUtil.parseForm(node, "BillPaymentConfirm",
				page.url);

		// parse the information
		ArrayList<NameValuePair> li_list = parseLabelValue(node);
		page.to = new BPayAccount();
		for (NameValuePair p : li_list) {
			if (p.getName().equals("from")) {
				page.from = p.getValue();
			} else if (p.getName().equals("to")) {
				page.to.description = p.getValue();
			} else if (p.getName().equals("biller code")) {
				page.to.code = p.getValue();
			} else if (p.getName().equals("reference")) {
				page.to.reference = p.getValue();
			} else if (p.getName().equals("amount")) {
				try {
					page.amount = Formatter.WEB_BALANCE_FORMAT.parse(
							p.getValue().trim().substring(1)).doubleValue();
				} catch (ParseException e) {
					page.amount = 0;
					AnzMobileUtil.logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return page;
	}

	/**
	 * Confirm the BPay payment and get the result.
	 * 
	 * @param context
	 * @return
	 * @throws PageErrorException
	 */
	public static BPayReceiptPage getBPayReceiptPage(BPayConfirmPage context)
			throws PageErrorException {
		AnzMobileUtil.logger.info("<< BPay Receipt Page >>");
		TagNode node = AnzMobileUtil.getNode(context.form.action, context.url,
				context.form.params);
		BPayReceiptPage page = new BPayReceiptPage();
		page.url = context.form.action;

		// parse the function links
		parseFunctionLinks(node, page);

		// parse confirmation information
		ArrayList<NameValuePair> list = parseLabelValue(node);
		page.to = new BPayAccount();
		for (NameValuePair p : list) {
			if (p.getName().equals("from")) {
				page.from = p.getValue();
			} else if (p.getName().equals("your / business name")) {
				page.from_name = p.getValue();
			} else if (p.getName().equals("to")) {
				page.to.description = p.getValue();
			} else if (p.getName().equals("biller code")) {
				page.to.code = p.getValue();
			} else if (p.getName().equals("reference")) {
				page.to.reference = p.getValue();
			} else if (p.getName().equals("amount")) {
				try {
					page.amount = Formatter.WEB_BALANCE_FORMAT.parse(
							p.getValue().trim().substring(1)).doubleValue();
				} catch (ParseException e) {
					page.amount = 0;
					AnzMobileUtil.logger.log(Level.WARNING, e.getMessage(), e);
				}
			} else if (p.getName().equals("date")) {
				page.date = p.getValue();
			} else if (p.getName().equals("lodgement number")) {
				page.lodgement_number = p.getValue();
			} else if (p.getName().equals("receipt number")) {
				page.receipt_number = p.getValue();
			}
		}
		return page;
	}

	/**
	 * Fetch $name and $value in following form: "<li>
	 * <label>$name</label><div>$value</div></li>"
	 * 
	 * @param node
	 * @return
	 */
	private static ArrayList<NameValuePair> parseLabelValue(TagNode node) {
		TagNode[] list = node.getElementsByName("li", true);
		ArrayList<NameValuePair> array = new ArrayList<NameValuePair>();
		for (TagNode li : list) {
			TagNode label_node = li.findElementByName("label", true);
			if (label_node != null) {
				// has label
				String label = label_node.getText().toString().trim()
						.toLowerCase();
				TagNode text_node = li.findElementByName("div", true);
				if (text_node != null) {
					// has div
					String text = text_node.getText().toString().trim();
					array.add(new BasicNameValuePair(label, text));
				}
			}
		}
		return array;
	}

}
