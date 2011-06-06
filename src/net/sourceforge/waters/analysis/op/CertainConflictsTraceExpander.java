//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   CertainConflictsTraceExpander
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A tool to extend a conflict error trace from a state of certain conflicts
 * to a true blocking state.
 *
 * @author Robi Malik
 */
public class CertainConflictsTraceExpander
{

  //#########################################################################
  //# Constructors
  public CertainConflictsTraceExpander(final ProductDESProxyFactory factory,
                                       final KindTranslator translator,
                                       final SafetyVerifier verifier)
  {
    mFactory = factory;
    mKindTranslator = new CertainConflictsKindTranslator(translator);
    mSafetyVerifier = verifier;
    mSafetyVerifier.setKindTranslator(mKindTranslator);
  }


  //#########################################################################
  //# Configuration
  public void setStartStates(final TraceStepProxy step)
  {
    mStartStates = step;
  }

  public void setCertainConflictsAutomaton
    (final AutomatonProxy original,
     final AutomatonProxy converted,
     final Collection<EventProxy> uncontrollables)
  {
    mOriginalCertainConflictsAutomaton = original;
    mConvertedCertainConflictsAutomaton = converted;
    mUncontrollableEvents = uncontrollables;
  }


  //#########################################################################
  //# Invocation
  public List<TraceStepProxy> run()
  throws AnalysisException
  {
    final ProductDESProxy des = createControllabilityModel();
    mSafetyVerifier.setModel(des);
    //MarshallingTools.saveModule(des, "xxx.wmod");
    if (mSafetyVerifier.run()) {
      return null;
    } else {
      final SafetyTraceProxy trace = mSafetyVerifier.getCounterExample();
      final List<TraceStepProxy> steps = trace.getTraceSteps();
      return convertTraceSteps(steps);
    }
  }

  public Collection<AutomatonProxy> getTraceAutomata()
  {
    final int numAutomata = mOriginalAutomata.size();
    final Collection<AutomatonProxy> result =
      new ArrayList<AutomatonProxy>(numAutomata);
    for (final AutomatonProxy aut : mOriginalAutomata) {
      if (aut != mOriginalCertainConflictsAutomaton) {
        result.add(aut);
      }
    }
    result.add(mConvertedCertainConflictsAutomaton);
    return result;
  }


  //#########################################################################
  //# Auxiliary Methods
  private ProductDESProxy createControllabilityModel()
  {
    final Map<AutomatonProxy,StateProxy> stepMap = mStartStates.getStateMap();
    final int numAutomata = stepMap.size();
    int numStates = 0;
    for (final AutomatonProxy aut : stepMap.keySet()) {
      numStates += aut.getStates().size();
    }
    mReverseStateMap = new HashMap<StateProxy,StateProxy>(numStates);
    mOriginalAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    mConvertedAutomata = new ArrayList<AutomatonProxy>(numAutomata);
    mOriginalAutomata.add(mOriginalCertainConflictsAutomaton);
    mConvertedAutomata.add(mConvertedCertainConflictsAutomaton);
    final Collection<EventProxy> ccevents =
      mConvertedCertainConflictsAutomaton.getEvents();
    final Collection<EventProxy> events = new THashSet<EventProxy>(ccevents);
    for (final Map.Entry<AutomatonProxy,StateProxy> entry :
         stepMap.entrySet()) {
      final AutomatonProxy aut = entry.getKey();
      if (aut != mOriginalCertainConflictsAutomaton) {
        mOriginalAutomata.add(aut);
        final StateProxy init = entry.getValue();
        final AutomatonProxy converted =
          createControllabilityAutomaton(aut, init);
        mConvertedAutomata.add(converted);
        final Collection<EventProxy> local = converted.getEvents();
        events.addAll(local);
      }
    }
    final List<EventProxy> eventList = new ArrayList<EventProxy>(events);
    Collections.sort(eventList);
    final String name = mOriginalCertainConflictsAutomaton.getName();
    final String comment =
      "Automatically generated to expand conflict error trace for '" +
      name + "' from certain conflicts to blocking state.";
    final ProductDESProxy des =
      mFactory.createProductDESProxy(name, comment, null,
                                     eventList, mConvertedAutomata);
    return des;
  }

  private AutomatonProxy createControllabilityAutomaton
    (final AutomatonProxy aut, final StateProxy init)
  {
    final Collection<EventProxy> oldevents = aut.getEvents();
    final int numevents = oldevents.size();
    final Collection<EventProxy> newevents =
      new ArrayList<EventProxy>(numevents);
    for (final EventProxy event : oldevents) {
      if (mKindTranslator.getEventKind(event) != EventKind.PROPOSITION) {
        newevents.add(event);
      }
    }
    final Collection<StateProxy> oldstates = aut.getStates();
    final int numstates = oldstates.size();
    final Collection<StateProxy> newstates =
      new ArrayList<StateProxy>(numstates);
    final Map<StateProxy,StateProxy> statemap =
      new HashMap<StateProxy,StateProxy>(numstates);
    for (final StateProxy oldstate : oldstates) {
      final String statename = oldstate.getName();
      final StateProxy newstate =
        mFactory.createStateProxy(statename, oldstate == init, null);
      newstates.add(newstate);
      statemap.put(oldstate, newstate);
      mReverseStateMap.put(newstate, oldstate);
    }
    final Collection<TransitionProxy> oldtransitions = aut.getTransitions();
    final int numtrans = oldtransitions.size();
    final Collection<TransitionProxy> newtransitions =
        new ArrayList<TransitionProxy>(numtrans);
    for (final TransitionProxy oldtrans : oldtransitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final StateProxy newsource = statemap.get(oldsource);
      final StateProxy oldtarget = oldtrans.getTarget();
      final StateProxy newtarget = statemap.get(oldtarget);
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        mFactory.createTransitionProxy(newsource, event, newtarget);
      newtransitions.add(newtrans);
    }
    final String autname = aut.getName();
    return mFactory.createAutomatonProxy
      (autname, ComponentKind.PLANT, newevents, newstates, newtransitions);
  }

  private List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> steps)
  {
    final int len = steps.size();
    final List<TraceStepProxy> newsteps = new ArrayList<TraceStepProxy>(len);
    newsteps.add(mStartStates);
    final Iterator<TraceStepProxy> iter = steps.iterator();
    iter.next();
    while (iter.hasNext()) {
      final TraceStepProxy oldstep = iter.next();
      final TraceStepProxy newstep = convertTraceStep(oldstep);
      newsteps.add(newstep);
    }
    return newsteps;
  }

  private TraceStepProxy convertTraceStep(final TraceStepProxy oldstep)
  {
    final Map<AutomatonProxy,StateProxy> oldmap = oldstep.getStateMap();
    final int size = oldmap.size();
    final Map<AutomatonProxy,StateProxy> newmap =
      new HashMap<AutomatonProxy,StateProxy>(size);
    final Iterator<AutomatonProxy> olditer = mConvertedAutomata.iterator();
    final Iterator<AutomatonProxy> newiter = mOriginalAutomata.iterator();
    while (olditer.hasNext()) {
      final AutomatonProxy oldaut = olditer.next();
      final AutomatonProxy newaut = newiter.next();
      final StateProxy oldstate = oldmap.get(oldaut);
      if (oldstate != null) {
        final StateProxy newstate = mReverseStateMap.get(oldstate);
        if (newstate == null) {
          newmap.put(newaut, oldstate);
        } else {
          newmap.put(newaut, newstate);
        }
      }
    }
    final EventProxy event = oldstep.getEvent();
    return mFactory.createTraceStepProxy(event, newmap);
  }


  //#########################################################################
  //# Inner Class CertainConflictsKindTranslator
  private class CertainConflictsKindTranslator
    implements KindTranslator
  {

    //#######################################################################
    //# Constructor
    private CertainConflictsKindTranslator(final KindTranslator parent)
    {
      mParentKindTranslator = parent;
    }

    //#######################################################################
    //# Data Members
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      if (aut == mConvertedCertainConflictsAutomaton) {
        return ComponentKind.SPEC;
      } else {
        return ComponentKind.PLANT;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    {
      if (mParentKindTranslator.getEventKind(event) == EventKind.PROPOSITION) {
        return EventKind.PROPOSITION;
      } else if (mUncontrollableEvents.contains(event)) {
        return EventKind.UNCONTROLLABLE;
      } else {
        return EventKind.CONTROLLABLE;
      }
    }

    //#######################################################################
    //# Data Members
    private final KindTranslator mParentKindTranslator;

  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final KindTranslator mKindTranslator;
  private final SafetyVerifier mSafetyVerifier;

  private TraceStepProxy mStartStates;
  private AutomatonProxy mOriginalCertainConflictsAutomaton;
  private AutomatonProxy mConvertedCertainConflictsAutomaton;
  private Collection<EventProxy> mUncontrollableEvents;

  private List<AutomatonProxy> mOriginalAutomata;
  private List<AutomatonProxy> mConvertedAutomata;
  private Map<StateProxy,StateProxy> mReverseStateMap;

}
