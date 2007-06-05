//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java,v 1.11 2007-06-05 13:45:21 robi Exp $
//###########################################################################


package net.sourceforge.waters.analysis.modular.supremica;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.modular.*;
import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.algorithms.AutomataSynchronizer;
import org.supremica.automata.algorithms.EquivalenceRelation;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.minimization.AutomataMinimizer;
import org.supremica.automata.algorithms.minimization.AutomatonMinimizer;
import org.supremica.automata.algorithms.minimization.MinimizationOptions;
import org.supremica.automata.IO.ProjectBuildFromWaters;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import java.util.Arrays;


public class ProjectingControllabilityChecker
  extends AbstractModelVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ProjectingControllabilityChecker(ProductDESProxy model,
                                          ProductDESProxyFactory factory,
                                          ControllabilityChecker checker,
                                          ModularHeuristic heuristic,
                                          boolean least)
  {
    super(model, factory);
    mChecker = checker;
    mHeuristic = heuristic;
    mTranslator = ControllabilityKindTranslator.getInstance();
    mStates = 0;
    mLeast = least;
    setStateLimit(5000000);
  }
  
  public SafetyTraceProxy getCounterExample()
  {
    return (SafetyTraceProxy)super.getCounterExample();
  }
  
  public KindTranslator getKindTranslator()
  {
    return mTranslator;
  }
  
  public void setKindTranslator(KindTranslator trans)
  {
    mTranslator = trans;
  }
  
  public AutomatonProxy[] convertSpec(AutomatonProxy a)
  {
    Set<EventProxy> uncont = new HashSet<EventProxy>();
    for (EventProxy e : a.getEvents()) {
      if (getKindTranslator().getEventKind(e) == EventKind.UNCONTROLLABLE) {
        uncont.add(e);
      }
    }
    Map<StateProxy, Set<EventProxy>> stateevents = new HashMap<StateProxy, Set<EventProxy>>();
    for (StateProxy s : a.getStates()) {
      stateevents.put(s, new HashSet<EventProxy>());
    }
    for (TransitionProxy t : a.getTransitions()) {
      if (getKindTranslator().getEventKind(t.getEvent()) == EventKind.UNCONTROLLABLE) {
        stateevents.get(t.getSource()).add(t.getEvent());
      }
    }
    EventProxy uncontrollable = getFactory().createEventProxy(a.getName() + "uncont", EventKind.UNCONTROLLABLE);
    StateProxy uncontstate = getFactory().createStateProxy("dumph");
    Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    Set<TransitionProxy> transitions = new HashSet<TransitionProxy>(a.getTransitions());
    Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.add(uncontrollable);
    for (StateProxy s : states) {
      for (EventProxy e: uncont) {
        if (!stateevents.get(s).contains(e)) {
          transitions.add(getFactory().createTransitionProxy(s, e, uncontstate));
        }
      }
    }
    states.add(uncontstate);
    StateProxy onestate = getFactory().createStateProxy("one", true, new HashSet<EventProxy>());
    transitions.add(getFactory().createTransitionProxy(uncontstate, uncontrollable, uncontstate));
    AutomatonProxy plant = getFactory().createAutomatonProxy(a.getName() + "plant", ComponentKind.PLANT,
                                                       events, states, transitions);
    AutomatonProxy spec = getFactory().createAutomatonProxy(a.getName() + "spec", ComponentKind.SPEC,
                                                      Collections.singleton(uncontrollable), Collections.singleton(onestate),
                                                      new HashSet<TransitionProxy>());
    return new AutomatonProxy[] {plant, spec};
  }
  
  public ProjectionList project(ProductDESProxy model, Set<EventProxy> forbiddenEvents)
  {
    Map<Set<AutomatonProxy>, AutomataHidden> newMap = new HashMap<Set<AutomatonProxy>, AutomataHidden>();
    Set<AutomatonProxy> automata = model.getAutomata();
    ProjectionList p = null;
    System.out.println(4);
    while (true) {
      Map<Set<AutomatonProxy>, Integer> numoccuring = new HashMap<Set<AutomatonProxy>, Integer>();
      System.out.println("5");
      for (EventProxy e : model.getEvents()) {
        if (!forbiddenEvents.contains(e)) {
          Set<AutomatonProxy> possess = new HashSet<AutomatonProxy>();
          for (AutomatonProxy a : automata) {
            if (a.getEvents().contains(e)) {
              possess.add(a);
            }
          }
          if (!possess.isEmpty()) {
            if (numoccuring.get(possess) == null) {
              numoccuring.put(possess, 0);
            }
            System.out.println(possess.size() + "," + e);
            numoccuring.put(possess, numoccuring.get(possess) + 1);
          }
        }
      }
      Set<AutomatonProxy> minset = null;
      double num = Double.POSITIVE_INFINITY;
      System.out.println(6);
      for (Set<AutomatonProxy> s : numoccuring.keySet()) {
        Set<EventProxy> ev = new HashSet<EventProxy>();
        double numstates = 1;
        for (AutomatonProxy a : s) {
          ev.addAll(a.getEvents());
          numstates *= a.getStates().size();
        }
        if (ev.size() == 0) {
          //System.out.println(s);
        }
        numstates /= ev.size();
        numstates *= (ev.size() - numoccuring.get(s));
        if (numstates < num) {
          num = numstates;
          minset = s;
        }
        /*if (minset == null) {
          minset = s;
        } /*else if (minset.size() > s.size()) {
          minset = s;
        } else if (minset.size() == s.size()
                   && numoccuring.get(minset) < numoccuring.get(s)) {
          minset = s;
        }*/
      }
      System.out.println(minset == null? null :minset.size());
      System.out.println(7);
      if (minset == null || minset.size() + 1 >= automata.size()) {
        break;
      }
      System.out.println(8);
      try {
        ProjectionList t = new ProjectionList(p, automata, minset, forbiddenEvents);
        p = t;
        /*newMap.put(minset, p.mCache);*/
        automata = p.getModel().getAutomata();
      } catch (Throwable t) {
        t.printStackTrace();
        mMinAutMap = newMap;
        //System.out.println(p);
        return p;
      }
    }
    mMinAutMap = newMap;
    //System.out.println(p);
    return p;
  }
  
  public boolean run()
    throws AnalysisException
  {
    LOGGER.debug("ProjectingControllabilityChecker: STARTING on " +
                 getModel().getName());
    mStates = 0;
    mChecker.setStateLimit(getStateLimit());
    final Map<AutomatonProxy, AutomatonProxy> getorig = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Map<AutomatonProxy, AutomatonProxy> getplant = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
    final Set<EventProxy> forbiddenEvents = new HashSet<EventProxy>();
    final Map<AutomatonProxy, AutomatonProxy> spectospec = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Map<AutomatonProxy, AutomatonProxy> spectoplant = new HashMap<AutomatonProxy, AutomatonProxy>();
    final SortedSet<AutomatonProxy> specs = 
      new TreeSet<AutomatonProxy>(new Comparator<AutomatonProxy>() {
      public int compare(AutomatonProxy a1, AutomatonProxy a2)
      {
        if (a1.getStates().size() < a2.getStates().size()) {
          return -1;
        } else if (a1.getStates().size() > a2.getStates().size()) {
          return 1;
        }
        if (a1.getTransitions().size() < a2.getTransitions().size()) {
          return -1;
        } else if (a1.getTransitions().size() > a2.getTransitions().size()) {
          return 1;
        }
        if (a1.getEvents().size() < a2.getEvents().size()) {
          return -1;
        } else if (a1.getEvents().size() > a2.getEvents().size()) {
          return 1;
        }
        return a1.getName().compareTo(a2.getName());
      }
    });
    for (AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  AutomatonProxy[] aut = convertSpec(automaton);
                      spectospec.put(automaton, aut[1]);
                      spectoplant.put(automaton, aut[0]);
                      forbiddenEvents.addAll(aut[1].getEvents());
                      specs.add(automaton);
                      break;
        default : break;
      }
    }
    //System.out.println(specs);
    System.out.println(forbiddenEvents);
    while (!specs.isEmpty()) {
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      SortedSet<AutomatonProxy> uncomposedplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecs = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      uncomposedplants.addAll(plants);
      uncomposedspecplants.addAll(specplants);
      uncomposedspecs.addAll(specs);
      final AutomatonProxy spec = mLeast ? specs.first() : specs.last();
      composition.add(spectoplant.get(spec));
      composition.add(spectospec.get(spec));
      //forbiddenEvents = aut[1].getEvents();
      for (AutomatonProxy a : composition) {
        events.addAll(a.getEvents());
      }
      uncomposedspecs.remove(spec);
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
      ProjectionList proj = null;
      mChecker.setModel(comp);
      //System.out.println(comp);
      //System.out.println(spec);
      mChecker.setKindTranslator(new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return getKindTranslator().getEventKind(e);
        }
        
        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return a == spectospec.get(spec) ? ComponentKind.SPEC : ComponentKind.PLANT;
        }
      });
      while (!mChecker.run()) {
        //System.out.println("start");
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        System.out.println("getTrace");
        TraceProxy counter = proj == null ? mChecker.getCounterExample() : proj.getTrace(mChecker.getCounterExample(), comp);
        System.out.println("gotTrace");
        Collection<AutomatonProxy> newComp =
          mHeuristic.heur(comp,
                          uncomposedplants,
                          uncomposedspecplants,
                          uncomposedspecs,
                          counter,
                          getKindTranslator());
        System.out.println("1");
        if (newComp == null) {
          List<EventProxy> e = counter.getEvents();
          counter = getFactory().createSafetyTraceProxy(comp, e.subList(0, e.size() - 1));
          setFailedResult(counter);
          return false;
        }
        System.out.println("2");
        for (AutomatonProxy automaton : newComp) {
          uncomposedplants.remove(automaton);
          uncomposedspecplants.remove(automaton);
          if (uncomposedspecs.remove(automaton)) {
            automaton = spectoplant.get(automaton);
          }
          composition.add(automaton);
          events.addAll(automaton.getEvents());
        }
        System.out.println("3");
        comp = getFactory().createProductDESProxy("comp", events, composition);
        proj = project(comp, forbiddenEvents);
        mChecker.setModel(proj == null ? comp : proj.getModel());
        //System.out.println(mChecker.getModel());
        System.out.println(10);
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      /*specs.removeAll(composition);
      plants.addAll(composition);*/
      specs.remove(spec);
      specplants.add(spec);
      /*for (AutomatonProxy automaton : composition) {
        if (specs.contains(automaton)) {
          //System.out.println(mChecker.getAnalysisResult().getTotalNumberOfStates() + " " + automaton.getName() + " size " + automaton.getStates().size());
          specs.remove(automaton);
          specplants.remove(getplant.get(automaton));
          specplants.add(getorig.get(automaton));
        }*/
        /*if (specplants.contains(automaton) || specs.contains(automaton)) {
          thing.append(automaton.getName());
          thing.append(',');
        }*/
      //}
      //System.out.println(thing);
    }
    setSatisfiedResult();
    return true;
  }
  
  protected void addStatistics(VerificationResult result)
  {
    result.setNumberOfStates(mStates);
  }
  
  private final static class AutomatonComparator
    implements Comparator<AutomatonProxy>
  {
    public int compare(AutomatonProxy a1, AutomatonProxy a2)
    {
      return a1.getName().compareTo(a2.getName());
    }
  }
  
  private class ProjectionList
  {
    final Set<AutomatonProxy> mAutomata;
    final ProjectionList mParent;
    final Set<AutomatonProxy> mCompautomata;
    final Set<EventProxy> mOriginalAlphabet;
    final Set<EventProxy> mHidden;
    final ProductDESProxy mModel;
    final AutomataHidden mCache;
    
    public ProjectionList(ProjectionList parent, Set<AutomatonProxy> automata,
                          Set<AutomatonProxy> compAutomata, EventProxy special, boolean supremica)
      throws Exception
    {
      System.out.println("start");
      mParent = parent;
      mCompautomata = compAutomata;
      mAutomata = new HashSet<AutomatonProxy>(automata);
      mAutomata.removeAll(compAutomata);
      Set<EventProxy> events = new HashSet<EventProxy>();
      for (AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mOriginalAlphabet = events;

      ProductDESProxy comp =
        getFactory().createProductDESProxy("comp", events, compAutomata);
      mHidden = new HashSet<EventProxy>(events);

      /*for (AutomatonProxy a : compAutomata) {

      for (final AutomatonProxy a : compAutomata) {
        mHidden.retainAll(a.getEvents());
 ProjectingControllabilityChecker.java
      }*/
      for (AutomatonProxy a : mAutomata) {
      }
      for (final AutomatonProxy a : mAutomata) {
        mHidden.removeAll(a.getEvents());
      }
      System.out.println(mHidden);

      final Set<EventProxy> targ = new HashSet<EventProxy>(events);
      targ.removeAll(mHidden);
      LOGGER.debug("Events: " + events);
      LOGGER.debug("Hidden: " + mHidden);
      final Alphabet alph = new Alphabet();
      for (final EventProxy event : targ) {
        final LabeledEvent label = new LabeledEvent(event);
        alph.addEvent(label);
      }

      final ProjectBuildFromWaters builder =
        new ProjectBuildFromWaters(new DocumentManager());
      final Automata supmodel = builder.build(comp);
      final SynchronizationOptions synchopt =
        SynchronizationOptions.getDefaultSynthesisOptions();
      synchopt.setUseShortStateNames(true);
      LOGGER.debug("ProjectingControllabilityChecker: synchronising " +
                   comp.getAutomata().size() + " automata ...");
      final Automaton synchprod =
        AutomataSynchronizer.synchronizeAutomata(supmodel, synchopt);
      LOGGER.debug("ProjectingControllabilityChecker: minimizing ...");
      LOGGER.debug("Original alphabet:" + synchprod.getEvents());
      LOGGER.debug("Target alphabet (alph): " + alph);
      LOGGER.debug("Target alphabet (targ): " + targ);
      final AutomatonMinimizer minimizer =
        new AutomatonMinimizer(synchprod);
      final MinimizationOptions opt = new MinimizationOptions();
      opt.setAlsoTransitions(true);
      opt.setComponentSizeLimit(Integer.MAX_VALUE);
      opt.setCompositionalMinimization(true);
      opt.setIgnoreMarking(true);
      opt.setKeepOriginal(false);
      opt.setMinimizationType(EquivalenceRelation.LANGUAGEEQUIVALENCE);
      opt.setTargetAlphabet(alph);
      System.out.println("target alphabet:" + alph);
      System.out.println(synchprod);
      System.out.println("Synch: " + synchprod.getEvents() + ", " + synchprod.getStates().size() + ", " + synchprod.getTransitions().size());
      final Automaton minAutomaton = minimizer.getMinimizedAutomaton(opt);
      System.out.println(minAutomaton);
      System.out.println("Synch1: " + synchprod.getEvents() + ", " + synchprod.getStates().size());
      System.out.println("Min: " + minAutomaton.getEvents() + ", " + minAutomaton.getStates().size() + ", " + minAutomaton.getTransitions().size());
      LOGGER.debug("ProjectingControllabilityChecker: done so far.");

      LOGGER.debug("Result alphabet:" + minAutomaton.getEvents());
      for (AutomatonProxy a : automata) {
        targ.addAll(a.getEvents());
      }
      mAutomata.add(minAutomaton);
      mModel = getFactory().createProductDESProxy("model", targ, automata);
      System.out.println("end");
      mCache = new AutomataHidden(minAutomaton, mHidden);
    }
    
    public ProjectionList(ProjectionList parent, Set<AutomatonProxy> automata,
                          Set<AutomatonProxy> compAutomata, Set<EventProxy> forbiddenEvents)
      throws Exception
    {
      //System.out.println("start");
      mParent = parent;
      mCompautomata = compAutomata;
      mAutomata = new HashSet<AutomatonProxy>(automata);
      mAutomata.removeAll(compAutomata);
      Set<EventProxy> events = new HashSet<EventProxy>();
      for (AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      /*for (AutomatonProxy a : compAutomata) {
        mHidden.retainAll(a.getEvents());
      }*/
      for (AutomatonProxy a : mAutomata) {
        mHidden.removeAll(a.getEvents());
      }
      mHidden.removeAll(forbiddenEvents);
      AutomatonProxy minAutomaton = null;
      AutomataHidden cache = mMinAutMap.get(compAutomata);
      if (cache != null && cache.mHidden.equals(mHidden)) {
        minAutomaton = cache.mAutomaton;
        System.out.println("used cache:" + minAutomaton.getStates().size());
      }
      if (minAutomaton == null) {
        ProductDESProxy comp = getFactory().createProductDESProxy("comp", events,
                                                                  compAutomata);
        //System.out.println(mHidden);
        Projection proj = new Projection(comp, getFactory(), mHidden, forbiddenEvents);
        minAutomaton = proj.project();
      }
      mAutomata.add(minAutomaton);
      Set<EventProxy> targ = new HashSet<EventProxy>();
      for (AutomatonProxy a : mAutomata) {
        targ.addAll(a.getEvents());
      }
      mModel = getFactory().createProductDESProxy("model", targ, mAutomata);
      mCache = cache == null ? new AutomataHidden(minAutomaton, mHidden) : cache;
      //System.out.println("end");
    }
    
    public ProductDESProxy getModel()
    {
      return mModel;
    }
    
    public TraceProxy getTrace(TraceProxy trace, ProductDESProxy model)
    {
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
      List<EventProxy> oldevents = trace.getEvents();
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
        Set<EventProxy> possevents = new HashSet<EventProxy>(mHidden);
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
      ProductDESProxy mod = mParent == null ? model : mParent.getModel();
      trace = getFactory().createSafetyTraceProxy(mod, place.getTrace());
      return mParent == null ? trace : mParent.getTrace(trace, model);
    }
    
    private class Place
      implements Comparable<Place>
    {
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
    
    private class Key
    {
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
  }
  
  private static class AutomataHidden
  {
    public final AutomatonProxy mAutomaton;
    public final Set<EventProxy> mHidden;
    
    public AutomataHidden(AutomatonProxy automon, Set<EventProxy> hidden)
    {
      mAutomaton = automon;
      mHidden = hidden;
    }
  }


  //#########################################################################
  //# Data Members
  private final ControllabilityChecker mChecker;
  private ModularHeuristic mHeuristic;
  private KindTranslator mTranslator;
  private int mStates;
  private final boolean mLeast;
  private Map<Set<AutomatonProxy>, AutomataHidden> mMinAutMap = new HashMap<Set<AutomatonProxy>, AutomataHidden>();


  //#########################################################################
  //# Class Constants
  private static final Logger LOGGER =
    LoggerFactory.createLogger(ProjectingControllabilityChecker.class);

}
