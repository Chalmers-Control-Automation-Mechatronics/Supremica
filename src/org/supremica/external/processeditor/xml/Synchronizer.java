package org.supremica.external.processeditor.xml;

import java.io.*;
import java.util.*;

//import org.supremica.automata.*;
//import org.supremica.automata.IO.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;

public class Synchronizer {          
    private ObjectFactory factory = new ObjectFactory();

    public Synchronizer() {}    

    public Object synchronizeDOPs(File[] files ) {	
	//DEBUG
	System.out.println("synchronizer.synchronizeDOPs()");
	//END DEBUG
	try {
	    Loader loader = new Loader();	  			    
	    Object[][] op = new Object[files.length][];
	    int[] opIndex = new int[files.length];
	    String machineName = "";	    
	    Object[] prev = new Object[op.length];
	    for(int i = 0; i < files.length; i++) {	    
		ROP tmpRop = ((ROP)loader.open(files[i]));		
		op[i] = tmpRop.getRelation().getActivityRelationGroup().toArray();
		opIndex[i] = 0;
		if(i != 0) {
		    machineName += "||";
		}
		machineName += tmpRop.getMachine();
		prev[i] = null;
	    }  	    
	    Relation[] relations = new Relation[0];
	    Boolean someOneHasNext = true;
	    while(someOneHasNext) {			
		int[] tmpOpIndex = new int[opIndex.length];
		for(int i = 0; i < tmpOpIndex.length; i++) {
		    tmpOpIndex[i] = opIndex[i];
		}				
		for(int i = 0; i < op.length; i++) {		   
		    if(opIndex[i] < op[i].length) {
			someOneHasNext = true;			
			Object next = op[i][opIndex[i]];
			if(next instanceof Activity) {			       
			    if(predecessorFinish(relations, next)) {
				int[] prevIndex = findPredecessor(relations, next, prev[i]);	    
				if(prevIndex.length == 0) {
				    relations = newRelation(relations, next);
				}else if(prevIndex.length == 1) {			    
				    relations[prevIndex[0]].getActivityRelationGroup().add(next);
				}else {
				    relations = mergeRelations(relations, prevIndex, next);
				}
				prev[i] = next;
				opIndex[i]++;
			    }
			}
		    }
		}
		someOneHasNext = false;
		for(int i = 0; i < opIndex.length; i++) {		   
		    if(tmpOpIndex[i] !=  opIndex[i]) {
			someOneHasNext = true;
			break;
		    }
		}
	    }
	    ROP newRop = factory.createROP();
	    newRop.setType(ROPType.COP);
	    newRop.setId("1");
	    newRop.setMachine(machineName);	
	    newRop.setRelation(factory.createRelation());
	    if(relations.length > 1) {
		newRop.getRelation().setType(RelationType.PARALLEL);
	    }else {
		newRop.getRelation().setType(RelationType.SEQUENCE);
	    }
	    for(int i = 0; i < relations.length; i++) {
		newRop.getRelation().getActivityRelationGroup().add(relations[i]);
	    }
	    loader.save(newRop, new File(files[0].getParent()+"//synchronized.xml"));	    
	    return (Object)newRop;
	}catch(Exception ex) {	   
	}
	return null;
    }    
    public Relation[] newRelation(Relation[] relations, Object op) {
	//DEBUG
	System.out.print("Synchronizer.newRelation(): ");
	System.out.println(relations.length);
	//END DEBUG
	try {
	    Relation[] tmpRelations = new Relation[relations.length+1];
	    for(int i = 0; i < relations.length; i++) {
		tmpRelations[i] = relations[i];	    
	    }	
	    tmpRelations[relations.length] = factory.createRelation();
	    tmpRelations[relations.length].setType(RelationType.SEQUENCE);
	    tmpRelations[relations.length].getActivityRelationGroup().add(op); 
	    return tmpRelations;
	}catch(Exception ex) {
	    System.out.println("ERROR in Synchronizer.newRelation()");
	}
	return relations;
    }
    public Relation[] mergeRelations(Relation[] relations, int[] index, Object op) {
	//DEBUG
	System.out.println("Synchronizer.mergeRelations()");
	//END DEBUG
	try {
	    Relation newParallelRelation = factory.createRelation();
	    newParallelRelation.setType(RelationType.PARALLEL);
	    for(int i = 0; i < index.length; i++) {
		newParallelRelation.getActivityRelationGroup().add(relations[index[i]]);
	    }
	    Relation newSequenceRelation = factory.createRelation();
	    newSequenceRelation.setType(RelationType.SEQUENCE);
	    newSequenceRelation.getActivityRelationGroup().add(newParallelRelation);
	    newSequenceRelation.getActivityRelationGroup().add(op);
	    
	    Relation[] newRelations = new Relation[relations.length - index.length +1];
	    int m = 0;
	    int n = 0;
	    for(int i = 0; i < relations.length; i++) {
		if(i != index[m]) {
		    newRelations[n++] = relations[i];
		}else {
		    m++;
		}
	    }
	    newRelations[newRelations.length-1] = newSequenceRelation;
	    return newRelations;
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR in Synchronizer.mergeRelation()");
	    //END DEBUG
	}
	return relations;
    }
    public boolean predecessorFinish(Relation[] relations, Object o) {       
	if(o instanceof Activity) {	    
	    if(((Activity)o).getPrecondition() != null) {
		Iterator<OperationReferenceType> predIt = ((Activity)o).getPrecondition().getPredecessor().iterator();
		while(predIt.hasNext()) {
		    if(!predecessorFinish(relations, predIt.next())) {
			return false;
		    }
		}
	    }			    
	}else if(o instanceof OperationReferenceType) {
	    for(int i = 0; i < relations.length; i++) {
		Object[] activities = relations[i].getActivityRelationGroup().toArray();
		for(int j = 0; j < activities.length; j++) {
		    if(activities[j] instanceof Activity) {
			if(((Activity)activities[j]).getOperation().equals(((OperationReferenceType)o).getOperation())) {
			    return true;
			}
		    }
		}
	    }
	    return false;
	}
	return true;
    }    
    public int[] findPredecessor(Relation[] relations, Object op, Object prev) {
	//DEBUG
	System.out.println("Synchronizer.findPredecessor(Relation[], Object, Object)");
	//END DEBUG
	int[] prevIndex = new int[0];
	int numOfPreds = 0;
	if(op instanceof Activity) {
	    if(((Activity)op).getPrecondition() != null) {
		numOfPreds = ((Activity)op).getPrecondition().getPredecessor().size();
	    }
	}	
	if(prev != null && prev instanceof Activity) {
	    numOfPreds++;
	}	
	String[] preds = new String[numOfPreds];
	if(op instanceof Activity) {
	    if(((Activity)op).getPrecondition() != null) {
		int predsIndex = 0;
		Iterator<OperationReferenceType> itPred = ((Activity)op).getPrecondition().getPredecessor().iterator();
		while(itPred.hasNext()) {
		    Object next = itPred.next();
		    if(next instanceof OperationReferenceType) {
			preds[predsIndex++] = ((OperationReferenceType)next).getOperation();
		    }
		}
	    }
	}       
	if(prev != null && prev instanceof Activity) {
	    preds[preds.length-1] = ((Activity)prev).getOperation();
	}		
	for(int i = 0; i < relations.length; i++) {	    
	    if(findPredecessor(relations[i].getActivityRelationGroup().iterator(), preds)) {
		prevIndex = addIndex(prevIndex, i);
	    }
	}	
	//DEBUG
	for(int i = 0; i < prevIndex.length; i++) {
	    System.out.print(prevIndex[i]+",");
	}
	System.out.println("");
	//END DEBUG
	return prevIndex;
    }
    public boolean findPredecessor(Iterator<?> it, String[] preds) {
	//DEBUG
	System.out.println("Synchronizer.findPredecessor(Iterator, String[]");
	//END DEBUG
	if(it.hasNext()) {
	    Object next = it.next();
	    if(next instanceof Activity) {		
		for(int i = 0; i < preds.length; i++) {
		    if(((Activity)next).getOperation().equals(preds[i])) {
			//DEBUG
			System.out.println("true");
			//END DEBUG
			return true;
		    }else {
			return findPredecessor(it, preds);
		    }
		}
	    }
	}
	//DEBUG
	System.out.println("false");
	//END DEBUG
	return false;	
    }
    public int[] addIndex(int[] vector, int index) {
	int[] tmpVector = new int[vector.length+1];
	for(int i = 0; i < vector.length; i++) {
	    tmpVector[i] = vector[i];
	}
	tmpVector[vector.length] = index;
	return tmpVector;
    }
}
