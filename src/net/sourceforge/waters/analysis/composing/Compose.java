package net.sourceforge.waters.analysis.composing;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
//import java.util.HashMap;
//import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
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
    events  = new ArrayList<EventProxy>(mEvents);
    newAutomata = new HashSet<AutomatonProxy>();
    newEvents = new HashSet<EventProxy>();
  }
  
  public ProductDESProxy run() throws AnalysisException {
  
    Set<AutomatonProxy>  plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy>  specs  = new HashSet<AutomatonProxy>();    
    
    for (AutomatonProxy automaton : mModel.getAutomata()) {      
      //Retain all events which are not mentioned in specs. This algorithm
      //only consider the events contained in the plants not in the specs.
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  specs.add(automaton);
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
    //PS: (This might not be right, just for temporary use.)
    int loop = 0;
    //for (int j=0;j<hiddenEvents.size();j++) {
    while (true) {  
      loop++;
      ArrayList<Candidate> composition = new ArrayList<Candidate>();        
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
	      if (!composition.contains(newCandidate)) {	        
	        composition.add(newCandidate);
	        //continue;
	      }	else {
			      int i = composition.indexOf(newCandidate);
			      Set<EventProxy> eh = new HashSet<EventProxy>(composition.get(i).getLocalEvents());
			      eh.add(e);
			      composition.get(i).setLocalEvents(eh);	
	        }      
	    }
	    
	    //Step 2
	    //maxL: Choose the candidate with the highest proportion of 
	    //      local events(that can be hidden).
	    ArrayList<Double> proportion = new ArrayList<Double>(composition.size());
	    for (int i=0;i<composition.size();i++) {
	      double numLocalEvents = composition.get(i).getLocalEvents().size();
	      double totalEvents  = composition.get(i).getAllEvents().size();
	      proportion.add(numLocalEvents/totalEvents);
	    }
	    Double p[] = new Double[composition.size()];
	    p = proportion.toArray(p);
	    Arrays.sort(p);
    
			//Built an ordered array of proportion of local events
			//call projecter
			boolean projectOK = true;
			for(int i=composition.size()-1;i>=0;i--) {			  
			  Candidate maxL = composition.get(proportion.indexOf(p[i]));		    
		    ProductDESProxy newP = 
		            mFactory.createProductDESProxy(maxL.getName(),maxL.getAllEvents(),maxL.getAllAutomata());	
		    Set<EventProxy> eForbidden = new HashSet<EventProxy>();    
		    Projection2 proj = new Projection2(newP, mFactory, maxL.getLocalEvents(), eForbidden);	        
	      proj.setNodeLimit(10000);
	      try {
	        AutomatonProxy newAutomaton = proj.project();	        
          projectOK = true;	 
                       
	        plants.removeAll((HashSet)maxL.getAllAutomata());
	        plants.add(newAutomaton);

	        events.removeAll((HashSet)maxL.getLocalEvents());

	        break;
	      } catch (OverflowException oe) {
	          //try next candidate
	          projectOK = false;
	          continue;
	        }
      }
      if (!projectOK) {
        if (loop==1) return mModel;
        else break;
      }
      if (events.isEmpty()) break;           
		}
		//Create new model
	  newAutomata.addAll(plants);
    newAutomata.addAll(specs);

    newEvents.addAll(mEvents);
    newEvents.removeAll(hiddenEvents); 
    /*
          for (EventProxy e : newEvents) {
        System.out.println("new events "+e.getName());
      }
                for (EventProxy e : hiddenEvents) {
        System.out.println("removed events "+e.getName());
      }*/
    newModel = mFactory.createProductDESProxy(mModel.getName(), newEvents, newAutomata);
    return newModel;
  }
  
  private ProductDESProxy        mModel;
  private ProductDESProxyFactory mFactory;
  private Set<EventProxy>        mEvents;
  private KindTranslator         mTranslator; 
  private Collection<EventProxy> events;
  private Collection<EventProxy> hiddenEvents;
  private ProductDESProxy        newModel;
  private Set<AutomatonProxy>    newAutomata;
  private Set<EventProxy>        newEvents;
}
