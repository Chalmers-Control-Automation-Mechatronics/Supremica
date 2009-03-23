//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ComposingSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.composing;

import java.lang.Comparable;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.io.File;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.analysis.bdd.BDDSafetyVerifier;
import net.sourceforge.waters.cpp.analysis.NativeSafetyVerifier;

import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class ComposingSafetyVerifier 
  extends AbstractModelVerifier
  implements SafetyVerifier {

  //#########################################################################
  //# Constructors
  public ComposingSafetyVerifier(final KindTranslator translator,
                                 final ProductDESProxyFactory factory)
  {
    this(null, translator, factory); 
  }

  public ComposingSafetyVerifier(final ProductDESProxy model,
                                 final KindTranslator translator,
                                 final ProductDESProxyFactory factory) {
    super(model, factory); 
    mTranslator = translator; 
    setNodeLimit(10000000);    
    setProjectionNodeLimit(1000);    
  }
  
  
  //#########################################################################
  //# Simple Access
  public SafetyTraceProxy getCounterExample() {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator() {
    return mTranslator;
  }

  public void setKindTranslator(KindTranslator trans) {
    mTranslator = trans;
  }

  public int getProjectionNodeLimit()
  {
    if (getNodeLimit() < mProjectionNodeLimit) {
      return getNodeLimit();
    } else {
      return mProjectionNodeLimit;
    }
  }

  public void setProjectionNodeLimit(final int limit)
  {
    mProjectionNodeLimit = limit;
  }
  
  public String getHeuristic() {
    return mHeuristic;
  }
  
  public void setHeuristic(final String heuristic) {
    mHeuristic = heuristic;
  }


  //#########################################################################
  //# Invocation
  public boolean run() throws AnalysisException {        
    final Composing composing =
      new Composing(getConvertedModel(),
		                getConvertedKindTranslator(),
		                getFactory());		                
		composing.setHeuristic(getHeuristic());
    composing.setNodeLimit(getProjectionNodeLimit());     
    ProductDESProxy des = composing.run(); 
    System.out.println("Composing is done!"); 
    
    saveIntoFile(des);
    
    ArrayList<Candidate> candidates = new ArrayList<Candidate>(composing.getCandidates());
    ArrayList<Set<ASTAutomaton>> astautomata = new ArrayList<Set<ASTAutomaton>>(composing.getASTAutomata());
    /*
    //Display the composing infomation
    System.out.println(candidates.size()+" candidates:");
    for (int i=0; i<candidates.size(); i++) {
      System.out.print("Step "+(i+1)+": ");
      System.out.println(candidates.get(i).getAllAutomata().size()+" automata");
      System.out.print("Hide "+candidates.get(i).getLocalEvents().size()+" events: ");
      for (EventProxy e : candidates.get(i).getLocalEvents()) {
        System.out.print(e.getName()+",");
      }
      System.out.println("\nAutomata: "+candidates.get(i).getName());
    }*/
    /*
    for (int i=0; i<astautomata.size(); i++) {
      System.out.println("Step "+(i+1)+": ");
      if (!astautomata.get(i).isEmpty()){
        for (ASTAutomaton a : astautomata.get(i)) {
        System.out.println("Automaton: "+a.getAutomaton().getName());
        Map<EventProxy,Set<EventProxy>> map = 
        	new HashMap<EventProxy,Set<EventProxy>>(a.getRevents());
        for (EventProxy e : map.keySet()) {
          System.out.println(e.getName()+" replaces:");
          for (EventProxy e2 : map.get(e)) {
            System.out.println(e2.getName()+", ");
          }
        }
        }
      } else {
      	System.out.println("no ASTAutomaton!!!");
      }      
    }*/
     
    final SafetyVerifier checker =
      //new NativeSafetyVerifier(des, getConvertedKindTranslator(),getFactory());
      new BDDSafetyVerifier(des, getConvertedKindTranslator(), getFactory());
    checker.setNodeLimit(getNodeLimit());        
    final boolean result = checker.run(); 
    //mStates = (int)checker.getAnalysisResult().getTotalNumberOfStates();
    mStates = composing.getTotalNumberOfStates();
    mNodes = (int)checker.getAnalysisResult().getPeakNumberOfNodes();    

    
    if (result) {     
      return setSatisfiedResult();
    } else {
      final String tracename = getModel().getName() + ":uncontrollable";
      final SafetyTraceProxy counterexample = checker.getCounterExample();
            
      List<EventProxy> composedTrace = new LinkedList<EventProxy>(counterexample.getEvents());
      /*
      System.out.println("old counter example: ");
		  for (int i=0; i<composedTrace.size(); i++){
		    System.out.print(composedTrace.get(i).getName()+" --> ");
		  }
		  System.out.println();*/
    
      if (candidates.isEmpty()) {   
        return setFailedResult(counterexample);
      }
      
      for (int i=candidates.size()-1;i>=0;i--) {
                
        for (ASTAutomaton astaut : astautomata.get(i+1)) {
          composedTrace = renovateTrace(composedTrace,astaut);          
        }
        composedTrace = extendTrace(composedTrace,candidates.get(i));        
      }
      
      for (ASTAutomaton astaut : astautomata.get(0)) {
        composedTrace = renovateTrace(composedTrace,astaut);          
      }
      
      //decode the manmade event only if model is converted
      composedTrace=convertTrace(composedTrace);
            
      final SafetyTraceProxy extendCounterexample =
               getFactory().createSafetyTraceProxy(tracename, getModel(), composedTrace); 
      return setFailedResult(extendCounterexample);
    }
  }
  
  //Works only for determinstic model
  private List<EventProxy> renovateTrace(List<EventProxy> oldlist,
                                         ASTAutomaton astaut) {
    Map<StateProxy, Set<EventProxy>> stateEvents = 
      new HashMap<StateProxy, Set<EventProxy>>();
    Map<Key, StateProxy> trans = 
      new HashMap<Key, StateProxy>();
    StateProxy currstate = null;
    for (StateProxy s : astaut.getAutomaton().getStates()) {
      if (s.isInitial()) {          
        currstate = s;
      }
      stateEvents.put(s, new HashSet<EventProxy>());
    }
    for (TransitionProxy t : astaut.getAutomaton().getTransitions()) {
      stateEvents.get(t.getSource()).add(t.getEvent());
      trans.put(new Key(t.getSource(), t.getEvent()), t.getTarget());
    }
    /*
    System.out.println("old list: ");
    for (int i=0; i<oldlist.size(); i++){
      System.out.print(oldlist.get(i).getName()+" --> ");
    }
    System.out.println();*/    
    List<EventProxy> newlist = new LinkedList<EventProxy>();
    for (int i=0;i<oldlist.size();i++) {
      EventProxy currevent = oldlist.get(i);
      //If the current event is included in this automaton
      if (astaut.getAutomaton().getEvents().contains(currevent)){
        //If the current event is not enabled at the current state
        if (!stateEvents.get(currstate).contains(currevent)) {
          for (EventProxy e : astaut.getRevents().get(currevent)) {
		        if (stateEvents.get(currstate).contains(e)) {
		          currevent = e;
		        }
		      }
	      }
        currstate = trans.get(new Key(currstate,currevent));
      }
      newlist.add(currevent);
    }
    assert(newlist.size() == oldlist.size());
    return newlist;
  }
  
  //Works for both determinstic and nondeterminstic
  /*
  private List<EventProxy> rTrace(List<EventProxy> oldlist,
                                  StateProxy cState,
                                  Map<StateProxy, Set<EventProxy>> stateEvents,
                                  Map<Key, StateProxy> trans,
                                  ASTAutomaton astaut) {
    if (oldlist.isEmpty()) return new LinkedList<EventProxy>();
    EventProxy currEvent = oldlist.get(0);
    if (oldlist.size() == 1) {
      //if this automaton contains the current event
      if (astaut.getAutomaton().getEvents().contains(currEvent)) {
        if (stateEvents.get(cState).contains(currEvent)) {          
          return oldlist;
        } 
        //if the current event is replaced
        else if (astaut.getRevents().keySet().contains(currEvent)) {
          List<EventProxy> newTrace = new LinkedList<EventProxy>();
          newTrace.add(astaut.getRevents().get(currEvent).iterator().next());
          return newTrace;
        } else {
          return new LinkedList<EventProxy>();
        }
      } else {
        return oldlist;
      }
    } else {
      //if this automaton contains the current event
      if (astaut.getAutomaton().getEvents().contains(currEvent)) {
        if (stateEvents.get(cState).contains(currEvent)) {
            List<EventProxy> newTrace = new LinkedList<EventProxy>(oldlist);        
			      newTrace.remove(currEvent);
			      StateProxy target = trans.get(new Key(cState,currEvent));
			      newTrace = rTrace(newTrace,
			                        target,
			                        stateEvents,
			                        trans,
			                        astaut);
			      if (!newTrace.isEmpty()
			        &&newTrace.size() == oldlist.size()-1) {
				      List<EventProxy> newlist = new LinkedList<EventProxy>();
		          newlist.add(currEvent);
		          newlist.addAll(newTrace);
			        return newlist;
		        } else {
		          return new LinkedList<EventProxy>();
		        }
        }
        //if the current event is replaced
        else if (astaut.getRevents().keySet().contains(currEvent)) {
          for (EventProxy e : astaut.getRevents().get(currEvent)) {
            List<EventProxy> newTrace = new LinkedList<EventProxy>(oldlist);
            newTrace.remove(currEvent);
            Key key = new Key(cState,e);
            if (trans.keySet().contains(key)) {
              StateProxy target = trans.get(key);
              newTrace = rTrace(newTrace,
			                          target,
			                          stateEvents,
			                          trans,
			                          astaut);
			        if (!newTrace.isEmpty()
			          &&newTrace.size() == oldlist.size()-1) {
			          List<EventProxy> newlist = new LinkedList<EventProxy>();
			          newlist.add(e);
			          newlist.addAll(newTrace);
				        return newlist;
			        } else {
			          return new LinkedList<EventProxy>();
			        }
            } else {
              continue;
            }
          }
        } else {
        	return new LinkedList<EventProxy>();
        }
      } else {
        List<EventProxy> newTrace = new LinkedList<EventProxy>(oldlist);        
        newTrace.remove(currEvent);        
        newTrace = rTrace(newTrace,
                          cState,
                          stateEvents,
                          trans,
                          astaut);
        if (!newTrace.isEmpty()
			    &&newTrace.size() == oldlist.size()-1) {
	        List<EventProxy> newlist = new LinkedList<EventProxy>();
	        newlist.add(currEvent);
	        newlist.addAll(newTrace);
	        return newlist;
        } else {
          return new LinkedList<EventProxy>();
        }
      }
    }
    return oldlist;
  }*/

  private List<EventProxy> extendTrace(List<EventProxy> eventlist,                                       
                                       Candidate candidate) {
    Set<AutomatonProxy> mCompautomata = new HashSet<AutomatonProxy>(candidate.getAllAutomata()); 
    Set<EventProxy> mOriginalAlphabet = new HashSet<EventProxy>(candidate.getAllEvents());
    List<Map<StateProxy, Set<EventProxy>>> events =
      new ArrayList<Map<StateProxy, Set<EventProxy>>>(mCompautomata.size());
    List<Map<Key, StateProxy>> automata =
      new ArrayList<Map<Key, StateProxy>>(mCompautomata.size());
    List<StateProxy> currstate = new ArrayList<StateProxy>(mCompautomata.size());
    AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
    int i = 0;
    for (AutomatonProxy proxy : mCompautomata) {
      events.add(new HashMap<StateProxy, Set<EventProxy>>(proxy.getStates().size()));
      automata.add(new HashMap<Key, StateProxy>(proxy.getTransitions().size()));
      Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
      //System.out.println(autevents);
      autevents.removeAll(proxy.getEvents());
      //System.out.println(autevents);
      int init = 0;
      for (StateProxy s : proxy.getStates()) {
        if (s.isInitial()) {
          init++;
          currstate.add(s);
        }
        events.get(i).put(s, new HashSet<EventProxy>(autevents));
      }
      assert(init == 1);
      for (TransitionProxy t : proxy.getTransitions()) {
        events.get(i).get(t.getSource()).add(t.getEvent());
        automata.get(i).put(new Key(t.getSource(), t.getEvent()), t.getTarget());
      }
      aut[i] = proxy;
      i++;
    }
    Queue<Place> stateList = new PriorityQueue<Place>();
    Place place = new Place(currstate, null, 0, null);
    stateList.offer(place);
    List<EventProxy> oldevents = new LinkedList<EventProxy>(eventlist);
    //System.out.println(oldevents);

    Set<Place> visited = new HashSet<Place>();
    visited.add(place);
    while (true) {
      place = stateList.poll();
      //System.out.println(place.getTrace());
      if (place.mIndex >= oldevents.size()) {
        break;
      }
      currstate = place.mCurrState;
      Set<EventProxy> possevents = new HashSet<EventProxy>(candidate.getLocalEvents());
      //System.out.println(mHidden);
      hidden:
      for (EventProxy pe : possevents) {
        //System.out.println(pe);
        List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
        for (i = 0; i < currstate.size(); i++) {
          if (aut[i].getEvents().contains(pe)) {
            StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
            //System.out.println(t);
            if (t == null) {
              continue hidden;
            }
            newstate.add(t);
          } else {
            newstate.add(currstate.get(i));
          }
        }
        //System.out.println(newstate);
        Place newPlace = new Place(newstate, pe, place.mIndex, place);
        if (visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
      }
      EventProxy currevent = oldevents.get(place.mIndex);
      List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
      boolean contains = true;
      for (i = 0; i < currstate.size(); i++) {
        if (aut[i].getEvents().contains(currevent)) {
          StateProxy t = automata.get(i).get(new Key(currstate.get(i), currevent));
          if (t == null) {
            contains = false;
          }
          newstate.add(t);
        } else {
          newstate.add(currstate.get(i));
        }
      }
      Place newPlace = new Place(newstate, currevent, place.mIndex + 1, place);
      if (contains && visited.add(newPlace)) {
        stateList.offer(newPlace);
      }
      assert(!stateList.isEmpty());
    }
    stateList = null;    
    return place.getTrace();
     
  }

  private class Place implements Comparable<Place> {
    public final List<StateProxy> mCurrState;
    public final EventProxy mEvent;
    public final int mIndex;
    public final Place mParent;

    public Place(List<StateProxy> currState, EventProxy event,
                  int index, Place parent)
    {
      mCurrState = currState;
      mEvent = event;
      mIndex = index;
      mParent = parent;
    }

    public List<EventProxy> getTrace()
    {
      if (mParent == null) {
        return new LinkedList<EventProxy>();
      }
      List<EventProxy> events = mParent.getTrace();
      events.add(mEvent);
      return events;
    }

    public int compareTo(Place other)
    {
      return other.mIndex - mIndex;
    }

    public int hashCode()
    {
      int hash = 7;
      hash = hash + mIndex * 31;
      hash = hash + mCurrState.hashCode();
      return hash;
    }

    public boolean equals(Object o)
    {
      Place p = (Place) o;
      return p.mIndex == mIndex && p.mCurrState.equals(mCurrState);
    }
  }

  private class Key {
    private final StateProxy mState;
    private final EventProxy mEvent;
    private final int mHash;

    public Key(StateProxy state, EventProxy event)
    {
      int hash = 7;
      hash += state.hashCode() * 31;
      hash += event.hashCode() * 31;
      mState = state;
      mEvent = event;
      mHash = hash;
    }

    public int hashCode()
    {
      return mHash;
    }

    public boolean equals(final Object other)
    {
      if (other != null && other.getClass() == getClass()) {
        final Key key = (Key) other;
        return mState.equals(key.mState) && mEvent.equals(key.mEvent);
      } else {
        return false;
      }
    }

  }
  
  protected void addStatistics(final VerificationResult result) {
    result.setNumberOfStates(mStates);
    result.setPeakNumberOfNodes(mNodes);
    //result.setNumberOfAutomata(10);    
  }
  
  public ProductDESProxy getConvertedModel() {
    return getModel();
  }
  
  public KindTranslator getConvertedKindTranslator() {
    return getKindTranslator();
  }
  
  public List<EventProxy> convertTrace(List<EventProxy> trace) {
    return trace;
  }
  
  //################################################################
  //Convert the Product DES structure into the Module structure and 
  //save it into a file.
  private void saveIntoFile(ProductDESProxy des) {    
    try {
	    final ModuleProxyFactory moduleFactory =  
	      ModuleElementFactory.getInstance();
	    final DocumentManager docManager = 
	      new DocumentManager();
	    final OperatorTable optable = 
	      CompilerOperatorTable.getInstance();	    
	    final JAXBModuleMarshaller moduleMarshaller =
	      new JAXBModuleMarshaller(moduleFactory, optable);	        
	    docManager.registerMarshaller(moduleMarshaller);	    
	    final ProductDESImporter pdi = 
	      new ProductDESImporter(moduleFactory,docManager);
	    final ModuleProxy module = pdi.importModule(des);	    
	    docManager.saveAs(module,new File("composedModel.wmod"));
	  } catch (final Throwable exception) {
        System.err.println("FATAL ERROR !!!");
        System.err.println(exception.getClass().getName() +
                         " caught in saving model!");
        exception.printStackTrace(System.err);
      }
  }

  
  private KindTranslator mTranslator;
  private int mProjectionNodeLimit;
  private int mStates;
  private int mNodes;
  private String mHeuristic;


  //#########################################################################
  //# Class Constants
  private static final Logger LOGGER =
    LoggerFactory.createLogger(ComposingSafetyVerifier.class);
}
