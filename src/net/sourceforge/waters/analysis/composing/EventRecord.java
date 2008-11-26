package net.sourceforge.waters.analysis.composing;

import java.util.Map;
import java.util.HashMap;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class EventRecord {
	
	public EventRecord(EventProxy event,
	                   Map<AutomatonProxy,TransitionRecord> transmap){
		mEvent = event;
	   mMap = new HashMap<AutomatonProxy,TransitionRecord>(transmap);
	}
	
	public EventProxy getEvent() {
	  return mEvent;
	}
	
	public Map<AutomatonProxy,TransitionRecord> getMap() {
	  return mMap;
	}
	
	public void addMap(Map<AutomatonProxy,TransitionRecord> newMap) {
	  mMap.putAll(newMap);
	}
	
	public boolean equals(Object obj){
		if(obj != null && obj.getClass() == getClass()){
		  EventRecord temp = (EventRecord)obj;
		  if (mEvent == temp.getEvent()
		    &&mMap.equals(temp.getMap())) {
		    return true;
			} else return false;
		}else {
		   return false;
		 }
	}
	
	public int hashCode(){
		return mEvent.hashCode()+5*mMap.hashCode();
	}
	
	private EventProxy mEvent;
	private Map<AutomatonProxy,TransitionRecord> mMap;
}
