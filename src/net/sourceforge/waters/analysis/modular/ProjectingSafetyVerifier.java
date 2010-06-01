//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingSafetyVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.lang.Comparable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.analysis.CertainDeath;
import net.sourceforge.waters.analysis.TransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * The projecting safety check algorithm.
 * This is essentially a controllability checker, which can be
 * customised for language inclusion using a kind translator.
 *
 * @author Simon Ware
 */

public class ProjectingSafetyVerifier
  extends AbstractModularSafetyVerifier
  implements SafetyVerifier
{

  //#########################################################################
  //# Constructors
  public ProjectingSafetyVerifier(final ProductDESProxy model,
                                  final KindTranslator translator,
                                  final ProductDESProxyFactory factory,
                                  final SafetyVerifier checker)
  {
    this(model, translator, factory, checker, 16000);
  }

  public ProjectingSafetyVerifier(final ProductDESProxy model,
                                  final KindTranslator translator,
                                  final ProductDESProxyFactory factory,
                                  final SafetyVerifier checker,
                                  final int projsize)
  {
    super(model, translator, factory);
    setHeuristicMethod(ModularHeuristicFactory.Method.MaxCommonEvents);
    setHeuristicPreference
      (ModularHeuristicFactory.Preference.PREFER_REAL_PLANT);
    mChecker = checker;
    mStates = 0;
    mMaxProjStates = projsize;
  }


  //#########################################################################
  //# Configuration
  public void setNodeLimit(final int limit)
  {
    setMaxSyncProductStates(limit);
    setMaxProjStates(limit);
  }

  public void setMaxSyncProductStates(final int limit)
  {
    super.setNodeLimit(limit);
  }

  public void setMaxProjStates(final int limit)
  {
    mMaxProjStates = limit;
  }


  //#########################################################################
  //# Invocation
  /*public boolean run()
    throws AnalysisException
  {
    //LOGGER.debug("ProjectingSafetyVerifier: STARTING on " +
    //             getModel().getName());
    mStates = 0;
    mChecker.setNodeLimit(getNodeLimit());
    final Map<AutomatonProxy, AutomatonProxy> getorig = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Map<AutomatonProxy, AutomatonProxy> getplant = new HashMap<AutomatonProxy, AutomatonProxy>();
    final Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specplants = new HashSet<AutomatonProxy>();
    final Set<EventProxy> forbiddenEvents = new HashSet<EventProxy>();
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
        case SPEC  :  specs.add(automaton);
                      break;
        default : break;
      }
    }
    //System.out.println(specs);
    while (!specs.isEmpty()) {
      mMinAutMap.clear();
      Collection<AutomatonProxy> composition = new ArrayList<AutomatonProxy>();
      Set<EventProxy> events = new HashSet<EventProxy>();
      SortedSet<AutomatonProxy> uncomposedplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecplants = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      SortedSet<AutomatonProxy> uncomposedspecs = new TreeSet<AutomatonProxy>(new AutomatonComparator());
      final AutomatonProxy origspec = mLeast ? specs.first() : specs.last();
      Object[] array = convertSpec(origspec);
      final AutomatonProxy spec = (AutomatonProxy) array[1];
      AutomatonProxy plant = (AutomatonProxy) array[0];
      final Map<EventProxy,EventProxy> uncont = Casting.toMap((Map) array[2]);
      forbiddenEvents.clear();
      forbiddenEvents.addAll(spec.getEvents());
      for (AutomatonProxy a : plants) {
        uncomposedplants.add(convertPlant(a, uncont));
      }
      for (AutomatonProxy a : specplants) {
        uncomposedspecplants.add(convertPlant(a, uncont));
      }
      //uncomposedspecplants.addAll(specplants);
      uncomposedspecs.addAll(specs);
      mSpec = spec;
      composition.add(spec);
      composition.add(plant);
      //forbiddenEvents = aut[1].getEvents();
      for (AutomatonProxy a : composition) {
        events.addAll(a.getEvents());
      }
      uncomposedspecs.remove(origspec);
      ProductDESProxy comp = getFactory().createProductDESProxy("comp", events, composition);
      ProjectionList proj = null;
      mChecker.setModel(comp);
      KindTranslator translator = new KindTranslator()
      {
        public EventKind getEventKind(EventProxy e)
        {
          return forbiddenEvents.contains(e) ? EventKind.UNCONTROLLABLE
                                             : EventKind.CONTROLLABLE;
        }

        public ComponentKind getComponentKind(AutomatonProxy a)
        {
          return a == spec ? ComponentKind.SPEC : ComponentKind.PLANT;
        }
      };
      mChecker.setKindTranslator(translator);
      final ModularHeuristic heuristic = getHeuristic();
      while (!mChecker.run()) {
        mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
        final TraceProxy counter =
          proj == null ?
          mChecker.getCounterExample() :
          proj.getTrace(mChecker.getCounterExample(), comp);
        Collection<AutomatonProxy> newComp =
          heuristic.heur(comp,
                         uncomposedplants,
                         uncomposedspecplants,
                         uncomposedspecs,
                         counter,
                         translator);
        if (newComp == null) {
          return setFailedResult(counter, uncont);
        }
        for (AutomatonProxy automaton : newComp) {
          uncomposedplants.remove(automaton);
          uncomposedspecplants.remove(automaton);
          uncomposedspecs.remove(automaton);
          composition.add(automaton);
          events.addAll(automaton.getEvents());
        }
        comp = getFactory().createProductDESProxy("comp", events, composition);
        proj = project(comp, forbiddenEvents);
        mChecker.setModel(proj == null ? comp : proj.getModel());
      }
      mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      specs.remove(origspec);
      specplants.add(origspec);
    }
    setSatisfiedResult();
    return true;
  }*/

  public boolean run()
    throws AnalysisException
  {
    setUp();
    Set<AutomatonProxy> plants = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> specs = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> origspecs = new HashSet<AutomatonProxy>();
    final Set<AutomatonProxy> automata = new HashSet<AutomatonProxy>();
    final Set<EventProxy> forbiddenEvents = new HashSet<EventProxy>();
    final Set<EventProxy> events = new HashSet<EventProxy>(getModel().getEvents());
    final Map<EventProxy, EventProxy> forbtouncont = new HashMap<EventProxy, EventProxy>();
    for (final AutomatonProxy automaton : getModel().getAutomata()) {
      switch (getKindTranslator().getComponentKind(automaton)) {
        case PLANT :  plants.add(automaton);
                      break;
        case SPEC  :  origspecs.add(automaton);
                      break;
        default : break;
      }
    }
    int l = 0;
    for (final AutomatonProxy s : origspecs) {
      final Object[] array = convertSpec(s, l, forbtouncont);
      l++;
      final AutomatonProxy spec = (AutomatonProxy) array[1];
      specs.add(spec);
      forbiddenEvents.addAll(spec.getEvents());
      final AutomatonProxy plant = (AutomatonProxy) array[0];
      automata.add(plant);
      final Map<EventProxy,EventProxy> uncont = Casting.toMap((Map<?,?>) array[2]);
      final Set<AutomatonProxy> newplants = new HashSet<AutomatonProxy>(plants.size());
      for (final AutomatonProxy p : plants) {
        newplants.add(convertPlant(p, uncont));
      }
      plants = newplants;
    }
    automata.addAll(plants);
    automata.addAll(specs);
    for (final AutomatonProxy a : automata) {
      events.addAll(a.getEvents());
    }
    final ProductDESProxy model =
      getFactory().createProductDESProxy("model", events, automata);
    final ProjectionList list = project(model, forbiddenEvents);
    final ProductDESProxy pmodel = list == null ? model : list.getModel();
    mChecker.setModel(pmodel);
    mChecker.setNodeLimit(getNodeLimit());
    mChecker.setKindTranslator(new KindTranslator()
    {
      public EventKind getEventKind(final EventProxy e)
      {
        return getKindTranslator().getEventKind(e);
      }

      public ComponentKind getComponentKind(final AutomatonProxy a)
      {
        return specs.contains(a) ? ComponentKind.SPEC : ComponentKind.PLANT;
      }
    });
    mMinAutMap.clear();
    if (mChecker.run()) {
      //mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      mStates = (int) mChecker.getAnalysisResult().getTotalNumberOfStates();
      return setSatisfiedResult();
    } else {
      //mStates += mChecker.getAnalysisResult().getTotalNumberOfStates();
      mStates = (int) mChecker.getAnalysisResult().getTotalNumberOfStates();
      TraceProxy counter = mChecker.getCounterExample();
      counter = list.getTrace(counter, model);
      final List<EventProxy> e = new ArrayList<EventProxy>(counter.getEvents());
      e.set(e.size() - 1, forbtouncont.get(e.get(e.size() - 1)));
      counter = getFactory().createSafetyTraceProxy(getModel().getName(),
                                                    getModel(), e);
      return setFailedResult(counter);
    }
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void addStatistics()
  {
    super.addStatistics();
    final VerificationResult result = getAnalysisResult();
    result.setNumberOfStates(mStates);
  }


  //#########################################################################
  //# Auxiliary Methods
  private Object[] convertSpec(final AutomatonProxy a, final int num,
                               final Map<EventProxy, EventProxy> forbtouncont)
  {
    final Map<EventProxy, EventProxy> uncont = new HashMap<EventProxy, EventProxy>();
    for (final EventProxy e : a.getEvents()) {
      if (getKindTranslator().getEventKind(e) == EventKind.UNCONTROLLABLE) {
        uncont.put(e, getFactory().createEventProxy(e.getName() + ":" + num,
                                                    EventKind.UNCONTROLLABLE));
        forbtouncont.put(uncont.get(e), e);
      }
    }
    final Map<StateProxy, Set<EventProxy>> stateevents =
      new HashMap<StateProxy, Set<EventProxy>>();
    for (final StateProxy s : a.getStates()) {
      stateevents.put(s, new HashSet<EventProxy>());
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (getKindTranslator().getEventKind(t.getEvent()) ==
          EventKind.UNCONTROLLABLE) {
        stateevents.get(t.getSource()).add(t.getEvent());
      }
    }
    final Set<StateProxy> states = new HashSet<StateProxy>(a.getStates());
    final Set<TransitionProxy> transitions =
      new HashSet<TransitionProxy>(a.getTransitions());
    final Set<EventProxy> events = new HashSet<EventProxy>(a.getEvents());
    events.addAll(uncont.values());
    for (final StateProxy s : states) {
      for (final EventProxy e: uncont.keySet()) {
        if (!stateevents.get(s).contains(e)) {
          transitions.add
            (getFactory().createTransitionProxy(s, uncont.get(e), s));
        }
      }
    }
    final StateProxy onestate =
      getFactory().createStateProxy("one", true, new HashSet<EventProxy>());
    final AutomatonProxy plant =
      getFactory().createAutomatonProxy(a.getName() + ":plant",
                                        ComponentKind.PLANT,
                                        events, states, transitions);
    final AutomatonProxy spec =
      getFactory().createAutomatonProxy(a.getName() + ":spec",
                                        ComponentKind.SPEC,
                                        uncont.values(),
                                        Collections.singleton(onestate),
                                        new HashSet<TransitionProxy>());
    return new Object[] {plant, spec, uncont};
  }

  private AutomatonProxy convertPlant(final AutomatonProxy a,
                                      final Map<EventProxy, EventProxy> uncont)
  {
    final Set<EventProxy> same = new HashSet<EventProxy>(a.getEvents());
    same.retainAll(uncont.keySet());
    if (same.isEmpty()) {
      return a;
    }
    final Collection<TransitionProxy> trans =
      new ArrayList<TransitionProxy>(a.getTransitions());
    final Collection<EventProxy> events = new ArrayList<EventProxy>(a.getEvents());
    for(final EventProxy e : same) {
      events.add(uncont.get(e));
    }
    for (final TransitionProxy t : a.getTransitions()) {
      if (uncont.containsKey(t.getEvent())) {
        trans.add(getFactory().createTransitionProxy(t.getSource(),
                                                     uncont.get(t.getEvent()),
                                                     t.getSource()));
      }
    }
    return getFactory().createAutomatonProxy
      (a.getName() + ":plant", ComponentKind.PLANT,
       events, a.getStates(), trans);
  }

  private ProjectionList project(final ProductDESProxy model,
                                 final Set<EventProxy> forbiddenEvents)
    throws AnalysisException
  {
    mChecked.clear();
    Set<AutomatonProxy> automata =
      new TreeSet<AutomatonProxy>(model.getAutomata());
    ProjectionList p = null;
    while (true) {
      final SortedMap<SortedSet<AutomatonProxy>, Integer> numoccuring =
        new TreeMap<SortedSet<AutomatonProxy>,Integer>
        (new AutomataComparator());
      for (final EventProxy e : model.getEvents()) {
        if (!forbiddenEvents.contains(e)) {
          final SortedSet<AutomatonProxy> possess = new TreeSet<AutomatonProxy>();
          for (final AutomatonProxy a : automata) {
            if (a.getEvents().contains(e)) {
              possess.add(a);
            }
          }
          if (!possess.isEmpty()) {
            if (numoccuring.get(possess) == null) {
              numoccuring.put(possess, 0);
            }
            numoccuring.put(possess, numoccuring.get(possess) + 1);
          }
        }
      }
      final Set<Tuple> possible = new TreeSet<Tuple>();
      boolean stop = true;
      for (final SortedSet<AutomatonProxy> s : numoccuring.keySet()) {
        if (s.size() > 4 && s.size() != automata.size()) {
          continue;
        }
        double size = 0;
        final Set<EventProxy> common = new HashSet<EventProxy>(model.getEvents());
        final Set<EventProxy> total = new HashSet<EventProxy>();
        for (final AutomatonProxy a : s) {
          size += Math.log(a.getStates().size());
          total.addAll(a.getEvents());
          common.retainAll(a.getEvents());
        }
        final double tot = total.size();
        final double uncom = tot - common.size();
        size *= uncom;
        possible.add(new Tuple(s, size));
      }
      int overflows = 0;
      ProjectionList minlist = null;
      minSize = Integer.MAX_VALUE / 4;
      for (final Tuple tup : possible) {
        try {
          final ProjectionList t =
            new ProjectionList(p, automata, tup.mSet, forbiddenEvents);
          if (minSize >= t.getNew().getStates().size()) {
            minlist = t;
            minSize = t.getNew().getStates().size();
            break;
          }
        } catch (final OverflowException exception) {
          overflows++;
          if (overflows >= 2) {
            //break;
          }
        }
      }
      if (minlist != null) {
        p = minlist;
        automata = new TreeSet<AutomatonProxy>(p.getModel().getAutomata());
        stop = false;
      }
      if (stop) {
        break;
      }
    }
    final Iterator<AutomataHidden> it = mMinAutMap.keySet().iterator();
    while (it.hasNext()) {
      final AutomataHidden ah = it.next();
      if (!mChecked.contains(ah)) {
        it.remove();
      }
    }
    return p;
  }

  @SuppressWarnings("unused")
  private boolean setFailedResult(final TraceProxy counterexample,
                                  final Map<EventProxy,EventProxy> uncont)
  {
    final ProductDESProxyFactory factory = getFactory();
    final ProductDESProxy des = getModel();
    final String desname = des.getName();
    final String tracename = desname + ":uncontrollable";
    final List<EventProxy> events = counterexample.getEvents();
    final int len = events.size();
    final List<EventProxy> modevents = new ArrayList<EventProxy>(len);
    final Iterator<EventProxy> iter = events.iterator();
    EventProxy event = iter.next();
    while (iter.hasNext()) {
      modevents.add(event);
      event = iter.next();
    }
    for (final Map.Entry<EventProxy,EventProxy> entry : uncont.entrySet()) {
      if (entry.getValue() == event) {
        final EventProxy key = entry.getKey();
        modevents.add(key);
        break;
      }
    }
    final SafetyTraceProxy wrapper =
      factory.createSafetyTraceProxy(tracename, des, modevents);
    return super.setFailedResult(wrapper);
  }


  //#########################################################################
  //# Inner Class ProjectionList
  private class ProjectionList
  {
    final Set<AutomatonProxy> mAutomata;
    final ProjectionList mParent;
    final Set<AutomatonProxy> mCompautomata;
    final Set<EventProxy> mOriginalAlphabet;
    final Set<EventProxy> mHidden;
    final ProductDESProxy mModel;
    final AutomatonProxy mNew;

    public ProjectionList(final ProjectionList parent,
                          final Set<AutomatonProxy> automata,
                          final Set<AutomatonProxy> compAutomata,
                          final Set<EventProxy> forbiddenEvents)
      throws AnalysisException
    {
      final Set<EventProxy> localforbiddenEvents =
        new HashSet<EventProxy>(forbiddenEvents);
      mParent = parent;
      mCompautomata = compAutomata;
      mAutomata = new HashSet<AutomatonProxy>(automata);
      mAutomata.removeAll(compAutomata);
      final Set<EventProxy> events = new HashSet<EventProxy>();
      for (final AutomatonProxy a : compAutomata) {
        events.addAll(a.getEvents());
      }
      mOriginalAlphabet = events;
      mHidden = new HashSet<EventProxy>(events);
      for (final AutomatonProxy a : mAutomata) {
        if (a != mSpec) {
          mHidden.removeAll(a.getEvents());
        }
      }
      localforbiddenEvents.retainAll(mHidden);
      final AutomataHidden ah =
        new AutomataHidden(compAutomata, new HashSet<EventProxy>(mHidden));
      mHidden.removeAll(forbiddenEvents);
      mChecked.add(ah);
      AutomatonProxy minAutomaton;
      if (mMinAutMap.containsKey(ah)) {
        minAutomaton = mMinAutMap.get(ah);
        if (minAutomaton == null) {
          throw new OverflowException();
        }
      } else {
        final ProductDESProxy comp =
          getFactory().createProductDESProxy("comp", events, compAutomata);
        final Projection2 proj =
          new Projection2(comp, getFactory(), mHidden, localforbiddenEvents);
        try {
          proj.setNodeLimit(mMaxProjStates);
          minAutomaton = proj.project();
          if (mOriginalAlphabet.containsAll(forbiddenEvents)) {
            final TransitionRelation tr = new TransitionRelation(minAutomaton, null);
            int states = tr.getAutomaton(getFactory()).getStates().size();
            tr.setMarkingToStatesWithOutgoing(forbiddenEvents);
            final CertainDeath con = new CertainDeath(tr); con.run();
            minAutomaton = tr.getAutomaton(getFactory());
            states -= tr.getAutomaton(getFactory()).getStates().size();
          }
          mStates += minAutomaton.getStates().size();
          mMinAutMap.put(ah, minAutomaton);
        } catch (final OverflowException exception) {
          mStates += mMaxProjStates;
          mMinAutMap.put(ah, null);
          throw exception;
        }
      }
      mAutomata.add(minAutomaton);
      mNew = minAutomaton;
      final Set<EventProxy> targ = new HashSet<EventProxy>();
      for (final AutomatonProxy a : mAutomata) {
        targ.addAll(a.getEvents());
      }
      mModel = getFactory().createProductDESProxy("model", targ, mAutomata);
    }

    public ProductDESProxy getModel()
    {
      return mModel;
    }

    public AutomatonProxy getNew()
    {
      return mNew;
    }

    public TraceProxy getTrace(TraceProxy trace, final ProductDESProxy model)
    {
      final List<Map<StateProxy, Set<EventProxy>>> events =
        new ArrayList<Map<StateProxy, Set<EventProxy>>>(mCompautomata.size());
      final List<Map<Key, StateProxy>> automata =
        new ArrayList<Map<Key, StateProxy>>(mCompautomata.size());
      List<StateProxy> currstate = new ArrayList<StateProxy>(mCompautomata.size());
      final AutomatonProxy[] aut = new AutomatonProxy[mCompautomata.size()];
      int i = 0;
      for (final AutomatonProxy proxy : mCompautomata) {
        events.add(new HashMap<StateProxy, Set<EventProxy>>(proxy.getStates().size()));
        automata.add(new HashMap<Key, StateProxy>(proxy.getTransitions().size()));
        final Set<EventProxy> autevents = new HashSet<EventProxy>(mOriginalAlphabet);
        //System.out.println(autevents);
        autevents.removeAll(proxy.getEvents());
        //System.out.println(autevents);
        int init = 0;
        for (final StateProxy s : proxy.getStates()) {
          if (s.isInitial()) {
            init++;
            currstate.add(s);
          }
          events.get(i).put(s, new HashSet<EventProxy>(autevents));
        }
        assert(init == 1);
        for (final TransitionProxy t : proxy.getTransitions()) {
          events.get(i).get(t.getSource()).add(t.getEvent());
          automata.get(i).put(new Key(t.getSource(), t.getEvent()), t.getTarget());
        }
        aut[i] = proxy;
        i++;
      }
      Queue<Place> stateList = new PriorityQueue<Place>();
      Place place = new Place(currstate, null, 0, null);
      stateList.offer(place);
      final List<EventProxy> oldevents = trace.getEvents();
      //System.out.println(oldevents);

      final Set<Place> visited = new HashSet<Place>();
      visited.add(place);
      while (true) {
        place = stateList.poll();
        //System.out.println(place.getTrace());
        if (place.mIndex >= oldevents.size()) {
          break;
        }
        currstate = place.mCurrState;
        final Set<EventProxy> possevents = new HashSet<EventProxy>(mHidden);
        //System.out.println(mHidden);
        hidden:
        for (final EventProxy pe : possevents) {
          //System.out.println(pe);
          final List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
          for (i = 0; i < currstate.size(); i++) {
            if (aut[i].getEvents().contains(pe)) {
              final StateProxy t = automata.get(i).get(new Key(currstate.get(i), pe));
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
          final Place newPlace = new Place(newstate, pe, place.mIndex, place);
          if (visited.add(newPlace)) {
            stateList.offer(newPlace);
          }
        }
        final EventProxy currevent = oldevents.get(place.mIndex);
        final List<StateProxy> newstate = new ArrayList<StateProxy>(currstate.size());
        boolean contains = true;
        for (i = 0; i < currstate.size(); i++) {
          if (aut[i].getEvents().contains(currevent)) {
            final StateProxy t = automata.get(i).get(new Key(currstate.get(i), currevent));
            if (t == null) {
              contains = false;
            }
            newstate.add(t);
          } else {
            newstate.add(currstate.get(i));
          }
        }
        final Place newPlace = new Place(newstate, currevent, place.mIndex + 1, place);
        if (contains && visited.add(newPlace)) {
          stateList.offer(newPlace);
        }
        assert(!stateList.isEmpty());
      }
      stateList = null;
      final ProductDESProxy mod = mParent == null ? model : mParent.getModel();
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

      public Place(final List<StateProxy> currState, final EventProxy event,
                   final int index, final Place parent)
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
        final List<EventProxy> events = mParent.getTrace();
        events.add(mEvent);
        return events;
      }

      public int compareTo(final Place other)
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

      public boolean equals(final Object o)
      {
        final Place p = (Place) o;
        return p.mIndex == mIndex && p.mCurrState.equals(mCurrState);
      }
    }

    private class Key
    {
      private final StateProxy mState;
      private final EventProxy mEvent;
      private final int mHash;

      public Key(final StateProxy state, final EventProxy event)
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
    public final Set<AutomatonProxy> mAutomata;
    public final Set<EventProxy> mHidden;

    public AutomataHidden(final Set<AutomatonProxy> automata, final Set<EventProxy> hidden)
    {
      mAutomata = automata;
      mHidden = hidden;
    }

    public int hashCode()
    {
      int code = 31 + mAutomata.hashCode();
      code = code * 31 + mHidden.hashCode();
      return code;
    }

    public boolean equals(final Object o)
    {
      if (o instanceof AutomataHidden) {
        final AutomataHidden a = (AutomataHidden) o;
        return mAutomata.equals(a.mAutomata) && mHidden.equals(a.mHidden);
      }
      return false;
    }
  }

  private static class AutomataComparator
    implements Comparator<SortedSet<AutomatonProxy>>
  {
    public int compare(final SortedSet<AutomatonProxy> s1, final SortedSet<AutomatonProxy> s2)
    {
      if (s1.size() < s2.size()) {
        return -1;
      } else if (s1.size() > s2.size()) {
        return 1;
      }
      final Iterator<AutomatonProxy> i1 = s1.iterator();
      final Iterator<AutomatonProxy> i2 = s2.iterator();
      while (i1.hasNext()) {
        final AutomatonProxy a1 = i1.next();
        final AutomatonProxy a2 = i2.next();
        final int res = a1.compareTo(a2);
        if (res != 0) {
          return res;
        }
      }
      return 0;
    }
  }

  private static class Tuple
    implements Comparable<Tuple>
  {
    public final Set<AutomatonProxy> mSet;
    public final double mSize;

    public Tuple(final Set<AutomatonProxy> set, final double size)
    {
      mSet = set;
      mSize = size;
    }

    public int compareTo(final Tuple t)
    {
      if (mSize < t.mSize) {
        return -1;
      } else if (mSize == t.mSize) {
        return 0;
      } else {
        return 1;
      }
    }
  }


  //#########################################################################
  //# Data Members
  private int minSize = 1000;
  private final SafetyVerifier mChecker;
  private final AutomatonProxy mSpec = null;
  private int mStates;
  private int mMaxProjStates;
  private final Map<AutomataHidden, AutomatonProxy> mMinAutMap =
    new HashMap<AutomataHidden, AutomatonProxy>();
  private final Set<AutomataHidden> mChecked = new HashSet<AutomataHidden>();


  //#########################################################################
  //# Class Constants
  @SuppressWarnings("unused")
  private static final Logger LOGGER =
    LoggerFactory.createLogger(ProjectingSafetyVerifier.class);

}
