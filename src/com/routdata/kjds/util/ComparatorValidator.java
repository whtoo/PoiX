package com.routdata.kjds.util;
import java.util.Comparator;

public class ComparatorValidator implements Comparator<Object> {
 
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		CellValidator tmp1 = (CellValidator)o1;
		CellValidator tmp2 = (CellValidator)o2;
		
		int flag = tmp1.getMappedIndex() > tmp2.getMappedIndex()?1:0;
		
		return flag;
	}

}
