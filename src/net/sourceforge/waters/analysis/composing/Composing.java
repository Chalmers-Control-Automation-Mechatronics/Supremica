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
    specs  = new HashSet<AutomatonProxy>(); 
    almostsameAut = null;  
  }
  
  public ProductDESProxy run() throws AnalysisException {    
   
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
	      if(mTranslator.getEventKind(e)==EventKind.PROPOSITION) {
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
	        boolean sub = false;	        
	        for (int i=0; i<composition.size();i++) {
	          //new candidate is a subset of another
	          if (composition.get(i).getAllAutomata().containsAll(comp)) {
	            composition.set(i,newCandidate);
	            sub = true;
	          } 
	          //it includs some exist sets	
	          else if (comp.containsAll(composition.get(i).getAllAutomata())) {
	            sub = true;
	          }
	        }
	        if (!sub) composition.add(newCandidate);
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
			//minS: Choose the candidate with the minimum synchronized product
			//      states
			composition = minS(composition);
			
		  //###############################################################	       
			//minT: Choose the candidate with the minimum synchronized product
			//      transitions
			//!!!!!useless for the converted model!!!!!
			//composition = minT(composition);
			
			//###############################################################	       
			//minCut: Choose the candidate with the minimum cut (graph theory)
			//        
			//composition = minCut(composition);
			
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
    nodelimit = limit;
  }
  
  private void project (Candidate can) throws AnalysisException {
    ProductDESProxy newP = 
		            mFactory.createProductDESProxy(can.getName(),can.getAllEvents(),can.getAllAutomata());
    Set<EventProxy> eForbidden = new HashSet<EventProxy>();    
		Projection2 proj = new Projection2(newP, mFactory, can.getLocalEvents(), eForbidden);		       
	  proj.setNodeLimit(nodelimit);
	  AutomatonProxy newAutomaton = proj.project();
	  //System.out.println(newAutomaton.getName()+" has "+newAutomaton.getTransitions().size()+" transitions!");
	  newAutomaton=selfloopCheck(newAutomaton);
	  
	  mCandidate.add(can);
                       
	  plants.removeAll((HashSet)can.getAllAutomata());
	  plants.add(newAutomaton);
	  
	  moreSelfloopCheck();
	  sameTransCheck();

	  events.removeAll((HashSet)can.getLocalEvents());
  }
  
  //remove the selfloop events which occur at all states
  private AutomatonProxy selfloopCheck(AutomatonProxy aut) {
    Map<EventProxy,Set<StateProxy>> selfloopStates = new HashMap<EventProxy,Set<StateProxy>>();
    Set<EventProxy> removableEvents = new HashSet<EventProxy>();    
    for (TransitionProxy trans : aut.getTransitions()) {
      EventProxy e = trans.getEvent();
      if (trans.getSource() == trans.getTarget()) {
        if (selfloopStates.containsKey(e)) {
	        if (selfloopStates.get(e)!=null) {
	          selfloopStates.get(e).add(trans.getSource());
	        }
	      } else {
	          Set<StateProxy> temp = new HashSet<StateProxy>();
	          temp.add(trans.getSource());
	          selfloopStates.put(e,temp);
	        }
      } else {
          selfloopStates.put(e,null);
        }
    }
    for (EventProxy e : aut.getEvents()) {
      if (selfloopStates.get(e)!=null) {
        //not suited for nondeterminstic system
	      if (selfloopStates.get(e).size() == aut.getStates().size()) {	        
	        //System.out.println(e.getName()+"  ##########"+aut.getName());
	        removableEvents.add(e);
	      }
      }
    }    
    return removeEvents(aut,removableEvents);
  }
  
  //If event e only ever is used as selfloops then
  //  If e is controllable OR e appears in plants only
  //       delete e
  //  End If
  //End If
  private void moreSelfloopCheck() {
    Map<EventProxy,Set<AutomatonProxy>> eventAutomata =
      new HashMap<EventProxy,Set<AutomatonProxy>>();
    Map<AutomatonProxy,Set<EventProxy>> automatonEvents =
      new HashMap<AutomatonProxy,Set<EventProxy>>();
    Set<EventProxy> specEvents = new HashSet<EventProxy>();
    
    for (AutomatonProxy aut : plants) {      
      for (TransitionProxy tran : aut.getTransitions()) {
        EventProxy e = tran.getEvent();
        if (tran.getSource() == tran.getTarget()) {
          if (eventAutomata.containsKey(e)) {
	          if (eventAutomata.get(e)!=null) {
	            eventAutomata.get(e).add(aut);
	          }
          } else {
              Set<AutomatonProxy> temp = new HashSet<AutomatonProxy>();
              temp.add(aut);
              eventAutomata.put(e,temp);
            }
        } else {
            eventAutomata.put(e,null);
          }
      }
    }
    for (AutomatonProxy aut : specs) {
      specEvents.addAll(aut.getEvents());
    }
    for (EventProxy e : eventAutomata.keySet()) {
      if (eventAutomata.get(e)!=null) {
        if (mTranslator.getEventKind(e)==EventKind.CONTROLLABLE
          ||!specEvents.contains(e)) {
          for (AutomatonProxy aut : eventAutomata.get(e)) {
	          if (automatonEvents.containsKey(aut)) {
	            automatonEvents.get(aut).add(e);
	          } else {
	              Set<EventProxy> temp = new HashSet<EventProxy>();
	              temp.add(e);
	              automatonEvents.put(aut,temp);
	            }
          }
        }
      }
    }
    for (AutomatonProxy aut : automatonEvents.keySet()) {
      AutomatonProxy newAut = removeEvents(aut,automatonEvents.get(aut));
      plants.remove(aut);
      plants.add(newAut);
    }
  }
  
  // If events a and b always appear on the same transitoin,
  // one of them can be removed
  private void sameTransCheck() {
    ArrayList<EventProxy> esList = 
      new ArrayList<EventProxy>();
    ArrayList<ArrayList<TransitionProxy>> transList =
      new ArrayList<ArrayList<TransitionProxy>>();
    Set<EventProxy> usedEvents = 
      new HashSet<EventProxy>();    
    Set<EventProxy> removableEvents = 
      new HashSet<EventProxy>();
    //Map<EventProxy,Collection<EventProxy>> replaceableEvents = 
      //new HashMap<EventProxy,Collection<EventProxy>>();
    ArrayList<EventProxy> replaceesList = 
      new ArrayList<EventProxy>();
    ArrayList<Collection<EventProxy>> replaceableesList =
      new ArrayList<Collection<EventProxy>>();
    Set<ArrayList<EventProxy>> ges = 
      new HashSet<ArrayList<EventProxy>>();
    Set<ArrayList<EventProxy>> ases = 
      new HashSet<ArrayList<EventProxy>>(); 
    Set<AutomatonProxy> newAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> modifiedAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> checkAutomata = 
    	new HashSet<AutomatonProxy>(plants);
    checkAutomata.addAll(specs);
    Set<EventProxy> forbiddenEvents = 
    	new HashSet<EventProxy>();
    Map<AutomatonProxy,Collection<TransitionProxy>> automatonTrans =
    	new HashMap<AutomatonProxy,Collection<TransitionProxy>>();
    Map<Collection<TransitionProxy>,AutomatonProxy> transAutomaton = 
    	new HashMap<Collection<TransitionProxy>,AutomatonProxy>();
    int same = 0;
    
    for (AutomatonProxy aut : checkAutomata) {
      Set<EventProxy> autEvents = new HashSet<EventProxy>(aut.getEvents());
      Set<EventProxy> enabledEvents = new HashSet<EventProxy>(); 
      automatonTrans.put(aut,aut.getTransitions());
      transAutomaton.put(aut.getTransitions(),aut);
      for (TransitionProxy tran : aut.getTransitions()) {
        EventProxy e = tran.getEvent();
        enabledEvents.add(e);
        if (esList.contains(e)) {
          transList.get(esList.indexOf(e)).add(tran);
        } else {
            esList.add(e);
            ArrayList<TransitionProxy> temp = 
              new ArrayList<TransitionProxy>();
            temp.add(tran);
            transList.add(temp);
          }
      }
      autEvents.removeAll(enabledEvents);
      forbiddenEvents.addAll(autEvents);
    }
    assert(esList.size() == transList.size());
    for (int i=0;i<esList.size();i++) {
      if (forbiddenEvents.contains(esList.get(i))) {
        transList.set(i,null);
      }
    }
    
    if (esList.size()<2) return;
    for (int i=0;i<esList.size()-1;i++) {
      if (usedEvents.contains(esList.get(i))
        ||transList.get(i) == null
        ||transList.get(i).size()<2) continue;
      ArrayList<EventProxy> goodEvents = 
        new ArrayList<EventProxy>();
      ArrayList<EventProxy> almostsameEvents = 
        new ArrayList<EventProxy>();      
      for (int j=i+1;j<esList.size();j++) {
        if (usedEvents.contains(esList.get(j))
          ||transList.get(j) == null
          ||transList.get(j).size()<2) continue;
        same = compareTrans(transList.get(i),transList.get(j),transAutomaton);
        if (same == 1) {
          //System.out.println("same: "+esList.get(j).getName());  
          usedEvents.add(esList.get(i));
          usedEvents.add(esList.get(j));
          if (!goodEvents.contains(esList.get(i))) goodEvents.add(esList.get(i));                    
          if (!goodEvents.contains(esList.get(j))) goodEvents.add(esList.get(j));
        } else if (same == -1) {
            System.out.println("almost same: "+esList.get(i).getName()+" # "+esList.get(j).getName());
            usedEvents.add(esList.get(i));
            usedEvents.add(esList.get(j));
            if (!almostsameEvents.contains(esList.get(i))) almostsameEvents.add(esList.get(i));
            if (!almostsameEvents.contains(esList.get(i))) almostsameEvents.add(esList.get(j));
          }
      }
      if (goodEvents.size()>1) {        
        ges.add(new ArrayList<EventProxy>(goodEvents));
      }
      if (almostsameEvents.size()>1) {
        ases.add(new ArrayList<EventProxy>(almostsameEvents));
      }      
    }
    if (ges.isEmpty() && ases.isEmpty()) return;
    for (AutomatonProxy a : checkAutomata) {
      for (ArrayList<EventProxy> es : ges) {
        if (a.getEvents().containsAll(es)) {
          ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
          temp.remove(0);
          removableEvents.addAll(temp);          
        }
      }
      AutomatonProxy newaut1 = removeEvents(a,removableEvents);
      if (newaut1 != a) {    
	      newAutomata.add(a);
	      modifiedAutomata.add(a);
      }
      removableEvents.clear();
      if (a == almostsameAut) {
        for (ArrayList<EventProxy> es : ases) {
          if (a.getEvents().containsAll(es)) {
            ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
            EventProxy e = temp.get(0);
            temp.remove(e);
            replaceesList.add(e);
            replaceableesList.add(temp);            
          }
        }
        AutomatonProxy newaut2 = replaceEvents(a,replaceesList,replaceableesList);
        if (newaut2 != a) {
          newAutomata.add(a);
	        modifiedAutomata.add(a);
	      }
	      replaceesList.clear();
	      replaceableesList.clear();
      } else {
          for (ArrayList<EventProxy> es : ges) {
		        if (a.getEvents().containsAll(es)) {
		          ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
		          temp.remove(0);
		          removableEvents.addAll(temp);          
		        }
		      }
		      AutomatonProxy newaut3 = removeEvents(a,removableEvents);
		      if (newaut3 != a) {    
			      newAutomata.add(a);
			      modifiedAutomata.add(a);
		      }
		      removableEvents.clear();
        }
    }
    plants.removeAll(modifiedAutomata);
    plants.addAll(newAutomata);
  }
  
  private AutomatonProxy removeEvents(AutomatonProxy aut, Set<EventProxy> re) {
    if (re.isEmpty()) return aut;
    Set<EventProxy> newEvents = new HashSet<EventProxy>(aut.getEvents()); 
    Set<TransitionProxy> newTrans = new HashSet<TransitionProxy>(aut.getTransitions());
    newEvents.removeAll(re);
    for (EventProxy e : re) {
      for (TransitionProxy trans : aut.getTransitions()) {
        if (trans.getEvent() == e) {
          newTrans.remove(trans);
        }
      }
	  }
	  return mFactory.createAutomatonProxy(aut.getName(),
	                                       aut.getKind(),
	                                       newEvents,
	                                       aut.getStates(),
	                                       newTrans);    
	}
	
	private AutomatonProxy replaceEvents(AutomatonProxy aut,
	                                     //Map<EventProxy,Collection<EventProxy>> rpes
	                                     ArrayList<EventProxy> replaceesList,
	                                     ArrayList<Collection<EventProxy>> replaceableesList) {
	  /*
	  for (EventProxy e1 : replaceesList) {
	    System.out.println(e1.getName()+" : ");
	    
	      for (EventProxy e2 : replaceableesList.get(replaceesList.indexOf(e1))) {
	        System.out.println("----"+e2.getName());
	      }
	    
	  }*/
	  if (replaceesList.isEmpty()) return aut;
    Set<EventProxy> newEvents = new HashSet<EventProxy>(aut.getEvents()); 
    Set<TransitionProxy> newTrans = new HashSet<TransitionProxy>(aut.getTransitions());    
    for (Collection<EventProxy> es : replaceableesList) {      
      newEvents.removeAll(es);      
      for (EventProxy e : es) {        
	      for (TransitionProxy trans : aut.getTransitions()) {
	        if (trans.getEvent() == e) {
	          newTrans.remove(trans);
	          TransitionProxy newTran = 
	            mFactory.createTransitionProxy(trans.getSource(),
														                 replaceesList.get(replaceableesList.indexOf(es)),
														                 trans.getTarget());
	          newTrans.add(newTran);
	        }
	      }
      }
	  }
	  return mFactory.createAutomatonProxy(aut.getName(),
	                                       aut.getKind(),
	                                       newEvents,
	                                       aut.getStates(),
	                                       newTrans);
	}
	
	//return 1 : same transition with different events
	//       0 : not same transiton
	//      -1 : almost same except one
	private int compareTrans(ArrayList<TransitionProxy> trans1,
	                         ArrayList<TransitionProxy> trans2,
	                         Map<Collection<TransitionProxy>,AutomatonProxy> tsa) {
	  int s1 = trans1.size();
	  int s2 = trans2.size();
	  int same = 0;
	  int notSame = 0;
	  boolean sameFlag = false;
	  boolean almostSame = false;
	  AutomatonProxy asAut = null;
	  almostsameAut = null;	  
	  if (s1 != s2 || s1<2 || s2<2) return 0;
	  for (int i=0;i<s1;i++) {
	    sameFlag = false;
	    for (int j=0;j<s2;j++) {
	      if (trans1.get(i).getSource() == trans2.get(j).getSource()
	        &&trans1.get(i).getTarget() == trans2.get(j).getTarget()) {
	        //check if they are in the same automaton
	        for (Collection<TransitionProxy> trans : tsa.keySet()) {
	          if (trans.contains(trans1.get(i)) 
	            &&trans.contains(trans2.get(j))) {
	            same++;
	            sameFlag = true;
	          }
	        }
	      }
	    }
	    if (!sameFlag) {
	      notSame++;
	      if (notSame>1) return 0;
	      for (Collection<TransitionProxy> trans : tsa.keySet()) {
          if (trans.contains(trans1.get(i))) {
            AutomatonProxy temp = tsa.get(trans);
            if (temp.getEvents().contains(trans2.get(i).getEvent())) {
              almostSame = true;
              asAut = temp;
            }
          }
        }
	    }
	  }
	  if (same == s1) return 1;
	  else if (almostSame) {
	    almostsameAut = asAut;
	    return -1;
	  }
	  else return 0;	  
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
	        } else if (composition.get(i).getLocalProportion() == composition.get(j).getLocalProportion()) {
              if (composition.get(i).getName().compareTo(composition.get(j).getName())>0) {
                Candidate ctemp = composition.get(i);
			          composition.set(i,composition.get(j));
			          composition.set(j,ctemp);
			        }
			      }
	      }
	    }
    }
    return composition;
  }
  
  private ArrayList<Candidate> minS(ArrayList<Candidate> composition) {
	  if (composition.size()>1) {
	    for (int i=0; i<composition.size()-1; i++) {
	      for (int j=i+1;j<composition.size();j++) {
	        if (composition.get(i).getSPSNumber() > composition.get(j).getSPSNumber()) {
	          Candidate ctemp = composition.get(i);
	          composition.set(i,composition.get(j));
	          composition.set(j,ctemp);
	        } else if (composition.get(i).getSPSNumber() == composition.get(j).getSPSNumber()) {
              if (composition.get(i).getName().compareTo(composition.get(j).getName())>0) {
                Candidate ctemp = composition.get(i);
			          composition.set(i,composition.get(j));
			          composition.set(j,ctemp);
			        }
			      }
	      }
	    }
    }
    return composition;
  }
  
  private ArrayList<Candidate> minT(ArrayList<Candidate> composition) {
	  if (composition.size()>1) {
	    for (int i=0; i<composition.size()-1; i++) {
	      for (int j=i+1;j<composition.size();j++) {
	        if (composition.get(i).getSPTNumber() > composition.get(j).getSPTNumber()) {
	          Candidate ctemp = composition.get(i);
	          composition.set(i,composition.get(j));
	          composition.set(j,ctemp);
	        } else if (composition.get(i).getSPTNumber() == composition.get(j).getSPTNumber()) {
              if (composition.get(i).getName().compareTo(composition.get(j).getName())>0) {
                Candidate ctemp = composition.get(i);
			          composition.set(i,composition.get(j));
			          composition.set(j,ctemp);
			        }
			      }
	      }
	    }
    }/*
    for (int i=0; i<composition.size();i++) {
      System.out.println(composition.get(i).getName()+" predict transition number: "+composition.get(i).getSPTNumber());
    }*/
    return composition;
  }
  
  private ArrayList<Candidate> minCut(ArrayList<Candidate> composition) {
    Set<AutomatonProxy> source = new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> target = new HashSet<AutomatonProxy>();
    int[] cn = new int[composition.size()];
    for (int i=0;i<composition.size();i++) {
      source.addAll(composition.get(i).getAllAutomata());
      target.addAll(plants);
      target.removeAll(source);
      cn[i] = getCutNumber(source,target);
      source.clear();
      target.clear();
    }
    for (int i=0;i<composition.size()-1;i++) {
      for (int j=i+1;j<composition.size();j++) {
        if (cn[i]>cn[j]) {
          Candidate ctemp = composition.get(i);
	        composition.set(i,composition.get(j));
	        composition.set(j,ctemp);
	        int cntemp = cn[i];
	        cn[i] = cn[j];
	        cn[j] =cntemp;
        } else if (cn[i]==cn[j]) {
            if (composition.get(i).getName().compareTo(composition.get(j).getName())>0) {
              Candidate ctemp = composition.get(i);
			        composition.set(i,composition.get(j));
			        composition.set(j,ctemp);
			        int cntemp = cn[i];
			        cn[i] = cn[j];
			        cn[j] =cntemp;
            }
          }
      }
    }
    return composition;
  }
  
  private int getCutNumber(Set<AutomatonProxy> source,
                           Set<AutomatonProxy> target) {
    int cutnumber = 0;
    Set<EventProxy> targetEvents = new HashSet<EventProxy>();     
    for (AutomatonProxy t : target) {
      targetEvents.addAll(t.getEvents());
    }
    for (AutomatonProxy s : source) {      
      for (EventProxy event : s.getEvents()){
        if (targetEvents.contains(event)) cutnumber++; 
      }      
    }    
  	return cutnumber;
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
  private Set<AutomatonProxy>        specs;
  private AutomatonProxy             almostsameAut;       
}
