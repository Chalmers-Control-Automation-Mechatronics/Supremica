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
	    if (name==null) {
	      name = a.getName();	      
	    }else name += a.getName();
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

	public void setLocalEvents(Set<EventProxy> e){
		localEvents = e;
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
	
	public boolean equals(Object obj){
		if(obj instanceof Candidate){
			return cAutomata.equals((Candidate)obj);
		}
		return false;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	private Set<AutomatonProxy> cAutomata;
	private Set<EventProxy> cEvents;
	private Set<EventProxy> localEvents;
	private String name;
}
