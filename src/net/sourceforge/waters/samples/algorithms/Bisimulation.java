//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   Bisimulation
//###########################################################################
//# $Id: Bisimulation.java,v 1.4 2006-07-20 02:28:37 robi Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class Bisimulation {

  //#########################################################################
  //# Invocation
  public static AutomatonProxy reduce(final AutomatonProxy aut,
                                      final ProductDESProxyFactory factory)
  {
    final String name = aut.getName();
    return reduce(aut, name, factory);
  }

  public static AutomatonProxy reduce(final AutomatonProxy aut,
                                      final String resultname,
                                      final ProductDESProxyFactory factory)
  {
    final Bisimulation solver = new Bisimulation(aut, resultname, factory);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private Bisimulation(final AutomatonProxy aut,
                       final String resultname,
                       final ProductDESProxyFactory factory)
  {
    mAutomaton = aut;
    mResultName = resultname;
    mAdjacency = new AdjacencyMap(aut, AdjacencyMap.EXITING);
    mFactory = factory;
    mClassCodeMap = null;
    mStateCodeMap = null;
    mNewStateCodeMap = null;
    mNextCode = 0;
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
  {
    final Collection<StateProxy> states0 = mAutomaton.getStates();
    final List<StateProxy> states = new ArrayList<StateProxy>(states0);
    final int numstates = states.size();
    Collections.sort(states);
    mClassCodeMap = new HashMap<Set<DemiTransition>,Integer>(numstates);
    mNewStateCodeMap = new HashMap<StateProxy,Integer>(numstates);

    for (final StateProxy state : states) {
      final Set<DemiTransition> classinfo = createClass(state);
      recordClass(state, classinfo);
    }
    mStateCodeMap = mNewStateCodeMap;
    
    int prevnumcodes = 0;
    while (mNextCode > prevnumcodes && mNextCode < numstates) {
      prevnumcodes = mNextCode;
      mNextCode = 0;
      mClassCodeMap = new HashMap<Set<DemiTransition>,Integer>(numstates);
      mNewStateCodeMap = new HashMap<StateProxy,Integer>(numstates);
      for (final StateProxy state : states) {
        final Set<DemiTransition> classinfo = createClass(state);
        recordClass(state, classinfo);
      }
      mStateCodeMap = mNewStateCodeMap;
    }

    final ComponentKind kind = mAutomaton.getKind();
    final int numclasses = mNextCode;
    final DemiState[] demistates = new DemiState[numclasses];
    for (int code = 0; code < numclasses; code++) {
      demistates[code] = new DemiState(code);
    }
    for (final StateProxy state : states) {
      final int code = mStateCodeMap.get(state);
      demistates[code].includeState(state);
    }
    final StateProxy[] newstates = new StateProxy[numclasses];
    final Collection<StateProxy> resultstates = new LinkedList<StateProxy>();
    for (int code = 0; code < numclasses; code++) {
      final StateProxy state = demistates[code].createState(mFactory);
      newstates[code] = state;
      resultstates.add(state);
    }
    final Collection<TransitionProxy> transitions =
      mAutomaton.getTransitions();
    final Set<TransitionProxy> resulttrans = new HashSet<TransitionProxy>();
    for (final TransitionProxy oldtrans : transitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final int oldsourcecode = mStateCodeMap.get(oldsource);
      final StateProxy newsource = newstates[oldsourcecode];
      final StateProxy oldtarget = oldtrans.getTarget();
      final int oldtargetcode = mStateCodeMap.get(oldtarget);
      final StateProxy newtarget = newstates[oldtargetcode];
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        mFactory.createTransitionProxy(newsource, event, newtarget);
      resulttrans.add(newtrans);
    }
    final Collection<EventProxy> events = mAutomaton.getEvents();

    System.err.println
      ("Bisimulation reduction: " + 
       numstates + " >> " + numclasses + " states, " +
       transitions.size() + " >> " + resulttrans.size() + " transitions");

    return mFactory.createAutomatonProxy
      (mResultName, kind, events, resultstates, resulttrans);
  }

  private Set<DemiTransition> createClass(final StateProxy state)
  {
    final Set<DemiTransition> result = new HashSet<DemiTransition>();
    int code = 0;
    if (mStateCodeMap != null) {
      code = mStateCodeMap.get(state);
    }
    final Collection<EventProxy> propositions = state.getPropositions();
    for (final EventProxy event : propositions) {
      final DemiTransition demi = new DemiTransition(event, code);
      result.add(demi);
    }
    final Collection<TransitionProxy> exits =
      mAdjacency.getExitingTransitions(state);
    for (final TransitionProxy trans : exits) {
      final EventProxy event = trans.getEvent();
      if (mStateCodeMap != null) {
        final StateProxy target = trans.getTarget();
        code = mStateCodeMap.get(target);
      }
      final DemiTransition demi = new DemiTransition(event, code);
      result.add(demi);
    }
    return result;
  }

  private void recordClass(final StateProxy state,
                           final Set<DemiTransition> classinfo)
  {
    final Integer codeobj = mClassCodeMap.get(classinfo);
    if (codeobj != null) {
      mNewStateCodeMap.put(state, codeobj);
    } else {
      final int newcode = mNextCode++;
      mClassCodeMap.put(classinfo, newcode);
      mNewStateCodeMap.put(state, newcode);
    }
  }


  //#########################################################################
  //# Local Class DemiState
  private static class DemiState {

    //#######################################################################
    //# Constructors
    private DemiState(final int code)
    {
      mCode = code;
      mName = new StringBuffer();
      mInitial = false;
      mPropositions = new TreeSet<EventProxy>();
    }

    //#######################################################################
    //# Adding Information
    private void includeState(final StateProxy state)
    {
      if (mName != null) {
        final String name = state.getName();
        if (mName.length() + name.length() > 12 || name.indexOf(":b") >= 0) {
          mName = null;
        } else {
          if (mName.length() > 0) {
            mName.append(':');
          }
          mName.append(name);
        }
      }
      if (state.isInitial()) {
        mInitial = true;
      }
      mPropositions.addAll(state.getPropositions());
    }

    //#######################################################################
    //# Creating States
    private String getName()
    {
      if (mName == null) {
        return ":b" + mCode;
      } else {
        return mName.toString();
      }
    }

    private StateProxy createState(final ProductDESProxyFactory factory)
    {
      return factory.createStateProxy(getName(), mInitial, mPropositions);
    }

    //#######################################################################
    //# Data Members
    private final int mCode;
    private final Set<EventProxy> mPropositions;
    private StringBuffer mName;
    private boolean mInitial;
  }


  //#########################################################################
  //# Local Class DemiTransitions
  private static class DemiTransition {

    //#######################################################################
    //# Constructors
    private DemiTransition(final EventProxy event, final int target)
    {
      mEvent = event;
      mTarget = target;
    }

    //#######################################################################
    //# Equals and HashCode
    public boolean equals(final Object partner)
    {
      if (partner != null && partner.getClass() == getClass()) {
        final DemiTransition trans = (DemiTransition) partner;
        return mEvent == trans.mEvent && mTarget == trans.mTarget;
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mEvent.hashCode() + 5 * mTarget;
    }

    //#######################################################################
    //# Data Members
    private final EventProxy mEvent;
    private final int mTarget;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final String mResultName;
  private final AdjacencyMap mAdjacency;
  private final ProductDESProxyFactory mFactory;
  private Map<Set<DemiTransition>,Integer> mClassCodeMap;
  private Map<StateProxy,Integer> mStateCodeMap;
  private Map<StateProxy,Integer> mNewStateCodeMap;
  private int mNextCode;

}

