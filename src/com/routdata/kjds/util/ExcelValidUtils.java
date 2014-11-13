package com.routdata.kjds.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * excel校验
 * @author vivi207
 * @2014-7-25 下午03:05:58
 */
public class ExcelValidUtils {
	
	public static final Logger LOGGER  = Logger.getLogger(ExcelValidUtils.class);
	public static final String IS_NUMBER  = "^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$";
	
	private static final HashMap<String,Method> validMethodMap = new HashMap();
	static{
		try {
			validMethodMap.put("notnull", ExcelValidUtils.class.getDeclaredMethod("validNotNull", String.class, int.class));
			validMethodMap.put("number", ExcelValidUtils.class.getDeclaredMethod("validIsNumber", String.class, int.class));
			validMethodMap.put("maxlength", ExcelValidUtils.class.getDeclaredMethod("validMaxLength", String.class, int.class));
		} catch (Exception e) {
			LOGGER.error("", e);
		} 
	}
	
	/**
	 * 校验数据
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public List<String[]> validData(List<String[]> data, List<String[]> valids, int[] maxLength) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		List<String[]> errors = new ArrayList();
		int len = data.size();
		for(int i = 0; i < len; i++){
			String[] row = data.get(i);
			if(row.length!=maxLength.length){
				errors.add(new String[]{String.valueOf(i+1),"整行数据格式错误"});
				continue;
			}
			for (int j = 0; j < row.length; j++) {
				String[] validNames = valids.get(j);
				for(String validName : validNames){
					Method m = validMethodMap.get(validName);
					Object o = m.invoke(ExcelValidUtils.class, row[j], maxLength[j]);
					if(o!=null){
						errors.add(new String[]{String.valueOf(i+1),String.valueOf(j+1),String.valueOf(o)});
						continue;
					}
				}
			}
		}
		return errors;
	}
	
	/**
	 * excel数据转List
	 */
	public static List<String[]> excelToList(File file){
		return null;
	}
	
	
	public static String validNotNull(String val, int len){
		return val==null?" 不能为空":null;
	}
	public static String validIsNumber(String val, int len){
		return val.matches(IS_NUMBER)?"请输入正确的数字":null;
	}
	public static String validMaxLength(String val, int len){
		return val.length()>len?"长度不能超过"+len+"个字符":null;
	}
}
