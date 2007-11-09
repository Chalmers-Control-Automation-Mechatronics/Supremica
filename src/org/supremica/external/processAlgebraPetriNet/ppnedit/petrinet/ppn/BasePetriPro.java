package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

import java.util.LinkedList;

//class to describe a PteriNet process
//No operatons allowed at the end or begining
//not alowed to have numbers at the beginning at activity

//Pro
//  |___ PetriPro
public class BasePetriPro extends Pro{
    
    protected static LinkedList petriProList;
    
    //private
    private int id = -1;
    
    //insatnce common
    private static int numberOfBasePetriPro = 0;
	
	//private static String[] op = new String[]{"->","+","*","&","u","<",">"};
    
    //constructor
    public BasePetriPro(){
		super();
        
		numberOfBasePetriPro = numberOfBasePetriPro + 1;
        id = numberOfBasePetriPro;
		
        setName("P"+id); //default name
    }
    
    public void setExp(String newexp){
        if(PPN.validExp(newexp)){
        	super.setExp(newexp);
		}else{
			System.err.println("Not a valid PPN exp " + newexp);
		}
    }
    
    public static String getOp(String operation){
        if(operation == null){
			return "";
		}
		
        for(int i = 0; i < INTERNAL_OPERATIONS.length; i++){
        	if(operation.equals(INTERNAL_OPERATIONS[i])){
            	return HUMAN_OPERATIONS[i];
            }
        }
		
        return "";
    }
    
    protected String[] getStringsBetweenPattern(String pattern, String s){
        
        String regex = pattern;  //regular expresion
        int all = -1;   //get all
        
        return s.split(regex,all);
    }
}

