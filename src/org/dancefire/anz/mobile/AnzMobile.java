package org.dancefire.anz.mobile;

import java.util.ArrayList;

public class AnzMobile {
	// members
	private String m_userid;
	private String m_password;
	private ArrayList<Account> m_account_list;
	private ArrayList<BPayAccount> m_bpay_account_list;

	private ArrayList<PayAnyoneAccount> m_payanyone_account_list;
	private double m_daily_limit;

	private Page m_current_page;
	private long m_last_access;
	private boolean m_login = false;

	/**
	 * Create an agent to perform mobile internet banking action.
	 * 
	 * @param userid
	 *            - A valid CRN which should be validate before pass it to
	 *            constructor.
	 * @param password
	 *            - A valid password which should validate before pass it to
	 *            constructor
	 */
	public AnzMobile(String userid, String password) {
		this.m_userid = userid;
		this.m_password = password;
		AnzMobileUtil.logger.fine("new AnzMobile: " + userid + "/" + password);
	}

	/**
	 * Login to the ANZ Mobile banking and get account number from Transfer page
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized boolean login() throws PageErrorException {
		if (!isValid()) {
			AnzMobileUtil.logger.severe("Username or Password is not valid.");
			return false;
		}

		// get the login form
		Form login_form = Parser.getLoginForm();

		// post it back with identity and get the response
		AccountsPage page = Parser.login(m_userid, m_password,
				login_form.action, login_form.params);
		this.m_account_list = page.accounts;
		m_login = true;
		update(page);
		getAccountNumber();
		return true;
	}

	/**
	 * Get account numbers, which is in Transfer page
	 * 
	 * @throws PageErrorException
	 */
	public synchronized void getAccountNumber() throws PageErrorException {
		validate();

		TransferPage page = Parser.getTransferPage(this.m_current_page);
		update(page);
		if (page.account_numbers.length == this.m_account_list.size()) {
			for (int i = 0; i < this.m_account_list.size(); ++i) {
				this.m_account_list.get(i).index = i;
				this.m_account_list.get(i).number = page.account_numbers[i];
			}
		}
	}

	public synchronized void logout() {
		if (m_login
				&& System.currentTimeMillis() - this.m_last_access < TIME_EXPIRED
				&& m_current_page != null) {
			Parser.logout(m_current_page);
		}

		m_login = false;
		m_current_page = null;
	}

	/**
	 * Get transactions of a specified account.
	 * 
	 * @param account
	 * @return Return account transactions of the first page.
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<Transaction> getTransactions(Account account)
			throws PageErrorException {
		validate();

		// To get the transaction page we have to make sure access accounts
		// page
		// immediately before access transaction page
		if (!(m_current_page instanceof AccountsPage)) {
			AccountsPage page_account = Parser.getAccountsPage(m_current_page);
			update(page_account);
			account.link = page_account.accounts.get(account.index).link;
		}

		TransactionPage page_transaction = Parser.getTransactionPage(account,
				m_current_page);
		update(page_transaction);

		account.current_page = page_transaction;
		account.transactions = page_transaction.transactions;
		return page_transaction.transactions;
	}

	/**
	 * Should be called immediately after getTransactions().
	 * 
	 * @param account
	 * @return Return today's transactions.
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<Transaction> getTodayTransaction(
			Account account) throws PageErrorException {
		validate();

		if (account.current_page.link_today != null
				&& account.current_page.link_today.length() > 0) {
			TransactionPage page_today = Parser.getTodayTransactionPage(
					account, account.current_page);
			update(page_today);

			// To access previous transaction page again, we should click
			// back.
			// So we set the
			// account link to 'back'
			account.link = page_today.link_back;
			TransactionPage page_previous = Parser.getTransactionPage(account,
					page_today);
			update(page_previous);

			account.current_page = page_previous;

			// Update today's transactions to account's transactions
			if (page_today.transactions != null
					&& page_today.transactions.size() > 0) {
				ArrayList<Transaction> old_trans = new ArrayList<Transaction>();
				old_trans.addAll(account.transactions);
				account.transactions.clear();
				account.transactions.addAll(page_today.transactions);
				account.transactions.addAll(old_trans);
			}

			return page_today.transactions;
		} else {
			return null;
		}
	}

	/**
	 * Get more transactions from an account. Only available when the global
	 * current page is same as account's latest page.
	 * 
	 * @param account
	 * @return more transactions, or null if fail.
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<Transaction> getMoreTransactions(
			Account account) throws PageErrorException {
		validate();
		if (account.current_page == this.m_current_page) {
			TransactionPage page = Parser.getMoreTransactionPage(account,
					(TransactionPage) this.m_current_page);
			update(page);
			account.current_page = page;
			account.transactions.addAll(page.transactions);
			return page.transactions;
		} else {
			System.out
					.println("getMoreTransactions(): account.current_page != this.m_current_page.");
			return null;
		}
	}

	/**
	 * Transfer funds between accounts.
	 * 
	 * @param from
	 * @param to
	 * @param amount
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized TransferReceiptPage transfer(Account from, Account to,
			double amount) throws PageErrorException {
		validate();
		TransferPage page_transfer = Parser
				.getTransferPage(this.m_current_page);
		update(page_transfer);
		// call transfer() and get the confirm page
		AnzMobileUtil.logger.finer("[Before] Parser.transfer");
		TransferConfirmPage page_confirm = Parser.transfer(page_transfer,
				from.index, to.index, amount);
		AnzMobileUtil.logger.finer("[After ] Parser.transfer");
		update(page_confirm);
		AnzMobileUtil.logger.finest(Formatter.toString(page_confirm));

		// confirm the transfer and get the result page.
		AnzMobileUtil.logger.finer("[Before] Parser.getTransferReceiptPage");
		TransferReceiptPage page_receipt = Parser
				.getTransferReceiptPage(page_confirm);
		AnzMobileUtil.logger.finer("[After ] Parser.getTransferReceiptPage");
		update(page_receipt);
		AnzMobileUtil.logger.finest(Formatter.toString(page_receipt));

		// After transfer, we need update the accounts balances
		updateAccounts();

		return page_receipt;
	}

	/**
	 * Pay Anyone
	 * 
	 * @param from
	 *            - From which account
	 * @param name
	 *            - Account name shows on payee's transaction
	 * @param payee
	 *            - payee
	 * @param amount
	 * @param reference
	 *            - Reference/Description shows on payee's transaction
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized PayAnyoneReceiptPage payAnyone(Account from,
			String name, PayAnyoneAccount payee, double amount, String reference)
			throws PageErrorException {
		validate();

		PayAnyonePage page_anyone = Parser
				.getPayAnyonePage(this.m_current_page);
		update(page_anyone);

		PayAnyoneConfirmPage page_confirm = Parser.getPayAnyoneConfirmPage(
				page_anyone, from.index, name, payee, amount, reference);
		update(page_confirm);

		PayAnyoneReceiptPage page_receipt = Parser
				.getPayAnyoneResultPage(page_confirm);
		update(page_receipt);

		// After transfer, we need update the accounts balances
		updateAccounts();

		return page_receipt;
	}

	/**
	 * Pay bills by BPay.
	 * 
	 * @param from
	 * @param from_name
	 * @param payee
	 * @param amount
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized BPayReceiptPage payBills(Account from,
			BPayAccount payee, double amount) throws PageErrorException {
		validate();

		PayAnyonePage page_anyone = Parser.getPayAnyonePage(m_current_page);
		update(page_anyone);

		BPayPage page_bpay = Parser.getBPayPage(page_anyone);
		update(page_bpay);

		BPayConfirmPage page_confirm = Parser.getBPayConfirmPage(page_bpay,
				from.index, payee, amount);
		update(page_confirm);

		BPayReceiptPage page_receipt = Parser.getBPayReceiptPage(page_confirm);
		update(page_receipt);

		// After transfer, we need update the accounts balances
		updateAccounts();

		return page_receipt;
	}

	/**
	 * get Account by index
	 * 
	 * @param index
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized Account getAccount(int index) throws PageErrorException {
		validate();
		return this.m_account_list.get(index);
	}

	/**
	 * Return the account list
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<Account> getAccounts()
			throws PageErrorException {
		validate();
		return this.m_account_list;
	}

	/**
	 * Get the Pay Anyone pre-saved account list.
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<PayAnyoneAccount> getPayAnyoneAccounts()
			throws PageErrorException {
		validate();

		if (this.m_payanyone_account_list == null
				|| this.m_payanyone_account_list.size() == 0) {
			updatePayAnyoneAccounts();
		}

		return this.m_payanyone_account_list;
	}

	/**
	 * Get the BPay pre-saved account list.
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized ArrayList<BPayAccount> getBPayAccounts()
			throws PageErrorException {
		validate();

		if (this.m_bpay_account_list == null
				|| this.m_bpay_account_list.size() == 0) {
			updateBPayAccounts();
		}

		return this.m_bpay_account_list;
	}

	/**
	 * Get the daily limit from the Pay Anyone page
	 * 
	 * @return
	 * @throws PageErrorException
	 */
	public synchronized double getPayAnyoneDailyLimit()
			throws PageErrorException {
		validate();

		if (this.m_daily_limit <= 0) {
			updatePayAnyoneAccounts();
		}

		return this.m_daily_limit;
	}

	/**
	 * update this.m_current_page to page, and update the last access time.
	 * 
	 * @param page
	 */
	private void update(Page page) {
		if (page != null) {
			this.m_current_page = page;
			this.m_last_access = System.currentTimeMillis();
		}
	}

	private static final long TIME_EXPIRED = 3 * 60 * 1000;

	/**
	 * check m_last_access to make sure it's within 3 mins, otherwise login
	 * again. also check whether m_account_list is valid.
	 * 
	 * @throws PageErrorException
	 */
	private void validate() throws PageErrorException {
		if (!m_login
				|| System.currentTimeMillis() - this.m_last_access > TIME_EXPIRED
				|| m_account_list == null || m_account_list.size() == 0) {
			// not logged in or expired, need login again.
			login();
		}
	}

	private void updateAccounts() throws PageErrorException {
		AccountsPage page = Parser.getAccountsPage(m_current_page);
		if (page == null) {
			System.out
					.println("[updateBalance()]: Cannot get the account page.");
		} else if (page.hasError()) {
			AnzMobileUtil.logger.severe("[updateBalance()]: Got Error ["
					+ page.getErrorString() + "]");
		} else if (page.accounts.size() != m_account_list.size()) {
			System.out
					.println("[updateBalance()]: Account list size didn't match. ["
							+ m_account_list.size()
							+ "] => ["
							+ page.accounts.size() + "]");
		} else {
			for (int i = 0; i < m_account_list.size(); ++i) {
				Account o = m_account_list.get(i);
				Account n = page.accounts.get(i);
				if (!o.name.equals(n.name)) {
					System.out
							.println("[updateBalance()]: Account name doesn't match. ["
									+ i
									+ "] '"
									+ o.name
									+ "' => '"
									+ n.name
									+ "'");
				} else {
					o.available_balance = n.available_balance;
					o.current_balance = n.current_balance;
					o.link = n.link;
					o.current_page = null;
				}
			}
			update(page);
		}

	}

	private void updatePayAnyoneAccounts() throws PageErrorException {
		PayAnyonePage page = Parser.getPayAnyonePage(m_current_page);
		update(page);

		if (page.payee_list != null) {
			this.m_payanyone_account_list = page.payee_list;
		} else {
			this.m_payanyone_account_list = new ArrayList<PayAnyoneAccount>();
		}
		this.m_daily_limit = page.daily_limit;
	}

	private void updateBPayAccounts() throws PageErrorException {
		// Go to Pay Anyone page to get the BPay page link
		if (!(m_current_page instanceof PayAnyonePage)) {
			updatePayAnyoneAccounts();
		}

		BPayPage page_bpay = Parser.getBPayPage((PayAnyonePage) m_current_page);
		update(page_bpay);

		if (page_bpay.payee_list != null) {
			this.m_bpay_account_list = page_bpay.payee_list;
		} else {
			this.m_bpay_account_list = new ArrayList<BPayAccount>();
		}
	}

	public boolean isValid() {
		if (!AnzMobileUtil.isUseridValid(m_userid)
				|| !AnzMobileUtil.isPasswordValid(m_password)) {
			return false;
		} else {
			return true;
		}
	}
}
