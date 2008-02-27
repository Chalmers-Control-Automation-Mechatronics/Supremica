/**
 * class to test ILTableFiller
 */
package org.supremica.external.processeditor.processgraph.ilcell;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import java.util.List;
import java.util.LinkedList;
import java.io.File;

import org.supremica.external.processeditor.processgraph.ilcell.*;
import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.il.*;

import org.supremica.external.processeditor.xml.Loader;

public class TestILTable
					extends 
						TestCase
{
	private static IL il = null;
	private static BasicTable table = null;
	
	public TestILTable(String name){
        super(name);
    }
    
    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp(){
    	
    	Loader loader = new Loader();
    	Object o = loader.openIL(TestFiles.getFile(TestFiles.ILExample));
    	
    	if(o instanceof IL){
    		il = (IL) o;
    	}
    	
    	table = new BasicTable();
    }
    
    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown(){
    	table = null;
    	il = null;
    }
    
    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(TestILTable.class);
        return suite;
    }
    
    //Start of test code
    public static void testInsertAndExtractZoneModeConditionFromTermToTable(){
		
		Term[] terms = null;
    	Term originalTerm = null;
    	
    	String str1 = "", str2 = "";
    	
    	ModeTablePane tablePane = new ModeTablePane();
    	
    	tablePane.insertTerms(il.getILStructure().getTerm());
    	table = tablePane.getTable();
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(table, null, null, null, null, null);
    	
    	//test row and row
    	for(int i = 0; i < il.getILStructure().getTerm().size(); i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		str1 = originalTerm.getMode();
    		str2 = terms[i].getMode();
    		if(!str1.equals(str2)){
    			assertTrue(false);
    		}
    	}
	}
    
    public static void testInsertAndExtractInternalConditionActuatorValueFromTermToTable(){
    	Term[] terms = null;
    	Term originalTerm = null;
   
    	String str1 = "", str2 = "";
    	
    	//fill table with all terms
    	for(Term term : il.getILStructure().getTerm()){
    		ILTableFiller.insertInternalConditionFromTermToTable(term, table);
    	}
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, table, null, null, null, null);
    	
    	//test row and row
    	for(int i = 0; i < terms.length; i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		//test actuator value
    		for(int ii = 0; ii < terms[i].getActuatorValue().size(); ii++){
    			
    			//same actuator
    			str1 = terms[i].getActuatorValue().get(ii).getActuator();
    			str2 = originalTerm.getActuatorValue().get(ii).getActuator();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    			
    			//same value
    			str1 = terms[i].getActuatorValue().get(ii).getValue();
    			str2 = originalTerm.getActuatorValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    		}
    	}
	}
    
    public static void testInsertAndExtractInternalConditionSensorValueFromTermToTable(){
    	Term[] terms = null;
    	Term originalTerm = null;
   
    	String str1 = "", str2 = "";
    	
    	//fill table with all terms
    	for(Term term : il.getILStructure().getTerm()){
    		ILTableFiller.insertInternalConditionFromTermToTable(term, table);
    	}
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, table, null, null, null, null);
    	
    	//test row and row
    	for(int i = 0; i < terms.length; i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		//test actuator value
    		for(int ii = 0; ii < terms[i].getSensorValue().size(); ii++){
    			
    			//same actuator
    			str1 = terms[i].getSensorValue().get(ii).getSensor();
    			str2 = originalTerm.getSensorValue().get(ii).getSensor();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    			
    			//same value
    			str1 = terms[i].getSensorValue().get(ii).getValue();
    			str2 = originalTerm.getSensorValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    		}
    	}
	}
	
    public static void testInsertAndExtractInternalConditionVariableValueFromTermToTable(){
    	Term[] terms = null;
    	Term originalTerm = null;
   
    	String str1 = "", str2 = "";
    	
    	//fill table with all terms
    	for(Term term : il.getILStructure().getTerm()){
    		ILTableFiller.insertInternalConditionFromTermToTable(term, table);
    	}
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, table, null, null, null, null);
    	
    	//test row and row
    	for(int i = 0; i < terms.length; i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		//test actuator value
    		for(int ii = 0; ii < terms[i].getVariableValue().size(); ii++){
    			
    			//same actuator
    			str1 = terms[i].getVariableValue().get(ii).getVariable();
    			str2 = originalTerm.getVariableValue().get(ii).getVariable();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    			
    			//same value
    			str1 = terms[i].getVariableValue().get(ii).getValue();
    			str2 = originalTerm.getVariableValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    		}
    	}
	}
    
    
	public static void testInsertAndExtractExternalConditionFromTermToTable(){
		
		Term[] terms = null;
    	Term originalTerm = null;
    	ExternalTablePane extTablePane = new ExternalTablePane(il.getILStructure().getExternalComponents());
    	
    	String str1 = "", str2 = "";
    	
    	extTablePane.insertTerms(il.getILStructure().getTerm());
    	table = extTablePane.getTable();
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, null, table, null, null, null);
    	
    	//test row and row
    	for(int i = 0; i < il.getILStructure().getTerm().size(); i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		//test actuator value
    		for(int ii = 0; ii < terms[i].getExternalComponentValue().size(); ii++){
    			
    			//same component
    			str1 = terms[i].getExternalComponentValue().get(ii).getExternalComponent().getComponent();
    			str2 = originalTerm.getExternalComponentValue().get(ii).getExternalComponent().getComponent();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    			
    			//same machine
    			str1 = terms[i].getExternalComponentValue().get(ii).getExternalComponent().getMachine();
    			str2 = originalTerm.getExternalComponentValue().get(ii).getExternalComponent().getMachine();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    			
    			//same value
    			str1 = terms[i].getExternalComponentValue().get(ii).getValue();
    			str2 = originalTerm.getExternalComponentValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
    			}
    		}
    	}
	}
	
	public static void testInsertAndExtractOperationConditionFromTermToTable(){
		
		Term[] terms = null;
    	Term originalTerm = null;
    	
    	OperationTablePane tablePane = new OperationTablePane();
    	
    	NotOngoing notOngoingOrig = null;
    	NotOngoing notOngoing = null;
    	
    	NotStarted notStartedOrig = null;
    	NotStarted notStarted = null;
    	
    	tablePane.insertTerms(il.getILStructure().getTerm());
    	table = tablePane.getTable();
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, null, null, table, null, null);
    	
    	//test row and row
    	for(int i = 0; i < il.getILStructure().getTerm().size(); i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		
    		for(int ii = 0; ii < terms[i].getOperationCheck().size(); ii++){
    			
    			notOngoingOrig = originalTerm.getOperationCheck().get(ii).getNotOngoing();
    			notOngoing = terms[i].getOperationCheck().get(ii).getNotOngoing();
    			
    			for(String op : notOngoingOrig.getOperation()){
    				if(!notOngoing.getOperation().contains(op)){
    					assertTrue(false);
    				}
    			}
    			
    			notStartedOrig = originalTerm.getOperationCheck().get(ii).getNotStarted();
    			notStarted = terms[i].getOperationCheck().get(ii).getNotStarted();
    			
    			for(String op : notStartedOrig.getOperation()){
    				if(!notStarted.getOperation().contains(op)){
    					assertTrue(false);
    				}
    			}
    		}
    	}
	}
	
	public static void testInsertAndExtractZoneConditionFromTermToTable(){
		
		Term[] terms = null;
    	Term originalTerm = null;
    	
    	ZoneTablePane tablePane = new ZoneTablePane();
    	
    	ZoneCheck zoneCheckOrig = null;
    	ZoneCheck zoneCheck = null;
    	
    	tablePane.insertTerms(il.getILStructure().getTerm());
    	table = tablePane.getTable();
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, null, null, null, table, null);
    	
    	//test row and row
    	for(int i = 0; i < il.getILStructure().getTerm().size(); i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		
    		for(int ii = 0; ii < originalTerm.getZoneCheck().size(); ii++){
    			
    			zoneCheckOrig = originalTerm.getZoneCheck().get(ii);
    			zoneCheck = terms[i].getZoneCheck().get(ii);
    			
    			for(String op : zoneCheckOrig.getAfterZones().getZone()){
    				if(!zoneCheck.getAfterZones().getZone().contains(op)){
    					assertTrue(false);
    				}
    			}
    			
    			for(String op : zoneCheckOrig.getBeforeZones().getZone()){
    				if(!zoneCheck.getBeforeZones().getZone().contains(op)){
    					assertTrue(false);
    				}
    			}
    		}
    	}
	}
	
	
	public static void testInsertAndExtractProductConditionFromTermToTable(){
		
		Term[] terms = null;
    	Term originalTerm = null;
    	
    	ProductTablePane tablePane = new ProductTablePane();
    	
    	tablePane.insertTerms(il.getILStructure().getTerm());
    	table = tablePane.getTable();
    	
    	//extract terms
    	terms = ILTableExtractor.getTerms(null, null, null, null, null, table);
    	
    	//test row and row
    	for(int i = 0; i < il.getILStructure().getTerm().size(); i++){
    		
    		originalTerm = il.getILStructure().getTerm().get(i);
    		for(int ii = 0; ii < originalTerm.getProducts().size(); ii++){
    			for(String p : originalTerm.getProducts().get(ii).getProduct()){
    				if(!terms[i].getProducts().get(ii).getProduct().contains(p)){
    					assertTrue(false);
    				}
    			}
    		}
    	}
	}
}

