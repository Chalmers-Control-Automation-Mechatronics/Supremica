/**
 * class to test DOP to EFA 
 */
package org.supremica.external.processeditor.tools.dop2efa;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.testhelpers.*;
import java.util.List;
import java.util.LinkedList;
import java.io.File;

import org.supremica.external.processeditor.tools.dop2efa.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;

public class TestDOPtoEFA extends TestCase{
	
	private ObjectFactory factory = new ObjectFactory();
	private File tmpFile = null;
    
    public TestDOPtoEFA(String name){
        super(name);
    }
    
    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp(){
    	try{
    		tmpFile = File.createTempFile("tmp", "tmp");
    	}catch(Exception e){
    		e.printStackTrace();
    	}	
    }
    
    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown(){
    	tmpFile.delete();
    }
    
    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(TestDOPtoEFA.class);
        return suite;
    }
    
    
    public void testBuildModuleFromFourDifferentNodeTypesROP(){
    	
    	Module module = null;
    	List<ROP> ropList = new LinkedList<ROP>();
    	String moduleName = "test";
    	boolean block = false;
    	
    	//Add ROPs with four different relation types
    	ropList.add(buildSequenceROP());
    	ropList.add(buildAlternativeROP());
    	ropList.add(buildParallelROP());
    	ropList.add(buildArbitraryROP());
    	
    	try{
    		module = DOPtoEFA.buildModuleFromROP(ropList, moduleName, block);
    		module.writeToFile(tmpFile);
    	}catch(Exception e){
    		e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    public void testBuildModuleWithROPsContainingPreconditions(){
    	
    	Module module = null;
    	List<ROP> ropList = new LinkedList<ROP>();
    	String moduleName = "test";
    	boolean block = false;
    	
    	ROP seq = buildSequenceROP();
    	ROP alt = buildAlternativeROP();
    	ROP par = buildParallelROP();
    	ROP arb = buildArbitraryROP();
    	
    	Activity a = factory.createActivity();
    	
    	a.setOperation("PreCon");
    	a.setPrecondition(buildPrecondition());
    	
    	seq.getRelation().getActivityRelationGroup().add(1,a);
    	alt.getRelation().getActivityRelationGroup().add(1,a);
    	par.getRelation().getActivityRelationGroup().add(1,a);
    	arb.getRelation().getActivityRelationGroup().add(1,a);
    	
    	ropList.add(seq);
    	ropList.add(alt);
    	ropList.add(par);
    	ropList.add(arb);
    	
    	try{
    		module = DOPtoEFA.buildModuleFromROP(ropList, moduleName, block);
    		module.writeToFile(tmpFile);
    	}catch(Exception e){
    		e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    public void testBuildModuleWithROPsContainingResources(){
    	
    	Module module = null;
    	List<ROP> ropList = new LinkedList<ROP>();
    	String moduleName = "test";
    	boolean block = false;
    	
    	ROP seq = buildSequenceROP();
    	ROP alt = buildAlternativeROP();
    	ROP par = buildParallelROP();
    	ROP arb = buildArbitraryROP();
    	
    	Activity a = factory.createActivity();
    	
    	Properties prop = factory.createProperties();
    	prop.getAttribute().add(buildResourceAttribute("Zon1", true, true));
    	prop.getAttribute().add(buildResourceAttribute("Zon2", false, true));
    	prop.getAttribute().add(buildResourceAttribute("Zon3", true, false));
    	
    	a.setOperation("ResourceBocking");
    	a.setProperties(prop);
    	
    	seq.getRelation().getActivityRelationGroup().add(1,a);
    	alt.getRelation().getActivityRelationGroup().add(1,a);
    	par.getRelation().getActivityRelationGroup().add(1,a);
    	arb.getRelation().getActivityRelationGroup().add(1,a);
    	
    	ropList.add(seq);
    	ropList.add(alt);
    	ropList.add(par);
    	ropList.add(arb);
    	
    	try{
    		module = DOPtoEFA.buildModuleFromROP(ropList, moduleName, block);
    		module.writeToFile(tmpFile);
    	}catch(Exception e){
    		e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    /* Sequence nodes should be collapsed */
    public void testCollapseRelationTreeSequence(){
    	
    	/* build test relation */
    	Relation relation = buildRelationWithSameType(RelationType.SEQUENCE);
    	
        /* call function */
        relation = DOPtoEFA.collapseRelationTree(relation);
        	
        /* All seq schould be gone */
        assertEquals(0, relation.getActivityRelationGroup().size());
    }
    
    /* Alternative nodes should be collapsed */
    public void testCollapseRelationTreeAlternative(){
    	
    	/* build test relation */
    	Relation relation = buildRelationWithSameType(RelationType.ALTERNATIVE);
    	
        /* call function */
        relation = DOPtoEFA.collapseRelationTree(relation);
        	
        /* All seq schould be gone */
        assertEquals(0, relation.getActivityRelationGroup().size());
    }
    
    /* Parallel nodes should be collapsed */
    public void testCollapseRelationTreeParallel(){
    	Relation relation = buildRelationWithSameType(RelationType.PARALLEL);
    	relation = DOPtoEFA.collapseRelationTree(relation);
    	assertEquals(0, relation.getActivityRelationGroup().size());
    }

    /* Arbitrary order nodes should NOT be collapsed */
    public void testCollapseRelationTreeArbitrary(){
    	
    	int nodes = 0;
    	Relation relation = buildRelationWithSameType(RelationType.ARBITRARY);
    	
    	nodes = relation.getActivityRelationGroup().size();
    	
    	relation = DOPtoEFA.collapseRelationTree(relation);
    	
    	/* No arbitrary should be gone */
    	assertEquals(nodes, relation.getActivityRelationGroup().size());
    }
    
    public void testAddAttributeToActivities(){
    	
    	final int NUMBER_OF_ACTIVITIES = 4*10;
    	List<Object> objList;
    	
    	Attribute att;
    	
    	Relation seq;
    	Relation alt;
    	Relation par;
    	Relation arb;
    	
    	// ------- Build Test ROP ------- //
    	Activity as[] = new Activity[NUMBER_OF_ACTIVITIES];
    	for(int i = 0; i < as.length; i++){
    		as[i] = factory.createActivity();
    	}
    	
    	//
    	att = factory.createAttribute();
    	att.setAttributeValue("test_attribute");
    	att.setType("test_type");
    	//
    	
    	seq = factory.createRelation();
    	seq.setType(RelationType.SEQUENCE);
    	
    	alt = factory.createRelation();
    	alt.setType(RelationType.SEQUENCE);
    	
    	par= factory.createRelation();
    	par.setType(RelationType.SEQUENCE);
    	
    	arb = factory.createRelation();
    	arb.setType(RelationType.SEQUENCE);
    	
    	int i = 0;
    	while(i < as.length){
    		seq.getActivityRelationGroup().add(as[i]);
    		if(i < as.length)i++;
    		
    		alt.getActivityRelationGroup().add(as[i]);
    		if(i < as.length)i++;
    		
    		par.getActivityRelationGroup().add(as[i]);
    		if(i < as.length)i++;
    		
    		arb.getActivityRelationGroup().add(as[i]);
    		if(i < as.length)i++;
    	}
    	
    	seq.getActivityRelationGroup().add(seq.getActivityRelationGroup().size()/2, alt);
    	arb.getActivityRelationGroup().add(arb.getActivityRelationGroup().size()/2, par);
    	
    	seq.getActivityRelationGroup().add(arb);
    	// ------- End Build Test ROP ------- //
    	
    	
    	DOPtoEFA.addAttributeToActivities(seq, att);
    	
    	
    	objList = seq.getActivityRelationGroup();
    	for(Object o : objList){
    		if(o instanceof Activity){
    			if(!((Activity)o).getProperties().getAttribute().contains(att)){
    				assertTrue(false);
    			}
    		}
    	}
    	
    	assertTrue(true);
    }
    
    /**
     *	Help function 
     */
    private Relation buildRelationWithSameType(RelationType type){
    	
    	/* create relation */
    	Relation r1 = factory.createRelation();
    	Relation r2 = factory.createRelation();
    	Relation r3 = factory.createRelation();
    	Relation r4 = factory.createRelation();
    	
    	/* set same type */
    	r1.setType(type);
    	r2.setType(type);
    	r3.setType(type);
    	r4.setType(type);
    	
    	/* same relation in relation */
    	r3.getActivityRelationGroup().add(r4);
    	r2.getActivityRelationGroup().add(r3);
    	r1.getActivityRelationGroup().add(r2);
    	
    	return r1;
    }
    
    /**
     * Builds a ROP with one sequence containing three activities.
     * @return ROP
     */
    private ROP buildSequenceROP(){
    	ROP rop = factory.createROP();
    	Relation r = buildRelationOfType(RelationType.SEQUENCE);
    	
    	rop.setMachine(r.getType().toString());
    	rop.setComment("Test ROP " + r.getType().toString());
    	rop.setId("1");
    	rop.setType(ROPType.ROP);
    	rop.setRelation(r);
    	
    	return rop;
    }
    
    /**
     * Builds a ROP with one alternative containing three activities.
     * @return ROP
     */
    private ROP buildAlternativeROP(){
    	ROP rop = factory.createROP();
    	Relation r = buildRelationOfType(RelationType.ALTERNATIVE);
    	
    	rop.setMachine(r.getType().toString());
    	rop.setComment("Test ROP " + r.getType().toString());
    	rop.setId("2");
    	rop.setType(ROPType.ROP);
    	rop.setRelation(r);
    	
    	return rop;
    }
    
    /**
     * Builds a ROP with one parallel containing three activities.
     * @return ROP
     */
    private ROP buildParallelROP(){
    	ROP rop = factory.createROP();
    	Relation r = buildRelationOfType(RelationType.PARALLEL);
    	
    	rop.setMachine(r.getType().toString());
    	rop.setComment("Test ROP " + r.getType().toString());
    	rop.setId("2");
    	rop.setType(ROPType.ROP);
    	rop.setRelation(r);
    	
    	return rop;
    }
    
    /**
     * Builds a ROP with one arbitrary order containing three activities.
     * @return ROP
     */
    private ROP buildArbitraryROP(){
    	ROP rop = factory.createROP();
    	Relation r = buildRelationOfType(RelationType.ARBITRARY);
    	
    	rop.setMachine(r.getType().toString());
    	rop.setComment("Test ROP " + r.getType().toString());
    	rop.setId("2");
    	rop.setType(ROPType.ROP);
    	rop.setRelation(r);
    	
    	return rop;
    }
    
    
    /**
     * Builds a relation containing three activities.
     * @return relation with three activities of type in data.
     */
    private Relation buildRelationOfType(RelationType type){
    	Relation relation = factory.createRelation();
    	
    	Activity op1 = factory.createActivity();
    	Activity op2 = factory.createActivity();
    	Activity op3 = factory.createActivity();
    	
    	relation.setType(type);
    	relation.getActivityRelationGroup().add(op1);
    	relation.getActivityRelationGroup().add(op2);
    	relation.getActivityRelationGroup().add(op3);
    	
    	op1.setOperation("Op1");
    	op2.setOperation("Op2");
    	op3.setOperation("Op3");
    
    	return relation;
    }
    
    private Precondition buildPrecondition(){
    	
    	Precondition precon = factory.createPrecondition();
    	
    	OperationReferenceType opRefType1 = factory.createOperationReferenceType();
    	OperationReferenceType opRefType2 = factory.createOperationReferenceType();
    	OperationReferenceType opRefType3 = factory.createOperationReferenceType();
    	
    	opRefType1.setMachine("machine1");
    	opRefType1.setOperation("Op1");
    	
    	opRefType2.setMachine("machine2");
    	opRefType2.setOperation("Op2");
    	
    	opRefType3.setMachine("machine3");
    	opRefType3.setOperation("Op3");
    	
    	precon.getPredecessor().add(opRefType1);
    	precon.getPredecessor().add(opRefType2);
    	precon.getPredecessor().add(opRefType3);
    	
    	return precon;
    }
    
    private Attribute buildResourceAttribute(String name, boolean book, boolean unbook){
    	
    	Attribute att = factory.createAttribute();
    	UpperIndicator ui = factory.createUpperIndicator();
    	LowerIndicator li = factory.createLowerIndicator();
    	
    	ui.setIndicatorValue(book);
    	li.setIndicatorValue(unbook);
    	
    	att.setAttributeValue(name);
    	att.setUpperIndicator(ui);
    	att.setLowerIndicator(li);
    	
    	return att;
    }
}

