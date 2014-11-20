package com.routdata.kjds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.poi.hssf.eventusermodel.FormatTrackingHSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.eventusermodel.MissingRecordAwareHSSFListener;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BlankRecord;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.StringRecord;
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

public class ExcelFilter implements HSSFListener {
	Logger logger = LoggerFactory.getLogger(ExcelFilter.class);  
	/**
	 *规则设定类
	 */
	private FilterRule rules;
	
	private boolean callbacks = false;
	public boolean isEventMode = false;
	private IExcelFilterCallbacks caller;
	private Map<String,Object> rowMap = new HashMap<String,Object>();
	
	/**
	 * 
	 * */
	private int minColumns;
	private POIFSFileSystem fs;
	
	private int lastRowNumber;
	private int lastColumnNumber;

	/** Should we output the formula, or the value it has? */
	private boolean outputFormulaValues = true;

	/** For parsing Formulas */
	private SheetRecordCollectingListener workbookBuildingListener;
	private HSSFWorkbook stubWorkbook;

	// Records we pick up as we process
	private SSTRecord sstRecord;
	private FormatTrackingHSSFListener formatListener;
	
	/** So we known which sheet we're on */
	private int sheetIndex = -1;
	private BoundSheetRecord[] orderedBSRs;
	private ArrayList boundSheetRecords = new ArrayList();

	// For handling formulas with string results
	private int nextRow;
	private int nextColumn;
	private boolean outputNextStringRecord;
	
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
		if(!isEventMode){
			this.startDeal();
		}
		else{
			try {
				this.startStreamRowDeal();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void processRecord(Record record) {
		int thisRow = -1;
		int thisColumn = -1;
		String thisStr = null;

		switch (record.getSid())
		{
		case BoundSheetRecord.sid:
			boundSheetRecords.add(record);
			break;
		case BOFRecord.sid:
			BOFRecord br = (BOFRecord)record;
			if(br.getType() == BOFRecord.TYPE_WORKSHEET) {
				// Create sub workbook if required
				if(workbookBuildingListener != null && stubWorkbook == null) {
					stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
				}
				
				// Output the worksheet name
				// Works by ordering the BSRs by the location of
				//  their BOFRecords, and then knowing that we
				//  process BOFRecords in byte offset order
				sheetIndex++;
				if(orderedBSRs == null) {
					orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
				}
				logger.debug( 
						orderedBSRs[sheetIndex].getSheetname() +
						" [" + (sheetIndex+1) + "]:"
				);
			}
			
			break;

		case SSTRecord.sid:
			sstRecord = (SSTRecord) record;
			logger.debug("SSTRecord sid");
			break;

		case BlankRecord.sid:
			BlankRecord brec = (BlankRecord) record;

			thisRow = brec.getRow();
			thisColumn = brec.getColumn();
			thisStr = "";
			logger.debug("BlankRecord sid");
			rowMap.put(String.valueOf(thisColumn), thisStr);
			break;
		case BoolErrRecord.sid:
			BoolErrRecord berec = (BoolErrRecord) record;

			thisRow = berec.getRow();
			thisColumn = berec.getColumn();
			thisStr = "";
			logger.debug("BoolErrRecord sid");
			rowMap.put(String.valueOf(thisColumn), thisStr);
			break;
		case FormulaRecord.sid:
			FormulaRecord frec = (FormulaRecord) record;

			thisRow = frec.getRow();
			thisColumn = frec.getColumn();

			if(outputFormulaValues) {
				if(Double.isNaN( frec.getValue() )) {
					// Formula result is a string
					// This is stored in the next record
					outputNextStringRecord = true;
					nextRow = frec.getRow();
					nextColumn = frec.getColumn();
				} else {
					thisStr = formatListener.formatNumberDateCell(frec);
				}
			} else {
				thisStr = '"' +
					HSSFFormulaParser.toFormulaString(stubWorkbook, frec.getParsedExpression()) + '"';
			}
			Object[] data = new Object[2];
			data[0] = thisStr;
			data[1] = "String";
			rowMap.put(String.valueOf(thisColumn), data);
			break;
		case StringRecord.sid:
			if(outputNextStringRecord) {
				// String for formula
				StringRecord srec = (StringRecord)record;
				thisStr = srec.getString();
				thisRow = nextRow;
				thisColumn = nextColumn;
				outputNextStringRecord = false;
				Object[] data2 = new Object[2];
				data2[0] = thisStr;
				data2[1] = "String";
				rowMap.put(String.valueOf(thisColumn), data2);
			}
			logger.debug("StringRecord sid");
			break;

		case LabelRecord.sid:
			LabelRecord lrec = (LabelRecord) record;

			thisRow = lrec.getRow();
			thisColumn = lrec.getColumn();
			thisStr = String.valueOf(lrec.getValue());
			Object[] data1 = new Object[2];
			data1[0] = thisStr;
			data1[1] = "String";
			rowMap.put(String.valueOf(thisColumn), data1);
			break;
		case LabelSSTRecord.sid:
			LabelSSTRecord lsrec = (LabelSSTRecord) record;

			thisRow = lsrec.getRow();
			thisColumn = lsrec.getColumn();
			Object[] data3 = new Object[2];
		
			if(sstRecord == null) {
				thisStr = '"' + "(No SST Record, can't identify string)" + '"';
				
			} else {
				thisStr =sstRecord.getString(lsrec.getSSTIndex()).toString();
				
			}
			data3[0] = thisStr;
			data3[1] = "String";
			rowMap.put(String.valueOf(thisColumn), data3);
			break;
		case NoteRecord.sid:
			NoteRecord nrec = (NoteRecord) record;

			thisRow = nrec.getRow();
			thisColumn = nrec.getColumn();
			// TODO: Find object to match nrec.getShapeId()
			thisStr = '"' + "(TODO)" + '"';
			
			rowMap.put(String.valueOf(thisColumn), thisStr);
			break;
		case NumberRecord.sid:
			NumberRecord numrec = (NumberRecord) record;

			thisRow = numrec.getRow();
			thisColumn = numrec.getColumn();
			
			// Format
			thisStr = formatListener.formatNumberDateCell(numrec);
			Object[] data4 = new Object[2];
			data4[0] = thisStr;
			data4[1] = "Number";
			rowMap.put(String.valueOf(thisColumn), data4);
			break;
		case RKRecord.sid:
			RKRecord rkrec = (RKRecord) record;

			thisRow = rkrec.getRow();
			thisColumn = rkrec.getColumn();
			thisStr = '"' + "(TODO)" + '"';
			rowMap.put(String.valueOf(thisColumn), thisStr);
			break;
		
			
		default:
			//logger.debug("record sid"+record.getSid());
			
			break;
		}

		// Handle new row
		if(thisRow != -1 && thisRow != lastRowNumber) {
			lastColumnNumber = -1;
		}

		// Handle missing column
		if(record instanceof MissingCellDummyRecord) {
			MissingCellDummyRecord mc = (MissingCellDummyRecord)record;
			thisRow = mc.getRow();
			thisColumn = mc.getColumn();
			thisStr = "";
			Object[] obx = new Object[2];
			obx[0] = null;
			obx[1] = "miss";
			rowMap.put(String.valueOf(thisColumn), obx);
			
			
		}

		// If we got something to print out, do so
		if(thisStr != null) {
			
			logger.debug("row idx "+ thisRow);
		}

		// Update column and row count
		if(thisRow > -1)
			lastRowNumber = thisRow;
		if(thisColumn > -1)
			lastColumnNumber = thisColumn;

		// Handle end of row
		if(record instanceof LastCellOfRowDummyRecord) {
			// Print out any missing commas if needed
			if(minColumns > 0) {
				// Columns are 0 based
				if(lastColumnNumber == -1) { lastColumnNumber = 0; }
				
			}

			// We're onto a new row
			lastColumnNumber = -1;
			try {
				this.handlRowMap(rowMap,((LastCellOfRowDummyRecord) record).getRow());
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.rowMap.clear();
			
		}
		
	}
	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 * */
	private void startStreamRowDeal() throws FileNotFoundException, IOException{
		
		this.fs = new POIFSFileSystem(new FileInputStream(this.filePath));
		
		this.minColumns = 6;
		
		MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
		formatListener = new FormatTrackingHSSFListener(listener);

		HSSFEventFactory factory = new HSSFEventFactory();
		HSSFRequest request = new HSSFRequest();

		if(outputFormulaValues) {
			request.addListenerForAllRecords(formatListener);
		} else {
			workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
			request.addListenerForAllRecords(workbookBuildingListener);
		}

		try {

			if(this.getRules() != null){
				this.getRules().initWithPath(this.getRules().getRuleDefPath());
	   		}
			
			if(this.getResultSet() == null){
				this.resultSet = new FilterResult();
			}
			if(this.resultSet.getDiscardIndexList() == null){
				this.resultSet.setDiscardIndexList(new ArrayList<List<String>>());
			}
			if(this.getResultSet().getDataList() == null){
				this.getResultSet().setDataList(new ArrayList<Map<String,Object>>());
			}
			
			factory.processWorkbookEvents(request, fs);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
	
	private void handlRowMap(Map<String,Object> inRow,int i) throws DocumentException{
		
		
		if(this.getRules().getIsHasHeader() && i == 0){
			logger.debug("跳过第一行");
		}
		else{
			List<String> err = this.validRow(inRow,i);
			if(err.size() == 0){
				Map<String,Object> rowMap = new HashMap<String,Object>();
				rowMap.putAll(inRow);
				this.getResultSet().getDataList().add(rowMap);
			}
			else{
				this.getResultSet().getDiscardIndexList().add(err);
			}
			
		}

		logger.debug("row map at "+i+" data "+inRow);
		
	}
	
	private List<String> validRow(Map<String,Object> inRow,int line){
		List<String> err = new ArrayList<String>();
		
		for(Entry<String,Object> item : inRow.entrySet()){
			int j = Integer.valueOf(item.getKey());
			Object[] data = (Object[]) item.getValue();
			String valExp =  (String) data[0];
			String cellType = (String) data[1];
			CellValidator vad = this.getRules().getValidators().get(j);
			StringBuffer errorMsg = new StringBuffer("第"+line+"行,"+(j+1)+"列");
			
			
			/**
        	 * 验证是否允许为空
        	 */
        	if(vad.getIsRequaried()==true){
        		//若此字段必须，且获取excel中的值为空字符串，此row不合法直接跳出
        		if(valExp == null){
        			errorMsg.append("Cell不可为空");
        			err.add(errorMsg.toString());
            		break;
        		}
        		else if(cellType == "String" || cellType == "StringNum" ){
        			String mt = (String)valExp;
        			if(mt.trim().isEmpty()){
        				errorMsg.append("Cell不可为空");
        				err.add(errorMsg.toString());
	            		break;
        			}
        		}
        	}
        	else if(vad.getIsRequaried()==false && valExp == null ){
        		//如果非必填项,并且空,直接跳过
        		
        		continue;
        	}
        	
        	

        	/**
    	     * 校验字符类型
        	 */
        	if(vad.getTypeStr().equals("String")){
        		if(!cellType.equals("String")){
        			errorMsg.append("期望数据类型"+vad.getTypeStr()+" , 但是实际得到 "+cellType);
        			err.add(errorMsg.toString());
        			break;
        		}  
        	}

        	/**
        	 * 校验Long、Double、Float数据类型
        	 */
        	if(vad.getTypeStr().equals("Long") || vad.getTypeStr().equals("Double") || vad.getTypeStr().equals("Float") ){
        		if(!cellType.equals("Number")){
        			errorMsg.append("期望数据类型 "+vad.getTypeStr()+" , 但是实际得到 "+cellType);
        			err.add(errorMsg.toString());
        			break;
        		}
        	}
        	
        	if(vad.getTypeStr().equals("Date")){
        		if(!cellType.equals("Number")){
        			errorMsg.append("期望数据类型 "+vad.getTypeStr()+" , 但是实际得到 "+cellType);
        			err.add(errorMsg.toString());
        			break;
        		}
        	}

        	if(vad.getTypeStr().equals("StringNum")){
        		if(!cellType.equals("String")){
        			errorMsg.append("期望数据类型 "+vad.getTypeStr()+" , 但是实际得到 "+cellType);
        			err.add(errorMsg.toString());
        			break;
        		}
        	}
        	if(!vad.getRegPatternStr().isEmpty() && cellType.equals("String")){
        		String mt =  (String)valExp;
        		if(!mt.matches(vad.getRegPatternStr())){
            		//若此字段含有正则匹配，且excel中的值无法匹配，此row不合法直接退出
            		errorMsg.append("期望匹配正则"+vad.getRegPatternStr()+" , 但是实际不符合");
            		err.add(errorMsg.toString());
            		break;
        		}
        	}
        	
        	if(vad.getIsLenChecked() && (vad.getTypeStr().equals("String") || vad.getTypeStr().equals("StringNum"))){
            	boolean flag = false;
            	int maxLen = vad.getMaxLen();
            	int minLen = vad.getMinLen();
            	String testStr = String.valueOf(valExp).trim();
	    		
            	if(testStr.length() >= minLen && testStr.length() <= maxLen){
            		flag = true;
            	}
            	
	    		if(!flag){
	    			errorMsg.append("期望长度"+vad.getMinLen()+"~"+vad.getMaxLen()+" , 但是实际" + testStr.length());
	    			err.add(errorMsg.toString());
	    			break;
	    		}
        	}
        	
		}
		
		return err;
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
