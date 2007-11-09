package org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn;

import java.lang.*;
import java.util.ListIterator;
import java.util.LinkedList;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.Converter;
import org.supremica.manufacturingTables.xsd.processeditor.*;



//class to describe a PteriNet process
//processen består endast av ETT uttryck, expressions

//Pro
//  |___ BasePetriPro
//              |__ PetriProROP
//                      |__ PetriPro
public class PetriPro
				extends BasePetriPro
                              implements PetriProListener{
    
    private PetriProListener[] petriProListeners = null;
    
    private LinkedList pps;
    
    //
    private String tmp = "";
    
    //constructor
    public PetriPro(){
        super();
        setExp(name.toLowerCase());
        addProcess();
    }
    
    public PetriPro(String name){
        super();
        setName(name);
        setExp(name.toLowerCase());
        addProcess();
    }
    
    private void addProcess(){
        if(petriProList == null ){
            petriProList = new LinkedList();
            petriProList.add(this);
        }else{
            if(check()){
                petriProList.add(this);
            } else {
                ;//vad ska jag göra om namn upptaget???????????????????????????
            }
        }
    }
    
	public String getExp(){
		PetriPro pro;
		tmp = super.getExp();
		
		//check if we have processes in expression
		if(pps != null && pps.size() > 0){
			ListIterator listIterator = pps.listIterator();
			
			if(listIterator != null){
				while(listIterator.hasNext()){
					pro = (PetriPro)listIterator.next();
					tmp.replaceAll(pro.getName(),pro.getExp());
				}
			}
		}
		return tmp;
	}
    
    public String getHumanExp(){
      return PPN.toHumanExp(super.getExp());
    }
    
    public static LinkedList getAllPetriPro(){
        return petriProList;
    }
    
    public static PetriPro getPetriProByExp(String exp){
      PetriPro pro;
      ListIterator listIterator = petriProList.listIterator();
        
      if(listIterator != null){
        while(listIterator.hasNext()){
            pro = (PetriPro)listIterator.next();
            if(exp.equals(pro.getExp())){
                return pro;
            }
        }
      }
      return null;
    }
    
    public static PetriPro getPetriProByName(String name){
      PetriPro pro;
      ListIterator listIterator = petriProList.listIterator();
        
      if(listIterator != null){
        while(listIterator.hasNext()){
            pro = (PetriPro)listIterator.next();
            if(name.equals(pro.getName())){
                return pro;
            }
        }
      }
      return null;
    }
    
    public void finalize() throws Throwable{
        petriProList.remove(this);
    }
    
    /**
	*
	*	function to check if name already taken by 
	*	another PetriPro
	*	
	*/
    private boolean check(){
        
        if(petriProList == null ){
            return true;
        }
        
        Object[] o = petriProList.toArray();
        if(o.length == 0){
            return true;
        }
        
        if(o instanceof PetriPro[]){
            PetriPro[] pros = (PetriPro[])o;
            
            for(int i = 0; i < pros.length; i++){
                if((pros[i].getName()).equals(name)){
                    return false;
                }
            }
        }
        return true;
    }
    
    public void setExp(String exp){
       
	   String expToParse = exp;
	   tmp = this.exp; //store expression
	   
	   super.setExp(exp);
	   
	   //tell the world if changed
	   if(!tmp.equals(this.exp)){ //if changed
	   	//check if we have other processes in exp
		while(expToParse.length() > 0){
			//check event
			tmp = PPN.getNextExp(expToParse);
			expToParse = PPN.removeFirst(tmp,expToParse);
			
			//if first char is uppercase it is a
			//process
			//funkar då det inte finns parentheser
			
			if(Character.isUpperCase(tmp.charAt(0))){
				addPetriPro(getPetriProByName(tmp));
			}
			
			//remove next operation
			expToParse = PPN.removeFirst(PPN.getNextOp(expToParse),expToParse);
		}
	  
	  	expChanged();
		}
    }
	
	private void expChanged(){
		if(petriProListeners == null ||
		   petriProListeners.length == 0){ 
			return;
		}
		
		/* tell that we have changed*/
		for(int i = 0; i < petriProListeners.length; i++){
			petriProListeners[i].expChanged(this);
		}
	}
    
   /*
    * Store processeses in exprecssion
    *
    */
    private void addPetriPro(PetriPro pp){
      
      //check indata
      if(pp == null){
        return;
      }
      
      //indata ok add
      if(pps == null ){
            pps = new LinkedList();
            pps.add(pp);
      }else{
            pps.add(pp);
      }
      
      pp.addPetriProListener(this);
      
      System.out.println("Added " + pp.getName() + " to " + this.getName());
    }
     
   /**
    * Remove one PetriPro from LinkedList pps
    * And tell that PetriPro to remove this from listening
    */
    private void removePetriPro(PetriPro pp){
    
      //check indata
      if(pp == null){
        return;
      }
      
      if(pps == null ){
        ;//do nothing
      }else{
        pps.remove(pp);
      }
      
      pp.removePetriProListener(this);
    }
    
    public String toString(){
        return name + EQUAL + getHumanExp();
    }
    
    
    public void addPetriProListener(PetriProListener l){
        if(petriProListeners == null){
            petriProListeners = new PetriProListener[] {l};
        } else {
            PetriProListener[] tmp = petriProListeners;
            petriProListeners = new PetriProListener[tmp.length+1];
            for(int i = 0; i < tmp.length; i++) {
                petriProListeners[i] = tmp[i];
            }
            petriProListeners[tmp.length] = l;
        }
    }
    
    public void removePetriProListener(PetriProListener l){
        if(petriProListeners == null ||
	   petriProListeners.length == 0){
            ;//do nothing
        } else {
	
	  int exist = -1;
	  for(int i = 0; i < petriProListeners.length; i++) {
	    if(petriProListeners[i].equals(l)) {
		exist = i;
		break;
	    }
	  }
	
	  if(exist != -1) {	
	    PetriProListener[] tmpls = petriProListeners;
	    
	    petriProListeners = new PetriProListener[tmpls.length-1];	    
	    int j = 0;
	    for(int i = 0; i < tmpls.length; i++) {
		if(i != exist) {
		  petriProListeners[j++] = tmpls[i];
		}		
	    }
	  }	
	}
    }
	
	public ROP getROP(){
		return Converter.createROP(exp);
	}
	
	public Relation getRelation(){
        return Converter.createRelation(exp);
    }
    
    //----------------- Listeners -------------------------------- 
    public void expChanged(PetriPro pp) {
      //a process in our exp has changed      
      System.out.println("Internal process changed exp");
      //tell the world we have changed
      expChanged();
    }
}
