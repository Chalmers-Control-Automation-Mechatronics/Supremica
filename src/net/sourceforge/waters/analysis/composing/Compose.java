package net.sourceforge.waters.analysis.composing;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.analysis.modular.Projection2;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

public class Compose {

  //#########################################################################
  //# Constructors
  public Compose(final ProductDESProxy        model,
                 final KindTranslator         translator,
                 final ProductDESProxyFactory factory) {
    mModel = model;
    mFactory = factory;
    mTranslator = translator;
    mEvents = new HashSet<EventProxy>(mModel.getEvents());
    events  = new ArrayList<EventProxy>();
    newAutomata = new HashSet<AutomatonProxy>();
    newEvents = new HashSet<EventProxy>();
  }
  
  public ProductDESProxy run() throws AnalysisException {
  
    Set<AutomatonProxy>  plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy>  specs  = new HashSet<AutomatonProxy>();
    Map<String, Candidate> composition = new HashMap<String, Candidate>();
    
    for (AutomatonProxy automaton : mModel.getAutomata()) {      
      //Retain all events which are not mentioned in specs. This algorithm
      //only consider the events contained in the plants not in the specs.
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
                      mEvents.removeAll(automaton.getEvents());                      
                      break;
        default : break;
      }
    }
    events.addAll(mEvents);
    //Case: no plant  
    if (plants.isEmpty()) return mModel;
    //Case: no removable events  
    if (mEvents.isEmpty()) return mModel;
    
    //Assumption: All events which are not related with specs will be removed.
    //PS: (This might not be right, just for temporary use.)
    //for (int i=0;i<mEvents.size();i++) {
    while (true) {          
		  //Step 1
	    //mustL: A set of Automata using the particular event.	    
	    for (EventProxy e : events) {
	      Set<AutomatonProxy> comp = new HashSet<AutomatonProxy>();
	      Set<EventProxy> eventHidden = new HashSet<EventProxy>();	      
	      for (AutomatonProxy aut : plants) {
	        if (aut.getEvents().contains(e)) {
	          comp.add(aut);	          
	        }
	      }	
	      eventHidden.add(e);
	      Candidate newCandidate = new Candidate(comp, eventHidden);	      
	      if (!composition.containsKey(newCandidate.getName())) {	        
	        composition.put(newCandidate.getName(),newCandidate);
	        continue;
	      }	
   
	      Set<EventProxy> eh = new HashSet<EventProxy>(composition.get(newCandidate.getName()).getLocalEvents());
	      eh.add(e);
	      newCandidate.setLocalEvents(eh);      
	      composition.put(newCandidate.getName(),newCandidate);
	    }
	    
	    //Step 2
	    //maxL: Choose the candidate with the highest proportion of 
	    //      local events(that can be hidden).
	    Candidate maxL = null;
	    double highestProportion = 0;
	    for (Candidate can : composition.values()) {
	      double numLocalEvents = 0;
	      double totalEvents  = 0;
	      
	      numLocalEvents = can.getLocalEvents().size();
	      totalEvents = can.getAllEvents().size();
	      
	      if (numLocalEvents/totalEvents>highestProportion) {
	        highestProportion = numLocalEvents/totalEvents;
	        maxL = can;
	      }
	    }
	    //System.out.println("555555555555555555555 ");
	    /*
	    //maxS: Choose the candicate with the highest proportion of 
	    //      common events(shared events).
	    ProductDESProxy maxS;
	    double maxs = 0;
	    for (ProductDESProxy pdpS : composition.keySet()) {
	      int common = 0;
	      int totalS  = 0;
	      totalS = pdpS.getEvents().size();
	      for (EventProxy e1 : pdpS.getEvents()) {
	        boolean isCommon = true;
	        for (AutomtonProxy a1 : pdpS.getAutomata()) {
	          if (!a1.getEvents().comtains(e1)) {
	            isCommon = false; 
	            break;
	          }
	        }
	        if (isCommon) common++;        
	      }
	      if (common/totalS>maxs) maxS = pdpS;
	    }
	    */
	    
			System.out.println("Get a new composing automaton "+maxL.getName());
	    //call projecter
	    ProductDESProxy newP = mFactory.createProductDESProxy(maxL.getName(),maxL.getAllEvents(),maxL.getAllAutomata());	
	    Set<EventProxy> eForbidden = new HashSet<EventProxy>();    
	    Projection2 proj = new Projection2(newP, mFactory, maxL.getLocalEvents(), eForbidden);	        
      proj.setNodeLimit(20000000);
      
      AutomatonProxy newAutomaton = proj.project();
      
      composition.remove(maxL.getName());      
      plants.removeAll((HashSet)maxL.getAllAutomata());
      plants.add(newAutomaton);

      events.removeAll((HashSet)maxL.getLocalEvents());

      if (events.isEmpty()) break;
      if (composition.isEmpty()) break;      
		}
		//Create new model
	  newAutomata.addAll(plants);
    newAutomata.addAll(specs);

    newEvents.addAll(mModel.getEvents());
    newEvents.removeAll(mEvents); 
   
          for (EventProxy e : newEvents) {
        System.out.println("new events "+e.getName());
      }
                for (EventProxy e : mEvents) {
        System.out.println("removed events "+e.getName());
      }
    newModel = mFactory.createProductDESProxy(mModel.getName(), newEvents, newAutomata);
    return newModel;
  }
  
  private ProductDESProxy        mModel;
  private ProductDESProxyFactory mFactory;
  private Set<EventProxy>        mEvents;
  private KindTranslator         mTranslator; 
  private Collection<EventProxy> events;
  private ProductDESProxy        newModel;
  private Set<AutomatonProxy>    newAutomata;
  private Set<EventProxy>        newEvents;
}
