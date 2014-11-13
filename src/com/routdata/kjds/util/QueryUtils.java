package com.routdata.kjds.util;

import java.util.List;
import java.util.Map;

/**
 * 查询帮助类
 * @author vivi207
 * @2014-7-25 上午10:47:39
 */
public class QueryUtils {
	/**
	 * 获取分页参数
	 * @param queryParams
	 * @return
	 */
	public static int[] getPageNoOrSize(Map queryParams){
		int pageNo = 1;
		int pageSize = 100;
		if(queryParams!=null){
			if(queryParams.get("pageNo")!=null && !"".equals(queryParams.get("pageNo"))){
				pageNo = Integer.parseInt(queryParams.get("pageNo").toString());
			}
			if(queryParams.get("pageSize")!=null && !"".equals(queryParams.get("pageSize"))){
				pageSize = Integer.parseInt(queryParams.get("pageSize").toString());
			}
		}
		return new int[]{pageNo,pageSize};
	}
	
	/**
	 * 查询条件参数绑定
	 * @param sql
	 * @param params
	 * @param whereSql
	 * @param val
	 */
	public static void bindWhere(StringBuffer sql , List params, String whereSql, Object val){
		bindWhere(sql, params, whereSql, val, null);
	}
	
	/**
	 * 查询条件参数绑定
	 * @param sql
	 * @param params
	 * @param whereSql
	 * @param val
	 * @param val2
	 */
	public static void bindWhere(StringBuffer sql , List params, String whereSql, Object val, Object val2){
		if(val==null || "".equals(val)){
			return ;
		}
		sql.append(whereSql);
		params.add(val2!=null?val2:val);
	}
	
	public static void bindWhereIn(StringBuffer sql , List params, String whereSql, Object[] val){
		String insql = buildWhereIn(params, val);
		if(!"".equals(insql)){
			sql.append(whereSql).append(insql);
		}
	}
	
	public static String buildWhereIn(List params, Object[] val){
		if(val==null || val.length==0){
			return "";
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" in (");
		String m = "";
		for(Object obj : val){
			sql.append(m).append("?");
			params.add(obj);
			m = ",";
		}
		sql.append(") ");
		return sql.toString();
	}
	
	
}
