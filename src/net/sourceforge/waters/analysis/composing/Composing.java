package net.sourceforge.waters.analysis.composing;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.analysis.modular.Projection2;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;

public class Composing {

  //#########################################################################
  //# Constructors
  public Composing(final ProductDESProxy        model,
                   final KindTranslator         translator,
                   final ProductDESProxyFactory factory) {
    mModel = model;
    mFactory = factory;
    mTranslator = translator;
    mEvents = new HashSet<EventProxy>(mModel.getEvents());
    events  = new ArrayList<EventProxy>(mEvents);
    newAutomata = new HashSet<AutomatonProxy>();
    newEvents = new HashSet<EventProxy>();
    mCandidate = new ArrayList<Candidate>();
    badCandidate = new HashSet<Candidate>();
    nodelimit = 3000;
    plants = new HashSet<AutomatonProxy>();    
  }
  
  public ProductDESProxy run() throws AnalysisException {    

    final Set<AutomatonProxy>  specs  = new HashSet<AutomatonProxy>();  
    
    for (AutomatonProxy automaton : mModel.getAutomata()) {      
      //Retain all events which are not mentioned in specs. This algorithm
      //only consider the events contained in the plants not in the specs.
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(selfloopCheck(automaton));
                      break;
        case SPEC  :  specs.add(selfloopCheck(automaton));
                      events.removeAll(automaton.getEvents());                      
                      break;
        default : break;
      }
    }    
    hiddenEvents = new ArrayList<EventProxy>(events);
    //Case: no plant  
    if (plants.isEmpty()) return mModel;
    //Case: no removable events  
    if (hiddenEvents.isEmpty()) return mModel;
    
    //Assumption: All events which are not related with specs will be removed.    
    int loop = 0; 
    //for (int k=0;k<hiddenEvents.size();k++) {  
    while (true) {        
      loop++;
      ArrayList<Candidate> composition = new ArrayList<Candidate>();
      Set<EventProxy> dependedEvents = new HashSet<EventProxy>();        
		  //Step 1
	    //mustL: A set of Automata using the particular event.	       
	    for (EventProxy e : events) {
	      if(e.getKind()==EventKind.PROPOSITION) {
	        dependedEvents.add(e);
	        hiddenEvents.remove(e);
	        continue;
	      }
	      Set<AutomatonProxy> comp = new HashSet<AutomatonProxy>();
	      Set<EventProxy> eventHidden = new HashSet<EventProxy>();	      
	      for (AutomatonProxy aut : plants) {
	        if (aut.getEvents().contains(e)) {
	          comp.add(aut);	          
	        }
	      }	
	      if (comp.size()==0) {
	        dependedEvents.add(e);
	        hiddenEvents.remove(e);
	        continue;
	      }
	      eventHidden.add(e);
	      Candidate newCandidate = new Candidate(comp, eventHidden);	      
	      //kick the bad candidate out
	      if (badCandidate.contains(newCandidate)) continue;
	      	      
	      if (!composition.contains(newCandidate)) {      
	        //Check if the new candidate is a subset of another
	        //or it include some exist sets	        
	        for (int i=0; i<composition.size();i++) {
	          //new candidate is a subset of another
	          if (composition.get(i).getAllAutomata().containsAll(comp)) {
	            composition.get(i).addLocalEvent(e);
	          } 
	          //it includs some exist sets	
	          else if (comp.containsAll(composition.get(i).getAllAutomata())) {
	            eventHidden.addAll(composition.get(i).getLocalEvents());
	            newCandidate.setLocalEvents(eventHidden);
	          }
	        }
	        composition.add(newCandidate);
	      }	else {
			      int i = composition.indexOf(newCandidate);
			      composition.get(i).addLocalEvent(e);	
	        }      
	    }
	    events.removeAll(dependedEvents);
	    
	    //Step 2
	    //###############################################################
	    //maxL: Choose the candidate with the highest proportion of 
	    //      local events(that can be hidden).
	    //composition = maxL(composition);
	    
	    //###############################################################	       
			//minS: Choose the candidate with the lest synchronized product
			//      states
			composition = minS(composition);
			
			//call projecter
			boolean projectOK = true;
			for(int i=0;i<composition.size();i++) {			  
			  Candidate maxL = composition.get(i);			  
			  try {
			    project(maxL);
			    projectOK = true;
			    break;			    
			  } catch (final OverflowException oe) {
			      projectOK = false;
			      badCandidate.add(maxL);
	          continue;
			  }
      }
      
      if (!projectOK) {
        if (loop==1) {                           
          return mModel;
        }
        else break;
      }
      if (events.isEmpty()) break;           
		}
		
		//Create new model
		
		//remove all events which supposed to be hidden but not.
		hiddenEvents.removeAll(events);
	  newAutomata.addAll(plants);
    newAutomata.addAll(specs);

    newEvents.addAll(mEvents);
    newEvents.removeAll(hiddenEvents);   

    newModel = mFactory.createProductDESProxy("composedModel", newEvents, newAutomata);    
    return newModel;
  }
  
  public Collection<Candidate> getCandidates() {
    return mCandidate;
  }
  
  public void setNodeLimit(final int limit) {    
    if (limit > 3000) {
      nodelimit = 3000;
    } else {
      nodelimit = limit;
    }
  }
  
  private void project (Candidate can) throws AnalysisException {
    ProductDESProxy newP = 
		            mFactory.createProductDESProxy(can.getName(),can.getAllEvents(),can.getAllAutomata());
    Set<EventProxy> eForbidden = new HashSet<EventProxy>();    
		Projection2 proj = new Projection2(newP, mFactory, can.getLocalEvents(), eForbidden);
		//setNodeLimit(can.getStateNumber()*2);	
		//System.out.println(can.getStateNumber());        
	  proj.setNodeLimit(nodelimit);
	  AutomatonProxy newAutomaton = proj.project();
	  newAutomaton=selfloopCheck(newAutomaton);
	  
	  mCandidate.add(can);
                       
	  plants.removeAll((HashSet)can.getAllAutomata());
	  plants.add(newAutomaton);

	  events.removeAll((HashSet)can.getLocalEvents());
  }
  
  //remove the selfloop events which occur at all states
  private AutomatonProxy selfloopCheck(AutomatonProxy aut) {
    Map<EventProxy,Set<StateProxy>> selfloopStates = new HashMap<EventProxy,Set<StateProxy>>();
    Set<EventProxy> newEvents = new HashSet<EventProxy>(aut.getEvents()); 
    Set<TransitionProxy> newTrans = new HashSet<TransitionProxy>(aut.getTransitions());
    for (TransitionProxy trans : aut.getTransitions()) {
      EventProxy e = trans.getEvent();
      if (trans.getSource() == trans.getTarget()) {
        if (selfloopStates.get(e)!=null) {
          selfloopStates.get(e).add(trans.getSource());
        }
      } else {
          selfloopStates.put(e,null);
        }
    }
    for (EventProxy e : aut.getEvents()) {
      if (selfloopStates.get(e)!=null) {
        //not suited for nondeterminstic system
	      if (selfloopStates.get(e).size() == aut.getStates().size()) {
	        //remove e
	        newEvents.remove(e);
	        //remove trans labelled with e
	        for (TransitionProxy trans : aut.getTransitions()) {
	          if (trans.getEvent() == e) {
	           newTrans.remove(trans);
	          }
	        }
	      }
      }
    }
    if (newEvents.size()==aut.getEvents().size()){
      return aut;
    } else {
      return mFactory.createAutomatonProxy(aut.getName(),
                                           aut.getKind(),
                                           newEvents,
                                           aut.getStates(),
                                           newTrans);
    }
  }
  
  private ArrayList<Candidate> maxL(ArrayList<Candidate> composition) {
	  //Sort the composition list
    if (composition.size()>1) {
	    for (int i=0; i<composition.size()-1; i++) {
	      for (int j=i+1;j<composition.size();j++) {
	        if (composition.get(i).getLocalProportion() < composition.get(j).getLocalProportion()) {
	          Candidate ctemp = composition.get(i);
	          composition.set(i,composition.get(j));
	          composition.set(j,ctemp);
	        }
	      }
	    }
    }
    return composition;
  }
  
  private ArrayList<Candidate> minS(ArrayList<Candidate> composition) {
	  //Sort the composition list
    if (composition.size()>1) {
	    for (int i=0; i<composition.size()-1; i++) {
	      for (int j=i+1;j<composition.size();j++) {
	        if (composition.get(i).getSPSNumber() > composition.get(j).getSPSNumber()) {
	          Candidate ctemp = composition.get(i);
	          composition.set(i,composition.get(j));
	          composition.set(j,ctemp);
	        }
	      }
	    }
    }
    return composition;
  }
  
  private ProductDESProxy            mModel;
  private ProductDESProxyFactory     mFactory;
  private Set<EventProxy>            mEvents;
  private KindTranslator             mTranslator; 
  private Collection<EventProxy>     events;
  private Collection<EventProxy>     hiddenEvents;
  private ProductDESProxy            newModel;
  private Set<AutomatonProxy>        newAutomata;
  private Set<EventProxy>            newEvents;
  private Collection<Candidate>      mCandidate;
  private Set<Candidate>             badCandidate; 
  private int                        nodelimit;  
  private Set<AutomatonProxy>        plants;        
}
