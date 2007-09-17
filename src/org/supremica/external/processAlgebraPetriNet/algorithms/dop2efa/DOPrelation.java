package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

/**
 * this class holds function to build single relations
 * to EFA.
 * 
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.supremica.manufacturingTables.xsd.rop_copvision.*;

public class DOPrelation{
	
	//
	// Relation function code
	//
	protected static void relation(Relation r,EFA efa){
		if(RelationType.SEQUENCE.equals(r.getType())){
			sequence(r, "start_", "end_", efa);
		}else if(RelationType.ALTERNATIVE.equals(r.getType())){
			alternative(r, "start_", "end_", efa);
		}else if(RelationType.PARALLEL.equals(r.getType())){
			parallel(r,"", efa.getModule());
		}
	}
	
	/**
	 * 
	 * 
	 * @param start is start state for sequence
	 * @param end is end state for sequence
	 * @param r is relation whit type sequence 
	 * 
	 */
	protected static void sequence(Relation r,
			                       String from, String to,
			                       EFA efa){
		//check indata
		if(r == null){
			return;
		}
		
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
		List<PGA> pgaList = new LinkedList<PGA>();
		
		String myLastState = from;
		String tmp;
			
		List activityList = r.getActivityRelationGroup();
		Iterator i = activityList.iterator();
		
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
				PGA pga = new PGA();
				pga.setProcess(((Activity)o).getOperation());
				pgaList.add(pga);
				
				
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
					
					/* Sequence node code */
					
					if(pgaList.size() > 0){
						tmp = efa.newUniqueState();
						nativeSequence(pgaList,myLastState,tmp,efa);
						myLastState = tmp;
						pgaList.clear();
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
					
					/* Alternative node code */
					if(pgaList.size() > 0){
						tmp = efa.newUniqueState();
						nativeSequence(pgaList,myLastState,tmp,efa);
						myLastState = tmp;
						pgaList.clear();
					}
					
					if(!i.hasNext()){
						alternative((Relation)o, myLastState,to,efa);
						return;
					}
					
					tmp = efa.newUniqueState();
					alternative((Relation)o, myLastState, tmp, efa);
					myLastState = tmp;
					
				}else if(RelationType.PARALLEL.equals(((Relation)o).getType())){
					
					Module m = efa.getModule();
					Relation parallel = (Relation)o;
					int ant_parallel_track = parallel.getActivityRelationGroup().size();
					PGA start_stop_parallel = new PGA();
					String var_name;
					
					/* Parallel node code */
					var_name = m.newParrallelInteger(ant_parallel_track + 1);
					
					//build Alternative path
					start_stop_parallel.setProcess(var_name);
					
					/* Start parallel node by seting it's variable to 1 */
					start_stop_parallel.setStartAction(var_name + "=1;");
					
					/* Continue then parallel node is done, and reset variabel */
					start_stop_parallel.setStopGuard(var_name + "==" + (ant_parallel_track + 1));
					start_stop_parallel.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_parallel);
					
					//build Parallel
					parallel(parallel,var_name,m);
				}else{
					/* Unknown node type */
					System.err.println("Unknown RelationType " + ((Relation)o).getType());
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
		if(!pgaList.isEmpty()){
			nativeSequence(pgaList,myLastState,to,efa);
		}
	}
	
	/**
	 * 
	 * 
	 * @param start is start state for sequence
	 * @param end is end state for sequence
	 * @param r is relation whit type sequence 
	 * 
	 */
	protected static void alternative(Relation r,
			                          String from, String to,
			                          EFA efa){
		//check indata
		if(r == null){
			return;
		}
		
		if(from.length() == 0){
			from = efa.newUniqueState();
		}
		if(to.length() == 0){
			to = efa.newUniqueState();
		}
		
		Object o = null;
		List<PGA> pgaList = new LinkedList<PGA>();
			
		List activityList = r.getActivityRelationGroup();
		Iterator i = activityList.iterator();
		
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
				PGA pga = new PGA();
				pga.setProcess(((Activity)o).getOperation());
				pgaList.add(pga);
					
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
					
					/* Sequence node code */
					nativeAlternative(pgaList,from,to,efa);
					pgaList.clear();

					sequence((Relation)o, from, to, efa);
					
				}else if(RelationType.ALTERNATIVE.equals(((Relation)o).getType())){
					/* Alternative node code */
					//recursion
					alternative((Relation)o, from, to, efa);
				}else if(RelationType.PARALLEL.equals(((Relation)o).getType())){
					
					Module m = efa.getModule();
					Relation parallel = (Relation)o;
					int ant_parallel_track = parallel.getActivityRelationGroup().size();
					PGA start_stop_parallel = new PGA();
					String var_name;
					
					/* Parallel node code */
					var_name = m.newParrallelInteger(ant_parallel_track + 1);
					
					//build Alternative path
					start_stop_parallel.setProcess(var_name);
					start_stop_parallel.setStartAction(var_name + "=1;");
					
					start_stop_parallel.setStopGuard(var_name + "==" + (ant_parallel_track + 1));
					start_stop_parallel.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_parallel);
					
					//build Parallel
					System.out.println("Build parallel from alternative");
					
					parallel(parallel,var_name,m);
				}else{
					/* Unknown node type */
					System.err.println("Unknown RelationType " + ((Relation)o).getType());
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
		
		/* build alternative from list */
		if(!pgaList.isEmpty()){
			nativeAlternative(pgaList,from,to,efa);
		}
	}
	
	protected static void parallel(Relation r, String parallel_var, Module m){
		
		final String firstState = "waiting";
		final String lastState = "finished";
		
		String startGuard = ""; 
		String stopGuard = "", stopAction = "";
		
		String parallel_track = "";
		
		Object[] activityRelations;
		EFA tmp; 
		
		ObjectFactory factory = new ObjectFactory();  
		
		//check in data
		if(r == null || m == null){
			return;
		}
		
		activityRelations = (r.getActivityRelationGroup()).toArray();
		
		if(parallel_var.length() > 0){
			startGuard = parallel_var + ">0";
			
			stopGuard = parallel_var + "<" + (activityRelations.length + 1);
			stopAction = parallel_var + "+=1;";
		}else{
			parallel_var = "pa";
			System.out.println("WARNING! a parallel node have no" +
					" variable asigned");
		}
		
		for(int i = 0; i < activityRelations.length; i++){
			
			parallel_track = parallel_var +"_"+ i;
			
			tmp = new EFA(parallel_track,m);
			m.addAutomaton(tmp);
			
			tmp.addState(firstState);
			tmp.addState(lastState);
			
			if(activityRelations[i] instanceof Relation){
				String start;
				String stop;
				
				/* Relation code */
				Relation seq = factory.createRelation();
				seq.setType(RelationType.SEQUENCE);
				
				Activity start_par = factory.createActivity();
				start = PGA.ONLY_START + PGA.GUARD+startGuard+PGA.GUARD
									   +parallel_track;
				
				start_par.setOperation(start);
				
				Activity stop_par = factory.createActivity();
				stop = PGA.ONLY_STOP +PGA.GUARD+stopGuard+PGA.GUARD
									 +PGA.ACTION+stopAction+PGA.ACTION
									 +parallel_track;
				
				stop_par.setOperation(stop);
				
				// lock the relation in a sequence
				seq.getActivityRelationGroup().add(start_par);
				seq.getActivityRelationGroup().add(activityRelations[i]);
				seq.getActivityRelationGroup().add(stop_par);
				
				//build sequence
				sequence(seq,firstState,lastState,tmp);
				
			}else if(activityRelations[i] instanceof Activity){
				/* Activity code */
				PGA pga = new PGA(((Activity)activityRelations[i]).getOperation());
				pga.setStartGuard(startGuard);
				
				pga.setStopGuard(stopGuard);
				pga.setStopAction(stopAction);
				
				nativeProcess(pga,firstState,lastState,tmp);
			}else{
				System.err.println("Unknown objekt " + activityRelations[i]);
			}
		}
		
	}
	
	//
	//	Native function code. Help function for relation code
	//
	
	
	/**
	 * Native model for process start and stop.
	 * 
	 * @param pga
	 * @param from 
	 * @param to
	 * @param efa
	 */
	protected static void nativeProcess(PGA pga, String from, String to, EFA efa){
		
		final String START_EVENT = "sta_";
		final String STOP_EVENT = "sto_";
		
		final String RUNNING_STATE = "_run";
		
		String event, guard, action;
		String runningState;
		
		// check in data
		if(pga == null || efa == null){
			return;
		}
		
		// check if state exist
		if(!efa.stateExist(from) || !efa.stateExist(to)){
			System.err.println("Unknown state From: " +from+ " To: " + to);
			return;
		}
		
		
		//debug
		System.out.println("Process: " + pga.getProcess());
		
		System.out.println("StartGuard: " + pga.getStartGuard());
		System.out.println("StartAction: " + pga.getStartAction());
		
		System.out.println("StopGuard: " + pga.getStopGuard());
		System.out.println("StopAction: " + pga.getStopAction());
		
		System.out.println("OnlyStart: " + pga.getOnlyStart());
		System.out.println("OnlyStop: " + pga.getOnlyStop());
		//debug
		
		/**************************/
		//only start event ?
		/**************************/
		
		if(pga.getOnlyStart()){
			event = START_EVENT.concat(pga.getProcess());
			guard = pga.getStartGuard();
			action = pga.getStartAction();
			efa.addTransition(from, to, event, guard, action);
			return;
		}
		
		/**************************/
		//only stop event 
		/**************************/
		
		if(pga.getOnlyStop()){
			event = STOP_EVENT.concat(pga.getProcess());
			guard = pga.getStopGuard();
			action = pga.getStopAction();
			efa.addTransition(from,to, event, guard, action);
			return;
		}
		
		/**************************/
		//from start to running to stop event
		/**************************/
		
		runningState = pga.getProcess().concat(RUNNING_STATE);
		
		//unique running state
		if(efa.stateExist(runningState)){
			int i = 0;
			do{
				i = i + 1;
			}while(efa.stateExist(runningState.concat(Integer.toString(i))));
			
			runningState = runningState.concat(Integer.toString(i));
		}
		
		//add running state
		efa.addState(runningState);
		
		//create transition
		event = START_EVENT.concat(pga.getProcess());
		guard = pga.getStartGuard();
		action = pga.getStartAction();
		
		efa.addTransition(from, runningState, event, guard, action);
		
		event = STOP_EVENT.concat(pga.getProcess());
		guard = pga.getStopGuard();
		action = pga.getStopAction();
		
		efa.addTransition(runningState,to, event, guard, action);
	}
	
	/**
	 * 
	 * 
	 * @param start is start state for sequence
	 * @param end is end state for sequence
	 * @param ega 
	 */
	protected static void nativeSequence(List<PGA> pgaList,
			                             String from, String to,
			                             EFA efa){
		//check in data
		if(pgaList.isEmpty()){
			return;
		}
		
		//add first state
		if(from.length() == 0){
			from = efa.nextState();
		}
		
		if(!efa.stateExist(from)){
			efa.addState(from);
		}
		
		//special case
		if(pgaList.size() == 1){
			//add last state
			if(to.length() == 0){
				to = efa.nextState();
			}
			if(!efa.stateExist(to)){
				efa.addState(to);	
			}
			
			nativeProcess(pgaList.get(0),from,to,efa);
			return;
		}
		
		//everything ok continue
		Iterator<PGA> i = pgaList.iterator();
		PGA pga = i.next();
		
		String tmpFrom = from;
		String tmpTo = efa.newUniqueState();

		nativeProcess(pga,tmpFrom,tmpTo,efa);
		
		while(i.hasNext()){
			pga = i.next();
			if(i.hasNext()){
				
				tmpFrom = tmpTo;
				tmpTo = efa.newUniqueState();
				
				nativeProcess(pga,tmpFrom,tmpTo,efa);
			}else{
				
				//connect to last state to
				tmpFrom = tmpTo;
				
				//add last state
				if(to.length() == 0){
					to = efa.nextState();
				}
				if(!efa.stateExist(to)){
					efa.addState(to);	
				}
				nativeProcess(pga,tmpFrom,to,efa);
			}
		}
	}
	

	protected static void nativeAlternative(List<PGA> pgaList,
            								String from, String to,
            								EFA efa){
		//chech indata
		if(pgaList.size() == 0){
			return;
		}
		
		//add first state
		if(from.length() == 0){
			from = efa.nextState();
		}
		
		if(!efa.stateExist(from)){
			efa.addState(from);
		}
		
		//add last state
		if(to.length() == 0){
			to = efa.nextState();
		}
		
		if(!efa.stateExist(to)){
			efa.addState(to);	
		}
		
		
		//special for alternative
		//egaList = removeDouble(egaList);
		//egaList = concatEvents(egaList);
		
		
		//everything ok continue
		Iterator<PGA> i = pgaList.iterator();
		
		while(i.hasNext()){
			nativeProcess(i.next(),from,to,efa);
		}
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
}
