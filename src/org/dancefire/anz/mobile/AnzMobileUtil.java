package org.dancefire.anz.mobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.net.http.AndroidHttpClient;

public class AnzMobileUtil {
	public static Logger logger = null;
	static {
		logger = Logger.getLogger("ANZ");
		logger.setLevel(Level.ALL);
	}

	public static HtmlCleaner parser;
	static {
		AnzMobileUtil.parser = new HtmlCleaner();
		CleanerProperties props = new CleanerProperties();
		props.setOmitComments(true);
		props.setOmitDoctypeDeclaration(true);
	}

	/**
	 * Get string content from an InputStream object
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String toString(InputStream in) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		in.close();
		return sb.toString();
	}

	public static String getLink(String link, String referer) {
		try {
			return new URL(new URL(referer), link).toString();
		} catch (MalformedURLException e) {
			AnzMobileUtil.logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return "";
	}

	private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 1.0; en-us; generic) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2";
	private static final int MAX_RETRY = 3;

	/**
	 * Execute HTTP request and parse the response to TagNode. The function will
	 * retry MAX_RETRY times if it received network IO exception.
	 * 
	 * @param request
	 * @return the retrieved page in TagNode
	 * @throws PageErrorException
	 */
	public static TagNode execute(HttpUriRequest request)
			throws PageErrorException {
		AndroidHttpClient client;
		try {
			client = AndroidHttpClient.newInstance(USER_AGENT);
		} catch (RuntimeException ex) {
			throw new PageErrorException(ex);
		}

		int count = 0;
		while (true) {
			try {
				logger.fine("\t [Timeout] : "
						+ HttpConnectionParams.getConnectionTimeout(client
								.getParams()));
				HttpResponse resp = client.execute(request);
				TagNode node = AnzMobileUtil.parser.clean(resp.getEntity()
						.getContent());
				logger.fine("\t [Length] : "
						+ (Formatter.toString(node).length() / 1024) + " KB");
				return node;
			} catch (ClientProtocolException e) {
				// Cannot retry protocol failure.
				AnzMobileUtil.logger.log(Level.SEVERE, e.getMessage(), e);
				throw new PageErrorException(e);
			} catch (IOException e) {
				// retry 3 times if received network IO exception.
				if (count < MAX_RETRY) {
					logger.warning("Received IOException: [" + e.toString()
							+ "]. Retry now... (" + (count + 1) + ")");
					++count;
					// Waiting for a second for another go.
					try {
						Thread.sleep(3 * 1000);
					} catch (InterruptedException e1) {
						throw new PageErrorException(e1);
					}
				} else {
					// reached MAX_RETRY, then throw the exception.
					throw new PageErrorException(e);
				}
			} catch (RuntimeException e) {
				throw new PageErrorException(e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}

	/**
	 * HTTP GET a TagNode of a HTML web page from an URL with specified referer
	 * 
	 * @param url
	 * @param referer
	 * @return
	 * @throws PageErrorException
	 */
	static TagNode getNode(String url, String referer)
			throws PageErrorException {
		if (url == null || url.length() == 0) {
			throw new PageErrorException("Cannot retrieve page [" + url + "]");
		}
		HttpGet get = new HttpGet(url);
		get.addHeader("Referer", referer);

		if (AnzMobileUtil.logger.isLoggable(Level.FINE)) {
			// Print URL
			AnzMobileUtil.logger.fine("[GET] " + url);
			// Print Headers
			for (Header h : get.getAllHeaders()) {
				AnzMobileUtil.logger.fine("\t [Header] " + h.getName() + ":["
						+ h.getValue() + "]");
			}
		}
		return execute(get);
	}

	/**
	 * HTTP POST to get a TagNode of HTML web page
	 * 
	 * @param url
	 * @param referer
	 * @param params
	 *            - post form parameters
	 * @return
	 * @throws PageErrorException
	 */

	static TagNode getNode(String url, String referer,
			ArrayList<NameValuePair> params) throws PageErrorException {
		if (url == null || url.length() == 0) {
			throw new PageErrorException("Cannot retrieve page [" + url + "]");
		}
		HttpPost post = new HttpPost(url);
		post.addHeader("Referer", referer);
		try {
			post.setEntity(new UrlEncodedFormEntity(params));

			if (AnzMobileUtil.logger.isLoggable(Level.FINE)) {
				// Print URL
				AnzMobileUtil.logger.fine("[POST] " + url);
				// Print Headers
				for (Header h : post.getAllHeaders()) {
					AnzMobileUtil.logger.fine("\t [Header] " + h.getName()
							+ ":[" + h.getValue() + "]");
				}
				// Print forms query
				for (NameValuePair item : params) {
					AnzMobileUtil.logger.fine("\t [Form] " + item.getName()
							+ ":[" + item.getValue() + "]");
				}
			}
		} catch (UnsupportedEncodingException e) {
			throw new PageErrorException(e);
		}
		return execute(post);
	}

	/**
	 * Parse a form from TagNode by specify the name
	 * 
	 * @param node
	 * @param form_name
	 * @return
	 * @throws PageErrorException
	 */
	static Form parseForm(TagNode node, String form_name, String referer)
			throws PageErrorException {
		Form form = new Form();
		TagNode form_node = node.findElementByAttValue("name", form_name, true,
				false);
		if (form_node != null) {
			// get action
			form.action = getLink(form_node.getAttributeByName("action"),
					referer);
			// get inputs
			TagNode[] inputs = form_node.getElementsByName("input", true);
			form.params = new ArrayList<NameValuePair>();
			for (TagNode i : inputs) {
				String name = i.getAttributeByName("name");
				String value = i.getAttributeByName("value");
				if (value == null) {
					value = "";
				}
				form.params.add(new BasicNameValuePair(name, value));
			}
			// get selects
			TagNode[] selects = form_node.getElementsByName("select", true);
			for (TagNode i : selects) {
				String name = i.getAttributeByName("name");
				form.params.add(new BasicNameValuePair(name, ""));
			}
			return form;
		} else {
			throw new PageErrorException("Cannot find form [" + form_name + "]");
		}
	}

	/**
	 * Check password is valid * Length should between 8 and 16 * Can only
	 * contain [0-9A-Za-z]
	 * 
	 * @param password
	 * @return return true if it's valid, otherwise false.
	 */
	public static boolean isPasswordValid(String password) {
		int length = password.length();
		if (length < 8 || length > 16)
			return false;

		for (char c : password.toCharArray()) {
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check CRN is valid or not * Length should be 9, 15 or 16. * All are
	 * digits.
	 * 
	 * @param userid
	 * @return return true if it's valid, otherwise false.
	 */
	public static boolean isUseridValid(String userid) {
		int length = userid.length();
		if (length != 9 && length != 15 && length != 16) {
			AnzMobileUtil.logger.severe("User ID's length is invalid.");
			return false;
		}
		for (char c : userid.toCharArray()) {
			if (!Character.isDigit(c)) {
				AnzMobileUtil.logger
						.severe("There should only contains numbers in the user id");
				return false;
			}
		}
		return true;
	}

	/**
	 * Clean CRN by removing '-' and ' '
	 * 
	 * @param userid
	 *            - CRN
	 * @return cleaned CRN
	 */
	public static String cleanUserid(String userid) {
		StringBuilder sb = new StringBuilder();
		for (char c : userid.toCharArray()) {
			if (c != '-' && c != ' ') {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
