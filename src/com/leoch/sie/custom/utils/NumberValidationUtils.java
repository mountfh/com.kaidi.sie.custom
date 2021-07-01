package com.leoch.sie.custom.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberValidationUtils {

	private static boolean isMatch(String regex, String orginal) {
		if (orginal == null || orginal.trim().equals("")) {
			return false;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher isNum = pattern.matcher(orginal);
		return isNum.matches();
	}

	// 正整数
	public static boolean isPositiveInteger(String orginal) {
		return isMatch("^\\+{0,1}[1-9]\\d*", orginal);
	}

	// 负整数
	public static boolean isNegativeInteger(String orginal) {
		return isMatch("^-[1-9]\\d*", orginal);
	}

	// 整数
	public static boolean isWholeNumber(String orginal) {
		return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal);
	}

	// 正小数
	public static boolean isPositiveDecimal(String orginal) {
		return isMatch("\\+{0,1}[0]\\.[1-9]*|\\+{0,1}[1-9]\\d*\\.\\d*", orginal);
	}

	// 负小数
	public static boolean isNegativeDecimal(String orginal) {
		return isMatch("^-[0]\\.[1-9]*|^-[1-9]\\d*\\.\\d*", orginal);
	}

	// 小数
	public static boolean isDecimal(String orginal) {
		return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);
	}

	// 实数
	public static boolean isRealNumber(String orginal) {
		return isWholeNumber(orginal) || isDecimal(orginal);
	}
	
	// 小数点前1-10位，小数点后1-3位的正小数
	public static boolean isQuantityNumber(String orginal) {
		return isMatch("^[+]{0,1}[0-9]{1,10}?(.[0-9]{1,3})?$", orginal);
	}
	
	// 小数点前1-10位，小数点后1-3位的小数
	public static boolean isQuantityNumber1(String orginal) {
		return isMatch("^[-+]{0,1}[0-9]{1,10}?(.[0-9]{1,3})?$", orginal);
	}
	
	// 小数点前1-6位，小数点后1-7位的小数
	public static boolean isQuantityNumber2(String orginal) {
		return isMatch("^[-+]{0,1}[0-9]{1,6}?(.[0-9]{1,7})?$", orginal);
	}

}