package net.sourceforge.waters.analysis.composing;

import java.util.Set;
import java.util.HashSet;
import java.lang.Object;
import java.lang.String;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;

public class Candidate extends Object{
	
	public Candidate(){
	}
	
	public Candidate(Set<AutomatonProxy> automata,Set<EventProxy> events){
		cAutomata = automata;
		cEvents = new HashSet<EventProxy>();
		name = null;
	  for (AutomatonProxy a : cAutomata) {
	    cStateNumber +=a.getStates().size();
	    if (name==null) {
	      name = a.getName();	      
	    } else { name = name+","+a.getName(); }
	    for (EventProxy e : a.getEvents()) {	      
	      if (!cEvents.contains(e)) {
	        cEvents.add(e);	        
	      }
	    }
	  }	
		localEvents = events;
	}
	
	public Set<AutomatonProxy> getAllAutomata(){
		return cAutomata;
	}
	
	public int getStateNumber(){
	  return cStateNumber;
	}

	public void setLocalEvents(Set<EventProxy> es){
		localEvents = es;
	}
	
	public void addLocalEvents(EventProxy e){
		localEvents.add(e);
	}
	
	public Set<EventProxy> getLocalEvents(){		
		return localEvents;
	}
	
	public Set<EventProxy> getAllEvents(){	
		return cEvents;
	}
	
	public String getName() {
	  return name;
	}
	
	public double getLocalProportion() {
	  return (localEvents.size())/(cEvents.size());
	}
	
	public boolean equals(Object obj){
		if(obj != null && obj.getClass() == getClass()){
		  Candidate temp = (Candidate)obj;
			return cAutomata.equals(temp.getAllAutomata());
		}else {
		   return false;
		 }
	}
	
	public int hashCode(){
		return cAutomata.hashCode();
	}
	
	private Set<AutomatonProxy> cAutomata;
	private Set<EventProxy> cEvents;
	private Set<EventProxy> localEvents;
	private String name;
	private int cStateNumber;
}
