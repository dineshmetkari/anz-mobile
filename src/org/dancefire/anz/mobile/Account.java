package org.dancefire.anz.mobile;

import java.util.ArrayList;

/**
 * Bank Account information
 * 
 */
public class Account {
	public int index;
	public String name;
	public String number;
	public double current_balance;
	public double available_balance;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public String link;
	public TransactionPage current_page;
}