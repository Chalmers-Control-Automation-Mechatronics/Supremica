package org.supremica.external.processeditor.xml;

import java.io.*;
import com.jniwrapper.win32.jexcel.*;
import com.jniwrapper.win32.jexcel.format.*;

public class XlsConverter implements FileConverter {         

    private Application application = null;
    private Workbook workbook = null;      

    private int row = 1;
    private int opCount = 0;    
    private int parallelCount = 0;
    private final int SEQUENCE = 0;
    private final int ALTERNATIVE = 1;
    private final int PARALLEL = 2;
    private final int ARBITRARY = 3;
    private int relationType = SEQUENCE;

    //private List breaks = new List();
    private int currentRow = 3;
    private String additionalOpInfo="";
    //public Application application; 
    public File xlsFile;   
    //public Workbook workbook;
    public Worksheet worksheet;    
    public XmlConverter xmlConverter = new XmlConverter();

    public String[] headers = {"SEQUENCE STEP",
		       "CONTROLS DESCRIPTION",
		       "E/R",
		       "DEVICE",
		       "VALVE",
		       "SENSOR",
		       "STEP TYPE",
		       "SEGMENT",
		       "PREVIOUS SEGMENT",
		       "CFLEX STEP",
		       "CFLEX PREVIOUS STEP",
		       "CFLEX NEXT STEP",
		       "CFLEX STEP DESCRIPTION",
		       "WELD NUMBER",
		       "WELD CONTROLLER",
		       "SCR NUMBER",
		       "WELD SCHEDULE",
		       "XFMR",
		       "START TIME",
		       "SECONDS",
		       "ROUTINE NAME SUFFIX",
		       "TAG NAME",
		       "AUTO SETUPS",
		       "CLEAR"};      
    public double[] ColumnWidth = {34.86, 59.29, 3.43, 9.14, 9.71, 12.57, 
				   19.71, 9.43, 19.86, 11.43, 22, 16.86,
				   24.86, 14.43, 18.86, 12.86, 16.57, 5.57,
				   11, 9.43, 21.57, 9.71, 13.43, 6.43};

    private int[] attCount = new int[headers.length];

    public XlsConverter() {	
	attCount = new int[headers.length];
	for(int i = 0; i < headers.length; i++) {
	    attCount[i] = 0;
	}
    }
    
    public void save(File file, Object o, FileConverter fc) {}

    public Object open(File file, FileConverter fc) {
	
	/*
	FileConverter o = fc;
	o.newResource("VA610");
	o.newRelation("Sequence");

	o.newOperation("OPERATOR LOADS PART 1");
	o.addOperation();

	o.newOperation("OPERATOR LOADS PART 2");
	o.addOperation();

	o.newOperation("OPERATOR DEPRESSES PALM BUTTON");
	o.addOperation();

	o.newRelation("Parallel");

	o.newOperation("CLOSE CLAMP 1");
	o.addAttribute("C100@Device");
	o.addAttribute("C110@Device");
	o.addAttribute("PM1VS14@Valve");
	o.addOperation();
	
	o.newOperation("CLOSE CLAMP 2");
	o.addAttribute("C200@Device");
	o.addAttribute("C210@Device");
	o.addAttribute("PM1VS14@Valve");
	o.addOperation();

	o.addRelation();

	o.newOperation("INITIATE WELD 1");
	o.addAttribute("WG134@Device");
	o.addAttribute("WG135@Device");
	o.addAttribute("PM1V3S14@Valve");
	o.addOperation();
	
	o.newRelation("Parallel");

	o.newOperation("OPEN CLAMPS 1");
	//o.addAttribute("C100@Device");
	o.addAttribute("C110Device");
	//o.addAttribute("PM1VS12@Valve");
	o.addOperation();
	
	o.newOperation("OPEN CLAMPS 2");
	//o.addAttribute("C200@Device");
	o.addAttribute("C210Device");
	//o.addAttribute("PM1VS12@Valve");
	o.addOperation();

	o.addRelation();

	o.newOperation("OPERATOR UNLOADS PART");
	o.addOperation();
	
	o.addRelation();

	return o.getResource();
	**/

	//Workbook workbook = Application.createWorkbook("Custom title");
	/*
    public String[] headers = {"SEQUENCE STEP","CONTROLS DESCRIPTION","E/R",
				      "DEVICE","VALVE"
			       ,"SENSOR","STEP TYPE","SEGMENT","PREVIOUS SEGMENT",
			       "CFLEX STEP","CFLEX PREVIOUS STEP","CFLEX NEXT STEP",
			       "CFLEX STEP DESCRIPTION","WELD NUMBER","WELD CONTROLLER",
			       "SCR NUMBER","WELD SCHEDULE","XFMR","START TIME","SECONDS",
			       "ROUTINE NAME SUFFIX","TAG NAME","AUTO SETUPS","CLEAR1"};
	*/
	
	try {
	    application = new Application();
	    //xlsFile= new File(fileName);	    
	    workbook = application.openWorkbook(file, true, "password");
	    worksheet = workbook.getWorksheet(1);
	    	        
	    if(file.getName().endsWith(".xls")) {
		fc.newResource(file.getName().substring(0,file.getName().length()-4));
	    }else {
		fc.newResource(file.getName());
	    }
	    fc.newRelation("Sequence");
	   
	    if(true)//readableFile())
		{
		    int longestColumn = 1;
		    String hasRelation = "false";

		    while(longestColumn != 0)
			{
			    
			    String seqNo = ((Cell)worksheet.getCell
					     (currentRow,1)).getString(); 
			    String opName = ((Cell)worksheet.getCell
					     (currentRow,2)).getString(); 		   
			    //System.out.println("INTERUPTING TEST 11111");
			    if(seqNo == null){
				if(hasRelation != "false")
				    fc.addRelation(); 
				fc.addRelation(); 
				break;
			    }
			    
			    if(hasRelation != "false" &&
			       (seqNo.endsWith("A") || seqNo.endsWith("0"))){	     
				    fc.addRelation();
				    hasRelation = "false";
				    //System.out.println("--------------END NEW RELATION");
				}	
			    if(seqNo.endsWith("A")){					   
				fc.newRelation("Parallel");
				hasRelation = seqNo.substring(0,seqNo.indexOf("."));
				//System.out.println("-------------------- NEW RELATION"); 
			    }			    			    	    
			    fc.newOperation(opName);
			    longestColumn = 0;
			    for(int col = 2;col<headers.length;col++){
				int row = 0;	
				while(!((Cell)worksheet.getCell(currentRow + row,col))
				      .isEmpty())
				    {
					if(col<3){
					   additionalOpInfo =  additionalOpInfo +
					       ((Cell)worksheet.getCell
						(currentRow+row,col)).getString();
					}
					else {					    
					    fc.addAttribute(((Cell)worksheet.getCell
								   (currentRow+row,col)).
								  getString()+"@"+headers[col-1]); 					    
					}
					//System.out.println("OPERATING AT ROW : "+ (row+ currentRow));		   
					if(row+1 > longestColumn)
					    longestColumn = row+1;
					row++;
				    }
			       
			    }
			    fc.addOperation();
			    
			    
			    currentRow = currentRow + longestColumn +1;   	       
			}
		
	    application.close();
	    return fc.getResource();
	    }else{
	//System.out.println("--------No valid file-----------");
	    }	    
	} 
	catch(Exception ex) {
	    System.out.println(ex);		
	}    
	return null;
	
    }
    /*
    public boolean readableFile(){  
	boolean readable = true;
	for(int col=1; col<headers.length; col++)
	    {	
		String data =((Cell)worksheet.getCell(1,col)).getString();
		
		if(data == headers[col-1]){ // FUNKAR EJ AV SKUM ANLEDNING (skall vara !=)
		    readable = false;
		    System.out.println("ReadableFile  FALSE because " +
				       data +" != "+ headers[col-1]);
		}	    
	    }
	
	return readable;
    } 
    **/
    
    public void newResource(String s) {	
	//DEBUG
	System.out.println("newResource("+s+")");	
	//END DEBUG
	try {
	    application = new Application();
	    workbook = application.createWorkbook("s");	    
	    Range range = workbook.getWorksheet(1).getColumn("A");
	    range.setHorizontalAlignment(TextAlignment.RIGHT);
	    addHeader();	    
	}catch(Exception ex) {}
	
    }
    public void newResource(String s,String id) {	
	//DEBUG
	System.out.println("newResource("+s+")");	
	//END DEBUG
	try {
	    application = new Application();
	    workbook = application.createWorkbook("s");	    
	    Range range = workbook.getWorksheet(1).getColumn("A");
	    range.setHorizontalAlignment(TextAlignment.RIGHT);
	    addHeader();	    
	}catch(Exception ex) {}
	
    }
    public void newRelation(String rt) {
	//DEBUG
	System.out.println("newRelation("+rt+")");
	//END DEBUG
	parallelCount = 0;
	opCount++;
	if(rt.equals("Sequence")) {
	    relationType = SEQUENCE;
	}else if(rt.equals("Alternative")) {
	    relationType = ALTERNATIVE;
	}else if(rt.equals("Parallel")) {
	    relationType = PARALLEL;
	}else if(rt.equals("Arbitrary")) {
	    relationType = ARBITRARY;
	}	
    }
    public void addRelation() {
	relationType = SEQUENCE;
    }    
    public void newOperation(String operation) {
	//DEBUG
	System.out.println("newOperation("+operation+")");
	//END DEBUG	
	System.out.println(row);
	if(relationType == PARALLEL) {	    
	    workbook.getWorksheet(1).getCell(row,1).setValue(opCount+"."+getAlphabetic(parallelCount++));
	}else {
	    opCount++;
	    workbook.getWorksheet(1).getCell(row,1).setValue(opCount);
	}	       
	workbook.getWorksheet(1).getCell(row,2).setValue(operation);       
    }
    public void addOperation() {		
	row = row + 2;
	int tmpOpLength = 0;
	for(int i = 0; i < headers.length; i++) {
	    if(attCount[i] > tmpOpLength) {
		tmpOpLength = attCount[i];
	    }
	    attCount[i] = 0;
	}
	if(tmpOpLength > 1) {
	    row = row + tmpOpLength -1;
	}
    }
    public void addAttribute(String attribute) {
	//DEBUG
	System.out.println("addAttribute("+attribute+")");
	//END DEBUG
	int indexAt = attribute.indexOf('@');
	int deviceIndex = -1;
	//DEBUG
	System.out.println("indexAt: "+indexAt);
	System.out.println(attribute.substring(indexAt));
	System.out.println(attribute.substring(0,indexAt));
	//END DEBUG	
	if(indexAt != -1) {
	    for(int i = 0; i < headers.length; i++) {
		if(attribute.substring(indexAt+1).toUpperCase().equals(headers[i])) {
		    deviceIndex = i;
		    break;
		}		
	    }	    
	    if(deviceIndex != -1 && !attribute.substring(0,indexAt).equals("")) {
		workbook.getWorksheet(1).getCell(row+attCount[deviceIndex]++,deviceIndex+1).setValue(attribute.substring(0,indexAt));
	    }
	}
    }
    public void addPredecessor(String pred) {
	System.out.println("Method XlsConverter.addPredecessor() is not in use");
    }
    public void addDescription(String s) {
	
    }
    public Object getResource() {	
	return null;
    }
    public void saveResource(File file) {
	//DEBUG
	System.out.println("XlsConverter.saveResource()");
	//END DEBUG
	if(workbook != null) {
	    try {
		
		workbook.saveAs(file, FileFormat.WORKBOOKNORMAL, true);
	    }catch(Exception ex) {}
	}
	application.close();
    }
    public FileConverter newInstance() {
	return new XlsConverter();
    }
    private int getIndex(String attributeType) {
	int index = -1;
	for(int i = 0; i < headers.length; i++) {
	    if(attributeType.equals(headers[i])) {
		index = i;
		break;
	    }
	}
	return index;
    }
    private void addHeader() {
	//DEBUG
	System.out.println("XlsConverter.addHeader()");
	//END DEBUG

	//ADD HEADER
	if(workbook != null) {
	    for(int i = 1; i <= headers.length; i++) {
		workbook.getWorksheet(1).getCell(row,i).setValue(headers[i-1]);
	    }	    
	    row = row + 2;
	}

	//SET COLUMN WIDTH

	for(int i = 0; i < ColumnWidth.length; i++) {
	    workbook.getWorksheet(1).getColumn(getAlphabetic(i)).setColumnWidth(ColumnWidth[i]);
	}
    }
    private String getAlphabetic(int index) {
	String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H",
			    "I", "J", "K", "L", "M", "N", "O", "P",
			    "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
	if(index - alphabet.length < 0) {
	    return alphabet[index];
	}else {
	    return getAlphabetic(index/alphabet.length)+
		getAlphabetic(index%alphabet.length);
	}
    }
    /*
     public static void main(String[] args) throws Exception {
	 XmlConverter o = new XmlConverter();
	 try {
	     Application application = new Application();
	     
	     File xlsFile = new File("VA610Sequence.xls");
	     Workbook workbook = application.openWorkbook(xlsFile, true, "password");

	     Worksheet worksheet = workbook.getWorksheet(1);
	     System.out.println(worksheet.getCell(3,2).getString());
	     worksheet.getCell(3,2).setValue(worksheet.getCell(3,2).getString()+"TEST");
	     o.newResource("");
	     o.newRelation("");
	     o.addRelation();
	     o.newOperation("");
	     o.addOperation();
	     o.addAttribute("");
	     //File newXlsFile = new File("newSequence.xls");
	     //workbook.saveAs(newXlsFile, FileFormat.WORKBOOKNORMAL, true);
	     //application.setVisible(true);	     
	 }catch(Exception ex) {
	     
	 }    
     }
    */
}
