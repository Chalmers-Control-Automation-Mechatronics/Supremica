package org.supremica.external.processeditor.xml;

import java.awt.Point;
import java.math.*;
import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.Properties;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.ROPType;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.Algebraic;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;

/**
 * The <code>Converter</code> class includes <code>static</code> methods,
 * necessary for manipulation of xml objects defined by the 
 * <code>org.xml.rop</code> package.
 */
public class Converter {    
    
    public static final int IS_VISIBLE_TRUE = 0;
    public static final int IS_VISIBLE_FALSE = 1;
    public static final int IS_VISIBLE_TRUE_CHANGED = 2;
    public static final int IS_VISIBLE_FALSE_CHANGED = 3;
    public static final int IS_VISIBLE_ERROR = 4;

    private static ObjectFactory objectFactory = new ObjectFactory(); 

    /**
     * Prints the object of ROP type at the standard output.
     *
     * @param o ROP object to be printed
     */
    public static void printROP(Object o) {		
	//DEBUG
	//System.out.println("Converter.printROP()");
	//END DEBUG
	if(o instanceof ROP) {
	    try {
		ROP resrc = (ROP)o;	
		System.out.println("------------PRINT RESOURCE-------------\n");
		System.out.println("TYPE:    "+resrc.getType());
		System.out.println("MACHINE: "+resrc.getMachine());
		System.out.println("ID:      "+resrc.getId());
		System.out.println("COMMENT: "+resrc.getComment());
		System.out.println("");
		printRelation(resrc.getRelation());
		System.out.println("\n--------------- Finished -----------------\n");
	    }catch(Exception ex) {
		if(ex instanceof NullPointerException) {
		    System.out.println("EMPTY ROP!");
		}else {
		    System.out.println("ERROR! while printing rop"+
				       "in Converter.printROP");
		}
	    }
	}else {
	    System.out.println("ERROR! in Converter.printROP: wrong type of object");
	}
    }  
    /**
     * Prints the object of Relation type at the standard output.
     *
     * @param relation the relation object to be printed
     */
    public static void printRelation(Relation relation) {	
	try {
	    System.out.println("---> RelationType: "+relation.getType()+" <---\n");      	
	    System.out.println("OPERATION\tPREDECESSOR\tATTRIBUTE\n");	   
	    List actRelGroup = relation.getActivityRelationGroup();
	    Iterator actRelGroupIterator =  actRelGroup.iterator();
	    while(actRelGroupIterator.hasNext()) {
		Object o = actRelGroupIterator.next();
		if(o instanceof Activity) {
		    printActivity((Activity)o);
		}else if(o instanceof Relation) {
		    printRelation((Relation)o);
		}
	    }	
	}catch(Exception ex) {
	    if(ex instanceof NullPointerException) {
		System.out.println("NO OPERATIONS!");
	    }else {
		System.out.println("ERROR! while printing operations "+
				   "in Converter.printRelation()");
	    }
	}
    }   
    /**
     * Prints the object of Activity type at the standard output.
     *
     * @param activity the activity object to be printed
     */
    public static void printActivity(Activity activity) {	    	
	try {
	    List predecessorList = activity.getPrecondition().getPredecessor();		
	    Iterator predecessorIterator = predecessorList.iterator();	    
	    while(predecessorIterator.hasNext()) {
		Object o = predecessorIterator.next();
		if(o instanceof OperationReferenceType) {        
		    System.out.println("\t\t"+
				       ((OperationReferenceType)o).getOperation()+"@"+
				       ((OperationReferenceType)o).getMachine());  
		}
	    }
	}catch(Exception ex) {
	    if(ex instanceof NullPointerException) {				    		
		System.out.println("\t\tnull");		
	    }else {		
		//DEBUG
		//System.out.println("ERROR! while printing predecessors "+
		//		   "in Converter.printActivity()");
		//END DEBUG
	    }
	}		
	try {
	    List attributeList = activity.getProperties().getAttribute();
	    Iterator attributeIterator = attributeList.iterator();	    
	    while(attributeIterator.hasNext()) {
		Object o = attributeIterator.next();
		if(o instanceof Attribute) {        
		    System.out.println("\t\t\t\t"+
				       ((Attribute)o).getType()+"@"+
				       ((Attribute)o).getAttributeValue());
		}
	    }
	}catch(Exception ex) {
	    if(ex instanceof NullPointerException) {				    		
		System.out.println("\t\t\t\tnull");		
	    }else {
		//DEBUG
		//System.out.println("ERROR! while printing predecessors "+
		//		   "in Converter.printActivity()");
		//END DEBUG
	    }
	}		
	System.out.println("--> "+activity.getOperation());  
    }
    /**
     * Returns a sting representation of the object.
     * <p>
     * <ul>
     * <li>If the object is of ROP type the machine name will be returned</li>
     * <li>If the object is of Relation type the algebraic expression of the
     * relation will be returned</li>
     * <li>If the object is fo Activity type the operation name 
     * will be returned</li>
     * <li>Otherwise an empty string will be returned</li>
     * </ul>
     *
     * @param o the object that is to be converted
     * @return a string representation of the object.
     */
    public static String convertToString(Object o) {
	if(o instanceof ROP) {
	    return ((ROP)o).getMachine();
	}else if(o instanceof Relation) {
	    return convertRelationToString((Relation)o);
	}else if(o instanceof Activity) {
	    return ((Activity)o).getOperation();
	}else {
	    //DEBUG
	    //System.out.println("ERROR! wrong type of object "+
	    //		       "in Converter.convertToString");
	    //END DEBUG
	    return "";
	}
    }
    /**
     * Returns the algebraic expression of the specified relation.
     *
     * @param relation the relation that is to be converted
     * @return a string representation of the relation
     */
    public static String convertRelationToString(Relation relation) {
    	String str = "";
    	try {
    		String operand = "";
    		if(relation.getType().equals("Sequence")) {		
    			operand = String.valueOf('\u2192');
    		}else if(relation.getType().equals("Alternative")) {
    			operand = "+";
    		}else if(relation.getType().equals("Parallel")) {
    			operand = "||";
    		}else if(relation.getType().equals("Arbitrary")) {
    			operand = String.valueOf('\u2295');		
    		}	    	    
	    
    		List actRelGroup = relation.getActivityRelationGroup();
    		Iterator actRelGroupIterator =  actRelGroup.iterator();
    		
    		while(actRelGroupIterator.hasNext()) {
    			
    			Object o = actRelGroupIterator.next();
    			
    			if(o instanceof Activity) {
    				str += ""+((Activity)o).getOperation()+"";
    			}else if(o instanceof Relation) {
    				str += "("+convertRelationToString((Relation)o)+")";
    			}
    			
    			if(actRelGroupIterator.hasNext()) {
    				str += operand;
    			}
    		}	
    	}catch(Exception ex) {
    		if(ex instanceof NullPointerException) {
    			//DEBUG
    			//System.out.println("NO OPERATIONS!");
    			//END DEBUG
    		}else {
    			//DEBUG
    			//System.out.println("ERROR! while converting relation "+
    			//		   "in Converter.convertRelationToString()");
    			//END DEBUG
    		}
    	}
    	return str;	        		    	 
    }	
    /**
     * Converts an algebraic expression to a relation (or just a activity).
     *
     * @param algebraic the algebraic expression
     * @return the resulting relation (or activity)
     */
    public static Object convertStringToActivityRelation(String algebraic) {
    	if(!algebraic.equals("")) {	   	    	    	    
    		
    		if(algebraic.startsWith("(") &&
    		   algebraic.endsWith(")") &&
    		   removeParenthesis(algebraic).equals(algebraic.substring(1,algebraic.length()-1)))
    		{				
    			Relation newRelation = null;
    			try {
    				newRelation = objectFactory.createRelation();
    			}catch(Exception ex) {}
    			
    			newRelation.setType(RelationType.SEQUENCE);
    			if(!algebraic.substring(1, algebraic.length()-1).equals("")) {
    				newRelation.getActivityRelationGroup().add(convertStringToActivityRelation(algebraic.substring(1, algebraic.length()-1)));		  
    				return newRelation;
    			}
    		}else {
    			RelationType type = getType(algebraic);
    			if(type == null || type.equals("")) {				    
    				Activity newActivity = null;
    				
    				try {
    					newActivity = objectFactory.createActivity();
    				}catch(Exception ex) {}
    				
    				newActivity.setOperation(algebraic);
    				return newActivity;		  
    			}else {		   
    				Relation newRelation = null;
    				try {
    					newRelation = objectFactory.createRelation();
    				}catch(Exception ex) {}
    					
    					newRelation.setType(type);
    					
    					char sign = '-';
    					
    					if(RelationType.SEQUENCE.equals(type)) {
    						sign = '-';
    					}else if(RelationType.ALTERNATIVE.equals(type)) {
    						sign = '+';
    					}else if(RelationType.PARALLEL.equals(type)) {
    						sign = '|';
    					}else if(RelationType.ARBITRARY.equals(type)) {
    						sign = '#';
    					}
    					String restOfExpression = algebraic;		    
    					
    					do {			
    						String firstExpression = getFirstExpression(restOfExpression, sign);
    						restOfExpression = getRestOfExpression(restOfExpression, sign);
    						firstExpression = removeParenthesis(firstExpression);    
    						newRelation.getActivityRelationGroup().add(convertStringToActivityRelation(firstExpression));
    					}while(!restOfExpression.equals(""));		    
    					
    					//debugg
    					//printRelation(newRelation);
    					//debugg
    					
    					return newRelation;
    			}//end else
    		}//end else
    	}//end if
    	
    	return null;
    }
    /**
     * Returns the relation type of the specified algebraic expression.
     *
     * @param algebraic the algebraic expression
     * @return the relation type
     */
    public static RelationType getType(String algebraic) {
    	int startParantes = 0;
    	int endParantes = 0;	
    	for(int i = 0; i < algebraic.length(); i++) {	    	    
    		if(algebraic.charAt(i) == '(') {
    			startParantes++;
    		}
    		if(algebraic.charAt(i) == ')') {
    			endParantes++;
    		}
    		if(startParantes == endParantes) {
    			if(algebraic.charAt(i) == '-') {
    				return RelationType.SEQUENCE;		    
    			}else if(algebraic.charAt(i) == '+') {
    				return  RelationType.ALTERNATIVE;		    
    			}else if(algebraic.charAt(i) == '|') {
    				return RelationType.PARALLEL;	    
    			}else if(algebraic.charAt(i) == '#') {
    				return RelationType.ARBITRARY;
    			}			 
    		}
    	}       
    	return null;
    }
    /**
     * Removes the outer parenthesis of an algebraic expression.
     *
     * @param str the algebraic expression
     * @return the algebraic expression without the outer parenthesis
     */
    public static String removeParenthesis(String str) {
	if(str.length() > 0) {
	    if(str.charAt(0) == '(') {		
		int parenthesis = 1;
		boolean removeParenthesis = true;
		for(int i = 1; i < str.length()-1; i++) {
		    if(str.charAt(i) == '(') {
			parenthesis++;
		    }else if(str.charAt(i) == ')') {
			parenthesis--;
		    }
		    if(parenthesis == 0) {
			removeParenthesis = false;
			break;
		    }
		}
		if(!removeParenthesis) {
		    return str;		   	       
		}else {		    
		    return str.substring(1, str.length()-1);
		}
	    }else {
		return str;
	    }       	    
	}
	return "";
    }
    /**
     * Returns the operand to the left of the operator in 
     * the algebraic expression.
     *
     * @param func the algebraic expression
     * @param operator the operator
     * @return the operand to the left of the operator
     */
    public static String getFirstExpression(String func, char operator) {
    	int operatorIndex = getOperatorIndex(func, operator);
    	if(operatorIndex > 0) {
    		return func.substring(0, operatorIndex);
    	}else {
    		return func;
    	}
    }
    
    /**
     * Returns the operand to the right of the operator in
     * the algebraic expression.
     *
     * @param func the algebraic expression
     * @param operator the operator 
     * @return the operand to the right of the operator
     */
    public static String getRestOfExpression(String func, char operator) {
    	int operatorIndex = getOperatorIndex(func, operator);
    	if(operatorIndex > -1) {
    		return func.substring(operatorIndex+1, func.length());
    	}else {
    		return "";
    	}
    }
    
    /**
     * Returns the position of the specified operator.
     * 
     * @param func the algebraic expression
     * @param operator the operator
     * @return the position of the operator
     */
    public static int getOperatorIndex(String func, char operator) {	
    	int parenthesis = 0;
    	for(int i = 0; i < func.length(); i++) {
    		if(func.charAt(i) == '(') {
    			parenthesis++;
    		}else if(func.charAt(i) == ')') {
    			parenthesis--;
    		}
    		if((parenthesis == 0) && (func.charAt(i) == operator)) {
    			return i;
    		}	    
    	}
    	return -1;
    }  
    /**
     * Returns the number of activities in the specified object.
     *
     * @param o the object
     * @return the number of activities
     */
    public static int numOfActivities(Object o) {
    	if(o instanceof ROP) {
    		return numOfActivities(((ROP)o).getRelation());
    	}else if(o instanceof Relation) {
    		Iterator actRelGroupIterator = ((Relation)o).getActivityRelationGroup().iterator();
    		
    		int numOfActivities = 0;
    		while(actRelGroupIterator.hasNext()) {
    			numOfActivities += numOfActivities(actRelGroupIterator.next());
    		}
    		return numOfActivities;
    	}else if(o instanceof Activity) {
    		return 1;
    	}else {
    		return -1;
    	}
    }    
    /**
     * Returns the activity in the object at position <code>i</code>.
     *
     * @param o the object
     * @param i the position
     * @return the activity object
     */
    public static Activity getActivityAt(Object o, int i) {	
    	if(o instanceof ROP) {	    
    		return getActivityAt(((ROP)o).getRelation(), i);  	    
    	}else if(o instanceof Relation) {
    		Iterator actRelGroupIterator = ((Relation)o).getActivityRelationGroup().iterator();
    		while(actRelGroupIterator.hasNext()) {
    			Object tmp = actRelGroupIterator.next();
    			if(tmp instanceof Activity) {
    				if(i <= 0) {
    					return (Activity)tmp;
    				}else {
    					i--;
    				}
    			}else if(tmp instanceof Relation) {
    				if(i < numOfActivities(tmp)) {
    					return getActivityAt(tmp, i);
    				}else {
    					i -= numOfActivities(tmp);
    				}
    			}
    		}	    
    		return null;
    	}else if(o instanceof Activity) {
    		return (Activity)o;
    	}else {	   
    		return null;
    	}	
    }
    
    /**
     * Creates and returns a copy of the specified object.
     * 
     * @param o the object
     * @return the copy
     */
    public static Object clone(Object o) {
	//DEBUG
	//System.out.print("Converter.clone(): ");
	//END DEBUG
	if(o instanceof ROP) {	    	    
	    try {
		//ROP newROP = new ROPImpl();
		ROP newROP = objectFactory.createROP();
		try {
		    newROP.setType(((ROP)o).getType());
		}catch(Exception ex) {		    
		    newROP.setType(ROPType.COP);
		}
		try {
		    newROP.setComment(((ROP)o).getComment());
		}catch(Exception ex) {}		
		try {
		    newROP.setRelation((Relation)clone(((ROP)o).getRelation()));
		}catch(Exception ex) {
		    //Relation newRelation = new RelationImpl();
		    try {
			Relation newRelation = objectFactory.createRelation();
			newRelation.setType(RelationType.SEQUENCE);
			newROP.setRelation(newRelation);
		    }catch(Exception ex2) {}
		}
		try {
		    newROP.setMachine(((ROP)o).getMachine());
		}catch(Exception ex) {
		    newROP.setMachine("");
		}
		try {
		    newROP.setId(((ROP)o).getId());
		}catch(Exception ex) {
		    newROP.setId("");
		}
		return newROP;
	    }catch(Exception ex) {
		return null;
	    }
	}else if(o instanceof Relation) {	 	    
	    try {
		//Relation newRelation = new RelationImpl();
		Relation newRelation = objectFactory.createRelation();
		try {
		    newRelation.setType(((Relation)o).getType());
		}catch(Exception ex) {
		    newRelation.setType(RelationType.SEQUENCE);
		}			       
		try {
		    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
		    while(iterator.hasNext()) {
			try {
			    newRelation.getActivityRelationGroup().add(clone(iterator.next()));
			}catch(Exception ex) {}
		    }		    
		}catch(Exception ex) {}
		try {
		    newRelation.setAlgebraic((Algebraic)clone(((Relation)o).getAlgebraic()));
		}catch(Exception ex) {}		    
		return newRelation;
	    }catch(Exception ex) {
		return null;
	    }	    
	}else if(o instanceof Activity) {
	    try {
		//Activity newActivity = new ActivityImpl();
		Activity newActivity = objectFactory.createActivity();
		try {
		    newActivity.setOperation(((Activity)o).getOperation());
		}catch(Exception ex) {
		    newActivity.setOperation("Op");
		}		       		
		try {
		    if(((Activity)o).getPrecondition().getPredecessor().size() > 0) {					       
			//newActivity.setPrecondition(new PreconditionImpl());
			newActivity.setPrecondition(objectFactory.createPrecondition());
			
			Iterator iterator = ((Activity)o).getPrecondition().getPredecessor().iterator();		    
			while(iterator.hasNext()) {
			    try {
				newActivity.getPrecondition().getPredecessor().add((OperationReferenceType)clone(iterator.next()));
			    }catch(Exception ex) {}			    
			}
		    }
		}catch(Exception ex) {}		    
		try {
		    if(((Activity)o).getProperties().getAttribute().size() > 0) {						
			//newActivity.setProperties(new PropertiesImpl());
			newActivity.setProperties(objectFactory.createProperties());
			try {
			    newActivity.getProperties().setUnextended(((Activity)o).getProperties().isUnextended());
			}catch(Exception ex) {}
			Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
			while(iterator.hasNext()) {
			    try {
				newActivity.getProperties().getAttribute().add((Attribute)clone(iterator.next()));
			    }catch(Exception ex) {}
			}
		    }				    
		}catch(Exception ex) {}		
		return newActivity;
	    }catch(Exception ex) {
		return null;
	    }
	}else if(o instanceof OperationReferenceType) {
	    try {
		//OperationReferenceType newPredecessor = new OperationReferenceTypeImpl();
		OperationReferenceType newPredecessor = objectFactory.createOperationReferenceType();
		try {
		    newPredecessor.setOperation(((OperationReferenceType)o).getOperation());
		}catch(Exception ex) {
		    newPredecessor.setOperation("Operation");
		}
		try {
		    newPredecessor.setMachine(((OperationReferenceType)o).getMachine());
		}catch(Exception ex) {
		    newPredecessor.setMachine("Machine");
		}
		return newPredecessor;
	    }catch(Exception ex) {
		return null;
	    }
	}else if(o instanceof Algebraic) {
	    try {
	    	//Algebraic newAlgebraic = new AlgebraicImpl();
	    	Algebraic newAlgebraic = objectFactory.createAlgebraic();
	    	try {
	    		newAlgebraic.setCompressed(((Algebraic)o).isCompressed());
	    	}catch(Exception ex) {		    
	    		newAlgebraic.setCompressed(false);		    
	    	}
	    	
	    	try {
	    		newAlgebraic.setUnextended(((Algebraic)o).isUnextended());
	    	}catch(Exception ex) {}		
	    		return newAlgebraic;
	    	}catch(Exception ex) {
	    		return null;
	    	}	
	}else if(o instanceof Attribute) {
	    try {
		//Attribute newAttribute = new AttributeImpl();
		Attribute newAttribute = objectFactory.createAttribute();
		try {
		    newAttribute.setType(((Attribute)o).getType());
		}catch(Exception ex) {
		    newAttribute.setType("Attribute");
		}
		try {
		    newAttribute.setAttributeValue(((Attribute)o).getAttributeValue());
		}catch(Exception ex) {
		    newAttribute.setAttributeValue("Value");
		}
		try {
		    if(((Attribute)o).getUpperIndicator() != null) {
			//newAttribute.setUpperIndicator(new UpperIndicatorImpl());
			newAttribute.setUpperIndicator(objectFactory.createUpperIndicator());
			newAttribute.getUpperIndicator().setIndicatorValue(((Attribute)o).getUpperIndicator().isIndicatorValue());
		    }
		}catch(Exception ex) {}
		try {
		    if(((Attribute)o).getLowerIndicator() != null) {
			//newAttribute.setLowerIndicator(new LowerIndicatorImpl());
			newAttribute.setLowerIndicator(objectFactory.createLowerIndicator());
			newAttribute.getLowerIndicator().setIndicatorValue(((Attribute)o).getLowerIndicator().isIndicatorValue());
		    }
		}catch(Exception ex) {}
		try {
		    newAttribute.setInvisible(((Attribute)o).isInvisible());
		}catch(Exception ex) {}
			return newAttribute;
	    }catch(Exception ex) {
	    	return null;
	    }
	}	    
	return null;
    }
    /**
     * Returns the unique attribute types of the object.
     * 
     * @param o the object
     * @return the array including all the unique attribute types
     */
    public static String[] getUniqueAttributes(Object o) {
	//DEBUG
	//System.out.println("Converter.getUniqueAttributes()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    return getUniqueAttributes(o, new String[0]);
	}else if(o instanceof Relation) {
	    return getUniqueAttributes(o, new String[0]);
	}else if(o instanceof Activity) {
	    return getUniqueAttributes(o, new String[0]);
	}
	return new String[0];
    }
    /**
     * Adds the new unique attribute types in the object to 
     * the specified array.
     *
     * @param o the object
     * @param attributes the already found unique attribute types
     * @return the array including all the unique attribute types
     */
    private static String[] getUniqueAttributes(Object o, String[] attributes) {
	//DEBUG
	//System.out.print("Converter.getUniqueAttributes(): ");
	//END DEBUG
	if(o instanceof ROP) {	    	    
	    try {
		return getUniqueAttributes(((ROP)o).getRelation(), attributes);
	    }catch(Exception ex) {
		return new String[0];
	    }
	}else if(o instanceof Relation) {	    
	    try {
		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
		String[] uniqueAttributes = new String[0];
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    try {			       
			String[] uniqueAttribute = getUniqueAttributes(next, attributes);
			attributes = mergeStringArray(attributes, uniqueAttribute);						      
			uniqueAttributes = mergeStringArray(uniqueAttributes, uniqueAttribute);
		    }catch(Exception ex) {}
		}		
		return uniqueAttributes;
	    }catch(Exception ex) {
		return new String[0];
	    }
	}else if(o instanceof Activity) {	 	    
	    try {
		Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
		String[] uniqueAttributes = new String[0];			       
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    try{		       		
			String[] uniqueAttribute = getUniqueAttributes(next, attributes);
			attributes = mergeStringArray(attributes,
						      uniqueAttribute);
			uniqueAttributes = mergeStringArray(uniqueAttributes, uniqueAttribute);
		    
		    }catch(Exception ex) {}
		}			
		return uniqueAttributes;
	    }catch(Exception ex) {
		return new String[0];
	    }
	}else if(o instanceof Attribute) {		    
	    String[] uniqueAttribute = new String[1];
	    try{
		for(int i = 0; i < attributes.length; i++) {
		    if(((Attribute)o).getType().equals(attributes[i])) {
			return new String[0];
		    }		    		    
		}
		uniqueAttribute[0] = ((Attribute)o).getType(); 	   
		return uniqueAttribute;
	    }catch(Exception ex) {
		return new String[0];
	    }
	}
	return new String[0];		
    }
    /**
     * Merge two arrays together.
     *
     * @param m one array
     * @param n another array
     * @return the merged array
     */
    private static String[] mergeStringArray(String[] m, String[] n) {
	String[] str = new String[m.length+n.length];
	for(int i = 0; i < m.length; i++) {
	    str[i] = m[i];
	}
	for(int i = 0; i < n.length; i++) {
	    str[m.length+i] = n[i];
	}
	return str;
    }
    /**
     * Returns information whether the attributes of specified attribute type
     * is visible in the specified object.
     *
     * @param  o the object
     * @param type the attribute type
     * return the array with the information whether the attributes are 
     * visible.
     * The informatin is structured as follow:
     * <ul>
     * <li>IS_VISIBLE_ERROR</li>
     * <li>IS_VISIBLE_TRUE</li>
     * <li>IS_VISIBLE_FALSE</li>
     * <li>IS_VISIBLE_TRUE_CHANGED</li>
     * <li>IS_VISIBLE_FALSE_CHANGED</li>
     * </ul>
     *  
     */
    public static int isAttributeTypeVisible(Object o, String type) {
	int[] visibleCount = new int[2];
	visibleCount[0] = 0;
	visibleCount[1] = 0;	
	visibleCount = attributeVisibleCount(o, type, visibleCount);	
	int returnStatement = IS_VISIBLE_ERROR;	
	if(visibleCount[0] > 0 && visibleCount[1] == 0) {
	    returnStatement = IS_VISIBLE_TRUE;
	}else if(visibleCount[0] == 0 && visibleCount[1] > 0) {
	    returnStatement = IS_VISIBLE_FALSE;
	}else if(visibleCount[0] >= visibleCount[1]) {
	    returnStatement = IS_VISIBLE_TRUE_CHANGED;
	}else if(visibleCount[0] < visibleCount[1]) {
	    returnStatement = IS_VISIBLE_FALSE_CHANGED;
	}	
	return returnStatement;
    }
    /**
     * Count and returns the number of attribute of specified attribut type
     * that is visible or not.          
     */
    private static int[] attributeVisibleCount(Object o, String type, int[] visibleCount) {
	try {
	    if(o instanceof ROP) {
		return attributeVisibleCount(((ROP)o).getRelation(), type, visibleCount);
	    }else if(o instanceof Relation) {
		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();	    
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    visibleCount = attributeVisibleCount(next, type, visibleCount);
		}		
	    }else if(o instanceof Activity) {
		Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    visibleCount = attributeVisibleCount(next, type, visibleCount);
		}		
	    }else if(o instanceof Attribute) {
		if(((Attribute)o).getType().equals(type)) {
		    if(!((Attribute)o).isInvisible()) {
			visibleCount[0]++;
		    }else {
			visibleCount[1]++;
		    }		
		}	       
	    }
	}catch(Exception ex) {	    
	}	
	return visibleCount;
    }
    /**
     * Sets all the activitie names in <code>objectToSet</code> unique 
     * comparing to the <code>objectToCheck</code>.
     *
     * @param objectToCheck the object to compare to
     * @param objectToSet the object to give unique activity names
     * @return the resulting object
     */
    public static Object setUniqueNames(Object objectToCheck, 
					Object objectToSet) {
	//DEBUG
	//System.out.print("Converter.setUniqueNames(): ");
	//END DEBUG
	try {
	    if(objectToSet instanceof ROP) {
		try {		    
		    ((ROP)objectToSet).setRelation((Relation)setUniqueNames(objectToCheck, ((ROP)objectToSet).getRelation()));	    
		}catch(Exception ex) {		    
		}
	    }else if(objectToSet instanceof Relation) {
		try{		    
		    //Relation objectToReturn = new RelationImpl();
		    Relation objectToReturn = objectFactory.createRelation();
		    objectToReturn.setType(((Relation)objectToSet).getType());
		    Iterator iterator = ((Relation)objectToSet).getActivityRelationGroup().iterator();
		    while(iterator.hasNext()) {
			objectToReturn.getActivityRelationGroup().add(setUniqueNames(objectToCheck, iterator.next()));    
		    }
		    return objectToReturn;
		}catch (Exception ex) {		    
		}
	    }else if(objectToSet instanceof Activity) {
		try{		    
		    String name = ((Activity)objectToSet).getOperation();
		    String tryThisName = name;
		    boolean nameUnique = false;
		    int numOfLoops = 0;
		    while(!isNameUnique(objectToCheck, tryThisName)) {
			tryThisName = name+(++numOfLoops);		
		    }			   		   
		    ((Activity)objectToSet).setOperation(tryThisName);  
		    return objectToSet;		    
		}catch(Exception ex) {		    
		}
	    }
	}catch(Exception o) {	    
	}	
	return objectToSet;
    }
    /**
     * Returns whether the name already exist as an activity name in the 
     * specified object.
     *
     * @param o the object
     * @param name the name to chech whether it is unique or not
     * @return <code>true</code> the name doesn't exist as an activity name,
     * otherwise <code>false</code>.
     */
    public static boolean isNameUnique(Object o, String name) {	
	boolean nameUnique = true;
	if(o instanceof ROP) {
	    try {		
		nameUnique = isNameUnique(((ROP)o).getRelation(), name);
	    }catch(Exception ex) {		
	    }
	}else if(o instanceof Relation) {	    
	    try {		
		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
		while(iterator.hasNext() && nameUnique) {
		    nameUnique = isNameUnique(iterator.next(), name);
		}	    
	    }catch(Exception ex) {		
	    }
	}else if(o instanceof Activity) {
	    try {		
		if(((Activity)o).getOperation().equals(name)) {
		    nameUnique = false;
		}
	    }catch(Exception ex) {		
	    }
	}       
	return nameUnique;
    }    
    /**
     * Returns whether the object is extended or not
     * 
     * @param o the object
     * @return <code>true</code> the object is extended, otherwise 
     * <code>false</code>
     */
    public static boolean isExtended(Object o) {
	int[] extendedCount = new int[2];
	extendedCount[0] = 0;
	extendedCount[1] = 0;	
	extendedCount = extendedCount(o, extendedCount);		
	if(extendedCount[0] > extendedCount[1]) {
	    return true;
	}else {
	    return false;
	}
    }
     /**
     * Count and returns the number of object that is extended or not.
     */
    private static int[] extendedCount(Object o, int[] extendedCount) {
	try {
	    if(o instanceof ROP) {
		return extendedCount(((ROP)o).getRelation(), extendedCount);
	    }else if(o instanceof Relation) {
		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();	    
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    extendedCount = extendedCount(next, extendedCount);
		}		
	    }else if(o instanceof Activity) {
		try {
		    if(((Properties)((Activity)o).getProperties()).isUnextended()) {
			extendedCount[1]++;
		    }else {
			extendedCount[0]++;
		    }
		}catch(Exception ex) {
		    extendedCount[1]++;
		}
	    }
	}catch(Exception ex) {	    
	}	
	return extendedCount;
    }
    /**
     * Returns the predecessors of the specified object.
     */
    public static String[] getPredecessors(Object o) {
	String[] returnStatement = new String[0];
	if(o instanceof ROP) {
	    try {
		return getPredecessors(((ROP)o).getRelation());
	    }catch(Exception ex) {}
	}else if(o instanceof Relation) {
	    try {
		if(((Relation)o).getActivityRelationGroup().size() > 0) {	    		    		    
		    if(((Relation)o).getType().equals("Sequence")) {						
			Object next = ((Relation)o).getActivityRelationGroup().get(0);
			String[] subPred = getPredecessors(next);	      
			returnStatement = subPred;
		    }else if(((Relation)o).getType().equals("Alternative")) {
			Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
			int firstPred = 0;
			while(iterator.hasNext()) {
			    Object next = iterator.next();		       
			    try {		
				String[] subPred = getPredecessors(next);
				returnStatement = mergeStringArray(returnStatement, subPred);			       
			    }catch(Exception ex) {}
			}
		    }else if(((Relation)o).getType().equals("Parallel")) {    	
			Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
			int firstPred = 0;
			while(iterator.hasNext()) {
			    Object next = iterator.next();
			    try {
				String[] subPred = getPredecessors(next);
				returnStatement = mergeStringArray(returnStatement, subPred);				
			    }catch(Exception ex) {}
			}					    	    
		    }else if(((Relation)o).getType().equals("Arbitrary")) {
			Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
			int firstPred = 0;
			while(iterator.hasNext()) {
			    Object next = iterator.next();		  
			    try {		
				String[] subPred = getPredecessors(next);
				returnStatement = mergeStringArray(returnStatement, subPred);				
			    }catch(Exception ex) {}
			}
		    }
		}
	    }catch(Exception ex) {}
	}else if(o instanceof Activity) {
	    try {		
		Iterator iterator = ((Activity)o).getPrecondition().getPredecessor().iterator();
		returnStatement = new String[1];
		returnStatement[0] = ((Activity)o).getOperation()+":";
		while(iterator.hasNext()) {		    
		    try {			
			returnStatement = mergeStringArray(returnStatement, getPredecessors(iterator.next()));
		    }catch(Exception ex) {}
		}
	    }catch(Exception ex) {}			    
	}else if(o instanceof OperationReferenceType) {
	    try {
		returnStatement = new String[1];
		returnStatement[0] = ((OperationReferenceType)o).getOperation()+"@"+
		    ((OperationReferenceType)o).getMachine();		
	    }catch(Exception ex) {
		returnStatement = new String[0];
	    }	   
	}	
	return returnStatement;
    }
    /**
     * Removes the comment xml-element from the specified object.
     * 
     * @param o the object
     */
    public static void removeComment(Object o) {
    	//DEBUG
    	//System.out.println("Converter.removeComment()");
    	//END DEBUG	
    	if(o instanceof ROP) {
    		((ROP)o).setComment(null);
    	}
    }
    /**
     * Removes the algebraic xml-element from the specified object.
     *
     * @param o the object
     */
    public static void removeAlgebraic(Object o) {
    	//DEBUG
    	//System.out.print("Converter.removeAlgebraic(): ");
    	//END DEBUG
    	if(o instanceof ROP) {	    
    		removeAlgebraic(((ROP)o).getRelation());
    	}else if(o instanceof Relation) {	    
    		((Relation)o).setAlgebraic(null);
    		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
    		while(iterator.hasNext()) {
    			removeAlgebraic(iterator.next());
    		}	    	   
    	}	
    } 
    /**
     * Removes the precondition xml-element from the specified object.
     *
     * @param o the object
     */
    public static void removePrecondition(Object o) {	
    	//DEBUG
    	//System.out.println("Converter.removePrecondition()");
    	//END DEBUG
    	if(o instanceof ROP) {	    
    		removePrecondition(((ROP)o).getRelation());
    	}else if(o instanceof Relation) {	   	    
    		Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
    		while(iterator.hasNext()) {
    			removePrecondition(iterator.next());
    		}	    	   
    	}else if(o instanceof Activity) {
    		try {
    			((Activity)o).setPrecondition(null);
    		}catch(Exception ex) {}
    	}
    } 
    
    /**
     * Removes the propterties xml-element from the specified object.
     * 
     * @param o the object
     */
    public static void removeProperties(Object o) {	
	//DEBUG
	//System.out.println("Converter.removeProperties()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    removeProperties(((ROP)o).getRelation());
	}else if(o instanceof Relation) {	   	    
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removeProperties(iterator.next());
	    }	    	   
	}else if(o instanceof Activity) {
	    try {
		((Activity)o).setProperties(null);
	    }catch(Exception ex) {}
	}
    } 
    /**
     * Removes the predecessor xml-element from the specified object.
     *
     * @param o the object
     */
    public static void removePredecessor(Object o) {	
	//DEBUG
	//System.out.println("Converter.removePredecessor()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    removePredecessor(((ROP)o).getRelation());
	}else if(o instanceof Relation) {	   	    
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removePredecessor(iterator.next());
	    }	    	   
	}else if(o instanceof Activity) {
	    try {
		((Activity)o).getPrecondition().getPredecessor().clear();
	    }catch(Exception ex) {}
	}
    }   
    /**
     * Removes the algebraic unextended xml-element from the specified object.
     *
     * @param o the object
     */
    public static void removeAlgebraicUnextended(Object o) {
	//DEBUG
	//System.out.println("removeAlgebraicUnextended()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    removeAlgebraicUnextended(((ROP)o).getRelation());	    
	}else if(o instanceof Relation) {	   	    
	    try {
		//((RelationType)o).getAlgebraic().setUnextended(null);	    
		//Algebraic newAlgebraic = new AlgebraicImpl();
		Algebraic newAlgebraic = objectFactory.createAlgebraic();
		newAlgebraic.setCompressed(((Relation)o).getAlgebraic().isCompressed());
		((Relation)o).setAlgebraic(newAlgebraic);
	    }catch(Exception ex) {}
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removeAlgebraicUnextended(iterator.next());
	    }	   	
	}
    } 
    /**
     * Removes the activity unextended xml-element from the specified object.
     *
     * @param o the object
     */    
    public static void removeActivityUnextended(Object o) {	
	//DEBUG
	//System.out.println("Converter.removeActivityUnextended()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    removeActivityUnextended(((ROP)o).getRelation());	    
	}else if(o instanceof Relation) {	   	       		    
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removeActivityUnextended(iterator.next());
	    }	   
	}else if(o instanceof Activity) {	    		   
	    try {
		//Properties newProperties = new PropertiesImpl();	
		Properties newProperties = objectFactory.createProperties();
		Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
		while(iterator.hasNext()) {
		    newProperties.getAttribute().add((Attribute)iterator.next());
		}		
		((Activity)o).setProperties(newProperties);
	    }catch(Exception ex) {}	    
	}
    }  
    /**
     * Removes the attribute xml-element from the specified object.
     *
     * @param o the object
     */   
    public static void removeAttribute(Object o) {
	//DEBUG
	//System.out.println("Converter.removeAttribute()");
	//NED DEBUG
	if(o instanceof ROP) {	    
	    removeAttribute(((ROP)o).getRelation());
	}else if(o instanceof Relation) {	   	    
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removeAttribute(iterator.next());
	    }	    	   
	}else if(o instanceof Activity) {
	    try {
		((Activity)o).getProperties().getAttribute().clear();
	    }catch(Exception ex) {}
	}
    }   
    /**
     * Removes the attribute characteristics xml-element from the specified 
     * object.
     * 
     * @param o the object
     * @param rmUpperIndicator if <code>true</code> the upper indicator 
     * xml-element is removed, otherwise <code>false</code>
     * @param rmLowerIndicator if <code>true</code> the lower indicator
     * xml-element is removed, otherwise <code>false</code>
     * @param rmInvisible if <code>true</code> the invisible 
     * xml-element is removed, otherwise <code>false</code>
     */ 
    public static void removeAttributeCharacteristics(Object o,
						      boolean rmUpperIndicator,
						      boolean rmLowerIndicator,
						      boolean rmInvisible) { 
	//DEBUG
	//System.out.println("Converter.removeAttributeCharacteristics()");
	//END DEBUG
	if(o instanceof ROP) {	    
	    removeAttributeCharacteristics(((ROP)o).getRelation(),
					   rmUpperIndicator,
					   rmLowerIndicator,
					   rmInvisible);
	}else if(o instanceof Relation) {	   	    
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		removeAttributeCharacteristics(iterator.next(),
					       rmUpperIndicator,
					       rmLowerIndicator,
					       rmInvisible);
	    }	    	   
	}else if(o instanceof Activity) {
	    try {
		Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
		int index = -1;
		while(iterator.hasNext()) {
		    Object next = iterator.next();
		    index++;
		    Attribute newAttribute = objectFactory.createAttribute();
		    newAttribute.setType(((Attribute)next).getType());
		    newAttribute.setAttributeValue(((Attribute)next).getAttributeValue());
		    if(!rmUpperIndicator) {
			if(((Attribute)next).getUpperIndicator() != null) {
			    newAttribute.setUpperIndicator(objectFactory.createUpperIndicator());
			    newAttribute.getUpperIndicator().setIndicatorValue(((Attribute)next).getUpperIndicator().isIndicatorValue());
			}
		    }
		    if(!rmLowerIndicator) {
			if(((Attribute)next).getLowerIndicator() != null) {
			    newAttribute.setLowerIndicator(objectFactory.createLowerIndicator());
			    newAttribute.getLowerIndicator().setIndicatorValue(((Attribute)next).getLowerIndicator().isIndicatorValue());
			}
		    }
		    if(!rmInvisible) {
			newAttribute.setInvisible(((Attribute)next).isInvisible());
		    }
		    ((Activity)o).getProperties().getAttribute().set(index, newAttribute);
		}
	    }catch(Exception ex) {}       
	}
    }  
    /**
     * Adds all the attribute of the specified attribute type 
     * within the specified object
     * <p>
     * If an attribute value is not convertible to a number the attribute
     * value will be treated as zero.
     *
     * @param o the object
     * @param type the attribute type
     * @return the calculated result
     */
    public static float sumAttribute(Object o, String type) {
	if(o instanceof ROP) {
	    return sumAttribute(((ROP)o).getRelation(), type);
	}else if(o instanceof Relation) {
	    Iterator iterator = ((Relation)o).getActivityRelationGroup().iterator();
	    float sum = 0;
	    if(RelationType.SEQUENCE.equals(((Relation)o).getType()) || RelationType.ARBITRARY.equals(((Relation)o).getType())) {				
	    	while(iterator.hasNext()) {
	    		sum += sumAttribute(iterator.next(), type);
	    	}
	    }else if(RelationType.PARALLEL.equals(((Relation)o).getType()) || RelationType.ALTERNATIVE.equals(((Relation)o).getType())) {
	    	while(iterator.hasNext()) {
	    		sum = Math.max(sum, sumAttribute(iterator.next(), type));
	    	}
	    }
	    return sum;
	}else if(o instanceof Activity) {
	    if(((Activity)o).getProperties() != null) {
	    	Iterator iterator = ((Activity)o).getProperties().getAttribute().iterator();
	    	while(iterator.hasNext()) {
	    		Object next = iterator.next();
	    		if(next instanceof Attribute) {
	    			if(((Attribute)next).getType().toLowerCase().equals(type.toLowerCase())) {
	    				try {
	    					return Float.parseFloat(((Attribute)next).getAttributeValue());
	    				}catch(Exception ex) {
	    					return 0;
	    				}
	    			}
	    		}
	    	}
	    }
	}
	return 0;
    }   
}
