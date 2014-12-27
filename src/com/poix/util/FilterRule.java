package com.poix.util;

import java.util.List;
import java.io.File; 
//工具包 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator; 
import org.dom4j.Document; 
import org.dom4j.DocumentException;
import org.dom4j.Element; 
import org.dom4j.io.SAXReader;

public class FilterRule {
	
	
	/**
	 * 读取的列数限制
	 * */
	private int limitColumn;
	
	/**
	 * rule define file path
	 * */
	private String ruleDefPath;
	
	private String groupColumn;
	/**
	 * 读取的行数限制
	 * */
	private int limitRows;
	/**
	 * 绑定的列合法性检查
	 * */
	private List<CellValidator> validators;
	/**
	 * 是否有表头，表头默认为第一行
	 * */
	private Boolean isHasHeader;
	/**
	 * 自定义表头，表头默认为第一行
	 * */
	private List<String> tHeaders;
	
	public void initWithPath(String path) throws DocumentException{
		this.setRuleDefPath(path);
		SAXReader reader = new SAXReader();  
		Document document = null;
		try {
			document = reader.read(new File(this.getRuleDefPath()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		Element rootElm = document.getRootElement();
		this.parseXML(rootElm);
	}
	
	public void parseXML(Element root){
		validators = new ArrayList<CellValidator>();
		 for (Iterator<?> iter = root.elementIterator("ValidatorItem"); iter.hasNext();) {// 遍历line结点的所有子节点,也可以使用root.elementIterator()    
            Element element = (Element) iter.next();    
            CellValidator validator = new CellValidator();
            for (Iterator<?> iterInner = element.elementIterator("property"); iterInner.hasNext();) { // 遍历station结点的所有子节点    
                Element elementInner = (Element) iterInner.next();    
        
                String name = elementInner.attributeValue("name");    
                String value = elementInner.attributeValue("value");
                if(name.equals("mappedIndex")){
                	validator.setMappedIndex(Integer.valueOf(value));
                }
                else if(name.equals("isRequaried")){
                	validator.setIsRequaried(Boolean.valueOf(value));
                }
                else if(name.equals("typeStr")){
                	validator.setTypeStr(String.valueOf(value));
                }
                else if(name.equals("regPatternStr")){
                	validator.setRegPatternStr(String.valueOf(value));
                }
                else if(name.equals("length")){
                	String lenVal = String.valueOf(value);
                	String[] lenArr = lenVal.split(",");
                	int min = Integer.valueOf(lenArr[0]);
                	int max = Integer.valueOf(lenArr[1]);
                	validator.setMinLen(min);
                	validator.setMaxLen(max);
                	validator.setIsLenChecked(new Boolean(true));

                	
                }
                
            }    
            validators.add(validator);
	     }  
		 
		 ComparatorValidator comparator=new ComparatorValidator();
		 Collections.sort(validators, comparator);
	}
	/**
	 * @return the limitColumn
	 */
	public int getLimitColumn() {
		return limitColumn;
	}

	/**
	 * @param limitColumn the limitColumn to set
	 */
	public void setLimitColumn(int limitColumn) {
		this.limitColumn = limitColumn;
	}

	/**
	 * @return the limitRows
	 */
	public int getLimitRows() {
		return limitRows;
	}

	/**
	 * @param limitRows the limitRows to set
	 */
	public void setLimitRows(int limitRows) {
		this.limitRows = limitRows;
	}

	/**
	 * @return the validators
	 */
	public List<CellValidator> getValidators() {
		return validators;
	}

	/**
	 * @param validators the validators to set
	 */
	public void setValidators(List<CellValidator> validators) {
		this.validators = validators;
	}

	/**
	 * @return the ruleDefPath
	 */
	public String getRuleDefPath() {
		return ruleDefPath;
	}

	/**
	 * @param ruleDefPath the ruleDefPath to set
	 */
	public void setRuleDefPath(String ruleDefPath) {
		this.ruleDefPath = ruleDefPath;
	}

	/**
	 * @return the isHasHeader
	 */
	public Boolean getIsHasHeader() {
		return isHasHeader;
	}

	/**
	 * @param isHasHeader the isHasHeader to set
	 */
	public void setIsHasHeader(Boolean isHasHeader) {
		this.isHasHeader = isHasHeader;
	}

	/**
	 * @return the tHeaders
	 */
	public List<String> gettHeaders() {
		return tHeaders;
	}

	/**
	 * @param tHeaders the tHeaders to set
	 */
	public void settHeaders(List<String> tHeaders) {
		this.tHeaders = tHeaders;
	}

	/**
	 * @return the groupColumn
	 */
	public String getGroupColumn() {
		return groupColumn;
	}

	/**
	 * @param groupColumn the groupColumn to set
	 */
	public void setGroupColumn(String groupColumn) {
		this.groupColumn = groupColumn;
	}
	
}
