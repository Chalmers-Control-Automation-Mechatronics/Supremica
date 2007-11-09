package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

/**
*
*	Simple expression stack used to parse PPN expression
*
*/
public class ExpStack{
        
    private String[] expStack = null;

    public ExpStack(){}

	/**
	*	put new String on top of stack
	*/
    public void pushExp(String newString){
        //check indata
        if(newString != null && newString.length() > 0){

            if(expStack == null){
                expStack = new String[] {newString};
            } else {
                String[] tmp = expStack;
                expStack = new String[tmp.length+1];
                for(int i = 0; i < tmp.length; i++) {
                    expStack[i] = tmp[i];
                }
                expStack[tmp.length] = newString;
            }
        }
		
		//printStack();
    }

	/**
	*	get first String from stack
	*/
    public String popExp(){
		if(expStack == null){
			return "";
		}else{
			String[] tmp = expStack;
			expStack = new String[tmp.length - 1];
			for(int i = 0; i < expStack.length; i++) {
				expStack[i] = tmp[i];
			}
			return tmp[tmp.length-1];
		}
	}


	/**
	*	concat String to first String in stack
	*/
    public void concatExp(String stringToConcat){
        if(expStack != null && expStack.length > 0){
            expStack[expStack.length - 1] = 
                    expStack[expStack.length - 1].concat(stringToConcat);
        }else{
            pushExp(stringToConcat);
        }
    }

	/**
	*	return stack as array of String
	*/
    public String[] getExpStack(){
        return expStack;
    }

	/**
	*	Empty stack
	*/
    public void flush(){
        expStack = null;
    }
	
	/**
	*	print the stack in console
	*/
    public void printStack(){
        for(int i = 0; i < expStack.length; i++) {
			System.out.println(expStack[i]);
		}
    }
}
