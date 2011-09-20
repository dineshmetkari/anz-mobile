package org.dancefire.anz.mobile;

public class PayAnyoneAccount {
	public String bsb;
	public String number;
	public String account_name;
	public String description;

	@Override
	public String toString() {
		return "[" + this.bsb + " " + this.number + "]\t[" + this.account_name
				+ "] \t [" + this.description + "]\n";
	}
}