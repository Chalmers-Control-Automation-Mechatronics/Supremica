package org.supremica.external.processeditor.xml.dop2efa;

import java.util.Iterator;
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
	
	protected static List<Activity> activityPreconList = new LinkedList<Activity>();
	
	/**
	 * Activity function code
	 */
	protected static void nativeActivity(Activity activity,
									String from,
									String to,
									EFA efa){
		
		
		
		PGA pga = null;
		
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
		
		/**************************/
		//Predecessors
		/**************************/
		if(activity.getPrecondition() != null){
			if(!activityPreconList.contains(activity)){
				activityPreconList.add(activity);
			}
		}
	}
	
	/**
	 * 
	 * @param module
	 */
	protected static void addPrecondition(Module module){
		
		EFA preconEFA = null;
		Precondition precon = null;
		
		List<OperationReferenceType> preconList = null;
		List<String> eventList = null;
		List<String> moduleEventList = null;
		
		for(Activity activity : activityPreconList){
			
			//get predecessors
			precon =  activity.getPrecondition();
			if(precon != null){
				preconList = precon.getPredecessor();
			}
			
			//no Predecessors
			if(preconList == null || preconList.size() == 0){
				return;
			}
			
			//predecessors exist 
			eventList = new LinkedList<String>();
			OperationReferenceType pre = null;
			
			for(int i = 0; i < preconList.size(); i++){
				
				Object o =  preconList.get(i);
				
				if(o instanceof OperationReferenceType){
					pre = (OperationReferenceType)o;
				
					eventList.add(EVENT_STOP_PREFIX
							+ pre.getOperation()+
							  EVENT_MACHINE_SEPARATOR
							  +pre.getMachine());
				}
			}
			
			/*
			 * search for same event but relabeled
			 */
			String totalEvents = "";
			
			moduleEventList = module.getEvents();
			for(int i = 0; i < eventList.size(); i++){
				int ii = 1;
				
				String event = eventList.get(i);
				String tmpEvent = event.replace(EVENT_MACHINE_SEPARATOR,
						                        "_"+ii+EVENT_MACHINE_SEPARATOR);
				
				totalEvents = event;
				
				/* add relabeled event from module*/
				while(moduleEventList.contains(tmpEvent)){
					totalEvents = totalEvents.concat(";" + tmpEvent);
					ii = ii + 1;
					tmpEvent = event.replace(EVENT_MACHINE_SEPARATOR,
											 "_"+ii+EVENT_MACHINE_SEPARATOR);
				}
				
				/* replace */
				eventList.remove(i);
				eventList.add(i,totalEvents);
			}
			
			/*
			 * build one efa for every precondition
			 */
			String event, preconEFAname;
			for(int i = 0; i < eventList.size(); i++){
				event = eventList.get(i);
				preconEFAname = "precon"+Integer.toString(i+1)+"_"+activity.getOperation();
				
				preconEFA = new EFA(preconEFAname,module);
				
				
				preconEFA.addInitialState(PRECON_NOT_FULFILLED_STATE);
				preconEFA.addState(PRECON_FULFILLED_STATE);
				
				//transition to precondition fulfilled
				preconEFA.addTransition(PRECON_NOT_FULFILLED_STATE,
										PRECON_FULFILLED_STATE, event,"","");
				
				//self loop for not block event 
				preconEFA.addTransition(PRECON_FULFILLED_STATE,
										PRECON_FULFILLED_STATE, event,"","");
				
				/*
				//back to not fulfilled
				preconEFA.addTransition(PRECON_FULFILLED_STATE,
										PRECON_NOT_FULFILLED_STATE,
										EVENT_START_PREFIX+pga.getProcess(),"","");
				*/
				
				
				//self loop to precon fulfilled
				preconEFA.addTransition(PRECON_FULFILLED_STATE,
						PRECON_FULFILLED_STATE,
						EVENT_START_PREFIX+activity.getOperation(),"","");
				
				module.addAutomaton(preconEFA);
			}
			
		}
	}
	
	protected static String getEvent(String event){
		if(event.contains(EVENT_MACHINE_SEPARATOR)){
			return event.substring(0,event.indexOf(EVENT_MACHINE_SEPARATOR));
		}
		return event;
	}
	
	protected static String getMachine(String event){
		
		if(event.endsWith(EVENT_MACHINE_SEPARATOR)){
			return event.replace(EVENT_MACHINE_SEPARATOR,"");
		}
		
		if(event.contains(EVENT_MACHINE_SEPARATOR)){
			for(int i = 0; i < event.length(); i++){
				if(event.substring(i).startsWith(EVENT_MACHINE_SEPARATOR)){
					return event.substring(i+EVENT_MACHINE_SEPARATOR.length());
				}
				
			}
		}
		return event;
	}
	
	protected static void resetPreconList(){
		activityPreconList = new LinkedList<Activity>();
	}
	
	
	/**
	 * 
	 * @param activity
	 * @param efa
	 * @return
	 */
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
		
		//create transitions
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
}
