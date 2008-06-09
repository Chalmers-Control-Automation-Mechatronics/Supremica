package org.supremica.external.avocades.dop2efa;

/**
 * this class holds function to build single relations
 * to EFA.
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.supremica.external.avocades.common.EFA;
import org.supremica.external.avocades.common.EGA;
import org.supremica.external.avocades.common.Module;
import org.supremica.manufacturingTables.xsd.processeditor.*;

public class DOPrelation
					extends DOPnative{
	/**
	 * 
	 * @param relation
	 * @param efa
	 */
	protected static void relation(Relation relation,EFA efa){
		if(RelationType.SEQUENCE.equals(relation.getType())){
			sequence(relation, "start_", "end_", efa);
		}else if(RelationType.ALTERNATIVE.equals(relation.getType())){
			alternative(relation, "start_", "end_", efa);
		}else if(RelationType.PARALLEL.equals(relation.getType())){
			parallel(relation,"", efa.getModule());
		}else if(RelationType.ARBITRARY.equals(relation.getType())){
			arbitrary(relation,"", efa.getModule());
		}
	}
	
	/**
	 * Builds a sequence relation from from event "from" to "to".
	 * 
	 * @param start is start state for sequence
	 * @param end is end state for sequence
	 * @param r is relation whit type sequence 
	 * 
	 */
	protected static void sequence(Relation r,
			                       String from, String to,
			                       EFA efa){
		String myLastState;
		String tmp;
		
		Iterator<Object> i;
		
		//check indata
		if(r == null){
			return;
		}
		
		//add state if missing
		if(from.length() == 0){
			from = efa.newUniqueState();
		}
		if(to.length() == 0){
			to = efa.newUniqueState();
		}
		
		//add first state
		if(!efa.stateExist(from)){
			efa.addState(from);
		}
		
		Object o = null;
		List<Activity> myActivityList = new LinkedList<Activity>();
		
		myLastState = from;
			
		List<Object> activityList = r.getActivityRelationGroup();
		
		if(activityList.isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
		i = activityList.iterator();
		
		/* start build EFA
		 * go down in tre structure
		 * and handel activities and relations
		 */
		while(i.hasNext() || o != null){
			
			if(o == null){
				o = i.next();
			}
			
			/* ------------ activity code -------------- */
			while(o instanceof Activity){
				myActivityList.add((Activity)o);
				if(!i.hasNext()){
					o = null;
					break; //exit first while
				}else{	
					o = i.next();
				}
			}
			/* ------------ end activity code ---------------- */
			
			
			
			/* --------------- relation code ----------------- */
			while(o instanceof Relation){
				
				//take care of different nodes here
				if(RelationType.SEQUENCE.equals(((Relation)o).getType())){
					
					/*--------------------*/
					/* Sequence node code */
					/*--------------------*/
					
					if(myActivityList.size() > 0){
						tmp = efa.newUniqueState();
						nativeSequence(myActivityList,myLastState,tmp,efa);
						myLastState = tmp;
						myActivityList.clear();
					}
					
					
					if(!i.hasNext()){
						sequence((Relation)o, myLastState, to, efa);
						return;
					}
					
					//recursion
					tmp = efa.newUniqueState();
					sequence((Relation)o, myLastState, tmp, efa);
					myLastState = tmp;
					
				}else if(RelationType.ALTERNATIVE.equals(((Relation)o).getType())){
					
					/*-----------------------*/
					/* Alternative node code */
					/*-----------------------*/
					
					if(myActivityList.size() > 0){
						tmp = efa.newUniqueState();
						nativeSequence(myActivityList,myLastState,tmp,efa);
						myLastState = tmp;
						myActivityList.clear();
					}
					
					if(!i.hasNext()){
						alternative((Relation)o, myLastState,to,efa);
						return;
					}
					
					tmp = efa.newUniqueState();
					alternative((Relation)o, myLastState, tmp, efa);
					myLastState = tmp;
					
				}else if(RelationType.PARALLEL.equals(((Relation)o).getType())){
					
					Module m;
					String var_name;
					
					//--------------------//
					// Parallel node code //
					//--------------------//
					
					m = efa.getModule();
					var_name = addWaitForNodeToFinish(myActivityList,(Relation)o,efa);
					
					//build Parallel
					parallel((Relation)o,var_name,m);
				}else if(RelationType.ARBITRARY.equals(((Relation)o).getType())){
					
					//--------------------//
					// Arbitrary node code//
					//--------------------//
					
					Module m = efa.getModule();
					String var_name;
					
					var_name = addWaitForNodeToFinish(myActivityList,(Relation)o,efa);
					
					//build arbitrary
					arbitrary( (Relation)o ,var_name ,m );
					
				}else{
					//Unknown node type
					System.err.println("Unknown RelationType "+
										((Relation)o).getType()+
										" in sequence");
				}
				
				
				if(!i.hasNext()){
					o = null;
					break; //exit while
				}else{
					o = i.next();
				}
			}
			/* --------------- end  relation code ------------ */
			
		}
		
		/* build sequence from list */
		if(!myActivityList.isEmpty()){
			nativeSequence(myActivityList,myLastState,to,efa);
		}
	}
	
	/**
	 * Builds a alternative relation from from event "from" to "to".
	 * 
	 * @param start is start state for alternative
	 * @param end is end state for alternative
	 * @param r is relation whit type alternative 
	 * 
	 */
	protected static void alternative(Relation r,
			                          String from,
			                          String to,
			                          EFA efa){
		Object o = null;
		List<Activity> myActivityList = null;
		List<Object> activityList = null;
		Iterator<Object> i = null;
		
		//check in data
		if(r == null){
			return;
		}
		
		//make sure from and to state exist
		if(from.length() == 0){
			from = efa.newUniqueState();
		}
		if(to.length() == 0){
			to = efa.newUniqueState();
		}
		
		myActivityList = new LinkedList<Activity>();
		activityList = r.getActivityRelationGroup();
		
		if(activityList.isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
		
		//---------- uncontrollable alternative --------
		if(false){
			uncontrollabelAlternative(r, from, to, efa);
			return;
		}
		//-------- end uncontrollable alternative ------
		
		
		i = activityList.iterator();
		
		// start build EFA
		// go down in tree structure
		// and handle activities and relations
		while(i.hasNext() || o != null){
			
			if(o == null){
				o = i.next();
			}
			
			// ------------ activity code -------------- //
			while(o instanceof Activity){
				
				myActivityList.add((Activity)o);
					
				if(!i.hasNext()){
					o = null;
					break; //exit first while
				}else{	
					o = i.next();
				}
			}
			// ------------ end activity code ---------------- //
			
			
			
			// --------------- relation code ----------------- //
			while(o instanceof Relation){
				
				//take care of different nodes here
				if(RelationType.SEQUENCE.equals(((Relation)o).getType())){
					
					//--------------------//
					// Sequence node code //
					//--------------------//
					
					nativeAlternative(myActivityList,from,to,efa);
					myActivityList.clear();

					sequence((Relation)o, from, to, efa);
					
				}else if(RelationType.ALTERNATIVE.equals(((Relation)o).getType())){
					
					//-----------------------//
					// Alternative node code //
					//-----------------------//
					//recursion
					alternative((Relation)o, from, to, efa);
					
				}else if(RelationType.PARALLEL.equals(((Relation)o).getType())){
					
					//--------------------//
					// Parallel node code //
					//--------------------//
					
					Module m = efa.getModule();
					String var_name;
					ObjectFactory factory = new ObjectFactory();
					Relation seq = factory.createRelation();
					
					List<Activity> tmp = new LinkedList<Activity>();
					
					var_name = addWaitForNodeToFinish(tmp,(Relation)o,efa);
					
					seq.setType(RelationType.SEQUENCE);
					
					for(Activity a : tmp){
						seq.getActivityRelationGroup().add(a);
					}
					
					sequence(seq,from,to,efa);
					
					//build Parallel
					parallel((Relation)o,var_name,m);
				}else if(RelationType.ARBITRARY.equals(((Relation)o).getType())){
					
					//---------------------------//
					// Arbitrary order node code //
					//---------------------------//
					
					Module m = efa.getModule();
					String var_name;
					ObjectFactory factory = new ObjectFactory();
					
					Relation seq = factory.createRelation();
					seq.setType(RelationType.SEQUENCE);
					
					List<Activity> tmp = new LinkedList<Activity>();
					var_name = addWaitForNodeToFinish(tmp,(Relation)o,efa);
					for(Activity a : tmp){
						seq.getActivityRelationGroup().add(a);
					}
					
					//build start and wait for arbitrary node
					sequence(seq,from,to,efa);
					
					//build arbitrary
					arbitrary((Relation)o,var_name,m);
				}else{
					// Unknown node type //
					System.err.println("Unknown RelationType " + ((Relation)o).getType());
				}
				
				
				if(!i.hasNext()){
					o = null;
					break; //exit while
				}else{
					o = i.next();
				}
			}
			// --------------- end  relation code ------------ //
			
		}
		
		// build alternative from list //
		if(!myActivityList.isEmpty()){
			nativeAlternative(myActivityList,from,to,efa);
		}
	}
	/**
	 * Function to build an uncontrollable alternative node.
	 * @param r
	 * @param from
	 * @param to
	 * @param efa
	 */
	protected static void uncontrollabelAlternative(Relation r,
            										String from,
            										String to,
            										EFA efa){
		
		String tmp = "";
		String altEvent = "alt_";
		String event = "";
		
		int number = 0;
		
		List<Object> objectList = null;
		
		//check in data
		if(r == null){
			return;
		}
		objectList = r.getActivityRelationGroup();
		
		if(objectList.isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
		//make sure from and to state exist
		if(from.length() == 0){
			from = efa.newUniqueState();
		}
		if(to.length() == 0){
			to = efa.newUniqueState();
		}
		
		//build unique event
		number = 0;
		while(efa.eventExist(altEvent + Integer.toString(number) + "_0")){
			number++;
		}
		
		altEvent = altEvent.concat(Integer.toString(number));
		altEvent = altEvent.concat("_");
		
		number = -1;
		for(Object o : objectList){
			number = number + 1;
			
			//--- Uncontrollable start
			tmp = efa.newUniqueState();
			event = altEvent.concat(Integer.toString(number));
			efa.addEvent(event, "uncontrollable");
			efa.addTransition(from, tmp, event, "", "");
			
			//--- Uncontrollable start
			
			if(o instanceof Activity){
				nativeActivity( (Activity)o, tmp, to, efa );
			}else if(o instanceof Relation){
				if(RelationType.SEQUENCE.equals(((Relation)o).getType()))
				{
					sequence( (Relation)o, tmp, to, efa );
				}
				else if(RelationType.ALTERNATIVE.equals(((Relation)o).getType()))
				{
					alternative( (Relation)o, tmp, to, efa );
				}
				else if(RelationType.PARALLEL.equals(((Relation)o).getType()))
				{
					List<Activity> activityList = new LinkedList<Activity>();
					String varName = addWaitForNodeToFinish(activityList, (Relation)o, efa);
					
					if(!activityList.isEmpty()){
						nativeSequence(activityList,tmp,to,efa);
					}
					
					parallel( (Relation)o, varName, efa.getModule());
				}
				else if(RelationType.ARBITRARY.equals(((Relation)o).getType()))
				{
					List<Activity> activityList = new LinkedList<Activity>();
					String varName = addWaitForNodeToFinish(activityList, (Relation)o, efa);
					
					if(!activityList.isEmpty()){
						nativeSequence(activityList,tmp,to,efa);
					}
					
					arbitrary( (Relation)o, varName, efa.getModule());
				}
			}//end if
		}
	}
	
	/**
	 * Manipulate the activityList to contain two new activities one who starts
	 * the node relation and one who wait for it to finish.
	 *  
	 * @param activityList list of activities to manipulate.
	 * @param relation node type.
	 * @param efa, needed to get module.
	 * @return variable name for node.
	 */
	private static String addWaitForNodeToFinish(List<Activity> activityList,
												 Relation relation, EFA efa){
		
		Activity start;
		Attribute onlystart;
		Attribute startguard;
		Attribute startaction;
		
		Activity stop;
		Attribute onlystop;
		Attribute stopguard;
		Attribute stopaction;
		
		Module m = efa.getModule();
		int numberOfTracks = relation.getActivityRelationGroup().size();
		String var_name = "";
		
		ObjectFactory factory = new ObjectFactory();
		
		String startGuard = "";
		String startAction = "";
		
		String stopGuard = "";
		String stopAction = "";
		
		// node code //
		if(RelationType.PARALLEL.equals(relation.getType())){
			var_name = m.newParrallelNodeInteger(numberOfTracks + 1);
		}else if(RelationType.ARBITRARY.equals(relation.getType())){
			var_name = m.newArbitraryOrderNodeInteger(numberOfTracks + 1);
		}else{
			System.err.println("Unknown node type " +
								relation.getType().toString() +
								" in addWaitForNodeToFinish");
			return var_name;
		}
		
		startGuard = "";
		startAction = var_name + "=1;";
		
		stopGuard = var_name + "==" + (numberOfTracks + 1);
		stopAction = var_name + "=0;";
		
		// set start conditions //
		onlystart = factory.createAttribute();
		onlystart.setType( ONLY_STA );
		
		startguard = factory.createAttribute();
		startguard.setType( AND_STA_GUARD );
		startguard.setAttributeValue(startGuard);
		
		startaction = factory.createAttribute();
		startaction.setType( ADD_STA_ACTION );
		startaction.setAttributeValue(startAction);
		
		start = factory.createActivity();
		start.setProperties(factory.createProperties());
		
		start.getProperties().getAttribute().add(onlystart);
		start.getProperties().getAttribute().add(startguard);
		start.getProperties().getAttribute().add(startaction);
		
		start.setOperation(var_name);
		// end set start conditions //
		
		
		// Set stop conditions //
		onlystop = factory.createAttribute();
		onlystop.setType( ONLY_STO );
		
		stopguard = factory.createAttribute();
		stopguard.setType( AND_STO_GUARD );
		stopguard.setAttributeValue(stopGuard);
		
		stopaction = factory.createAttribute();
		stopaction.setType( ADD_STO_ACTION );
		stopaction.setAttributeValue(stopAction);
		
		stop = factory.createActivity();
		stop.setProperties(factory.createProperties());
		
		stop.getProperties().getAttribute().add(onlystop);
		stop.getProperties().getAttribute().add(stopguard);
		stop.getProperties().getAttribute().add(stopaction);
		
		stop.setOperation(var_name);
		// End stop conditions //
		
		//add Activity who starts node
		activityList.add(start);
		
		//add Activity who wait for node to finish
		activityList.add(stop);
		
		//return the variable name assigned to this node
		return var_name;
	}
	
	
	/**
	 * parallel relation.
	 * 
	 * @param r relation tree of type parallel on top.
	 * @param parallel_var variable name for this parallel node from 0 to ant parallel track + 1
	 * @param m module to build parallel in.
	 */
	protected static void parallel(Relation r, String parallel_var, Module m){
		
		final String FIRST_STATE = "waiting";
		final String LAST_STATE = "finished";
		
		String startGuard = "", startAction = "";
		String stopGuard = "", stopAction = "";
		
		String parallel_track = "";
		
		Object[] activityRelations;
		EFA tmp; 
		
		ObjectFactory factory = new ObjectFactory();  
		
		//check in data
		if(r == null || m == null){
			return;
		}
		
		if(r.getActivityRelationGroup().isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
		activityRelations = (r.getActivityRelationGroup()).toArray();
		
		if(parallel_var.length() > 0){
			//start conditions
			startGuard = parallel_var + ">0";
			
			//stop conditions
			stopGuard = parallel_var + "<" + (activityRelations.length + 1);
			stopAction = parallel_var + "+=1;";
		}else{
			parallel_var = "pa";
			System.out.println("WARNING! a parallel node have no" +
					" variable asigned");
		}
		
		//for a parallel node only two options exist either we have
		//a relation or a activity.
		//Different relation type handle equals.
		
		for(int i = 0; i < activityRelations.length; i++){
			
			parallel_track = parallel_var +"_"+ i;
			
			tmp = new EFA(parallel_track,m);
			m.addAutomaton(tmp);
			
			tmp.addInitialState(FIRST_STATE);
			tmp.addState(LAST_STATE);
			
			if(activityRelations[i] instanceof Relation){
				//---------------------------//
				// Relation code             //
				//---------------------------//
				
				Relation seq = factory.createRelation();
				seq.setType(RelationType.SEQUENCE);
				
				//------- set start conditions --------//
				Activity start_par = factory.createActivity();
				start_par.setProperties(factory.createProperties());
				
				Attribute onlystart = factory.createAttribute();
				Attribute startguard = factory.createAttribute();
				Attribute startaction = factory.createAttribute();
				
				onlystart.setType(ONLY_STA);
				
				startguard.setType( AND_STA_GUARD );
				startaction.setType( ADD_STA_ACTION );
				
				startguard.setAttributeValue(startGuard);
				startaction.setAttributeValue(startAction);
				
				start_par.getProperties().getAttribute().add(onlystart);
				start_par.getProperties().getAttribute().add(startguard);
				start_par.getProperties().getAttribute().add(startaction);
				
				start_par.setOperation(parallel_track);
				//------- end set start conditions -------//
				
				// Set stop conditions //
				Activity stop_par = factory.createActivity();
				stop_par.setProperties(factory.createProperties());
				
				Attribute onlystop = factory.createAttribute();
				Attribute stopguard = factory.createAttribute();
				Attribute stopaction = factory.createAttribute();
				
				onlystop.setType(ONLY_STO);
				
				stopguard.setType( AND_STO_GUARD );
				stopguard.setAttributeValue(stopGuard);
				
				stopaction.setType( ADD_STO_ACTION );
				stopaction.setAttributeValue(stopAction);
				
				stop_par.getProperties().getAttribute().add(onlystop);
				stop_par.getProperties().getAttribute().add(stopguard);
				stop_par.getProperties().getAttribute().add(stopaction);
				
				stop_par.setOperation(parallel_track);			
				//End stop conditions //
				
				
				//lock the relation in a sequence
				//whit start and stop guards.
				
				seq.getActivityRelationGroup().add(start_par);
				seq.getActivityRelationGroup().add(activityRelations[i]);
				seq.getActivityRelationGroup().add(stop_par);
				
				//build sequence
				sequence(seq,FIRST_STATE,LAST_STATE,tmp);
				
			}else if(activityRelations[i] instanceof Activity){
				
				//---------------------------//
				// Activity code             //
				//---------------------------//
				
				//make sure it exist a property list
				if(((Activity)activityRelations[i]).getProperties() == null){
					((Activity)activityRelations[i]).setProperties(factory.createProperties());
				}
				
				//create start/stop guard and action
				Attribute startguard = factory.createAttribute();
				Attribute startaction = factory.createAttribute();
				
				Attribute stopguard = factory.createAttribute();
				Attribute stopaction = factory.createAttribute();
				
				startguard.setType( AND_STA_GUARD );
				startguard.setAttributeValue( startGuard );
				
				stopguard.setType( AND_STO_GUARD );
				stopguard.setAttributeValue( stopGuard );
				
				startaction.setType( ADD_STA_ACTION );
				startaction.setAttributeValue( startAction );
				
				stopaction.setType( ADD_STO_ACTION );
				stopaction.setAttributeValue( stopAction );
				
				//add to properties list
				((Activity)activityRelations[i]).getProperties().getAttribute().add( startguard );
				((Activity)activityRelations[i]).getProperties().getAttribute().add( stopguard );
				
				((Activity)activityRelations[i]).getProperties().getAttribute().add( startaction );
				((Activity)activityRelations[i]).getProperties().getAttribute().add( stopaction );
				
				nativeActivity((Activity)activityRelations[i],FIRST_STATE,LAST_STATE,tmp);
			}else{
				System.err.println("Unknown objekt " + activityRelations[i]);
			}
		}
		
	}
	
	/**
	 * arbitrary relation.
	 * 
	 * @param r relation tree of type arbitrary on top.
	 * @param arbitrary_var variable name for this arbitrary node from 0 to ant arbitrary track + 1
	 * @param m module to build arbitrary in.
	 */
	protected static void arbitrary(Relation r, String arbitraryNode_var, Module m){
		
		//final
		final String FIRST_STATE = "waiting";
		final String LAST_STATE = "finished";
		
		//lokal
		String startGuard = "", startAction = ""; 
		String stopGuard = "", stopAction = "";
		
		String arbitrary_track = "";
		String arbitrary_var = "";
		
		Relation seq;
		
		Activity start_ao;
		Attribute onlystart;
		Attribute startguard;
		Attribute startaction;
		
		Activity stop_ao;
		Attribute onlystop;
		Attribute stopguard;
		Attribute stopaction;
		
		Object[] activityRelations;
		EFA tmp;
		
		ObjectFactory factory = new ObjectFactory();  
		
		//test in data
		if(r == null || m == null){
			return;
		}
		
		if(r.getActivityRelationGroup().isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
		activityRelations = (r.getActivityRelationGroup()).toArray();
		
		arbitrary_var = m.newArbitraryInteger();
		
		if(arbitraryNode_var.length() > 0){
			//start conditions
			startGuard = arbitraryNode_var + ">0"+ EGA.AND +arbitrary_var+ "==1";
			startAction = arbitrary_var + "=0;";
			
			//stop conditions
			stopGuard = arbitraryNode_var + "<" + (activityRelations.length + 1);
			stopAction = arbitraryNode_var + "+=1;" + arbitrary_var + "=1;";
		}else{
			arbitraryNode_var = "ao";
			System.out.println("WARNING! a arbitraryorder node have no" +
					" variable asigned");
		}
		
		//for a arbitrary order node only two
		//options exist either we have
		//a relation or a activity.
		
		for(int i = 0; i < activityRelations.length; i++){
			
			arbitrary_track = arbitrary_var +"_"+ i;
			
			//create new automata
			tmp = new EFA(arbitrary_track,m);
			m.addAutomaton(tmp);
			
			tmp.addInitialState(FIRST_STATE);
			tmp.addState(LAST_STATE);
			
			if(activityRelations[i] instanceof Relation){
				
				//---------------------------//
				// Relation code             //
				//---------------------------//
				
				//------- Create start conditions --------//
				onlystart = factory.createAttribute();
				onlystart.setType(ONLY_STA);
				
				startguard = factory.createAttribute();
				startguard.setType( AND_STA_GUARD );
				startguard.setAttributeValue(startGuard);
				
				startaction = factory.createAttribute();
				startaction.setType( ADD_STA_ACTION );
				startaction.setAttributeValue(startAction);
				
				start_ao = factory.createActivity();
				start_ao.setProperties(factory.createProperties());
				start_ao.setOperation(arbitrary_track);
				
				start_ao.getProperties().getAttribute().add(onlystart);
				start_ao.getProperties().getAttribute().add(startguard);
				start_ao.getProperties().getAttribute().add(startaction);
				//------- End start conditions -------//
				
				// ------- Create stop conditions ------- //
				onlystop = factory.createAttribute();
				onlystop.setType( ONLY_STO );
				
				stopguard = factory.createAttribute();
				stopguard.setType( AND_STO_GUARD );
				stopguard.setAttributeValue(stopGuard);
				
				stopaction = factory.createAttribute();
				stopaction.setType( ADD_STO_ACTION );
				stopaction.setAttributeValue(stopAction);
				
				stop_ao = factory.createActivity();
				stop_ao.setProperties(factory.createProperties());
				stop_ao.setOperation(arbitrary_track);
				
				stop_ao.getProperties().getAttribute().add(onlystop);
				stop_ao.getProperties().getAttribute().add(stopguard);
				stop_ao.getProperties().getAttribute().add(stopaction);		
				// ------- End stop conditions ------- //
				
				// bind the relation in a sequence
				//whit start and stop guards
				seq = factory.createRelation();
				seq.setType(RelationType.SEQUENCE);
				
				seq.getActivityRelationGroup().add( start_ao );
				seq.getActivityRelationGroup().add( activityRelations[i] );
				seq.getActivityRelationGroup().add( stop_ao );
				
				//build sequence
				sequence( seq, FIRST_STATE, LAST_STATE, tmp );
				
			}else if(activityRelations[i] instanceof Activity){
				
				//---------------------------//
				// Activity code             //
				//---------------------------//
				
				//make sure it exist a property list
				if(((Activity)activityRelations[i]).getProperties() == null){
					((Activity)activityRelations[i]).setProperties(factory.createProperties());
				}
				
				//create start/stop guard and action
				startguard = factory.createAttribute();
				startguard.setType( AND_STA_GUARD );
				startguard.setAttributeValue( startGuard );
				
				startaction = factory.createAttribute();
				startaction.setType( ADD_STA_ACTION );
				startaction.setAttributeValue( startAction );
				
				stopguard = factory.createAttribute();
				stopguard.setType( AND_STO_GUARD );
				stopguard.setAttributeValue( stopGuard );
				
				stopaction = factory.createAttribute();
				stopaction.setType( ADD_STO_ACTION );
				stopaction.setAttributeValue( stopAction );
				
				//add to properties list
				((Activity)activityRelations[i]).getProperties().getAttribute().add( startguard );
				((Activity)activityRelations[i]).getProperties().getAttribute().add( stopguard );
				((Activity)activityRelations[i]).getProperties().getAttribute().add( startaction );
				((Activity)activityRelations[i]).getProperties().getAttribute().add( stopaction );
				
				nativeActivity((Activity)activityRelations[i],FIRST_STATE,LAST_STATE,tmp);
			}else{
				//No Relation and no Activity
				System.err.println("WARNING! Unknown objekt " + activityRelations[i]);
			}
		}
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	//				NOT FINISHED OR EPEREMENTAL FUNCTIONS						//
	//////////////////////////////////////////////////////////////////////////////
	
	protected static Relation addStartStopAttributes(Relation relation,
												 	 List<Attribute> attStartList,
												 	 List<Attribute> attStopList){
		
		// check input
		if(relation == null){
			return null;
		}
		
		if(attStartList == null || attStartList.size() == 0){
			if(attStopList == null|| attStopList.size() == 0){
				return relation;
			}
		}
		
		
		if(RelationType.SEQUENCE.equals(relation.getType())){
			Object o;
			
			// ------- sequence -------//
			
			//Add start Attributes
			o = relation.getActivityRelationGroup().get(0);
			
			if(o instanceof Activity){
				addAttributes((Activity)o, attStartList);
			}else if(o instanceof Relation){
				addStartStopAttributes(relation, attStartList, attStopList);
			}
			
			//Add stop Attribute
			o = relation.getActivityRelationGroup().
					get(relation.getActivityRelationGroup().size()-1);
			
			if(o instanceof Activity){
				addAttributes((Activity)o, attStopList);
			}else if(o instanceof Relation){
				addStartStopAttributes(relation, attStartList, attStopList);
			}
			// ------- end sequence -------//
			
		}else if(RelationType.ALTERNATIVE.equals(relation.getType()) ||
				 RelationType.PARALLEL.equals(relation.getType()) ||
				 RelationType.ARBITRARY.equals(relation.getType())){
			
			List<Object> activityRelationGroup;
			
			activityRelationGroup = relation.getActivityRelationGroup();
			
			//loop over all
			for(Object o : activityRelationGroup){
				if(o instanceof Activity){
					addAttributes((Activity)o, attStartList);
					addAttributes((Activity)o, attStopList);
				}else if(o instanceof Relation){
					addStartStopAttributes((Relation)o, attStartList, attStopList);
				}
			}
		}
		
		return relation;
	}
	
	/**
	 * Help function to <CODE>addStartAttributes</CODE>.
	 * @param activity
	 * @param attList
	 */
	private static void addAttributes(Activity activity, List<Attribute> attList){
		for(Attribute att : attList){
			activity.getProperties().getAttribute().add(att);
		}
	}
}
