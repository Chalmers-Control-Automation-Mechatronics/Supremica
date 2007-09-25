package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

/**
 * this class holds function to build single relations
 * to EFA.
 * 
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.supremica.manufacturingTables.xsd.processeditor.*;

public class DOPrelation extends DOPnative{
	
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
		List<PGA> pgaList = new LinkedList<PGA>();
		
		String myLastState = from;
		String tmp;
			
		List activityList = r.getActivityRelationGroup();
		
		if(activityList.isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
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
				pgaList.add(pgaFromActivity((Activity)o,efa));
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
					var_name = m.newParrallelNodeInteger(ant_parallel_track + 1);
					
					//build Alternative path
					start_stop_parallel.setProcess(var_name);
					
					/* Start parallel node by set it's variable to 1 */
					start_stop_parallel.setStartAction(var_name + "=1;");
					
					/* Continue then parallel node is done, and reset variable */
					start_stop_parallel.setStopGuard(var_name + "==" + (ant_parallel_track + 1));
					start_stop_parallel.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_parallel);
					
					//build Parallel
					parallel(parallel,var_name,m);
				}else if(RelationType.ARBITRARY.equals(((Relation)o).getType())){
					
					Module m = efa.getModule();
					Relation arbitrary = (Relation)o;
					int ant_arbitrary_track = arbitrary.getActivityRelationGroup().size();
					PGA start_stop_arbitrary = new PGA();
					String var_name;
					
					/* ArbitraryOrder node code */
					var_name = m.newArbitraryNodeInteger(ant_arbitrary_track + 1);
					
					//build Alternative path
					start_stop_arbitrary.setProcess(var_name);
					
					/* Start arbitrary node by set it's variable to 1 */
					start_stop_arbitrary.setStartAction(var_name + "=1;");
					
					/* Continue then arbitrary node is done, and reset variable */
					start_stop_arbitrary.setStopGuard(var_name + "==" + (ant_arbitrary_track + 1));
					start_stop_arbitrary.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_arbitrary);
					
					//build Parallel
					arbitrary(arbitrary,var_name,m);
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
		//check in data
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
		
		if(activityList.isEmpty()){
			System.err.println("WARNING empty " + r.getType().toString());
			return;
		}
		
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
				
				pgaList.add(pgaFromActivity((Activity)o, efa));
					
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
					var_name = m.newParrallelNodeInteger(ant_parallel_track + 1);
					
					//build Alternative path
					start_stop_parallel.setProcess(var_name);
					start_stop_parallel.setStartAction(var_name + "=1;");
					
					start_stop_parallel.setStopGuard(var_name + "==" + (ant_parallel_track + 1));
					start_stop_parallel.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_parallel);
					
					//build Parallel
					System.out.println("Build parallel from alternative");
					
					parallel(parallel,var_name,m);
				}else if(RelationType.ARBITRARY.equals(((Relation)o).getType())){
					
					Module m = efa.getModule();
					Relation arbitrary = (Relation)o;
					int ant_arbitrary_track = arbitrary.getActivityRelationGroup().size();
					PGA start_stop_arbitrary = new PGA();
					String var_name;
					
					/* ArbitraryOrder node code */
					var_name = m.newArbitraryNodeInteger(ant_arbitrary_track + 1);
					
					//build Alternative path
					start_stop_arbitrary.setProcess(var_name);
					
					/* Start arbitrary node by set it's variable to 1 */
					start_stop_arbitrary.setStartAction(var_name + "=1;");
					
					/* Continue then arbitrary node is done, and reset variable */
					start_stop_arbitrary.setStopGuard(var_name + "==" + (ant_arbitrary_track + 1));
					start_stop_arbitrary.setStopAction(var_name + "=0;");
					
					pgaList.add(start_stop_arbitrary);
					
					//build Parallel
					arbitrary(arbitrary,var_name,m);
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
	
	/**
	 * parallel relation.
	 * 
	 * @param r relation tree of type parallel on top.
	 * @param parallel_var variable name for this parallel node from 0 to ant parallel track + 1
	 * @param m module to build parallel in.
	 */
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
		
		/* 
		 * for a parallel node only two options exist either we have
		 * a relation or a activity.
		 * 
		 * Different relation type handle equals.
		 *  
		 */
		
		for(int i = 0; i < activityRelations.length; i++){
			
			parallel_track = parallel_var +"_"+ i;
			
			tmp = new EFA(parallel_track,m);
			m.addAutomaton(tmp);
			
			tmp.addInitialState(firstState);
			tmp.addState(lastState);
			
			if(activityRelations[i] instanceof Relation){
				String start;
				String stop;
				
				/* Relation code */
				
				Relation seq = factory.createRelation();
				seq.setType(RelationType.SEQUENCE);
				
				/* set start conditions */
				Activity start_par = factory.createActivity();
				start = PGA.ONLY_START + PGA.GUARD+startGuard+PGA.GUARD
									   +parallel_track;
				
				start_par.setOperation(start);
				
				/* set stop conditions */
				Activity stop_par = factory.createActivity();
				stop = PGA.ONLY_STOP +PGA.GUARD+stopGuard+PGA.GUARD
									 +PGA.ACTION+stopAction+PGA.ACTION
									 +parallel_track;
				
				stop_par.setOperation(stop);
				
				/* 
				 * lock the relation in a sequence
				 * whit start and stop guards.
				 */
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
	
	/**
	 * arbitrary relation.
	 * 
	 * @param r relation tree of type arbitrary on top.
	 * @param arbitrary_var variable name for this arbitrary node from 0 to ant arbitrary track + 1
	 * @param m module to build arbitrary in.
	 */
	protected static void arbitrary(Relation r, String arbitraryNode_var, Module m){
		
		final String firstState = "waiting";
		final String lastState = "finished";
		
		String startGuard = "", startAction = ""; 
		String stopGuard = "", stopAction = "";
		
		String parallel_track = "";
		
		String arbitrary_var = "";
		
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
		
		arbitrary_var = m.newArbitraryInteger();
		
		if(arbitraryNode_var.length() > 0){
			//start conditions
			startGuard = arbitraryNode_var + ">0"+ PGA.AND +arbitrary_var+ "==1";
			startAction = arbitrary_var + "=0;";
			
			//stop conditions
			stopGuard = arbitraryNode_var + "<" + (activityRelations.length + 1);
			stopAction = arbitraryNode_var + "+=1;" + arbitrary_var + "=1;";
		}else{
			arbitraryNode_var = "ao";
			System.out.println("WARNING! a arbitraryorder node have no" +
					" variable asigned");
		}
		
		
		
		/* 
		 * for a arbitraryorder node only two options exist either we have
		 * a relation or a activity.
		 *  
		 */
		
		for(int i = 0; i < activityRelations.length; i++){
			
			parallel_track = arbitrary_var +"_"+ i;
			
			tmp = new EFA(parallel_track,m);
			m.addAutomaton(tmp);
			
			tmp.addInitialState(firstState);
			tmp.addState(lastState);
			
			if(activityRelations[i] instanceof Relation){
				String start;
				String stop;
				
				/* Relation code */
				
				Relation seq = factory.createRelation();
				seq.setType(RelationType.SEQUENCE);
				
				/* set start conditions */
				Activity start_par = factory.createActivity();
				start = PGA.ONLY_START + PGA.GUARD+startGuard+PGA.GUARD
									   +PGA.ACTION+startAction+PGA.ACTION
									   +parallel_track;
				
				start_par.setOperation(start);
				
				/* set stop conditions */
				Activity stop_par = factory.createActivity();
				stop = PGA.ONLY_STOP +PGA.GUARD+stopGuard+PGA.GUARD
									 +PGA.ACTION+stopAction+PGA.ACTION
									 +parallel_track;
				
				stop_par.setOperation(stop);
				
				/* 
				 * bind the relation in a sequence
				 * whit start and stop guards 
				 */
				seq.getActivityRelationGroup().add(start_par);
				seq.getActivityRelationGroup().add(activityRelations[i]);
				seq.getActivityRelationGroup().add(stop_par);
				
				//build sequence
				sequence(seq,firstState,lastState,tmp);
				
			}else if(activityRelations[i] instanceof Activity){
				
				/* Activity code */
				
				PGA pga = new PGA(((Activity)activityRelations[i]).getOperation());
				pga.setStartGuard(startGuard);
				pga.setStartAction(startAction);
				
				pga.setStopGuard(stopGuard);
				pga.setStopAction(stopAction);
				
				nativeProcess(pga,firstState,lastState,tmp);
			}else{
				System.err.println("Unknown objekt " + activityRelations[i]);
			}
		}
		
	}
	
	
}
