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
    commonSP = new HashSet<StatePair>();
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
    
    sameTransCheck();
    
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
	        //or it includs some exist sets
	        
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
	        /*
	        for (int i=0; i<composition.size();i++) {
	          //new candidate is a subset of another
	          if (composition.get(i).getAllAutomata().containsAll(comp)) {
	            composition.get(i).addLocalEvent(e);	            
	          } 
	          //it includs some exist sets	
	          else if (comp.containsAll(composition.get(i).getAllAutomata())) {
	            Set<EventProxy> temp = 
	            	new HashSet<EventProxy>(composition.get(i).getLocalEvents());
	            temp.add(e);
	            newCandidate.setLocalEvents(temp);
	          }
	        }
	        composition.add(newCandidate);*/
	        
	      }	else {
			      int i = composition.indexOf(newCandidate);
			      composition.get(i).addLocalEvent(e);	
	        }      
	    }
	    events.removeAll(dependedEvents);
	    
	    //System.out.println("Step "+loop+": "+composition.size()+" candidates.");
	    /*
	    for (int i=0;i<composition.size();i++) {
	      System.out.print(composition.get(i).getName()+",");
	    }
	    System.out.println();*/
	    
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
    Set<AutomatonProxy> checkAutomata = 
    	new HashSet<AutomatonProxy>(plants);
    checkAutomata.addAll(specs);
    
    for (AutomatonProxy aut : checkAutomata) {      
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
    ArrayList<EventRecord> erList = 
    	new ArrayList<EventRecord>();
    ArrayList<EventProxy> esList = 
      new ArrayList<EventProxy>();    
    Set<EventProxy> usedEvents = 
      new HashSet<EventProxy>();    
    Set<EventProxy> removableEvents = 
      new HashSet<EventProxy>();    
    ArrayList<EventProxy> replaceesList = 
      new ArrayList<EventProxy>();
    ArrayList<Collection<EventProxy>> replaceableesList =
      new ArrayList<Collection<EventProxy>>();
    Set<ArrayList<EventProxy>> ges = 
      new HashSet<ArrayList<EventProxy>>();
    Map<ArrayList<EventProxy>,AutomatonProxy> ases = 
      new HashMap<ArrayList<EventProxy>,AutomatonProxy>();
    Set<AutomatonProxy> alsAutomata = 
      new HashSet<AutomatonProxy>(); 
    Set<AutomatonProxy> newAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> modifiedAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> checkAutomata = 
    	new HashSet<AutomatonProxy>(plants);
    checkAutomata.addAll(specs);
    Set<EventProxy> forbiddenEvents = 
    	new HashSet<EventProxy>();    
    int same = 0;
    
    for (AutomatonProxy aut : checkAutomata) {
      //System.out.println(aut.getName());
      Set<EventProxy> autEvents = new HashSet<EventProxy>(aut.getEvents());
      Set<EventProxy> enabledEvents = new HashSet<EventProxy>(); 
      
      ArrayList<EventProxy> eList = new ArrayList<EventProxy>();
      ArrayList<Set<StatePair>> spList = new ArrayList<Set<StatePair>>();
      for (TransitionProxy tran : aut.getTransitions()) {
        EventProxy e = tran.getEvent();
        enabledEvents.add(e);
        if (eList.contains(e)) {          
          spList.get(eList.indexOf(e)).add(
            new StatePair(tran.getSource(),
                          tran.getTarget()));
        } else {
            eList.add(e);
            Set<StatePair> temp = 
              new HashSet<StatePair>();
            temp.add(new StatePair(tran.getSource(),
                                   tran.getTarget()));
            spList.add(temp);
          }
      }
      for (int i=0;i<eList.size();i++) {
        Map<AutomatonProxy,TransitionRecord> tempMap= 
          new HashMap<AutomatonProxy,TransitionRecord>();
        if (esList.contains(eList.get(i))) {
          tempMap.clear();
          tempMap.put(aut,new TransitionRecord(aut,spList.get(i)));
          erList.get(esList.indexOf(eList.get(i))).addMap(tempMap);        	
        } else {
            tempMap.clear();
            esList.add(eList.get(i));            
            tempMap.put(aut,new TransitionRecord(aut,spList.get(i)));
            EventRecord temp = new EventRecord(eList.get(i),tempMap);
            erList.add(temp);
          }
      }
      autEvents.removeAll(enabledEvents);
      forbiddenEvents.addAll(autEvents);
    }
    assert(esList.size() == erList.size());
    
    for (int i=0;i<esList.size();i++) {
      /*
      System.out.println(esList.get(i).getName()+": ####");
      for (TransitionRecord tr : transList.get(i)) {
        System.out.print(tr.getAut().getName()+"--");
        for (StatePair sp : tr.getTrans()) {
          System.out.println("    "+sp.getSource().getName()+"->"+sp.getTarget().getName());
        }
      }
      System.out.println();*/
      if (forbiddenEvents.contains(esList.get(i))) {
        erList.set(i,null);
      }
    }
    
    if (esList.size()<2) return;
    for (int i=0;i<esList.size()-1;i++) {
      //System.out.println("new turn");
      if (usedEvents.contains(esList.get(i))
        ||erList.get(i) == null
        ||erList.get(i).getMap().size()<2) continue;
      ArrayList<EventProxy> goodEvents = 
        new ArrayList<EventProxy>();
      ArrayList<EventProxy> almostsameEvents = 
        new ArrayList<EventProxy>();      
      for (int j=i+1;j<esList.size();j++) {        
        if (usedEvents.contains(esList.get(j))
          ||erList.get(j) == null
          ||erList.get(j).getMap().size()<2) continue;
        same = compareTrans(erList.get(i).getMap(),erList.get(j).getMap());
        if (same == 1) {
          //System.out.println("same: "+esList.get(j).getName());  
          usedEvents.add(esList.get(i));
          usedEvents.add(esList.get(j));
          if (!goodEvents.contains(esList.get(i))) goodEvents.add(esList.get(i));                    
          if (!goodEvents.contains(esList.get(j))) goodEvents.add(esList.get(j));
        } else if (same == -1) {
            //System.out.println("almost same: "+esList.get(i).getName()+" # "+esList.get(j).getName());
            alsAutomata.add(almostsameAut);
            //System.out.println("found one "+almostsameAut.getName());
            if (alsAutomata.size()>1) {
              alsAutomata.remove(almostsameAut);              
            } else {
		            usedEvents.add(esList.get(i));
		            usedEvents.add(esList.get(j));
		            if (!almostsameEvents.contains(esList.get(i))) almostsameEvents.add(esList.get(i));
		            if (!almostsameEvents.contains(esList.get(j))) almostsameEvents.add(esList.get(j));
              }
          }
      }
      if (goodEvents.size()>1) {        
        ges.add(new ArrayList<EventProxy>(goodEvents));
      }
      if (almostsameEvents.size()>1) {
        //System.out.println(almostsameAut.getName());
        AutomatonProxy temp = null;
        for (AutomatonProxy aut: alsAutomata) {
	        temp = aut;
	      }
        ases.put(new ArrayList<EventProxy>(almostsameEvents),temp);
      }          
    }
    
    if (ges.isEmpty() && ases.isEmpty()) return;
    
    System.out.println(ases.size()); 
    for (ArrayList<EventProxy> al : ases.keySet()) {
      System.out.println(ases.get(al).getName()+" ## "+al.size());
      for (EventProxy el : al) {
        System.out.print(el.getName()+",");
      }
      System.out.println();
    } 
    
    for (AutomatonProxy a : checkAutomata) {
      for (ArrayList<EventProxy> es : ges) {
        if (a.getEvents().containsAll(es)) {
          ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
          temp.remove(0);
          removableEvents.addAll(temp);          
        }
      }      
      
      for (ArrayList<EventProxy> es : ases.keySet()) {
        if (a == ases.get(es)) {
          if (a.getEvents().containsAll(es)) {
            ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
            EventProxy e = temp.get(0);
            temp.remove(e);
            replaceesList.add(e);
            replaceableesList.add(temp);            
          }	        
	      } else {
	          if (a.getEvents().containsAll(es)) {
		          ArrayList<EventProxy> temp = new ArrayList<EventProxy>(es);
		          temp.remove(0);
		          removableEvents.addAll(temp);          
		        }
		      }
		  }
		  
		  AutomatonProxy newaut1 = removeEvents(a,removableEvents);
      if (newaut1 != a) {    
	      newAutomata.add(newaut1);
	      modifiedAutomata.add(a);
      }
      removableEvents.clear();
		  
		  AutomatonProxy newaut2 = replaceEvents(newaut1,replaceesList,replaceableesList);
      if (newaut2 != newaut1) {
        newAutomata.remove(newaut1);
        newAutomata.add(newaut2);
        modifiedAutomata.add(a);
      }
      replaceesList.clear();
      replaceableesList.clear();
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
	                                     ArrayList<EventProxy> replaceesList,
	                                     ArrayList<Collection<EventProxy>> replaceableesList) {
	  
	  if (replaceesList.isEmpty()) return aut;
     Set<EventProxy> newEvents = new HashSet<EventProxy>(aut.getEvents()); 
     Set<TransitionProxy> newTrans = new HashSet<TransitionProxy>(aut.getTransitions());    
     for (Collection<EventProxy> es : replaceableesList) {      
       newEvents.removeAll(es);      
       for (EventProxy e : es) {        
	      for (TransitionProxy trans : aut.getTransitions()) {
	        if (trans.getEvent() == e) {
	          newTrans.remove(trans);
	          if (!commonSP.contains(new StatePair(trans.getSource(),trans.getTarget()))) {
		          TransitionProxy newTran = 
		            mFactory.createTransitionProxy(trans.getSource(),
															    replaceesList.get(replaceableesList.indexOf(es)),
															    trans.getTarget());
		          newTrans.add(newTran);
	          }
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
	private int compareTrans(Map<AutomatonProxy,TransitionRecord> trans1,
	                         Map<AutomatonProxy,TransitionRecord> trans2) {
	                         
	  //if (trans1.equals(trans2)) return 1;
	  int s1 = trans1.size();
	  int s2 = trans2.size();
	  if (s1<s2) {
	    return compareTrans(trans2,trans1);
	  } 
	  else if (s1>s2+1) {
	    return 0;
	  } 
	  else {
	    AutomatonProxy diffAut = null;
	    for (TransitionRecord tr : trans1.values()) {
	      if (!trans2.values().contains(tr)) {
	        if (diffAut == null) {
	          diffAut = tr.getAutomaton();
	        } else {
	            return 0;
	          }
	      }
	    }
	    if (diffAut == null) {
	      return 1;
	    } else {
	        if (!trans2.keySet().contains(diffAut)) {
	          almostsameAut = diffAut;
	          return -1;
	        } else {
	            Set<StatePair> t1 = 
	              new HashSet<StatePair>(trans1.get(diffAut).getTrans());
	            Set<StatePair> t2 = 
	              new HashSet<StatePair>(trans2.get(diffAut).getTrans());
	            Set<StatePair> t1t2 = new HashSet<StatePair>(t1);	            
		  			  t1t2.retainAll(t2);
		  			  commonSP.addAll(t1t2);
		  				t1.removeAll(t1t2);
		  				t2.removeAll(t1t2);
		  				for (StatePair sp1 : t1) {
		    		  	for (StatePair sp2 : t2) {
		              if (sp1.getSource()==sp2.getSource()) {
		                return 0;
		              }
		            }
		          }
		          almostsameAut = diffAut;
		          return -1;
	          }
	      }
	  }
	}
	/*
	private int compareTrans(ArrayList<TransitionRecord> trans1,
	                         ArrayList<TransitionRecord> trans2) {
	  int s1 = trans1.size();
	  int s2 = trans2.size();
	  if (s1<2 || s2<2) return 0;
	  if (trans1.equals(trans2)) return 1;
	  
	  ArrayList<TransitionRecord> commonTrans = 
	    new ArrayList<TransitionRecord>(trans1);
	  ArrayList<TransitionRecord> t1Temp = 
	    new ArrayList<TransitionRecord>(trans1);
	  ArrayList<TransitionRecord> t2Temp = 
	    new ArrayList<TransitionRecord>(trans2);
	  commonTrans.retainAll(trans2);
	  Set<AutomatonProxy> alsAutomata = 
	    new HashSet<AutomatonProxy>();
	  t1Temp.removeAll(commonTrans);
	  t2Temp.removeAll(commonTrans);
	  for (TransitionRecord tr : t1Temp) {
	    alsAutomata.add(tr.getAut());
	  }
	  for (TransitionRecord tr : t2Temp) {
	    alsAutomata.add(tr.getAut());
	  }  
	  
	  if (alsAutomata.size()==1) {
	    Set<StatePair> t1 = new HashSet<StatePair>();
	    Set<StatePair> t2 = new HashSet<StatePair>();	    
	    for (AutomatonProxy aut: alsAutomata) {
	      almostsameAut = aut;
	    }
	    for (TransitionRecord tr : t1Temp) {
	      if (tr.getAut() == almostsameAut) {
	        t1.addAll(tr.getTrans());
	      }
		  }
		  for (TransitionRecord tr : t2Temp) {
		    if (tr.getAut() == almostsameAut) {
	        t2.addAll(tr.getTrans());
	      }
		  }
		  Set<StatePair> t1t2 = new HashSet<StatePair>(t1);
		  t1t2.retainAll(t2);
		  t1.removeAll(t1t2);
		  t2.removeAll(t1t2);
		  for (StatePair sp1 : t1) {
		    for (StatePair sp2 : t2) {
		      if (sp1.getSource()==sp2.getSource()) {
		        return 0;
		      }
		    }
		  }
		  	    
	    return -1;
	  }
	  else return 0;	  
	}*/
  
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
  private Set<StatePair>             commonSP;      
}
