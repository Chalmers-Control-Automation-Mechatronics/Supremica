/**
 *	class to convert from DOP to EFA 
 */
package org.supremica.external.avocades.dop2efa;


import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;

import org.supremica.external.avocades.common.EFA;
import org.supremica.external.avocades.common.Module;
import org.supremica.external.processeditor.xml.Loader;
import org.supremica.manufacturingTables.xsd.processeditor.*;

/*----------------------------------------------------------------------------
 * 
 * Static strings used by other algorithms
 * 
 *----------------------------------------------------------------------------*/
import static org.supremica.external.avocades.AutomataNames.OPERATION_START_PREFIX;
import static org.supremica.external.avocades.AutomataNames.OPERATION_STOP_PREFIX;
import static org.supremica.external.avocades.AutomataNames.EVENT_MACHINE_SEPARATOR;

import static org.supremica.external.avocades.AutomataNames.DONT_CARE_START_PREFIX;
import static org.supremica.external.avocades.AutomataNames.DONT_CARE_STOP_PREFIX;

import static org.supremica.external.avocades.AutomataNames.MACHINE_INITIAL_STATE_POSTFIX;
import static org.supremica.external.avocades.AutomataNames.MACHINE_END_STATE_POSTFIX;

/**
 * @author David Millares
 *
 */
public class DOPtoEFA
				extends DOPrelation{
	
	private static final String MAIN_AUTOMATA_PREFIX = "main_";
	
	public DOPtoEFA(){}
	
	/**
	 * Build a single <code>ROP</code> to a file.
	 *  
	 * @param rop
	 * @param f
	 * @param block
	 */
	public static void createEFA(ROP rop, File f, boolean block){
		//build module
		Module module = buildModuleFromROP(rop);
		
		if(block){
			blockStopEventWithNoStartEvent(module);
		}
		
		module.writeToFile(f);
	}
	
	/**
	 * Builds one ROP file to a Module
	 * @param rop ROP to be converted
	 * @return Module whit rop as EFAs
	 */
	public static Module buildModuleFromROP(ROP rop){
		
		String machine = rop.getMachine();
		
		if(machine == null || machine.length() == 0){
			machine = "no_machine";
		}
		
		return buildModuleFromROP(rop,new Module(machine, false));
	}
	
	/**
	 * Builds a number of ROP files to a Module
	 * 
	 * @param filePathList, list of paths to ROP files
	 * @param name, module name
	 * @param block, should stop events whit no start events be blocket. 
	 * @return a module containing all ROP files as EFAs
	 */
	public static Module buildModule(List<String> filePathList, String name, boolean block){		
		ROP rop = null;
		
		/*
		 * create the file list to a ROP list
		 */
		List<ROP> ropList = new LinkedList<ROP>();
		for(String filePath : filePathList){
			rop = getROPfromFile(new File(filePath));
			if(rop != null){
				ropList.add(rop);
			}
		}
		
		/*
		 * build ROP to Module 
		 */
		return buildModuleFromROP(ropList, name, block);
	}
	
	/**
	 * Build a waters module from a list of ROP
	 * 
	 * @param ropList list of ROP to be build in to module
	 * @param moduleName name of module
	 * @return a Module whit all ROP
	 */
	public static Module buildModuleFromROP(List<ROP> ropList, String moduleName, boolean block){
		
		Module module = null;
		
		//remove previous stored precondition
		//from earlier builds.
		resetPreconList();
		
		//check input
		if(moduleName == null || moduleName.length()== 0){
			moduleName = "new_module";
		}
		
		if(ropList == null || ropList.size() == 0){
			return new Module(moduleName, false);
		}
		
		
		module = new Module(moduleName, false);
		
		//does this first so they will be easy to find
		//in Supremica then many ROP:s are being converted.
		if(ropList.size() > 1){
			module.initNodeVariables();
			module.initResourceVariables();
		}
		
		//convert all rop in list in to module
		for(ROP rop : ropList){
			module = buildModuleFromROP(rop, module);
		}
		
		
		//-------- Last fixes ------------//
		
		//must be done last because
		//now we know the event names
		addPrecondition(module);
		
		//Block
		if(block){
			blockStopEventWithNoStartEvent(module);
		}
		
		//return a module whit all rops
		return module;
	}
	
	
	/**
	 * Add one ROP to a waters Module
	 * 
	 * @param rop the ROP to add to module
	 * @return a Module whit one ROP added
	 */
	public static Module buildModuleFromROP(ROP rop, Module module){
		
		String comment, machine;
		String startState, endState;
		
		EFA main_efa;
		
		Relation main_sequence;
		Activity start_machine, stop_machine;
		
		/*
		 * check input
		 */
		if(rop == null){
			return module;
		}
		
		if(module == null){
			module = new Module("module",false);
		}
		
		comment = rop.getComment(); 
		if(comment == null || comment.length() == 0){
			comment = "no comment";
		}
		
		machine = rop.getMachine(); 
		if(machine == null || machine.length() == 0){
			machine = "no_machine";
		}
		
		main_sequence = factory.createRelation();
		main_sequence.setType(RelationType.SEQUENCE);
		
		start_machine = factory.createActivity();
		stop_machine = factory.createActivity();
		
		Properties startProp = factory.createProperties();
		Properties stopProp = factory.createProperties();
		
		startProp.getAttribute().add(createOnlyStartAttribute());
		startProp.getAttribute().add(
				createStartPrefixAttribute(DONT_CARE_START_PREFIX));
		
		stopProp.getAttribute().add(createOnlyStopAttribute());
		stopProp.getAttribute().add(
				createStopPrefixAttribute(DONT_CARE_STOP_PREFIX));
		
		//special will be parsed in DOPnative 
		start_machine.setOperation(machine);
		start_machine.setProperties(startProp);
		
		stop_machine.setOperation(machine);
		stop_machine.setProperties(stopProp);
		
		//Relabeling of events, for uniqueness and traceable
		//to machine. 
		rop.setRelation( renameEqualOperationName( rop.getRelation(), machine) );
		
		//build main sequence
		main_sequence.getActivityRelationGroup().add( start_machine );
		main_sequence.getActivityRelationGroup().add( rop.getRelation() );
		main_sequence.getActivityRelationGroup().add( stop_machine );
		
		//------------------------------------//
		// preprocessing of the relation tree //
		//------------------------------------//
		main_sequence = removeEmtyRelations( main_sequence );
		main_sequence = collapseRelationTree( main_sequence );
		
		//create main efa
		main_efa = new EFA(MAIN_AUTOMATA_PREFIX + machine, module);
		module.addAutomaton(main_efa);
		
		startState 	= machine + MACHINE_INITIAL_STATE_POSTFIX;
		endState 	= machine + MACHINE_END_STATE_POSTFIX;
		
		//First state not marked and initial
		main_efa.addState(startState, false, true);
		
		//Last state marked
		main_efa.addState(endState,true,false);
		
		// Build sequence in module
		sequence(main_sequence,startState,endState,main_efa);
		
		//return module
		return module;
	}
	
	

	/**
	 * Open a ROP file and return the ROP
	 * @param ropFile 
	 * @return ROP from ropFile
	 */
	public static ROP getROPfromFile(File ropFile){
		
		Loader loader = null;
		Object o = null;
		
		//Sanity check
		if(null == ropFile || !ropFile.exists()){
			return null;
		}
		
		loader = new Loader();
		o = loader.open(ropFile);
		
		if(o instanceof ROP){
			return (ROP)o;
		}
		
		//debug
		//System.err.println("File " + ropFile + " contains no ROP.");
		//debug
		
		return null;
	}
	
	
	//
	//	Special
	//
	
	/**
	 *	Search through Relation r and return a new relation
	 *	there same relation in relation are collapsed.
	 *
	 *	Arbitrary and Parallel nodes are not collapsed.
	 *
	 *	Return a new relation whit no relation with same type as above.
	 * 
	 */
	public static Relation collapseRelationTree( Relation r ){
		
		Relation tmp;
		List<Object> list;
		int i;
		
		//get element in this relation
		list = r.getActivityRelationGroup();
		
		//loop over all element
		i = 0;
		while( i < list.size() ){
			Object o = list.get(i);
			
			if( o instanceof Relation ){
				tmp = (Relation)o;
				if( r.getType().equals( tmp.getType() ) && !( r.getType() == RelationType.ARBITRARY )){
					//same RelationType and not a arbitrary order node
					
					list.remove(i);
					list.addAll(i,tmp.getActivityRelationGroup());
				}else{
					//go down in relation tree
					
					//recursion
					tmp = collapseRelationTree(tmp);
					
					list.remove(i);
					list.add(i, tmp);
					
					i = i + 1; //next element
				}
			}else if( o instanceof Activity ){
				
				//Activities are OK go next
				i = i + 1; //next element
				
			}else{
				//Unknown object in Relation
				System.err.println("Unknown object in Relation tree: " + o);
				
				i = i + 1; //next element
			}
		}
		
		return r;
	}
	
	/**
	 * Add Attribute to all activities in relation.
	 * 
	 * @param relation
	 * @param att
	 * @return
	 */
	public static Relation addAttributeToActivities( Relation relation, Attribute att ){
		List<Object> objList = relation.getActivityRelationGroup();
		
		for(Object o : objList){
			if(o instanceof Activity){
				
				//make sure attribute list exist
				if(((Activity)o).getProperties() == null){
					((Activity)o).setProperties((new ObjectFactory()).createProperties());
				}
				
				//add attribute
				((Activity)o).getProperties().getAttribute().add(att);
				
			}else if(o instanceof Relation){
				addAttributeToActivities((Relation)o, att);
			}
		}
		return relation;
	}
	
	/**
	 *  
	 * @param m
	 */
	public static void blockStopEventWithNoStartEvent(Module m){
		
		List<String> block = new LinkedList<String>();
		List<String> events = m.getEvents();
		String tmp = "";
		
		for(String event : events){
			
			//search for stop events
			if(event.startsWith(OPERATION_STOP_PREFIX)){
				
				//tmp is corresponding start event
				tmp = event.replace(OPERATION_STOP_PREFIX,
									OPERATION_START_PREFIX);
				
				
				//if we not have a start event block
				//end event
				if(!events.contains(tmp)){
					block.add(event);
				}
			}
		}
		
		//block events in module 
		m.blockEvents(block);
	}
	
	/**
	 * Remove relation nodes who are empty.
	 * 
	 * @param relation
	 * @return
	 */
	private static Relation removeEmtyRelations(Relation relation){
		
		List<Object> objList;
		Object o;
		
		if(relation == null){
			return relation;
		}
		
		//get element in this relation
		objList = relation.getActivityRelationGroup();
		
		//check list
		if(objList == null || objList.isEmpty()){
			return null;
		}
		
		int i = 0;
		while(i < objList.size()){
			o = objList.get(i);
			
			if(o instanceof Relation){
				//if empty relation list remove
				if( ((Relation)o).getType() == null ||
					((Relation)o).getActivityRelationGroup() == null ||
					((Relation)o).getActivityRelationGroup().isEmpty()){
					objList.remove(i);
				}else{
					
					//recursion
					o = removeEmtyRelations((Relation)o);
					
					objList.remove(i);
					objList.add(i,o);
					
					//Next
					i = i + 1;
				}
			}else if(o instanceof Activity){
				
				//Next
				i = i + 1;
			}else{
				System.err.println("Unknown object " + o.toString());
			}
		}
		return relation;
	}
	
	
	/**
	 * Search through the relation tree and renames all
	 * activities whit the same operation name.
	 *  
	 * @param r
	 * @return
	 */
	private static Relation renameEqualOperationName(Relation r, String machineName){
		
		NumOfOperations op;
		Relation tmp;
		
		op = (new DOPtoEFA()).new NumOfOperations();
		tmp = renameEqualOperationName(r,op,machineName);
		
		return tmp;
	}
	
	private static Relation renameEqualOperationName(Relation r,
													 NumOfOperations operations, 
													 String machineName){
		Object o = null;
		List<Object> objList = null;
		
		String opName = "";
		
		if(r == null){
			return r;
		}
		
		//get element in this relation
		objList = r.getActivityRelationGroup();
		
		//check list
		if(objList == null || objList.isEmpty()){
			return r;
		}
		
		for(int i=0; i < objList.size(); i++){
			o = objList.get(i);
			
			if(o instanceof Relation){
				renameEqualOperationName((Relation)o,operations,machineName);
			}else if(o instanceof Activity){
				
				opName = ((Activity)o).getOperation();
				if(operations.exist(opName)){
					((Activity)o).setOperation(opName + 
							"_" + operations.getNumberOfOperationsWithName(opName));
				}
				//add opName
				operations.addOperation(opName);
				
				//add machine name to operation
				if(machineName != null && machineName.length() > 0){
					opName = ((Activity)o).getOperation();
					((Activity)o).setOperation(opName +
							                   EVENT_MACHINE_SEPARATOR +
							                   machineName);
				}
				
					
			}else{
				System.err.println("Unknown object: " + o.toString());
			}
		}
		return r;
	}
	
	/**
	 * Internal class to count operation names
	 * @author David Millares
	 */
	private class NumOfOperations{
		
		Hashtable<String,Integer> operations = null;
		
		public NumOfOperations(){
			operations = new Hashtable<String,Integer>();
		}
		
		public boolean exist(String name){
			return operations.containsKey(name);
		}
		
		public int getNumberOfOperationsWithName(String name){
			return operations.get(name);
		}
		
		public void addOperation(String name){
			if(exist(name)){
				operations.put(name, new Integer(operations.get(name)+1));
			}else{
				operations.put(name, new Integer(1));
			}
		}
		
		public String toString(){
			return operations.toString();
		}
	}	
}
