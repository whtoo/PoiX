package com.routdata.kjds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.WorkbookUtil;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.odysseus.el.ObjectValueExpression;

public class ExcelFilter {
	Logger logger = LoggerFactory.getLogger(ExcelFilter.class);  
	/**
	 *规则设定类
	 */
	private FilterRule rules;
	
	private boolean callbacks = false;
	
	private IExcelFilterCallbacks caller;
	
	/**
	 * 返回结果集
	 * */
	private FilterResult resultSet;
	/**
	 * 文件对象
	 */
	private Workbook exWorkBook;
	/**
	 * 原始excel的绝对地址
	 */
	private String filePath = "";
	public ExcelFilter(){
		logger.debug("ExcelFilter do");
	}
	public void initWithFilePath(String filePath) {
		this.setFilePath(filePath);
		try {
			Workbook wb = WorkbookFactory.create(new FileInputStream(this.filePath)); 
	        this.setExWorkBook(wb);
		} catch (InvalidFormatException e) {
			logger.debug("xlsx 异常:"+e.toString());
			e.printStackTrace();
		} 
		catch (IOException e) {
			logger.debug("io 异常:"+e.toString());
			e.printStackTrace();
		} 
	}
	
	public void close(){
		
		
		
	}
	public void validateWithSetting(FilterRule validator) throws DocumentException{
		this.setRules(validator);
		this.startDeal();
	}
	
	private void startDeal() throws DocumentException{
		this.resultSet = new FilterResult();
		ExpressionFactory factory = null;
		de.odysseus.el.util.SimpleContext context = null;
		if(this.getRules() != null){
			this.getRules().initWithPath(this.getRules().getRuleDefPath());
			factory = new de.odysseus.el.ExpressionFactoryImpl();
    		//de.odysseus.el.util provides包提供即时可用的子类ELContext
    		context = new de.odysseus.el.util.SimpleContext();	
   		}
		
		List<Map<String, Object>> dataList = new ArrayList<Map<String,Object>>();
		 Sheet sheet = exWorkBook.getSheetAt(0);
	   
	    int rowCount = sheet.getPhysicalNumberOfRows();
	    this.resultSet.setTotalSize((long) rowCount);
	    this.resultSet.setDiscardIndexList(new ArrayList<List<String>>());
	     /**
	      * 解析第几行
	      */
	     for (int i = sheet.getFirstRowNum(); i < rowCount; i++) {  
	    	 	
	    	 	if(this.getRules().getIsHasHeader() && i == 0){
	    	 		//如果第一行是表头，跳过
	    	 		continue;
	    	 	}
	    	 	
	            Row row = sheet.getRow(i); // 拿一行的内容  
	            List<Map<String, Object>> tmpX = new ArrayList<Map<String, Object>>();
	            String errorMsg = "";
	            
	            /**
	             * 第几列
	             */
	            int cellLength = row.getPhysicalNumberOfCells();
	            for (int j = row.getFirstCellNum(); j < cellLength; j++) {  
	            	Map<String,Object> cellMap = new HashMap<String,Object>();
	            	Cell cell = row.getCell(j);
	            	
	            	logger.debug("this.getRules().gettHeaders().size() "+this.getRules().gettHeaders().size());
	            	String theader = (this.getRules().gettHeaders().size() == cellLength)?this.getRules().gettHeaders().get(j):"";
	            	if(this.getRules().gettHeaders() == null || theader.isEmpty()){
	            		theader = String.valueOf(j);
	            	}
	            	Object valExp = null;
	            	int cellType = -1;
	            	switch (cell.getCellType()){
	            		case HSSFCell.CELL_TYPE_NUMERIC:  
							valExp = cell.getNumericCellValue();
							cellType = HSSFCell.CELL_TYPE_NUMERIC;
		
							if (HSSFDateUtil.isCellDateFormatted(cell)) {
								double d = cell.getNumericCellValue();
								Date date = HSSFDateUtil.getJavaDate(d);
								SimpleDateFormat dformat = new SimpleDateFormat(
										"yyyy-MM-dd hh:mm:ss");
								valExp = dformat.format(date);
								System.out.println("=========date="
										+ dformat.format(date));
							} else {
								NumberFormat nf = NumberFormat.getInstance();
								nf.setGroupingUsed(false);// true时的格式：1,234,567,890
								valExp = nf.format(cell.getNumericCellValue());// 数值类型的数据为double，所以需要转换一下
								System.out.println("===CELL_TYPE_NUMERIC" + valExp);
						}
	                    break;  
	                   case HSSFCell.CELL_TYPE_STRING:  
	                	   cellType = HSSFCell.CELL_TYPE_STRING;
	                	   valExp = cell.getStringCellValue();  
	                    break;  
	                   case HSSFCell.CELL_TYPE_BOOLEAN:  
	                	   cellType = HSSFCell.CELL_TYPE_BOOLEAN;
	                	   valExp = cell.getBooleanCellValue();  
	                    break;  
	                   case HSSFCell.CELL_TYPE_FORMULA:  
	                	   cellType = HSSFCell.CELL_TYPE_FORMULA;
	                	   valExp = cell.getCellFormula();  
	                    break;  
	                   default:  
	                	   valExp = "";
	                    break;  
	            	}
	            	cellMap.put(theader, valExp);
	            	logger.debug("val:"+valExp+" $$ Type "+ cellType);
	            	CellValidator vad = this.getRules().getValidators().get(j);
	            	errorMsg = "";
	            	
	            	
	            	/**
	            	 * 验证是否允许为空
	            	 */
	            	if(vad.getIsRequaried()==true){
	            		//若此字段必须，且获取excel中的值为空字符串，此row不合法直接跳出
	            		if(valExp == null){
	            			errorMsg = "Cell不可为空";
		            		break;
	            		}
	            		else if(cellType == HSSFCell.CELL_TYPE_STRING){
	            			String mt = (String)valExp;
	            			if(mt.trim().isEmpty()){
	            				errorMsg = "Cell不可为空";
			            		break;
	            			}
	            		}
	            	}
	            	else if(vad.getIsRequaried()==false && valExp == null ){
	            		//如果非必填项,并且空,直接跳过
	            		tmpX.add(cellMap);
	            		continue;
	            	}
	            	
	            	/**
            	     * 校验字符类型
	            	 */
	            	if(vad.getTypeStr().equals("String")){
	            		if(cellType != HSSFCell.CELL_TYPE_STRING){
	            			errorMsg = "期望数据类型"+vad.getTypeStr()+" , 但是实际得到 "+cellType;
	            			break;
	            		}  
	            	}

	            	/**
	            	 * 校验Long、Double、Float数据类型
	            	 */
	            	if(vad.getTypeStr().equals("Long") || vad.getTypeStr().equals("Double") || vad.getTypeStr().equals("Float") ){
	            		if(cellType !=  HSSFCell.CELL_TYPE_NUMERIC){
	            			errorMsg = "期望数据类型 "+vad.getTypeStr()+" , 但是实际得到 "+cellType;
	            			break;
	            		}
	            	}
	            	
	            	if(vad.getTypeStr().equals("Date")){
	            		if(cellType !=  HSSFCell.CELL_TYPE_NUMERIC){
	            			errorMsg = "期望数据类型 "+vad.getTypeStr()+" , 但是实际得到 "+cellType;
	            			break;
	            		}
	            	}

	            	if(!vad.getRegPatternStr().isEmpty() && cellType == HSSFCell.CELL_TYPE_STRING){
	            		String mt =  (String)valExp;
	            		if(mt.matches(vad.getRegPatternStr())){
		            		//若此字段含有正则匹配，且excel中的值无法匹配，此row不合法直接退出
		            		errorMsg = "期望匹配正则"+vad.getRegPatternStr()+" , 但是实际不符合";
		            		break;
	            		}
	            	}
	            	
	            	if(vad.getIsLenChecked() && vad.getTypeStr().equals("String")){
		            	boolean flag = false;
		            	int maxLen = vad.getMaxLen();
		            	int minLen = vad.getMinLen();
		            	String testStr = String.valueOf(valExp).trim();
			    		
		            	if(testStr.length() >= minLen && testStr.length() <= maxLen){
		            		flag = true;
		            	}
		            	
			    		if(!flag){
			    			errorMsg = "期望长度"+vad.getMinLen()+"~"+vad.getMaxLen()+" , 但是实际" + testStr.length();
			    			break;
			    		}
	            	}
	            	
	            	tmpX.add(cellMap);
	            }  
	           
	            if(tmpX.size() == cellLength){
	            	Map<String,Object> tmpMap = new HashMap<String,Object>();
	            	//int idx = 0;
	            	for(Map<String,Object> tmpItem:tmpX){
	            		String key = tmpItem.keySet().iterator().next();
	            		/*StringBuffer dotStr = new StringBuffer();
	            		dotStr.append("r");
	            		dotStr.append(idx);
	            		ObjectValueExpression dotVal = (ObjectValueExpression) factory.createValueExpression(tmpItem.get(key), Object.class);
	            		context.setVariable(dotStr.toString(), dotVal);
	            		idx++;
	            		StringBuffer exIR = new StringBuffer();
	            		exIR.append("${");
	            		exIR.append(dotStr.toString());
	            		exIR.append("}");
	            		
	            		ValueExpression e = factory.createValueExpression(context, exIR.toString(), Object.class);
	            		logger.debug("val juel at "+e.getValue(context));*/
	            		tmpMap.put(key, tmpItem.get(key));
	            	}
	            	if(this.callbacks && null != caller){
	            		caller.walkWithData(tmpMap);
	            	}
	            	else{
	            		dataList.add(tmpMap);
	            	}
	            }
	            else{
	            	 List<String> errorlist = new ArrayList<String>();
	            	 //excel行、列下标从0开始.
	            	 errorlist.add("<br/>");
	            	 errorlist.add("第"+String.valueOf(i+1)+"行"+"第"+String.valueOf(tmpX.size()+1)+"列");
	            	 errorlist.add(String.valueOf(errorMsg));
	            	 this.resultSet.getDiscardIndexList().add(errorlist);
	            }
	     }   
	     
	     
	  
		this.resultSet.setDataList(dataList);		
	}	
	/**
	 * @return the rules
	 */
	public FilterRule getRules() {
		return rules;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(FilterRule rules) {
		this.rules = rules;
	}

	/**
	 * @return the resultSet
	 */
	public FilterResult getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(FilterResult resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * @return the exWorkBook
	 */
	public Workbook getExWorkBook() {
		return exWorkBook;
	}

	/**
	 * @param exWorkBook the exWorkBook to set
	 */
	public void setExWorkBook(Workbook exWorkBook) {
		this.exWorkBook = exWorkBook;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * @return the callbacks
	 */
	public boolean isCallbacks() {
		return callbacks;
	}
	/**
	 * @param callbacks the callbacks to set
	 */
	public void setCallbacks(boolean callbacks) {
		this.callbacks = callbacks;
	}
	/**
	 * @return the caller
	 */
	public IExcelFilterCallbacks getCaller() {
		return caller;
	}
	/**
	 * @param caller the caller to set
	 */
	public void setCaller(IExcelFilterCallbacks caller) {
		this.caller = caller;
	}

	
}
