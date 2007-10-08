/**
 * class to convert from DOP to EFA 
 */
package org.supremica.external.processeditor.xml.dop2efa;


import java.io.File;
import java.util.List;
import java.util.LinkedList;
import java.util.Hashtable;

import org.supremica.external.processeditor.xml.Loader;
import org.supremica.manufacturingTables.xsd.processeditor.*;

/**
 * @author David Millares
 *
 */
public class DOPtoEFA extends DOPrelation{
	
	private static String outputFileName = "testModule.wmod";
	
	public DOPtoEFA(){}
	
	public static void createEFA(ROP rop){
		createEFA(rop,new File(outputFileName));
	}
	
	public static void createEFA(ROP rop, File f){
		
		Module module = buildModuleFromROP(rop);
		
		/* Last fix */
		/* Block */
		blockStopEventWhitNoStartEvent(module);
		
		module.writeToFile(f);
	}
	
	public static Module buildModuleFromROP(ROP rop){
		String machine = rop.getMachine();
		if(machine == null || machine.length() == 0){
			machine = "no_machine";
		}
		return buildModuleFromROP(rop,new Module(machine, false));
	}
	
	public static Module buildModule(List<String> filePathList, String name, boolean block){		
		ROP rop = null;
		
		List<ROP> ropList = new LinkedList<ROP>();
		for(String filePath : filePathList){
			rop = getROPfromFile(new File(filePath));
			if(rop != null){
				ropList.add(rop);
			}
		}
		
		return buildModuleFromROP(ropList, name, block);
	}
	
	/**
	 * Build a waters module from a list of ROP
	 * @param ropList list of ROP to be build in to module
	 * @param moduleName name of module
	 * @return a Module whit all ROP
	 */
	public static Module buildModuleFromROP(List<ROP> ropList, String moduleName, boolean block){
		
		Module module = null;
		
		/*
		 * remove previus stored precondition
		 */
		resetPreconList();
		
		/*
		 * check input
		 */
		if(moduleName == null || moduleName.length()== 0){
			moduleName = "new_module";
		}
		
		if(ropList == null || ropList.size() == 0){
			return new Module(moduleName,false);
		}
		
		/* create module */
		module = new Module(moduleName,false);
		
		/*
		 * does this first so they will be easy to find
		 * in Supremica then many ROP:s are being converted.
		 */
		if(ropList.size() > 1){
			module.initNodeVariables();
			module.initResourceVariables();
		}
		
		/*
		 * convert all rop in list in to module
		 */
		for(ROP rop : ropList){
			module = buildModuleFromROP(rop, module);
		}
		
		
		/* Last fix */
		
		/*
		 * must be done last because
		 * now we know the event names
		 */
		addPrecondition(module);
		
		/* Block */
		if(block){
			blockStopEventWhitNoStartEvent(module);
		}
		
		/*
		 * all rop in one module
		 */
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
		
		EFA main_efa;
		
		ObjectFactory factory;
		
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
		
		factory = new ObjectFactory();
		
		main_sequence = factory.createRelation();
		main_sequence.setType(RelationType.SEQUENCE);
		
		start_machine = factory.createActivity();
		stop_machine = factory.createActivity();
		
		//special will be parsed in class EGA 
		start_machine.setOperation(PGA.ONLY_START + machine);
		stop_machine.setOperation(PGA.ONLY_STOP + machine);
		
		/*
		 * Relabeling of events, for uniqueness and traceable
		 * to machine. 
		 */
		rop.setRelation(renameEqualOperationName(rop.getRelation(),machine));
		
		//build main sequence
		main_sequence.getActivityRelationGroup().add(start_machine);
		main_sequence.getActivityRelationGroup().add(rop.getRelation());
		main_sequence.getActivityRelationGroup().add(stop_machine);
		
		/*
		 * preprocessing of the relation tree
		 */
		
		//remove same relation in same relation
		main_sequence = collapseRelationTree(main_sequence);
		
		//remove empty relations
		main_sequence = removeEmtyRelations(main_sequence);
		
		
		/*
		 * create main efa
		 */
		main_efa = new EFA("main_" + machine,module);
		module.addAutomaton(main_efa);
		
		String startState = machine +  "_idle";
		String endState = machine + "_finish";
		
		/* First state marked and initial */
		main_efa.addState(startState, true, true);
		
		/* Last state marked */
		main_efa.addState(endState,true,false);
		
		/* Build sequence in module */
		sequence(main_sequence,startState,endState,main_efa);
		
		//return module
		return module;
	}
	
	
	
	public static void createEFA(File ropFile, File f){
		
		//check indata
		if(ropFile == null || f == null){
			return;
		}
		
		createEFA(getROPfromFile(ropFile),f);
	}
	
	
	
	public static ROP getROPfromFile(File f){
		
		if(f != null && f.exists()){
			Loader loader = new Loader();
			Object o = loader.open(f);
			
			if(o instanceof ROP){
				return (ROP)o;
			}
		}
		System.err.println("File " + f + " contains no ROP.");
		return null;
	}
	
	
	//
	//	Special
	//
	
	/**
	 *	Search through Relation r and return a new relation
	 *	there same relation in relation are collapsed. 
	 *
	 *	Return a new relation whit no relation with same type as above.
	 * 
	 */
	public static Relation collapseRelationTree(Relation r){
		
		Relation tmp;
		List list;
		int i;
		
		//get element in this relation
		list = r.getActivityRelationGroup();
		
		//loop over all element
		i = 0;
		while(i < list.size()){
			Object o = list.get(i);
			
			if(o instanceof Relation){
				tmp = (Relation)o;
				if(r.getType().equals(tmp.getType())){
					/* same RelationType */
					
					list.remove(i);
					list.addAll(i,tmp.getActivityRelationGroup());
				}else{
					/* go down in relation tree */
					
					//recursion
					tmp = collapseRelationTree(tmp);
					
					list.remove(i);
					list.add(i, tmp);
					
					i = i + 1; //next element
				}
			}else if(o instanceof Activity){
				
				/* Activities are OK go next*/
				i = i + 1; //next element
				
			}else{
				/* Unknown object in Relation */
				System.err.println("Unknown object in Relation tree: " + o);
				
				i = i + 1; //next element
			}
		}
		
		return r;
	}
	
	public static void blockStopEventWhitNoStartEvent(Module m){
		
		List<String> block = new LinkedList<String>();
		List<String> events = m.getEvents();
		String tmp = "";
		
		for(String event : events){
			
			/* search for stop events */
			if(event.startsWith(EVENT_STOP_PREFIX)){
				
				/* tmp is corresponding start event */
				tmp = event.replace(EVENT_STOP_PREFIX,
									EVENT_START_PREFIX);
				
				/* if we not have a start event block
				 * end event
				 */
				if(!events.contains(tmp)){
					block.add(event);
				}
			}
		}
		
		m.blockEvents(block);
		
	}
	
	/**
	 *	Search through Relation r and remove all Activities whit same
	 *	operation name.
	 */
	public static Relation removeDoubleActivity(Relation r){
		
		List objList = null;
		Object o = null;
		Object o2 = null;
		
		String op1="", op2=""; 
		
		//check in data
		if(r == null){
			return r;
		}
		
		//get element in this relation
		objList = r.getActivityRelationGroup();
		
		//check list
		if(objList == null || objList.isEmpty()){
			return r;
		}
		
		//search for doubles
		for(int i=0; i <= objList.size()-1; i++){
			o = objList.get(i);
			
			if(o instanceof Activity){
				for(int ii = i+1; ii < objList.size(); ii++){
					o2 = objList.get(ii);
					if(o2 instanceof Activity){
						
						op1 = ((Activity)o).getOperation();
						op2 = ((Activity)o2).getOperation();
						
						if(op1.equals(op2)){
							//double found remove
							objList.remove(o2);
						}
					}
				}//end for
			}//end if
			
		}//end for
		
		return r;
	}
	
	/**
	 * Remove relation nodes who are empty.
	 * 
	 * @param relation
	 * @return
	 */
	private static Relation removeEmtyRelations(Relation relation){
		
		List objList;
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
				/* if empty relation list remove */
				if(((Relation)o).getActivityRelationGroup().isEmpty()){
					objList.remove(i);
				}else{
					
					/* recursion */
					o = removeEmtyRelations((Relation)o);
					
					objList.remove(i);
					objList.add(i,o);
					
					/* Next */
					i = i + 1;
				}
			}else if(o instanceof Activity){
				
				/* Next */
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
		NumOfOperations op = (new DOPtoEFA()).new NumOfOperations();
		Relation tmp = renameEqualOperationName(r,op,machineName);
		
		//debug
		//System.out.println("Table");
		//System.out.println(op.toString());
		//debug
		
		return tmp;
	}
	
	private static Relation renameEqualOperationName(Relation r,
													 NumOfOperations operations, 
													 String machineName){
		
		Object o = null;
		List objList = null;
		
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
					((Activity)o).setOperation(opName +"::"+machineName);
				}
				
					
			}else{
				System.err.println("Unknown object: " + o.toString());
			}
		}
		return r;
	}
	
	/**
	 * Internal class for 
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
	
	/**
	 * Test main method to test this class
	 * @param args
	 */
	public static void main(String[] args) {
		
		Module m = new Module("Test",true);
		
		EFA efa = new EFA("seq",m);
		EFA efa1 = new EFA("alt",m);
		
		m.addAutomaton(efa);
		m.addAutomaton(efa1);
		
		//build pgaList
		
		PGA pga = new PGA();
		PGA pga1 = new PGA();
		PGA pga2 = new PGA();
		
		pga.setProcess("Op0");
		pga1.setProcess("Op1");
		pga2.setProcess("Op2");
		
		//efa.addIntegerVariable("i", 0, 5, 0, null);
		//efa.addIntegerVariable("j", 1, 5, 2, null);
		
		//pga.setStartGuard("i==1");
		//pga.setStartAction("i+=1");
		
		
		//pga.andStartGuard("j==2;");
		
		List<PGA> pgaList = new LinkedList<PGA>();
		pgaList.add(pga);
		pgaList.add(pga1);
		pgaList.add(pga2);
		
		//build egaList
		List<String> egaList = new LinkedList<String>();
		
		egaList.add("a");
		egaList.add("b");
		egaList.add("c");
		egaList.add("d");
		
		
		//build relation
		ObjectFactory factory  = new ObjectFactory();
		
		Relation seq = factory.createRelation();
		seq.setType(RelationType.SEQUENCE);
		
		Relation alt = factory.createRelation();
		alt.setType(RelationType.ALTERNATIVE);
		
		Relation par = factory.createRelation();
		par.setType(RelationType.PARALLEL);
		
		Activity a = factory.createActivity();
		a.setOperation("a");
		
		Activity b = factory.createActivity();
		b.setOperation("b");
		
		Activity c = factory.createActivity();
		c.setOperation("c");
		
		Activity d = factory.createActivity();
		d.setOperation("d");
		
		Activity e = factory.createActivity();
		e.setOperation("e");
		
		Activity f = factory.createActivity();
		f.setOperation("f");
		
		Activity g = factory.createActivity();
		g.setOperation("g");
		
		Activity h = factory.createActivity();
		h.setOperation("h");
		
		Activity i = factory.createActivity();
		i.setOperation("i");
		
		
		par.getActivityRelationGroup().add(g);
		par.getActivityRelationGroup().add(h);
		par.getActivityRelationGroup().add(i);
		
		seq.getActivityRelationGroup().add(a);
		seq.getActivityRelationGroup().add(b);
		seq.getActivityRelationGroup().add(par);
		seq.getActivityRelationGroup().add(c);
		
		alt.getActivityRelationGroup().add(d);
		alt.getActivityRelationGroup().add(e);
		alt.getActivityRelationGroup().add(f);
		
		
		
		/*
		//Test native
		//efa.addState("s0");
		//efa.addState("s1");

		//nativeProcess(pga,"s0","s1",efa);
		
		
		
		nativeSequence(pgaList,"start","end",efa);
		nativeAlternative(pgaList,"start","end",efa1);
		
		
		//nativeAlternative(egaList,"start","end",efa1);
		
		
		m.writeToFile(new File(outputFileName));
		System.out.println("Done test native");
		*/
		
		
		/*
		//Test Relation
		
		sequence(seq,"start","end",efa);
		alternative(alt, "from","to", efa1);
		
		m.writeToFile(new File(outputFileName));
		System.out.println("Done test Relation");
		*/
		
		//Test ROP
		
		ROP rop = factory.createROP();
		rop.setMachine("machine");
		rop.setComment("Test Arbitrary");
		
		Relation r = new Relation();
		r.setType(RelationType.ARBITRARY);
		
		r.getActivityRelationGroup().add(0, a);
		//r.getActivityRelationGroup().add(1, b);
		//r.getActivityRelationGroup().add(2, c);
		
		rop.setRelation(r);
		
		createEFA(rop);
		System.out.println("Done test ROP");
	}
}
