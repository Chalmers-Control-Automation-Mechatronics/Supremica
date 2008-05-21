/**
 * class to test EOPTable
 */
package org.supremica.external.processeditor.processgraph.eopcell;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import org.supremica.external.processeditor.processgraph.table.BasicTable;
import org.supremica.manufacturingTables.xsd.eop.*;

import org.supremica.external.processeditor.xml.Loader;

public class TestEOPTable
					extends 
						TestCase
{
	private static EOP eop = null;
	private static BasicTable table = null;
	
	public TestEOPTable(String name){
        super( name );
    }
    
    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp(){
    	
    	Loader loader = new Loader();
    	Object o = loader.openEOP( TestFiles.getFile( TestFiles.EOPExample ) );
    	
    	if(o instanceof EOP){
    		eop = (EOP) o;
    	}
    	
    	table = new BasicTable();
    }
    
    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown(){
    	table = null;
    	eop = null;
    }
    
    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite( TestEOPTable.class );
        return suite;
    }
    
    //Start of test code
    public static void testInsertAndExtractZoneConditionFromActionToTable(){
		
		Action[] actions = null;
    	Action originalAction = null;
    	
    	String str1 = "", str2 = "";
    	
    	ZoneTablePane tablePane = new ZoneTablePane( eop.getZones() );
    	
    	//insert actions
    	tablePane.insertActions( eop.getAction() );
    	table = tablePane.getTable();
    	
    	//extract actions
    	actions = EOPTableExtractor.getActions(null, null, null, table );
    	
    	//test
    	for(int i = 0; i < eop.getAction().size(); i++){
    		
    		originalAction = eop.getAction().get(i);
    		
    		for(int ii = 0; ii < originalAction.getZoneState().size(); ii++){
    			
    			str1 = originalAction.getZoneState().get(ii).getState();
    			str2 = actions[i+1].getZoneState().get(ii).getState();
    			
    			if(!str1.equals( str2 )){
    				assertTrue(false);
        		}
    			
    			str1 = originalAction.getZoneState().get(ii).getZone();
    			str2 = actions[i].getZoneState().get(ii).getZone();
    			
    			if(!str1.equals( str2 )){
        			assertTrue( false );
        		}
    		}
    	}
	}
    
    public static void testInsertAndExtractActuatorValueFromActionToTable(){
    	Action[] actions = null;
    	Action originalAction = null;
    	
    	String str1 = "", str2 = "";
    	
    	InternalTablePane tablePane = new InternalTablePane( eop.getInternalComponents() );
    	
    	//insert actions
    	tablePane.insertActions(eop.getAction());
    	table = tablePane.getTable();
    	
    	//extract actions
    	actions = EOPTableExtractor.getActions(null, table, null, null);
    	
    	//test
    	for(int i = 0; i < eop.getAction().size(); i++){
    		
    		originalAction = eop.getAction().get(i);
    		
    		for(int ii = 0; ii < originalAction.getActuatorValue().size(); ii++){
    			
    			str1 = originalAction.getActuatorValue().get(ii).getActuator();
    			str2 = actions[i+1].getActuatorValue().get(ii).getActuator();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
        		}
    			
    			str1 = originalAction.getActuatorValue().get(ii).getValue();
    			str2 = actions[i+1].getActuatorValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
        			assertTrue(false);
        		}
    		}
    	}
    }
    
    public static void testInsertAndExtractSensorValueFromActionToTable(){
    	Action[] actions = null;
    	Action originalAction = null;
    	
    	String str1 = "", str2 = "";
    	
    	InternalTablePane tablePane = new InternalTablePane( eop.getInternalComponents() );
    	
    	//insert actions
    	tablePane.insertActions( eop.getAction() );
    	table = tablePane.getTable();
    	
    	//extract actions
    	actions = EOPTableExtractor.getActions(null, table, null, null);
    	
    	//test
    	for(int i = 0; i < eop.getAction().size(); i++){
    		
    		originalAction = eop.getAction().get(i);
    		
    		for(int ii = 0; ii < originalAction.getSensorValue().size(); ii++){
    			
    			str1 = originalAction.getSensorValue().get(ii).getSensor();
    			str2 = actions[i+1].getSensorValue().get(ii).getSensor();
    			
    			if(!str1.equals( str2 )){
    				assertTrue(false);
        		}
    			
    			str1 = originalAction.getSensorValue().get(ii).getValue();
    			str2 = actions[i+1].getSensorValue().get(ii).getValue();
    			
    			if(!str1.equals( str2 )){
        			assertTrue(false);
        		}
    		}
    	}
    }
    
    public static void testInsertAndExtractVariableValueFromActionToTable(){
    	Action[] actions = null;
    	Action originalAction = null;
    	
    	String str1 = "", str2 = "";
    	
    	InternalTablePane tablePane = new InternalTablePane(eop.getInternalComponents());
    	
    	//insert actions
    	tablePane.insertActions(eop.getAction());
    	table = tablePane.getTable();
    	
    	//extract actions
    	actions = EOPTableExtractor.getActions(null, table, null, null);
    	
    	//test
    	for(int i = 0; i < eop.getAction().size(); i++){
    		
    		originalAction = eop.getAction().get(i);
    		
    		for(int ii = 0; ii < originalAction.getVariableValue().size(); ii++){
    			
    			str1 = originalAction.getVariableValue().get(ii).getVariable();
    			str2 = actions[i+1].getVariableValue().get(ii).getVariable();
    			
    			if(!str1.equals(str2)){
    				assertTrue(false);
        		}
    			
    			str1 = originalAction.getVariableValue().get(ii).getValue();
    			str2 = actions[i+1].getVariableValue().get(ii).getValue();
    			
    			if(!str1.equals(str2)){
        			assertTrue(false);
        		}
    		}
    	}
    }
    
    public static void testInsertAndExtractExternalConditionFromActionToTable(){
    	ExternalComponentValue[] extCompVal = null;
    	
    	String str1 = "", str2 = "";
    	
    	ExternalTablePane tablePane = new ExternalTablePane(eop.getExternalComponents());
    	
    	tablePane.insertActions(eop.getAction());
    	tablePane.fillExternalComponentsInitialValue(eop.getInitialState().getExternalComponentValue());
    	table = tablePane.getTable();
    	
    	extCompVal = EOPTableExtractor.getExternalComponentsInitialValueFromTable(table);
    	
    	int i = 0;
    	for(ExternalComponentValue e : eop.getInitialState().getExternalComponentValue()){
    		
    		str1 = e.getExternalComponent().getComponent();
    		str2 = extCompVal[i].getExternalComponent().getComponent();
    		
    		if(!str1.equals(str2)){
    			assertTrue(false);
    		}
    		
    		str1 = e.getExternalComponent().getMachine();
    		str2 = extCompVal[i].getExternalComponent().getMachine();
    		
    		if(!str1.equals(str2)){
    			assertTrue(false);
    		}
    		
    		str1 = e.getValue();
    		str2 = extCompVal[i].getValue();
    		
    		if(!str1.equals(str2)){
    			assertTrue(false);
    		}
    		
    		i = i + 1;
    	}
    }
}

