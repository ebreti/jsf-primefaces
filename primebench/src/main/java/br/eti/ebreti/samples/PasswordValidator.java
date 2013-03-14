package br.eti.ebreti.samples;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
	private Pattern pattern;
	private Matcher matcher;

	private static final String PASSWORD_PATTERN = "((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";

	public PasswordValidator() {
		pattern = Pattern.compile(PASSWORD_PATTERN);
	}

	/**
	 * Validate hex with regular expression
	 * 
	 * @param hex
	 *            hex for validation
	 * @return true valid hex, false invalid hex
	 */
	public boolean validate(final String hex) {
		matcher = pattern.matcher(hex);
		return matcher.matches();
	}

}
