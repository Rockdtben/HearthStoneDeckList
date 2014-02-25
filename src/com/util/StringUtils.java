package com.util;

import java.util.Collection;

public class StringUtils {
	
	public static String join(Collection<String> collection, String separator) {
		StringBuilder sb = new StringBuilder(500);
		for (String s : collection) {
			sb.append(s + separator);
		}
		return sb.toString();
	}
}
