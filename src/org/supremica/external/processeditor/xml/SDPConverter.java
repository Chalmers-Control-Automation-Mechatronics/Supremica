package org.supremica.external.processeditor.xml;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.jniwrapper.win32.jexcel.Application;
import com.jniwrapper.win32.jexcel.Cell;
import com.jniwrapper.win32.jexcel.Workbook;
import com.jniwrapper.win32.jexcel.Worksheet;


public class SDPConverter {  
    public int resourceCol = 2;
    public int seqstepCol = 3;
    public int descriptionCol = 4;
    public int linksCol = 16;

    public String[] attributeHeaders = {"TIME","MUTUAL","FORCED_PARALLEL","TOOL_NO","DESCRIPTION"};
    public int[] attributeColumns =     {13,18,19,5,4}; //Could be replaced by a 
                                                //Matrix to support several sheets   
    public List componentList = new List();
    public DefaultListModel<FileConverter> xmlConverterListModel = new DefaultListModel<FileConverter>();
    public JList<FileConverter> xmlConverterList = new JList<FileConverter>(xmlConverterListModel); 
    public List addedOperations = new List(); 

    public ListOfComponentTypes knownItemsList = new ListOfComponentTypes();
    public String listOfCompTypesfileName = "ListOfComponentTypes.txt";    
    private int listOfComponentTypesOrigalSize = 0;
  
    public String opInfoFileName = "attribute.txt";    
    public BufferedWriter opInfoDocument ;
    public Application application; 
    public Workbook workbook;
    public Worksheet worksheet;   

    private int pointer = -1; 
    private int resourceID = 0;
    
    public SDPConverter() {		
    }
    //public void save(File file, Object o, FileConverter fc) {}

    public void create(File file, FileConverter fc) {  	       				
	try {
	    application = new Application();	   
	    workbook = application.openWorkbook(file, true, "password");	    
	    worksheet = workbook.getWorksheet(1);
	    //opInfoDocument = new BufferedWriter(new OutputStreamWriter(
	    // new FileOutputStream(file.getParent()
	    //				 +"\\"+opInfoFileName)));
	    opInfoDocument = new BufferedWriter(new FileWriter(file.getParent()
							       +"\\"+opInfoFileName));
	    saveOpToFile("STATION NUMBER TO BE ADDED");
	    listOfComponentTypesOrigalSize = knownItemsList.getItemCount();
	   	    
	    int currentRow = 4;
	    while(!((Cell)worksheet.getCell(currentRow,seqstepCol)).isEmpty())
		{
		    String resource = ((Cell)worksheet.getCell
			   (currentRow,resourceCol)).getString(); 		   
		    String description = ((Cell)worksheet.getCell
					     (currentRow,descriptionCol)).getString();	  
		    if(doComponentExist(resource,description)){	 		
			String component = componentList.getItem(pointer);		
			createOperation(pointer,component,currentRow);			
		    }else{
			String component = componentInterpreter(description,resource);
			xmlConverterListModel.addElement(fc.newInstance());	       
			xmlConverterListModel.lastElement().
			    newResource(component,resourceID+"" );
			resourceID++;
			xmlConverterListModel.lastElement().
			    newRelation("Sequence");		      
			componentList.add(component);	  
			createOperation(componentList.getItemCount()-1,
					component,currentRow);	 
			}		    		  		    
		    currentRow = currentRow + 2;	    
		}
	    opInfoDocument.close();
	    closeAllResources(file);
	    application.close();
	}
	catch(Exception ex) {
	    System.out.println(ex);		
	}	
    }      
    public void createOperation(int index,String component,int currentRow){

	 String seqStep = ((Cell)worksheet.getCell
			   (currentRow,seqstepCol)).getString();
	 String links = ((Cell)worksheet.getCell
			   (currentRow,linksCol)).getString()+"";	 
	 xmlConverterListModel.get(index).newOperation(seqStep);
	 addedOperations.add(seqStep+"@"+component);

	 String opInfo = seqStep+" ";
	 for(int i = 0; i<attributeColumns.length; i++){
		String attribute = ((Cell)worksheet.getCell
				    (currentRow,attributeColumns[i])).getString();
		if(attribute != null) 
		    {
			opInfo = opInfo+attribute.replace(' ', '_')+"@"+attributeHeaders[i]+" ";
			if(attributeHeaders[i].toLowerCase().equals("description")) {
			    xmlConverterListModel.get(index).
				addDescription(attribute);			    			    
			}else {
			    //opInfo = opInfo+attribute+"@"+attributeHeaders[i]+" ";
			    xmlConverterListModel.get(index).
				addAttribute(attribute+"@"+attributeHeaders[i]);
			}
		    }
	 }
	 saveOpToFile(opInfo);//+"\n");
	 //PREDECESSORS
	 if(links != null && !links.equals("null") && !links.equals("")){
	     if(links.endsWith(".0"))	
		 links = links.substring(0,links.length()-2); 		
	     while(links.length() != 0){				 
		 if(links.indexOf(";")==-1 && 
		    !component.equals(getLinksComponent(links))){		         
		     xmlConverterListModel.get(index).
			 addPredecessor(links+"@"+(String)getLinksComponent(links));	       
		     links = "";		   
		 }
		 else if(links.indexOf(";")!=-1){		    
		     String pre = links.substring(0,links.indexOf(";"));		     
		     links = links.substring(links.indexOf(";")+1);
		     if(!((String)getLinksComponent(pre)).equals(component))
		     xmlConverterListModel.get(index).
			 addPredecessor(pre+"@"+(String)getLinksComponent(pre));   
		 }
		 else
		     break;
	     }
	 }else{
	     System.out.println("NO LINKS FOUND");}

	 xmlConverterListModel.get(index).
	    addOperation(); 
    } 
    public String getLinksComponent(String links){
	for(int i= 0;i<addedOperations.getItemCount();i++){
	    String op = addedOperations.getItem(i);	  
	    if(op.startsWith(links+"@")){
		return op.substring(op.indexOf("@")+1);
	    }
	}
	return "UNKNOWN"; 
    }
    public boolean doComponentExist(String resource,String descr){      	
	for(int k=0;k<componentList.getItemCount();k++){
		String item = componentList.getItem(k);
		String extResource = item.substring(0,item.indexOf("-"));
		String extComponent = item.substring(item.indexOf("-")+1);	
		if(resource.equals(extResource)&& descr.indexOf(extComponent)!=-1){	 
		    pointer = k; 		    
		    return true;
		}	
	}  
	return false;
    }	    
    @SuppressWarnings("deprecation")
	public String componentInterpreter(String description, String res)
    {
	String item = "";
	
	for(int i=0;i<knownItemsList.getItemCount();i++)
	    {
		String knownItem = knownItemsList.getItem(i);
		//int index = description.indexOf(knownItem);	
		if(description.indexOf(knownItem) !=-1)
		    item = knownItem;		
	    }
	if(item.equals("")) // don't do this! == "")
	{
	    item = "UNKNOWN "+(knownItemsList.getItemCount()-
			       listOfComponentTypesOrigalSize);
	    knownItemsList.addItem(item);
	}	   	
	else{    
	    int lastCaracter = description.indexOf(item)+item.length()-1;
	    int firstBlank = description.indexOf(" ",lastCaracter);
	    if(firstBlank != -1){
		int secondBlank = description.indexOf(" ",firstBlank+1);
		String number = "";
		if(secondBlank == -1 )
		    number = description.substring(firstBlank);
		else
		    number = description.substring(firstBlank,secondBlank);
		
		if(containingNumber(number))
		    item = item + number;	
	    }	    	    
	}	
	return res+"-"+item;   	
    }
    public boolean containingNumber(String str){
	for (int i=0;i<str.length();i++){
	    if (Character.isDigit(str.charAt(i)))
	    	return true;
	}
	return false;
    } 
    public void closeAllResources(File file)
    {
	new Loader();
	for(int index=0;index<componentList.getItemCount();index++)
	    {		
		xmlConverterListModel.get(index).
		    addRelation();
		xmlConverterListModel.get(index).saveResource(
			    new File(file.getParent()+"//"
				     +componentList.getItem(index)+".xml"));	
	    }
    }  
    public void saveOpToFile(String opInfo){
	try{
	    //opInfoDocument.write(5);
	    opInfoDocument.write(opInfo,0,opInfo.length());	   
	    opInfoDocument.newLine();
	    	}
	    catch(Exception ex){
		System.out.println(ex);}
    }
    class ListOfComponentTypes extends List {  	

		private static final long serialVersionUID = 1L;

		BufferedReader document ;

		@SuppressWarnings("deprecation")
		public ListOfComponentTypes(){
			try{
				document = new BufferedReader(new FileReader("ListOfComponentTypes.txt"));	 
				String line = document.readLine();;
				while(line != null){	
					this.addItem(line);
					line = document.readLine();	
				}
			} catch(Exception ex) {
				System.out.println(ex);
			}
		}
    }
}
