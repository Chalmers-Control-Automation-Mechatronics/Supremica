package org.supremica.external.processeditor.tools.dop2efa;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.Precondition;
import org.supremica.manufacturingTables.xsd.processeditor.Properties;

/*
 * Native function code. Base functions for DOPrelation class
 */
public class DOPnative {
	
	protected final static String EVENT_MACHINE_SEPARATOR = "::";
	
	protected final static String EVENT_STOP_PREFIX = "sto_";
	protected final static String EVENT_START_PREFIX = "sta_";
	
	protected final static String RUNNING_STATE_SUFFIX = "_run";
	
	protected final static String PRECON_NOT_FULFILLED_STATE = "precon_not_fulfilled";
	protected final static String PRECON_FULFILLED_STATE = "precon_fulfilled";
	
	protected static List<Activity> activityPreconList = new LinkedList<Activity>();
	
	protected final static int DEFAULT_RESOURCE_VALUE = 1;
	
	//---------------------------------------------------------------------------------------
	//Attribute types used by user
	//---------------------------------------------------------------------------------------
	protected final static String RESOURCE = "RESOURCE";
	
	
	//---------------------------------------------------------------------------------------
	//Attribute types used by algorithm
	//---------------------------------------------------------------------------------------
	protected final static String AND_STA_GUARD = "ANDSTARTGUARD";
	protected final static String OR_STA_GUARD = "ORSTARTGUARD";
	
	protected final static String AND_STO_GUARD = "ANDSTOPGUARD";
	protected final static String OR_STO_GUARD = "ORSTOPGUARD";
	
	protected final static String ADD_STA_ACTION = "ADDSTARTACTION";
	protected final static String ADD_STO_ACTION = "ADDSTOPACTION";
	
	protected final static String ONLY_STA = "ONLYSTART";
	protected final static String ONLY_STO = "ONLYSTOP";
	
	/**
	 * Build a basic Activity from one place to another.
	 * 
	 * @param activity
	 * @param from
	 * @param to
	 * @param efa
	 */
	protected static void nativeActivity(Activity activity,
										 String from,
										 String to,
										 EFA efa){

		String value = "";
		String runningState = "";

		EGA start = new EGA(); //stop event
		EGA stop = new EGA(); //start event

		boolean onlystart = false;
		boolean onlystop = false;

		// check in data
		if(activity == null || efa == null){
			return;
		}

		start.setEvent(EVENT_START_PREFIX + activity.getOperation());
		stop.setEvent(EVENT_STOP_PREFIX + activity.getOperation());

		Properties properties = activity.getProperties();
		if(properties != null){
			List<Attribute> attributeList = properties.getAttribute();

			//	Attributes
			for(Attribute att : attributeList){
				value = att.getAttributeValue();
				if(att.getType() != null){
					if(att.getType().equals(AND_STA_GUARD))
					{
						start.andGuard(value);
					}
					else if(att.getType().equals(OR_STA_GUARD))
					{
						start.orGuard(value);
					}
					else if(att.getType().equals(AND_STO_GUARD))
					{
						stop.andGuard(value);
					}
					else if(att.getType().equals(OR_STO_GUARD))
					{
						stop.orGuard(value);
					}
					else if(att.getType().equals(ADD_STA_ACTION))
					{
						start.addAction(value);
					}
					else if(att.getType().equals(ADD_STO_ACTION))
					{
						stop.addAction(value);
					}
					else if(att.getType().equals(ONLY_STA))
					{
						//only start event for this process
						onlystart = true;
					}
					else if(att.getType().equals(ONLY_STO))
					{
						//only stop event for this process
						onlystop = true;
					}
					else if(att.getType().equals(RESOURCE))
					{
						//Resources
						start = bookResource(start, efa.getModule(), att);
						stop = unBookResource(stop, efa.getModule(), att);
					}else{
						System.out.println("WARNING! unknown Attribute type "
								+ att.getType()+ 
								" in " + activity.getOperation());
					}
				}
			}// end for
		}


		//only start event
		if(onlystart){
			efa.addTransition(from, to, 
							  start.getEvent(),
							  start.getGuard(),
							  start.getAction());
			return;
		}


		//only stop event
		if(onlystop){
			efa.addTransition(from, to, 
					stop.getEvent(),
					stop.getGuard(),
					stop.getAction());
			return;
		}


		//------- Add running state ---------//
		runningState = activity.getOperation().concat(RUNNING_STATE_SUFFIX);

		if(efa.stateExist(runningState)){
			int i = 0;
			do{
				i = i + 1;
			}while(efa.stateExist(runningState.concat(Integer.toString(i))));

			runningState = runningState.concat(Integer.toString(i));
		}

		efa.addState(runningState);
		// ---------- End add running state ------------ //

		//add transition
		efa.addTransition(from, runningState,
						  start.getEvent(),
						  start.getGuard(),
						  start.getAction());

		efa.addTransition(runningState, to, 
					      stop.getEvent(),
					      stop.getGuard(),
					      stop.getAction());


		//Predecessors
		if(activity.getPrecondition() != null){
			if(!activityPreconList.contains(activity)){
				activityPreconList.add(activity);
			}
		}
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
	
	
	/**
	 * Create precondition
	 * @param module
	 */
	protected static void addPrecondition(Module module){
		
		EFA preconEFA = null;
		Precondition precon = null;
		
		List<OperationReferenceType> preconList = null;
		List<String> eventList = null;
		List<String> moduleEventList = null;
		
		if(activityPreconList == null ||
		   activityPreconList.size() == 0){
			return;
		}
		
		for(Activity activity : activityPreconList){
			
			//get predecessors
			precon =  activity.getPrecondition();
			if(precon != null){
				preconList = precon.getPredecessor();
			}
			
			
			if(preconList != null && preconList.size() != 0){
			
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
				
				//search for same event but relabeled
				String totalEvents = "";
			
				moduleEventList = module.getEvents();
				for(int i = 0; i < eventList.size(); i++){
					int ii = 1;
				
					String event = eventList.get(i);
					String tmpEvent = event.replace(EVENT_MACHINE_SEPARATOR,
						                        "_"+ii+EVENT_MACHINE_SEPARATOR);
				
					totalEvents = event;
				
					/* add relabeled event from module */
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
			
				//build one efa for every precondition
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
	}
	
	/**
	 * 
	 * @param event with event name and machine name separated by ::
	 * @return name of event without machine name
	 */
	protected static String getEvent(String event){
		if(event.contains(EVENT_MACHINE_SEPARATOR)){
			return event.substring(0,event.indexOf(EVENT_MACHINE_SEPARATOR));
		}
		return event;
	}
	
	/**
	 * 
	 * @param event with event name and machine name separated by :: 
	 * @return machine name from event
	 */
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
	
	/**
	 *  Resets the precondition list. 
	 */
	protected static void resetPreconList(){
		activityPreconList = new LinkedList<Activity>();
	}
	
	private static EGA bookResource(EGA ega, Module module, Attribute att){
		
		String resourceName = att.getAttributeValue();

		module.newResourceInteger(resourceName, DEFAULT_RESOURCE_VALUE);
		
		//Upper indicator books the resource
		if(att.getUpperIndicator() == null ||
		   !att.getUpperIndicator().isIndicatorValue()){
			return ega;
		}
		
		ega.andGuard(resourceName + ">0"); //test if resource free
		ega.addAction(resourceName + "-=1"); // book the resource
		
		return ega;
	}
	
	private static EGA unBookResource(EGA ega, Module module, Attribute att){
		
		String resourceName = att.getAttributeValue();
		module.newResourceInteger(resourceName, DEFAULT_RESOURCE_VALUE);
		
		//if lower indicator is true
		//resource is unbooked
		if(att.getLowerIndicator() == null ||
		   !att.getLowerIndicator().isIndicatorValue())
		{
			return ega; 
		}
		
		ega.andGuard(resourceName + "<" + module.getMaxValueResourceInteger(resourceName));
		ega.addAction(resourceName + "+=1");
		
		return ega;
	}
}
