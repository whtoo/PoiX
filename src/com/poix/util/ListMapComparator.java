package com.poix.util;

import java.util.Comparator;
import java.util.Map;

public class ListMapComparator implements Comparator<Map<String,Object>> {

	private String groupColum = "";
	

	@Override
	public int compare(Map<String, Object> o1, Map<String, Object> o2) {
		int flag = 0;
		String f = o1.get(this.groupColum).toString();
		String t = o2.get(this.groupColum).toString();
		flag = f.compareTo(t);
			
		return flag;
	}

}
