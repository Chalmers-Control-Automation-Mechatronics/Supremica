package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

import java.lang.*;

/**
*	class to help parse a PNN expression
*
*/
public class PPNparser extends PPNspec{

	public static String toHumanExp(String exp){
        //check indata
        if(INTERNAL_OPERATIONS == null || exp == null){
            return "";
        }
        
        /* replace internal to human operations */
        for(int i = 0; i < INTERNAL_OPERATIONS.length; i++){
            exp = exp.replace(INTERNAL_OPERATIONS[i],HUMAN_OPERATIONS[i]);
        }
        return exp;
    }
	
	public static String toInternalExp(String exp){
    
        //check indata
        if(INTERNAL_OPERATIONS == null || exp == null){
            return "";
        }
	
        //remove spaces
        exp = exp.replace(" ","");
        
        //change to internal representation
        for(int i =0; i < INTERNAL_OPERATIONS.length; i++){
            exp = exp.replace(HUMAN_OPERATIONS[i],INTERNAL_OPERATIONS[i]);
        }
        
        return exp;
    }
	
	public static String trimFirstAndLast(String exp){
		
		int parenthesis = 0;
		int sections = 0;
		
		/* if no parhentesis */
		if(!exp.contains(new String(new char[]{START}))){
			return exp;
		}
		
		for(int i = 0; i < exp.length(); i++){
			if(exp.charAt(i) == START){
				parenthesis++;
			}else if(exp.charAt(i) == END){
				parenthesis--;
				
				if(parenthesis == 0){
					sections = sections + 1;
				}
	    	}
		}
		
		//if only one section
		//remove first and last parhentesis
		if(sections == 1){
			if(exp.charAt(0) == START && exp.charAt(exp.length()-1) == END){
				return exp.substring(1,exp.length()-1);
			}
		}
		return exp; 
    }
	
	
	
	/**
	*	Input must first have been converted to internalExp
	*	see toInternalExp
	*/
	public static String getFirstEvent(String exp){
		//check indata
		if(exp == null || exp.length() == 0){
			return exp;
		}
		
		String event = "";
		String tmpEvent = "";
		String operation = "";
		
		//replace
		exp = exp.replace(ARBITARY_ORDER,ALTERNATIVE);
		event = exp;
		
		while(event.contains(SEQUENCE)){
			//System.out.println(SEQUENCE + " finns i " + event);
			//remove unnessesary parhenthesis
			
			event = trimParenthesis(event);
			exp = event;
			event = "";
			
			while(exp.length() > 0){
				//System.out.println(exp + " length() > 0");
				tmpEvent = getNextExp(exp);
				exp = removeFirst(tmpEvent,exp);
				
				//remove parenthesis
				if(tmpEvent.contains(SEQUENCE)){
					tmpEvent = trimParenthesis(tmpEvent);
				}
				
				//add event
				event = event + tmpEvent;
				operation = getNextOp(exp);
				
				//remove next event if sequence
				while(SEQUENCE.equals(operation)){
					//System.out.println(SEQUENCE + " equals " + operation);
					
					//remove sequence
					exp = removeFirst(operation,exp);
					
					//remove next event
					tmpEvent = getNextExp(exp);
					exp = removeFirst(tmpEvent,exp);	      
					
					//get next operation
					operation = getNextOp(exp);
				}
				
				//add operation to event
				event = event + operation;
				
				exp = removeFirst(operation,exp);
			}
		}
		
		//no sequences => this is the first event
		
		//remove unnessesary parhenthesis
		event = trimParenthesis(event);
		return event;
	}
	
	public static String getLastEvent(String exp){
		String event = "";
		String tmpEvent = "";
		String operation = "";
		
		//check indata
		if(exp == null || exp.length() == 0){
			return exp;
		}
		
		//replace
		exp = exp.replace(ARBITARY_ORDER,ALTERNATIVE);
		event = exp;
		
		while(event.contains(SEQUENCE)){
			//remove unnessesary parhenthesis
			event = trimParenthesis(event);
			exp = event;
			event = "";
			
			while(exp.length() > 0){
				//System.out.println("exp = " + exp);
				tmpEvent = getNextExp(exp);
				exp = removeFirst(tmpEvent,exp);
				
				//remove parenthesis
				if(tmpEvent.contains(SEQUENCE)){
					tmpEvent = trimParenthesis(tmpEvent);
				}
				
				operation = getNextOp(exp);
				exp = removeFirst(operation,exp);
				
				//dont add if sequence
				if(SEQUENCE.equals(operation)){
					//re
					tmpEvent = "";
					operation ="";
				}
				
				//add event
				event = event + tmpEvent;
				
				//add operation to event
				event = event + operation;
			}//end while
		}//end while
		
		//no sequences
		//this is the last event
		
		//remove unnessesary parhenthesis
		event = trimParenthesis(event);
		
		return event;
	}
	
	/**
	*  Funkar inte måste tänkas över två och tre ggr!!!!!!
	*/
	public static String removeFirstAndLastEvent(String exp){
		exp = removeFirstEvent(exp);
		exp = removeLastEvent(exp);
		
		return exp;
	}
	
	private static String removeFirstEvent(String exp){
	
		String rest = "";
		
		String tmpEvent = "";
		String operation = "";
		
		boolean again = false;
		
		//check indata
		if(exp == null || exp.length() == 0){
			return exp;
		}
		
		exp = trimParenthesis(exp);
		
		//remove first
		do{
			tmpEvent = getNextExp(exp);
			exp = removeFirst(tmpEvent,exp);
			
			operation = getNextOp(exp);
			exp = removeFirst(operation,exp);
			
			if(operation.equals(SEQUENCE) || exp.length() == 0){
				again = false;
			}else{
				again = true;
			}
	
		}while(again);
		
		return exp;
	}
	
	private static String removeLastEvent(String exp){
	
		String rest = "";
		String tmpEvent = "";
		String operation = "";
		boolean again = false;
		
		//check indata
		if(exp == null || exp.length() == 0){
			return exp;
		}
		
		exp = trimParenthesis(exp);
		
		while(exp.contains(SEQUENCE)){
			tmpEvent = getNextExp(exp);
			exp = removeFirst(tmpEvent,exp);
			
			rest = rest + tmpEvent;
			
			operation = getNextOp(exp);
			exp = removeFirst(operation,exp);
			
			if(!exp.contains(SEQUENCE)){
				break;  //exit while
			}
			
			//add event and operation
			rest = rest + operation;
		}
		return rest;
	}
	
	/**
	*	getNextExp() returns next expresion inside parenthesis 
    *	or to next operation.
    *	Ex. (a_ALTERNATIVE_b)_SEQUENCE_c returns (a_ALTERNATIVE_b)
    *   	 david_ALTERNATIVE_b_SEQUENCE_c returns david
    */
	public static String getNextExp(String exp){
	
		int parenthesis = 0;
		int index = 0;
		
		String tmp = "";
		if(exp.charAt(0) == START){
			//search for end parenthes
			for(index = 0; index < exp.length(); index++){
				if(exp.charAt(index) == START){
					parenthesis = parenthesis + 1;
				}else if(exp.charAt(index) == END){
					parenthesis = parenthesis - 1;
				}
				
				//end parenthes
				if(parenthesis == 0){
					return exp.substring(0,index+1);
				}
			}
			
			//unbalanced parenthesis
			//throw some exceptions
			System.err.println("!!! Warning unbalanced parenthesis !!!");
		}else{
			//go to next operation
			
			while(exp.length() > 0){
				//add first char to tmp
				tmp = tmp.concat(exp.substring(0,1));
				
				//remove first char from exp
				exp = exp.substring(1);
				
				//check if we have a operation
				for(int i = 0; i < INTERNAL_OPERATIONS.length; i++){
					if(exp.startsWith(INTERNAL_OPERATIONS[i])){
						return tmp;
					}
				}
			}
			//no operation left
			//this is a singel expression
			return tmp;
		}
		return ""; //something has gone wrong
	} 
	
	/* Måste fixas !!!! */
	public static String getProcess(String name){
		return name;
	}
	
	/**
	*getNextOp() returns the leading operation in a String  
	*operation String are stored in OPERATIONS and if
	*none of them matches null will be returned
	*/
	public static String getNextOp(String exp){
		exp = exp.trim(); 
		for(int i = 0; i < INTERNAL_OPERATIONS.length; i++){
			if(exp.startsWith(INTERNAL_OPERATIONS[i])){
				//return operation
				return INTERNAL_OPERATIONS[i];
			}
		}
		//something has gone wrong
		return "";
	}
	
	/**
	*removeFirst(patter,exp) if first sequence in exp
	*is equal to pattern returns. removes pattern from
	*exp and is returned. otherwise returns exp.
	*/
	public static String removeFirst(String pattern, String exp){
		//check indata
		if(pattern != null && exp != null){
			//exp moust be longer than pattern
			if(pattern.length() > exp.length()){
				return exp; //no match
			}
		}else{
			return exp; //null no match
		}
		
		//compare if pattern match first in exp
		for(int i = 0; i < pattern.length(); i++){
			if(pattern.charAt(i) != exp.charAt(i)){
				return exp; //no match
			}
		}
		
		//remove pattern
		return exp.substring(pattern.length());
	}
    
    
    /**
    *	String trimParenthesis(exp) removes unnesesary 
    *	pairs of parhenthesis.
    */
	public static String trimParenthesis(String exp){
		
		//check indata
		if(exp == null || exp.length() == 0){
			return "";
		}else if(exp.length() < 2){
			return exp;
		}
		
		boolean again = true;
		boolean noToken = true;
		
		int parenthesis = 0;
		
		while(again){
		//if leading parenthes
			if(exp.charAt(0) == START){
				//search for end parenthes
				for(int index = 0; index < exp.length(); index++){
					if(exp.charAt(index) == START){
						parenthesis = parenthesis + 1;
					}else if(exp.charAt(index) == END){
						parenthesis = parenthesis - 1;
					}else{
						noToken = false;
					}
					
					//end parenthes
					if(parenthesis == 0){
						if(index == exp.length()-1){
							exp = exp.substring(1,index);
							noToken = true;
						}else if(noToken){
							exp = exp.substring(index);
						}else{
							again = false;
						}
						break;
					}
				}//end for
			}else{
				//we are done
				again = false;
			}
		}//end while
		return exp;
	}
	
	/**
	*
	*	returns true if exp is
	*	a valid PPN expression
	*
	*/
    public static boolean validExp(String exp){
        
		/* Test indata */
		if(exp == null){
            return false;
        }
        
      
        //OBS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Om newexp inte klarar något test ska jag kasta exeptions
        //OBS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        //not allowed to have wrong number of
        //left and rigth brackets
        if(!matchedParenthesis(exp)){
            return false;
        }
        
        //not allowed to write () and )( in exp
        if(exp.contains(new String(new char[]{START, END})) ||
           exp.contains(new String(new char[]{END,START}))){
            return false;
        }
        
        //change to internal representation
        exp = toInternalExp(exp);
        
        //not allowed to start or begin whit a
        //operation
        for(int i =0; i < INTERNAL_OPERATIONS.length; i++){
            if(exp.startsWith(INTERNAL_OPERATIONS[i])){
                return false;
            }
            if(exp.endsWith(INTERNAL_OPERATIONS[i])){
                return false;
            }
        }
        
        /* Everything ok*/
        return true;
    }
	
    public static boolean containsNoOperations(String exp){
        for(int i=0; i < INTERNAL_OPERATIONS.length; i++){
            if(exp.contains(INTERNAL_OPERATIONS[i])){
                return false;
            }
        }
        return true;
    }
    
   /**
    * Function to check if we have the rigth number
    * of left and rigth brackets in an expression
    *
    */
	public static boolean matchedParenthesis(String exp){
		int parenthesis = 0;
		for(int i = 0; i < exp.length(); i++) {
			if(exp.charAt(i) == START) {
				parenthesis++;
			}else if(exp.charAt(i) == END) {
				parenthesis--;
			}
		}
		if(parenthesis == 0){
			return true;
		}
		return false;
	}
	
	public static boolean haveNoOperations(String exp){
        for(int i=0; i < INTERNAL_OPERATIONS.length; i++){
            if(exp.contains(INTERNAL_OPERATIONS[i])){
                return false;
            }
        }
        return true;
    }
}

