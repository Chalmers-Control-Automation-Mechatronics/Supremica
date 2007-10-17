package org.supremica.external.processeditor.xml.dop2efa;

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
		List<Activity> myActivityList = new LinkedList<Activity>();
		
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
					
					/*--------------------*/
					/* Parallel node code */
					/*--------------------*/
					
					Module m = efa.getModule();
					String var_name;
					
					var_name = addWaitForNodeToFinish(myActivityList,(Relation)o,efa);
					
					//build Parallel
					parallel((Relation)o,var_name,m);
				}else if(RelationType.ARBITRARY.equals(((Relation)o).getType())){
					
					/*--------------------*/
					/* Arbitrary node code*/
					/*--------------------*/
					
					Module m = efa.getModule();
					String var_name;
					
					var_name = addWaitForNodeToFinish(myActivityList,(Relation)o,efa);
					
					//build arbitrary
					arbitrary((Relation)o,var_name,m);
					
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
		if(!myActivityList.isEmpty()){
			nativeSequence(myActivityList,myLastState,to,efa);
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
		List<Activity> myActivityList = new LinkedList<Activity>();
			
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
					
					nativeAlternative(myActivityList,from,to,efa);
					myActivityList.clear();

					sequence((Relation)o, from, to, efa);
					
				}else if(RelationType.ALTERNATIVE.equals(((Relation)o).getType())){
					
					/*-----------------------*/
					/* Alternative node code */
					/*-----------------------*/
					//recursion
					alternative((Relation)o, from, to, efa);
					
				}else if(RelationType.PARALLEL.equals(((Relation)o).getType())){
					
					/*--------------------*/
					/* Parallel node code */
					/*--------------------*/
					
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
					
					/*---------------------------*/
					/* Arbitrary order node code */
					/*---------------------------*/
					
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
					
					//build start and wait for arbitrary node
					sequence(seq,from,to,efa);
					
					//build arbitrary
					arbitrary((Relation)o,var_name,m);
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
		if(!myActivityList.isEmpty()){
			nativeAlternative(myActivityList,from,to,efa);
		}
	}
	
	/**
	 * 
	 * @param activityList
	 * @param relation
	 * @param efa
	 * @return
	 */
	private static String addWaitForNodeToFinish(List<Activity> activityList,
												 Relation relation, EFA efa){
		
		Module m = efa.getModule();
		int numberOfTracks = relation.getActivityRelationGroup().size();
		String var_name = "";
		
		ObjectFactory factory = new ObjectFactory();
		
		/* node code */
		if(RelationType.PARALLEL.equals(relation.getType())){
			var_name = m.newParrallelNodeInteger(numberOfTracks + 1);
		}else if(RelationType.ARBITRARY.equals(relation.getType())){
			var_name = m.newArbitraryOrderNodeInteger(numberOfTracks + 1);
		}else{
			System.err.println("Unknown node type " +
								relation.getType().toString() +
								" in addWaitForNodeToFinish");
		}
		
		String startGuard = "";
		String startAction = var_name + "=1;";
		
		String stopGuard = var_name + "==" + (numberOfTracks + 1);
		String stopAction = var_name + "=0;";
		
		/* set start conditions */
		Activity start_par = factory.createActivity();
		String start = PGA.ONLY_START +
						PGA.GUARD+startGuard+PGA.GUARD +
						PGA.ACTION+startAction+PGA.ACTION
						+var_name;
		
		start_par.setOperation(start);
		
		
		/* set stop conditions */
		Activity stop_par = factory.createActivity();
		String stop = PGA.ONLY_STOP+
					  PGA.GUARD+stopGuard+PGA.GUARD+
					  PGA.ACTION+stopAction+PGA.ACTION
					  +var_name;
		
		stop_par.setOperation(stop);
		
		/* add Activity who starts node */
		activityList.add(start_par);
		
		/* add Activity who wait for node to finish*/
		activityList.add(stop_par);
		
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
			
			tmp.addInitialState(FIRST_STATE);
			tmp.addState(LAST_STATE);
			
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
				sequence(seq,FIRST_STATE,LAST_STATE,tmp);
				
			}else if(activityRelations[i] instanceof Activity){
				
				/* Activity code */
				
				PGA pga = pgaFromActivity((Activity)activityRelations[i],m);
				pga.andStartGuard(startGuard);
				
				pga.andStopGuard(stopGuard);
				pga.addStopAction(stopAction);
				
				nativeProcess(pga,FIRST_STATE,LAST_STATE,tmp);
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
		
		final String FIRST_STATE = "waiting";
		final String LAST_STATE = "finished";
		
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
			
			tmp.addInitialState(FIRST_STATE);
			tmp.addState(LAST_STATE);
			
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
				sequence(seq,FIRST_STATE,LAST_STATE,tmp);
				
			}else if(activityRelations[i] instanceof Activity){
				
				/* Activity code */
				
				PGA pga = pgaFromActivity((Activity)activityRelations[i],m);
				pga.andStartGuard(startGuard);
				pga.addStartAction(startAction);
				
				pga.andStopGuard(stopGuard);
				pga.addStopAction(stopAction);
				
				nativeProcess(pga,FIRST_STATE,LAST_STATE,tmp);
			}else{
				System.err.println("Unknown objekt " + activityRelations[i]);
			}
		}
		
	}
	
	
}
