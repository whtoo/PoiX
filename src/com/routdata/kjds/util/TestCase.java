package com.routdata.kjds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

public class TestCase implements IExcelFilterCallbacks{
	static Logger logger = Logger.getLogger(TestCase.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ExcelFilter testFilter = new ExcelFilter();
		testFilter.initWithFilePath("resources/goodsImportTemplate.xls");
		FilterRule rule = new FilterRule();
		rule.setRuleDefPath("resources/ValidatRules_goods.xml"); 
		rule.setIsHasHeader(new Boolean(true));
		rule.settHeaders(new ArrayList<String>());
		try {
			TestCase ca = new TestCase();
			testFilter.setCallbacks(false);
			//testFilter.setCaller(ca);
			testFilter.isEventMode = true;
			testFilter.validateWithSetting(rule);
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		List<Map<String,Object>> succList = testFilter.getResultSet().getDataList();
		
		List<List<String>> errorList = testFilter.getResultSet().getDiscardIndexList();
		logger.info("succ at "+succList);
		
		logger.info("error at "+errorList);
	}
	
	@Override
	public void walkWithData(Map<String, Object> data) {
		logger.info("log me at "+ data);
		
	}

}
