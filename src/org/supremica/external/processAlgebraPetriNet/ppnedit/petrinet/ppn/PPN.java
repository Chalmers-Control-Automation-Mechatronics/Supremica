package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

import java.lang.*;
import java.util.LinkedList;

/**
*
*	PPN class
*
*
*
*
*/
public class PPN extends PPNparser{
    
	private String process = "";
	private String exp = "";
	private String condition = "";
	
	public PPN(String func){
		setExp(func);
	}
	
	public void setExp(String exp){
		if(validExp(exp)){
			this.exp = toInternalExp(exp);
		}
	}
	
	public String getExp() {
        return toHumanExp(exp);
    }
	
	public void setProcess(String process){
		this.process = process;
	}
	
	public String getProcess() {
        return process;
    }
	
	public void setCondition(String condition){
		this.condition = condition;
	}
	
	public String getCondition() {
        return condition;
    }
	
	public String toString() {
        return process + EQUAL + exp + GSTART + condition + GEND;
    }
}

