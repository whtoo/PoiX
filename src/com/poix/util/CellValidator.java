package com.poix.util;

public class CellValidator {
	/**
	 * 映射excel中的第几列
	 */
	private int mappedIndex;
	/**
	 * 是否必须字段
	 */
	private Boolean isRequaried;
	/***
	 * 字段读入类型
	 */
	private String typeStr;
	/***
	 * 字段验证正则表达式
	 */
	private String regPatternStr;
	private String matchMode = "union";
	/**
	 * 是否检验长度
	 * */
	private Boolean isLenChecked;
	private int maxLen;
	private int minLen;
	
	public CellValidator(){
		
	}
	
	/**
	 * @return the regPatternStr
	 */
	public String getRegPatternStr() {
		return regPatternStr;
	}
	/**
	 * @param regPatternStr the regPatternStr to set
	 */
	public void setRegPatternStr(String regPatternStr) {
		this.regPatternStr = regPatternStr;
	}
	/**
	 * @return the typeStr
	 */
	public String getTypeStr() {
		return typeStr;
	}
	/**
	 * @param typeStr the typeStr to set
	 */
	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}
	/**
	 * @return the isRequaried
	 */
	public Boolean getIsRequaried() {
		return isRequaried;
	}
	/**
	 * @param isRequaried the isRequaried to set
	 */
	public void setIsRequaried(Boolean isRequaried) {
		this.isRequaried = isRequaried;
	}
	/**
	 * @return the mappedIndex
	 */
	public int getMappedIndex() {
		return mappedIndex;
	}
	/**
	 * @param mappedIndex the mappedIndex to set
	 */
	public void setMappedIndex(int mappedIndex) {
		this.mappedIndex = mappedIndex;
	}

	/**
	 * @return the matchMode
	 */
	public String getMatchMode() {
		return matchMode;
	}

	/**
	 * @param matchMode the matchMode to set
	 */
	public void setMatchMode(String matchMode) {
		this.matchMode = matchMode;
	}

	/**
	 * @return the maxLen
	 */
	public int getMaxLen() {
		return maxLen;
	}

	/**
	 * @param maxLen the maxLen to set
	 */
	public void setMaxLen(int maxLen) {
		this.maxLen = maxLen;
	}

	/**
	 * @return the minLen
	 */
	public int getMinLen() {
		return minLen;
	}

	/**
	 * @param minLen the minLen to set
	 */
	public void setMinLen(int minLen) {
		this.minLen = minLen;
	}

	/**
	 * @return the isLenChecked
	 */
	public Boolean getIsLenChecked() {
		return isLenChecked;
	}

	/**
	 * @param isLenChecked the isLenChecked to set
	 */
	public void setIsLenChecked(Boolean isLenChecked) {
		this.isLenChecked = isLenChecked;
	}
	
}
