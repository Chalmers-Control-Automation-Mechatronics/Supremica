package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.Precondition;

//
//	Native function code. Help function for DOPrelation class
//
public class DOPnative {
	
	protected final static String EVENT_MACHINE_SEPARATOR = "::";
	
	protected final static String EVENT_STOP_PREFIX = "sto_";
	protected final static String EVENT_START_PREFIX = "sta_";
	
	protected final static String RUNNING_STATE_SUFFIX = "_run";
	
	protected final static String PRECON_NOT_FULFILLED_STATE = "precon_not_fulfilled";
	protected final static String PRECON_FULFILLED_STATE = "precon_fulfilled";
	//
	// Activity function code
	//
	protected static void nativeActivity(Activity activity,
									String from,
									String to,
									EFA efa){
		
		
		EFA preconEFA = null;
		PGA pga = null;
		Precondition precon = null;
		List<OperationReferenceType> preconList = null;
		List<String> eventList = null;
		
		/**************************/
		//Check in data
		/**************************/
		if(activity == null || efa == null){
			return;
		}
		
		// check if state exist
		if(!efa.stateExist(from) || !efa.stateExist(to)){
			System.err.println("Unknown state From: " +from+
							   " or To: " + to);
			return;
		}
		
		/**************************/
		//Operation
		/**************************/
		//build operation
		pga = pgaFromActivity(activity,efa);
		nativeProcess(pga,from,to,efa);
		
		//get predecessors
		precon =  activity.getPrecondition();
		if(precon != null){
			preconList = precon.getPredecessor();
		}
		
		/**************************/
		//no Predecessors
		/**************************/
		if(preconList == null || preconList.size() == 0){
			return;
		}
		
		/**************************/
		//Predecessors
		/**************************/
		eventList = new LinkedList<String>();
		for(OperationReferenceType pre : preconList){
			eventList.add(EVENT_STOP_PREFIX
						+ pre.getOperation()+
						  EVENT_MACHINE_SEPARATOR
						  +pre.getMachine());
		}
		
		String event, preconEFAname;
		for(int i = 0; i < eventList.size(); i++){
			event = eventList.get(i);
			preconEFAname = "precon"+Integer.toString(i+1)+"_"+pga.getProcess();
			
			preconEFA = new EFA(preconEFAname,efa.getModule());
			
			
			preconEFA.addInitialState(PRECON_NOT_FULFILLED_STATE);
			preconEFA.addState(PRECON_FULFILLED_STATE);
			
			//transition to precondition fulfilled
			preconEFA.addTransition(PRECON_NOT_FULFILLED_STATE,
									PRECON_FULFILLED_STATE, event,"","");
			
			//self loop for not block event 
			preconEFA.addTransition(PRECON_FULFILLED_STATE,
									PRECON_FULFILLED_STATE, event,"","");
			
			//back to not fulfilled
			preconEFA.addTransition(PRECON_FULFILLED_STATE,
									PRECON_NOT_FULFILLED_STATE,
									EVENT_START_PREFIX+pga.getProcess(),"","");
			
			efa.getModule().addAutomaton(preconEFA);
		}
	}
	
	protected static PGA pgaFromActivity(Activity activity, EFA efa){
		
		final int MAX_RESOURCE_VALUE = 1;
		
		PGA pga;
		Module m;
		List attList;
		Attribute att;
		
		String resourceName, guard, action;
		boolean requireResource, releaseResource;
		
		//check indata
		if(activity == null){
			return null;
		}
		
		pga = new PGA();
		pga.setProcess(activity.getOperation());
		
		if(efa == null){
			return pga;
		}
		
		m = efa.getModule();
		if(m == null){
			return pga;
		}
		
		if(activity.getProperties() == null){
			return pga;
		}
		
		attList = activity.getProperties().getAttribute();
		if(attList == null || attList.isEmpty()){
			return pga;
		}
		
		//loop over list of attributes
		att = null;
		for(Object o : attList){
			if(o instanceof Attribute){
				att = (Attribute)o;
				
				resourceName = att.getAttributeValue();
				m.newResourceInteger(resourceName,MAX_RESOURCE_VALUE);
				
				if(att.getUpperIndicator() == null){
					requireResource = false;
				}else{
					requireResource = att.getUpperIndicator().isIndicatorValue();
				}
				
				if(att.getLowerIndicator() == null){
					releaseResource = false;
				}else{
					releaseResource = att.getLowerIndicator().isIndicatorValue();
				}
				
				if(requireResource){
					guard = resourceName + ">0" ;
					action = resourceName + "-=1";
					
					pga.andStartGuard(guard);
					pga.addStartAction(action);
				}
				
				if(releaseResource){
					guard = resourceName + "<" +
							m.getMaxValueResourceInteger(resourceName);
					
					action = resourceName + "+=1";
					
					pga.andStopGuard(guard);
					pga.addStopAction(action);
				}
				
			}else{
				System.err.println("Unknown object: " + o.toString());
			}
		}//end for
		
		return pga;
	}
	/**
	 * Native model for process start and stop.
	 * 
	 * @param pga
	 * @param from 
	 * @param to
	 * @param efa
	 */
	protected static void nativeProcess(PGA pga, String from, String to, EFA efa){
		
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
		/*
		System.out.println("Process: " + pga.getProcess());
		
		System.out.println("StartGuard: " + pga.getStartGuard());
		System.out.println("StartAction: " + pga.getStartAction());
		
		System.out.println("StopGuard: " + pga.getStopGuard());
		System.out.println("StopAction: " + pga.getStopAction());
		
		System.out.println("OnlyStart: " + pga.getOnlyStart());
		System.out.println("OnlyStop: " + pga.getOnlyStop());
		*/
		//debug
		
		
		
		/**************************/
		//only start event ?
		/**************************/
		
		if(pga.getOnlyStart()){
			event = EVENT_START_PREFIX.concat(pga.getProcess());
			guard = pga.getStartGuard();
			action = pga.getStartAction();
			efa.addTransition(from, to, event, guard, action);
			return;
		}
		
		/**************************/
		//only stop event 
		/**************************/
		
		if(pga.getOnlyStop()){
			event = EVENT_STOP_PREFIX.concat(pga.getProcess());
			guard = pga.getStopGuard();
			action = pga.getStopAction();
			efa.addTransition(from,to, event, guard, action);
			return;
		}
		
		/**************************/
		//from start to running to stop event
		/**************************/
		
		runningState = pga.getProcess().concat(RUNNING_STATE_SUFFIX);
		
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
		event = EVENT_START_PREFIX.concat(pga.getProcess());
		guard = pga.getStartGuard();
		action = pga.getStartAction();
		
		efa.addTransition(from, runningState, event, guard, action);
		
		event = EVENT_STOP_PREFIX.concat(pga.getProcess());
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
	protected static void nativeSequence(List<Activity> activityList,
			                             String from, String to,
			                             EFA efa){
		//check in data
		if(activityList.isEmpty()){
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
		if(activityList.size() == 1){
			//add last state
			if(to.length() == 0){
				to = efa.nextState();
			}
			if(!efa.stateExist(to)){
				efa.addState(to);	
			}
			
			nativeActivity(activityList.get(0),from,to,efa);
			return;
		}
		
		//everything ok continue
		Iterator<Activity> i = activityList.iterator();
		Activity activity = i.next();
		
		String tmpFrom = from;
		String tmpTo = efa.newUniqueState();

		nativeActivity(activity,tmpFrom,tmpTo,efa);
		
		while(i.hasNext()){
			activity = i.next();
			if(i.hasNext()){
				
				tmpFrom = tmpTo;
				tmpTo = efa.newUniqueState();
				
				nativeActivity(activity,tmpFrom,tmpTo,efa);
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
				nativeActivity(activity,tmpFrom,to,efa);
			}
		}
	}
	
	/**
	 * 
	 * @param pgaList
	 * @param from
	 * @param to
	 * @param efa
	 */
	protected static void nativeAlternative(List<Activity> activityList,
            								String from, String to,
            								EFA efa){
		//chech indata
		if(activityList.size() == 0){
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
		
		//everything ok continue
		Iterator<Activity> i = activityList.iterator();
		
		while(i.hasNext()){
			nativeActivity(i.next(),from,to,efa);
		}
	}
	
	protected static void diamond(String from, String to, List<String> events, EFA efa){
		String tmpTo = "";
		List<String> tmpEvents = new LinkedList<String>();
		
		//check indata
		if(events == null || events.size() == 0){
			System.err.println("WARNING DIAMOND");
			return;
		}
		
		System.out.println("Diamond");
		System.out.println("From: " + from);
		System.out.println("To: " + to);
		System.out.println(events.toString());
		
		//base case
		if(events.size() == 1){
			efa.addTransition(from, to, events.get(0),"","");
			return;
		}
		
		//copy events to tmpEvents
		for(String event : events){
			tmpEvents.add(event);
		}
		
		
		for(String event : events){
			
			//
			tmpTo = efa.newUniqueState();
			efa.addTransition(from, tmpTo, event,"","");
			
			System.out.println(tmpEvents.toString());
			System.out.println(events.toString());
			
			//copy events to tmpEvents
			Collections.copy(events,tmpEvents);
			
			//remove current event
			tmpEvents.remove(event);
			
			//recursion
			diamond(tmpTo,to,tmpEvents,efa);
		}
	}
}
