package org.dancefire.anz.mobile;

import java.util.ArrayList;

public class Page {
	public String url;
	public String link_back;
	public String link_logout;
	public String link_accounts;
	public String link_transfer;
	public String link_payanyone;
	public String link_bpay;
	public Form logout_form;
	public ArrayList<String> error_messages;

	public boolean hasError() {
		return error_messages.size() > 0;
	}

	public String getErrorString() {
		StringBuilder sb = new StringBuilder();
		if (error_messages != null) {
			for (String message : error_messages) {
				sb.append(message.trim() + "\n");
			}
		}
		return sb.toString();
	}
}