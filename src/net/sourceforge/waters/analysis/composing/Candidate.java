package net.sourceforge.waters.analysis.composing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.lang.Object;
import java.lang.String;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class Candidate extends Object{
	
	public Candidate(){
	}
	
	public Candidate(Set<AutomatonProxy> automata,Set<EventProxy> events){
		cAutomata = automata;
		localEvents = events;
		cEvents = new HashSet<EventProxy>();
		name = new ArrayList<String>();		
	  for (AutomatonProxy a : cAutomata) {	       
	    if (name.isEmpty()) {
	      spsNumber = a.getStates().size();	      
	    } else {	         
	        spsNumber = spsNumber*a.getStates().size();
	      }
	    name.add(a.getName());
	    cEvents.addAll(a.getEvents());	    
	  }	
	  Collections.sort(name);	
	}
	
	public Set<AutomatonProxy> getAllAutomata(){
		return cAutomata;
	}

	public void setLocalEvents(Set<EventProxy> es){
		localEvents = es;
	}
	
	public void addLocalEvent(EventProxy e){
		localEvents.add(e);
	}
	
	public Set<EventProxy> getLocalEvents(){		
		return localEvents;
	}
	
	public Set<EventProxy> getAllEvents(){	
		return cEvents;
	}
	
	public String getName() {	  
	  String newName = "";
	  for(int i=0;i<name.size();i++) {
	    newName += name.get(i);
	  }
	  return newName;
	}
	
	public double getLocalProportion() {
	  return (localEvents.size())/(cEvents.size());
	}
	
	public double getSPSNumber() {	  
	  return spsNumber*(cEvents.size()-localEvents.size())/cEvents.size();
	}
	
	public double getSPTNumber() {
	  double sptNumber = 0;
	  Set<EventProxy> es = new HashSet<EventProxy>(cEvents);
	  Set<EventProxy> pevents = new HashSet<EventProxy>();
	  //remove all hidden events
	  es.removeAll(localEvents);
	  
	  for (EventProxy e : es) {
	    if(e.getKind()==EventKind.PROPOSITION) {
	        pevents.add(e);	        
	    }
	  }
	  //remove all proposition events
	  es.removeAll(pevents);
	  
	  if (es.isEmpty()) return 0;
	  Map<EventProxy,Integer> eventStates = 
	    new HashMap<EventProxy,Integer>(es.size());
	  ArrayList<Map<EventProxy,Integer>> esList = 
	    new ArrayList<Map<EventProxy,Integer>>(cAutomata.size());
	  for (AutomatonProxy aut : cAutomata) {	      
	    for (TransitionProxy trans : aut.getTransitions()) {
	      EventProxy etemp = trans.getEvent();
	      if (!es.contains(etemp)) continue;	      
        if (eventStates.get(etemp)!=null) {
          eventStates.put(etemp,eventStates.get(etemp)+1);
        } else {
            eventStates.put(etemp,1);
          }      
	    }
	    int size = aut.getStates().size();
	    for (EventProxy e : es) {	      
	      if (!aut.getEvents().contains(e)) {
	        eventStates.put(e,size);
	      }
	    }
	    esList.add(eventStates);	    
	  }	  
	  for (EventProxy e : es) {	    
	    double tNumber = 1;
	    for (int i=0; i<cAutomata.size(); i++) {	      
	      if (esList.get(i).get(e)!=null) {	        
	        tNumber *= esList.get(i).get(e);
	      }
	    }
	    sptNumber +=tNumber;
	  }
	  //System.out.println(sptNumber);
	  return sptNumber;
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
	private ArrayList<String> name;
	private double spsNumber;
}
