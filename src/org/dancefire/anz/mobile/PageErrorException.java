package org.dancefire.anz.mobile;


public class PageErrorException extends Exception {
	private static final long serialVersionUID = 4620539301874699148L;
	private Page page = null;
	
	public PageErrorException(Throwable t) {
		super(t);
		this.page = null;
	}
	
	public PageErrorException(String message) {
		super(message);
		this.page = null;
	}
	public PageErrorException(Page page) {
		super(page.getErrorString());
		this.page = page;
	}
	
	public Page getPage() {
		return this.page;
	}
}
