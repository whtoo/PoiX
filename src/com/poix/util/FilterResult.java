package com.poix.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class FilterResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2492211148618432691L;
	private List<Map<String,Object>> dataList;
	private List<List<String>> discardIndexList;
	private Long totalSize;
	/**
	 * @return the dataList
	 */
	public List<Map<String,Object>> getDataList() {
		return dataList;
	}
	/**
	 * @param dataList the dataList to set
	 */
	public void setDataList( List<Map<String,Object>> dataList) {
		this.dataList = dataList;
	}
	/**
	 * @return the totalSize
	 */
	public Long getTotalSize() {
		return totalSize;
	}
	/**
	 * @param totalSize the totalSize to set
	 */
	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}
	/**
	 * @return the discardIndexList
	 */
	public List<List<String>> getDiscardIndexList() {
		return discardIndexList;
	}
	/**
	 * @param discardIndexList the discardIndexList to set
	 */
	public void setDiscardIndexList(List<List<String>> discardIndexList) {
		this.discardIndexList = discardIndexList;
	}
}
