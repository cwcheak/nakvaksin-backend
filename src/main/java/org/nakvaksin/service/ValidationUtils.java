package org.nakvaksin.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {
    private final static String EMAIL_VALIDATOR_REGEX = "^[a-zA-Z0-9.!#$%&'*+/=?^_`\\{|\\}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";

    public static boolean isValidEmail(String email) {
        final Pattern pattern = Pattern.compile(EMAIL_VALIDATOR_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
