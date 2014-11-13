package com.routdata.kjds.util;

import java.util.Map;
import java.util.regex.Pattern;

public class Constants {
	//单位
	public static Map<String,String> unitLevel;
	
	//判断是否为数字
	public static boolean isNumeric(String str){
		Pattern pattern = Pattern.compile("^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$");
		return pattern.matcher(str).matches();
	} 

}
