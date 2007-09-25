package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.util.Iterator;
import java.util.List;

import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;

//
//	Native function code. Help function for DOPrelation class
//
public class DOPnative {
	
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
	
	/**
	 * 
	 * @param pgaList
	 * @param from
	 * @param to
	 * @param efa
	 */
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
}
