package org.supremica.external.processeditor.xml;

import java.io.*;
import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.Algebraic;
import org.supremica.manufacturingTables.xsd.processeditor.ROPType;

public class XmlConverter implements FileConverter {
    private ObjectFactory factory = new ObjectFactory();     

    private ROP rop = null;
    private Relation[] relation = new Relation[0];
    private Activity activity = null; 

    public XmlConverter() {}    

    public Object open(File file, FileConverter fc) {
	return null;
    }

    public void save(File file, Object rop, FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.save()");
	//END DEBUG
	if(rop instanceof ROP) {
	    try{
		fc.newResource(((ROP)rop).getMachine());
		saveRelation(((ROP)rop).getRelation(), fc);
		fc.saveResource(file);
	    }catch(Exception ex) {}
	}
    }
    public static void saveRelation(Object relation, FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.saveRelation()");
	//END DEBUG
	if(relation instanceof Relation) {
	    fc.newRelation(((Relation)relation).getType().name());
	    Iterator<?> iterator = ((Relation)relation).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		Object next = iterator.next();
		if(next instanceof Relation) {
		    saveRelation(next, fc);
		}else if(next instanceof Activity) {
		    saveActivity(next, fc);
		}
	    }
	    fc.addRelation();
	}
    }
    public static void saveActivity(Object activity, FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.saveActivity()");
	//END DEBUG
	if(activity instanceof Activity) {
	    String opText = "";
	    if(((Activity)activity).getOperation() != null && !((Activity)activity).getOperation().equals("")) {
	    	opText = ((Activity)activity).getOperation();
	    }else {
	    	opText = ((Activity)activity).getOperation();
	    }
	    fc.newOperation(opText);
	    if(((Activity)activity).getProperties() != null) {
		Iterator<Attribute> iterator = ((Activity)activity).getProperties().getAttribute().iterator();
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    if(next instanceof Attribute) {
			fc.addAttribute(((Attribute)next).getAttributeValue()+"@"+((Attribute)next).getType());
		    }
		}
	    }
	    fc.addOperation();
	}
    }
public void newResource(String machine,String id) {
	//DEBUG
	System.out.println("newResource("+machine+")");
	//END DEBUG
	try {
	    rop = factory.createROP();			
	    rop.setType(ROPType.COP);
	    rop.setId(id);
	    rop.setMachine(machine);       
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newResource");
	    //END DEBUG
	}	    	
    }    
    public void newResource(String machine) {
	//DEBUG
	System.out.println("newResource("+machine+")");
	//END DEBUG
	try {
	    rop = factory.createROP();			
	    rop.setType(ROPType.COP);
	    rop.setId("1");
	    rop.setMachine(machine);       
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newResource");
	    //END DEBUG
	}	    	  	
    }
    
    public void newRelation(String relationType) {
    	if(relationType.equals(RelationType.SEQUENCE)){
    		newRelation(RelationType.SEQUENCE);
    	}else if(relationType.equals(RelationType.ALTERNATIVE)){
    		newRelation(RelationType.ALTERNATIVE);
    	}else if(relationType.equals(RelationType.PARALLEL)){
    		newRelation(RelationType.PARALLEL);
    	}else if(relationType.equals(RelationType.ARBITRARY)){
    		newRelation(RelationType.ARBITRARY);
    	}else{
    		System.err.println("Unknown relationType: " + relationType);
    	}
    		
    }
    
    public void newRelation(RelationType relationType) {
	//DEBUG
	System.out.println("newRelation("+relationType+")");
	//END DEBUG
	try {
	    Relation[] tmpRelation = new Relation[relation.length+1];
	    for(int i = 0; i < relation.length; i++) {
	    	tmpRelation[i] = relation[i];
	    }
	    
	    tmpRelation[relation.length] = factory.createRelation();
	    relation = tmpRelation;	
	    if(relationType.equals(RelationType.SEQUENCE)) {
	    	relation[relation.length-1].setType(RelationType.SEQUENCE);
	    }else if(relationType.equals(RelationType.ALTERNATIVE)) {
	    	relation[relation.length-1].setType(RelationType.ALTERNATIVE);
	    	relation[relation.length-1].getAlgebraic().setUnextended(true);
	    	relation[relation.length-1].getAlgebraic().setCompressed(true);
	    }else if(relationType.equals(RelationType.PARALLEL)) {		
	    	relation[relation.length-1].setType(RelationType.PARALLEL);
	    	
	    	Algebraic algebraic = factory.createAlgebraic();
	    	algebraic.setUnextended(true);
	    	algebraic.setCompressed(true);
	    	relation[relation.length-1].setAlgebraic(algebraic);
	    }else if(relationType.equals("Arbitrary")) {
	    	relation[relation.length-1].setType(RelationType.ARBITRARY);
	    	relation[relation.length-1].getAlgebraic().setUnextended(true);
	    	relation[relation.length-1].getAlgebraic().setCompressed(true);
	    }	       
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newRelation");
	    //END DEBUG
	}
    }
    public void addRelation() {
	try {
	    //DEBUG
	    System.out.println("addRelation()");
	    //END DEBUG
	    if(relation.length == 1) {
		rop.setRelation(relation[0]);
	    }else if(relation.length > 1){
		relation[relation.length-2].getActivityRelationGroup().add(relation[relation.length-1]);
		Relation[] tmpRelation = new Relation[relation.length-1];
		for(int i = 0; i < tmpRelation.length; i++) {
		    tmpRelation[i] = relation[i];		
		}
		relation = tmpRelation;
	    }
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addRelation");
	    //END DEBUG
	}
    }
    public void newOperation(String operation) {
	try {
	    //DEBUG
	    System.out.println("newOperation("+operation+")");
	    //END DEBUG
	    activity = factory.createActivity();
	    activity.setOperation(operation);
	}catch(Exception ex) {}
    }
    public void addOperation() {
	try {
	    //DEBUG
	    System.out.println("addOperation()");
	    //END DEBUG
	    if(activity != null) {
		relation[relation.length-1].getActivityRelationGroup().add(activity);
	    }
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addOperation");
	    //END DEBUG
	}
    }
    public void addAttribute(String att) {
	try {	
	    //DEBUG
	    System.out.println("addAttribute("+att+")");
	    //DEBUG
	    Attribute attribute = factory.createAttribute();
	    int index = att.indexOf('@');
	    if(index != -1) {
		attribute.setAttributeValue(att.substring(0,index));
		attribute.setType(att.substring(index+1,att.length()));	
		attribute.setInvisible(true);
		if(activity != null) {
		    if(activity.getProperties() == null) {
			activity.setProperties(factory.createProperties());
		    }
		    activity.getProperties().getAttribute().add(attribute);
		}	
	    }
	   
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addAttribute");
	    //END DEBUG
	}
    }
    public void addPredecessor(String pred) {
	try {
	    //DEBUG
	    System.out.println("addPredecessor("+pred+")");
	    //END DEBUG	
	    OperationReferenceType predecessor = factory.createOperationReferenceType();
	    int index = pred.indexOf('@');
	    if(index != -1) {
		predecessor.setOperation(pred.substring(0,index));
		predecessor.setMachine(pred.substring(index+1,pred.length()));
		if(activity != null) {
		    if(activity.getPrecondition() == null) {
			activity.setPrecondition(factory.createPrecondition());
		    }
		    activity.getPrecondition().getPredecessor().add(predecessor);
		}
	    }else {
		predecessor.setOperation(pred);
		predecessor.setMachine("");
		if(activity != null) {
		    activity.setPrecondition(factory.createPrecondition());
		}
		activity.getPrecondition().getPredecessor().add(predecessor);
	    }	    		
	}catch(Exception ex) {}
    }
    public void addDescription(String desc) {
	try {	
	    //DEBUG
	    System.out.println("addDescription("+desc+")");
	    //DEBUG	    
	    if(activity != null) {
	    	activity.setDescription(desc);
	    }
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addAttribute");
	    //END DEBUG
	}
    }
    public Object getResource() {
	return rop;
    }
    public void saveResource(File file) {
	Loader loader = new Loader();
	loader.save(getResource(),file);	
	/*
	loader.save(((FileConverter)xmlConverterListModel.get(index)).getResource(),
			    new File(file.getParent()+"//"+"test"+
			    Integer.toString(index)+".xml"));	*/
    }

    public FileConverter newInstance() {
	return new XmlConverter();
    }
}
