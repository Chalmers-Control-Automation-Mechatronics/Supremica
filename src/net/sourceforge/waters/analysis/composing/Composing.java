package net.sourceforge.waters.analysis.composing;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.ProxyAccessorHashMapByContents;
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
    mASTAutomata = new ArrayList<Set<ASTAutomaton>>();
    nodelimit = 3000;
    plants = new HashSet<AutomatonProxy>(); 
    specs  = new HashSet<AutomatonProxy>();
    mTotalNumberOfStates = 0;
  }
  
  public ProductDESProxy run() throws AnalysisException {    
    
    int projection_overflow = 0;
    for (AutomatonProxy automaton : mModel.getAutomata()) {      
      //Retain all events which are not mentioned in specs. This algorithm
      //only consider the events contained in the plants not in the specs.
      switch (mTranslator.getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);//selfloopCheck(automaton));
                      break;
        case SPEC  :  specs.add(automaton);//selfloopCheck(automaton));
                      events.removeAll(automaton.getEvents());                      
                      break;
        default : break;
      }
    }
   
    hiddenEvents = new ArrayList<EventProxy>(events);
    System.out.println("R evetns: "+(mEvents.size()-events.size()));    
    //Case: no plant  
    if (plants.isEmpty()) return mModel;
    //Case: no removable events  
    if (hiddenEvents.isEmpty()) return mModel;
    
    long timeTemp = System.currentTimeMillis();
    if(!sameTransCheck()){      
	    mASTAutomata.add(new HashSet<ASTAutomaton>());
	  }
	  simplificationTime += System.currentTimeMillis()-timeTemp;
    
    //Assumption: All events which are not related with specs will be removed.    
    int loop = 0;
    //Set<EventProxy> dependedEvents = new HashSet<EventProxy>();      
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
	      if (comp.isEmpty()) {
	        dependedEvents.add(e);	        
	        //hiddenEvents.remove(e);
	        continue;
	      }
	      eventHidden.add(e);
	      Candidate newCandidate = new Candidate(comp, eventHidden);	      
	      //kick the bad candidate out
	      if (badCandidate.contains(newCandidate)) continue;
	      	      
	      if (!composition.contains(newCandidate)) {      
	        //Check if the new candidate is a subset of another
	        //or it includs some exist sets	 
	        /*       
	        boolean sub = false;	        
	        for (int i=0; i<composition.size();i++) {
	          //new candidate is a subset of another
	          if (composition.get(i).getAllAutomata().containsAll(comp)) {
	            badCandidate.add(composition.get(i));
	            composition.set(i,newCandidate);	           
	            //System.out.println("----!!!!!!----"+newCandidate.getName());
	        		//System.out.println(newCandidate.getLocalEvents());
	            sub = true;	            
	          } 
	          //it includs some exist sets	
	          else if (comp.containsAll(composition.get(i).getAllAutomata())) {
	            badCandidate.add(newCandidate);
	            sub = true;	            
	          }
	        }	        
	        if (!sub) composition.add(newCandidate);*/
	        
	        Collection<Candidate> subCandidates = new ArrayList<Candidate>();
	        boolean sub = false;	        
	        for (int i=0; i<composition.size();i++) {
	          //new candidate is a subset of another
	          if (composition.get(i).getAllAutomata().containsAll(comp)) {
	            badCandidate.add(composition.get(i));
	            //composition.set(i,newCandidate);
	            subCandidates.add(composition.get(i));
	            //System.out.println("----!!!!!!----"+newCandidate.getName());
	        		//System.out.println(newCandidate.getLocalEvents());
	            sub = true;	            
	          } 
	          //it includs some exist sets	
	          else if (comp.containsAll(composition.get(i).getAllAutomata())) {
	            badCandidate.add(newCandidate);
	            sub = true;	            
	          }
	        }
	        if (sub & !subCandidates.isEmpty()) {
	          composition.removeAll(subCandidates);	
	          composition.add(newCandidate);          
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
	    /*
	    System.out.println("Step "+loop+": "+composition.size()+" candidates.");
	    for(int i=0;i<composition.size();i++) {	
			  Candidate can1 = composition.get(i);	
			  System.out.print(can1.getName()+"#########");
			  System.out.println(can1.getLocalEvents());
			}*/
	    
	    boolean projectOK = true;
	    if (mHeuristic == null) {
	      System.out.println("No heuristic selected!!!Default heuristic minS is used!!!");
	      mHeuristic = "minS";
			  composition = minS(composition);
				for(int i=0;i<composition.size();i++) {			  
				  Candidate candidate = composition.get(i);			  		  
				  try {
				    project(candidate);
				    projectOK = true;
				    break;			    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      projection_overflow++;
				      System.out.println("projection_overflow "+projection_overflow); 
		          continue;
				  }
	      }
	    }
	    //Step 2
	    //###############################################################
	    //maxL: Choose the candidate with the highest proportion of 
	    //      local events(that can be hidden).
	    else if (mHeuristic.equals("maxL")) {
	    	composition = maxL(composition);
	    	for(int i=0;i<composition.size();i++) {			  
				  Candidate candidate = composition.get(i);			  		  
				  try {
				    project(candidate);
				    projectOK = true;
				    break;			    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      projection_overflow++;
				      System.out.println("projection_overflow "+projection_overflow); 
		          continue;
				  }
	      }
	    }
	    
	    //###############################################################	       
			//minS: Choose the candidate with the minimum synchronized product
			//      states
			else if (mHeuristic.equals("minS")) {			  
				composition = minS(composition);
				//System.out.println("Candidates size: "+composition.size());				
				for(int i=0;i<composition.size();i++) {			  
				  Candidate candidate = composition.get(i);
				  //System.out.println("Candidates "+i+" :"+candidate.getName());
				  //System.out.println("Predicted State size: "+candidate.getSPSNumber());			  		  
				  try {
				    project(candidate);
				    projectOK = true;
				    break;				    			    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      projection_overflow++;
				      System.out.println("projection_overflow "+projection_overflow); 
		          continue;
				  }
	      }
		  }
			
		  //###############################################################	       
			//minT: Choose the candidate with the minimum synchronized product
			//      transitions
			//!!!!!useless for the converted model!!!!!
			else if (mHeuristic.equals("minT")) {
				composition = minT(composition);
				//System.out.println("Candidates size: "+composition.size());
				for(int i=0;i<composition.size();i++) {			  
				  Candidate candidate = composition.get(i);	
				  //System.out.println("Candidates "+i+" :"+candidate.getName());
				  //System.out.println("Predicted Transition size: "+candidate.getSPTNumber());		  		  
				  try {
				    project(candidate);
				    projectOK = true;
				    break;			    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      projection_overflow++;
				      System.out.println("projection_overflow "+projection_overflow); 
		          continue;
				  }
	      }
			}
			
			//###############################################################	       
			//minCut: Choose the candidate with the minimum cut (graph theory)
			// 
			else if (mHeuristic.equals("minCut")) { 		  
			        
				composition = minCut(composition);
				/*
				System.out.println("\nComposition Candidates$$$$$$$$$$$: "+composition.size());
				for(int i=0;i<composition.size();i++) {	
				  Candidate can2 = composition.get(i);	
				  System.out.print(can2.getName()+"|");
				  System.out.println(can2.getLocalEvents());
				}*/
				for(int i=0;i<composition.size();i++) {			  
				  Candidate candidate = composition.get(i);			  		  
				  try {
				    project(candidate);
				    projectOK = true;
				    break;			    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      projection_overflow++;
				      //System.out.println("\nprojection_overflow "+projection_overflow+
				      //                   " Candidate: "+candidate.getName()); 
		          continue;
				  }
	      }
		  }
			
			else if (mHeuristic.equals("getMinCut")) { 
			  Candidate candidate = new Candidate();			
				while(!composition.isEmpty()){
					candidate = getMinCut(composition);
					try {
				    project(candidate);			    
				    projectOK = true;	
				    break;	    		    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      composition.remove(candidate);
				      projection_overflow++;
				      //System.out.println("projection_overflow "+projection_overflow+
				      //                   " Candidate: "+candidate.getName());          
				  }
			  }
			}	
			
			else if (mHeuristic.equals("getMinS")) { 
			  Candidate candidate = new Candidate();			
				while(!composition.isEmpty()){
					candidate = getMinS(composition);					
					try {
				    project(candidate);			    
				    projectOK = true;	
				    break;	    		    
				  } catch (final OverflowException oe) {
				      projectOK = false;
				      badCandidate.add(candidate);
				      composition.remove(candidate);
				      projection_overflow++;
				      //System.out.println("projection_overflow "+projection_overflow);          
				  }
			  }
			}	else {
			  return mModel;
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
    //System.out.println(newEvents.size());
    newModel = mFactory.createProductDESProxy("composedModel", newEvents, newAutomata);    
    return newModel;
  }
  
  public long getSimplificationTime() {
    return simplificationTime;
  }
  
  public long getProjectionTime() {
    return projectionTime;
  }
  
  public int getTotalNumberOfStates() {
    return mTotalNumberOfStates;
  }
  
  public Collection<Candidate> getCandidates() {
    return mCandidate;
  }
  
  public Collection<Set<ASTAutomaton>> getASTAutomata() {
    return mASTAutomata;
  }
  
  public void setNodeLimit(final int limit) {    
    nodelimit = limit;
  }
  
  public void setHeuristic(final String heuristic) {
    mHeuristic = heuristic;
  }
  
  private void project (Candidate can) throws AnalysisException {   
    
    ProductDESProxy newP = 
	 	mFactory.createProductDESProxy(can.getName(),can.getAllEvents(),can.getAllAutomata());
    Set<EventProxy> eForbidden = new HashSet<EventProxy>();    
	  Projection2 proj = new Projection2(newP, mFactory, can.getLocalEvents(), eForbidden);		       
	  proj.setNodeLimit(nodelimit);
	  long timeTemp = System.currentTimeMillis();
	  AutomatonProxy newAutomaton = proj.project();	  
	  projectionTime += System.currentTimeMillis()-timeTemp;
	  //System.out.println("One Projection Time: "+projectionTime);
	  //System.out.println("Real State Size: "+newAutomaton.getStates().size());
	  //System.out.println("Real Transition Size: "+newAutomaton.getTransitions().size());
	  //System.out.println("Candidate's name: "+can.getName());
	  //System.out.println("New Automaton's name: "+newAutomaton.getName());
	  //System.out.println(newAutomaton.getName()+" has "+newAutomaton.getTransitions().size()+" transitions!");	  
    
	  mTotalNumberOfStates += newAutomaton.getStates().size();
	  //timeTemp = System.currentTimeMillis();
	  newAutomaton=selfloopCheck(newAutomaton);
	  //simplificationTime += System.currentTimeMillis()-timeTemp;
	   
	  mCandidate.add(can);
	            
	  //plants.removeAll((HashSet)can.getAllAutomata());	  
	  //plants.add(newAutomaton);
	  
	  //timeTemp = System.currentTimeMillis(); 
	  moreSelfloopCheck();
	  
	  timeTemp = System.currentTimeMillis();
	  if(!sameTransCheck()){	  	
	    mASTAutomata.add(new HashSet<ASTAutomaton>());
	  }
	  simplificationTime += System.currentTimeMillis()-timeTemp;

	  //events.removeAll((HashSet)can.getLocalEvents());	  
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
      if (plants.contains(aut)) {
      	plants.remove(aut);      	
      	plants.add(newAut);     	 
      } else {
        specs.remove(aut);
        specs.add(newAut);
      }
    }
  }
  
  // If events a and b always appear on the same transitoin,
  // one of them can be removed
  private boolean sameTransCheck() {
    ArrayList<EventRecord> erList = 
    	new ArrayList<EventRecord>();
    ArrayList<EventProxy> esList = 
      new ArrayList<EventProxy>();    
    Set<EventProxy> usedEvents = 
      new HashSet<EventProxy>();
    Set<AutomatonProxy> newAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> modifiedAutomata = 
      new HashSet<AutomatonProxy>();
    Set<AutomatonProxy> checkAutomata = 
    	new HashSet<AutomatonProxy>(plants);
    checkAutomata.addAll(specs);
    Set<EventProxy> forbiddenEvents = 
    	new HashSet<EventProxy>();
    Set<ASTAutomaton> astAutomata =
      new HashSet<ASTAutomaton>();   
    
    for (AutomatonProxy aut : checkAutomata) {
      
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
    
    List<EventRecord> forbiddenEventRecords
      = new ArrayList<EventRecord>();
    for (int i=0;i<esList.size();i++) {
      if (forbiddenEvents.contains(esList.get(i))) {
        forbiddenEventRecords.add(erList.get(i));
      }
    }
    erList.removeAll(forbiddenEventRecords);
    
    if (erList.size()<2) {    	
    	return false;
    }
    
    List<EventRecord> almostSameEventRecords =
    	new ArrayList<EventRecord>(erList.size());
    Set<EventProxy> eventsToBeRemoved =
      new HashSet<EventProxy>();
    Map<EventProxy,EventProxy> replacements =
      new HashMap<EventProxy,EventProxy>();
   	Collections.sort(erList);
   	for (EventRecord er1 : erList) {
   	  EventProxy event1 = er1.getEvent();
   	  Map<AutomatonProxy,TransitionRecord> map1= er1.getMap();
   	  if (usedEvents.contains(event1)
   	    ||map1.size()<2) continue;
   	  AutomatonProxy alsAutomaton = null;
   	  
   	  inner_loop:
   	  for (EventRecord er2 : erList) {
   	    if (er2 == er1) continue;
   	    EventProxy event2 = er2.getEvent();
   	  	Map<AutomatonProxy,TransitionRecord> map2= er2.getMap();
   	  	if (usedEvents.contains(event2)
   	  	  ||map2.size()<2) continue;
   	  	int same = compareTrans(map1,map2);
   	  	if (same == 1) {   	  	    	  	  
   	  	  usedEvents.add(event2);
   	  	  eventsToBeRemoved.add(event2);
   	  	} else if (same == -1) {   	  	  
   	  	  if (alsAutomaton == null) {
   	  	    if (determinismCheck(map1,map2,mAlmostSameAutomaton)) {
   	  	      alsAutomaton = mAlmostSameAutomaton;
   	  	      almostSameEventRecords.add(er1);
   	  	      almostSameEventRecords.add(er2);
   	  	      usedEvents.add(event1);
   	  	      usedEvents.add(event2);
   	  	    }
   	  	  } else if (alsAutomaton == mAlmostSameAutomaton)  {
   	  	    for (EventRecord er : almostSameEventRecords) {
   	  	      Map<AutomatonProxy,TransitionRecord> map= er.getMap();
   	  	      if (!determinismCheck(map,map2,alsAutomaton)) {
   	  	      	continue inner_loop;
   	  	      }
   	  	    }
   	  	    almostSameEventRecords.add(er2);
   	  	    usedEvents.add(event2);
   	  	  }
   	  	}
   	  }
   	  
   	  if (!almostSameEventRecords.isEmpty()) {
   	    EventRecord smallest = Collections.min(almostSameEventRecords);
   	    almostSameEventRecords.remove(smallest);
   	    Set<EventProxy> eventsToBeReplaced = 
   	      new HashSet<EventProxy>();
   	    for (EventRecord er : almostSameEventRecords) {
   	      replacements.put(er.getEvent(),smallest.getEvent());
   	      eventsToBeReplaced.add(er.getEvent());
   	    } 
   	    
   	    boolean astaContains = false;
   	    for (ASTAutomaton asta : astAutomata) {
   	      if (asta.getAutomaton() == alsAutomaton) {
   	      	astaContains = true;
   	        asta.getRevents().put(smallest.getEvent(),eventsToBeReplaced);
   	      }
   	    } 
   	    if (!astaContains) {
   	    	Map<EventProxy,Set<EventProxy>> revents =
   	        new HashMap<EventProxy,Set<EventProxy>>();
   	      revents.put(smallest.getEvent(),eventsToBeReplaced);
   	    	astAutomata.add(new ASTAutomaton(alsAutomaton,revents));
   	    }    
   	    almostSameEventRecords.clear();
   	  }
   	}
   	
    if (eventsToBeRemoved.isEmpty() && replacements.isEmpty()) {    	
    	return false;
    }
    
    for (AutomatonProxy a : checkAutomata) {
      Set<EventProxy> removableEvents =
        new HashSet<EventProxy>(a.getEvents());
      removableEvents.retainAll(eventsToBeRemoved);
      
		  AutomatonProxy newaut1 = removeEvents(a,removableEvents);
      if (newaut1 != a) {    
	      newAutomata.add(newaut1);
	      modifiedAutomata.add(a);
      }
      
      
		  Map<EventProxy,EventProxy> replaceableEvents =
        new HashMap<EventProxy,EventProxy>();
      for (EventProxy e : a.getEvents()) {
        if (replacements.get(e) != null) {
          replaceableEvents.put(e,replacements.get(e));          
        }
      }
   	  
		  AutomatonProxy newaut2 = replaceEvents(newaut1,replaceableEvents);
      if (newaut2 != newaut1) {
        newAutomata.remove(newaut1);
        newAutomata.add(newaut2);
        modifiedAutomata.add(a);
      }      
    }
    
    mASTAutomata.add(astAutomata);
    plants.removeAll(modifiedAutomata);
    plants.addAll(newAutomata);
    //works for the tests without model converted
    /*
    for (AutomatonProxy a : modifiedAutomata) {
      switch (mTranslator.getComponentKind(a)) {
        case PLANT :  plants.remove(a);
                      break;
        case SPEC  :  specs.remove(a);     
                      break;
        default : break;
      }
    }
    for (AutomatonProxy a : newAutomata) {
      switch (mTranslator.getComponentKind(a)) {
        case PLANT :  plants.add(a);
                      break;
        case SPEC  :  specs.add(a);     
                      break;
        default : break;
      }
    }*/
    return true;
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
	                                     Map<EventProxy,EventProxy> replacements) {
	  if (replacements.isEmpty()) return aut;
	  //Events:
	  Collection<EventProxy> oldevents = aut.getEvents();
	  Set<EventProxy> newevents = 
	  	new HashSet<EventProxy>(oldevents.size());
	  List<EventProxy> neweventlist = 
	  	new ArrayList<EventProxy>(oldevents.size());
	  for (EventProxy oldevent : oldevents) {
	    EventProxy newevent = replacements.get(oldevent);
	    if (newevent == null) {
	      newevent = oldevent;
	    }
	    if (newevents.add(newevent)) {
	      neweventlist.add(newevent);
	    }
	  }
	  //Transitions:
	  Collection<TransitionProxy> oldtrans = aut.getTransitions();
	  ProxyAccessorMap<TransitionProxy> newtrans = 
	  	new ProxyAccessorHashMapByContents<TransitionProxy>(oldtrans.size());
	  List<TransitionProxy> newtranlist = 
	  	new ArrayList<TransitionProxy>(oldtrans.size());
	  for (TransitionProxy oldtran : oldtrans) {
	    EventProxy oldevent = oldtran.getEvent();
	    EventProxy newevent = replacements.get(oldevent);
	    TransitionProxy newtran;
	    if (newevent == null) {
	      newtran = oldtran;
	    } else {
	      StateProxy source = oldtran.getSource();
	      StateProxy target = oldtran.getTarget();
	      newtran = mFactory.createTransitionProxy(source,newevent,target);
	    }
	    if (newtrans.addProxy(newtran)) {
	      newtranlist.add(newtran);
	    }
	  }	  
	  return mFactory.createAutomatonProxy(aut.getName(),
	                                       aut.getKind(),
	                                       neweventlist,
	                                       aut.getStates(),
	                                       newtranlist);
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
	  } else if (s1>s2+1) {
	    return 0;
	  } else {	  
	    AutomatonProxy diffAut = null;
	    for (TransitionRecord tr1 : trans1.values()) {
	      AutomatonProxy aut = tr1.getAutomaton();
	      TransitionRecord tr2 = trans2.get(aut);
	      if (tr2 == null) {
	        if (s1==s2) {
	          return 0;
	        } else if (diffAut != null) {
	          return 0;
	        } else {
	          diffAut = aut;
	        }
	      } else {
	        if (!tr1.equals(tr2)) {
	          if (diffAut != null) {
	            return 0;
	          } else {
	            diffAut = aut;
	          }
	        }
	      }
	    }
	    if (diffAut == null) {
	      return 1;
	    } else {
	    /*
	      System.out.println("trans1: ");
	      for (AutomatonProxy a : trans1.keySet()) {
	        System.out.println(a.getName());
	        for (StatePair sp : trans1.get(a).getTrans()) {
	          System.out.println("-- "+sp.getSource()+" --> "+sp.getTarget());
	        }
	      }
	      System.out.println("trans2: ");
	      for (AutomatonProxy a : trans2.keySet()) {
	        System.out.println(a.getName());
	        for (StatePair sp : trans2.get(a).getTrans()) {
	          System.out.println("-- "+sp.getSource()+" --> "+sp.getTarget());
	        }
	      }*/
	      
	      mAlmostSameAutomaton = diffAut;
	      return -1;
	    }
	  }
	}
	
	private boolean determinismCheck(Map<AutomatonProxy,TransitionRecord> map1,
	                                 Map<AutomatonProxy,TransitionRecord> map2,
	                                 AutomatonProxy alsAut) {
	  if (map1.get(alsAut) == null) return true;
	  for (StatePair sp1 : map1.get(alsAut).getTrans()) {
	    if (map2.get(alsAut) == null) return true;
	    for (StatePair sp2 : map2.get(alsAut).getTrans()) {
	      if (sp1.getSource() == sp2.getSource()
	        &&sp1.getTarget() != sp2.getTarget()) {
	        return false;
	      }
	    }
	  } 
	  return true;                          
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
  
  private Candidate getMinS(Collection<Candidate> composition) {
    Comparator<Candidate> comparator = new Comparator<Candidate>() {
	      public int compare(Candidate c1, Candidate c2) {	        
	        if (c1.getSPSNumber() < c2.getSPSNumber()) {
	          return -1;
	        } else if (c1.getSPSNumber() > c2.getSPSNumber()) {
	          return 1;
	        } else {
	          return c1.getName().compareTo(c2.getName());
	        }
	      }
	    };
    
    return Collections.min(composition,comparator);
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
    //System.out.println("New round: ");
    for (int i=0;i<composition.size();i++) {
      //System.out.print("\nCandidate"+i+1+": ");
      //for (AutomatonProxy aut : composition.get(i).getAllAutomata()) {
       // System.out.print(aut.getName()+";");
      //}
      
      source.addAll(composition.get(i).getAllAutomata());
      target.addAll(plants);
      target.removeAll(source);
      cn[i] = getCutNumber2(source,target);
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
  
  private Candidate getMinCut(Collection<Candidate> composition) {
    /*
    System.out.println("New round: ");
    int i = 0;
    for (Candidate can : composition) {
      i++;
      System.out.print("\nCandidate"+i+": ");
      for (AutomatonProxy aut : can.getAllAutomata()) {
        System.out.print(aut.getName()+";");
      }
    }*/
    Comparator<Candidate> comparator = new Comparator<Candidate>() {
	      public int compare(Candidate c1, Candidate c2) {
	        Set<AutomatonProxy> source1 = new HashSet<AutomatonProxy>(c1.getAllAutomata());
	        Set<AutomatonProxy> source2 = new HashSet<AutomatonProxy>(c2.getAllAutomata());
	        Set<AutomatonProxy> target1 = new HashSet<AutomatonProxy>();
	        Set<AutomatonProxy> target2 = new HashSet<AutomatonProxy>();
	        target1.addAll(plants);
	        target2.addAll(plants);
	        target1.removeAll(source1);
	        target2.removeAll(source2);
	        int cn1 = getCutNumber2(source1,target1);
	        int cn2 = getCutNumber2(source2,target2);
	        if (cn1 < cn2) {
	          return -1;
	        } else if (cn1 > cn2) {
	          return 1;
	        } else {
	          return c1.getName().compareTo(c2.getName());
	        }
	      }
	    };
    
    return Collections.min(composition,comparator);
  }
  
  /*
  private int getCutNumber1(Set<AutomatonProxy> source,
                            Set<AutomatonProxy> target) {
    int cutnumber = 0;
    Set<EventProxy> targetEvents = new HashSet<EventProxy>();
    Set<EventProxy> sourceEvents = new HashSet<EventProxy>();     
    for (AutomatonProxy t : target) {
      targetEvents.addAll(t.getEvents());
    }
    for (AutomatonProxy s : source) {
      sourceEvents.addAll(s.getEvents());
    }
    for (EventProxy event : sourceEvents) {      
      if (targetEvents.contains(event)) cutnumber++;            
    }    
  	return cutnumber;
  }
  */
  
  private int getCutNumber2(Set<AutomatonProxy> source,
                            Set<AutomatonProxy> target) {
    int cutnumber = 0;    
    for (AutomatonProxy s : source) {      
      for (EventProxy event : s.getEvents()){
        for (AutomatonProxy t : target) {
        	if (t.getEvents().contains(event)) cutnumber++; 
        }
      }      
    }    
  	return cutnumber;
  }
  
  private ProductDESProxy               mModel;
  private ProductDESProxyFactory        mFactory;
  private Set<EventProxy>               mEvents;
  private KindTranslator                mTranslator; 
  private Collection<EventProxy>        events;
  private Collection<EventProxy>        hiddenEvents;
  private ProductDESProxy               newModel;
  private Set<AutomatonProxy>           newAutomata;
  private Set<EventProxy>               newEvents;
  private Collection<Candidate>         mCandidate;
  private Collection<Set<ASTAutomaton>> mASTAutomata;
  private Set<Candidate>                badCandidate; 
  private int                           nodelimit; 
  private String                        mHeuristic; 
  private Set<AutomatonProxy>           plants; 
  private Set<AutomatonProxy>           specs;
  private AutomatonProxy                mAlmostSameAutomaton; 
  private int                           mTotalNumberOfStates;  
  private long                          simplificationTime;
  private long                          projectionTime;   
}
