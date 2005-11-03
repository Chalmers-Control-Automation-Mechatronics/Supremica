//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   ConflictEquiv
//###########################################################################
//# $Id: ConflictEquiv.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class ConflictEquiv {

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
    final ConflictEquiv solver = new ConflictEquiv(aut, resultname, factory);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private ConflictEquiv(final AutomatonProxy aut,
                        final String resultname,
                        final ProductDESProxyFactory factory)
  {
    final Collection states = aut.getStates();
    final int numstates = states.size();
    mAutomaton = aut;
    mResultName = resultname;
    mAdjacency = new AdjacencyMap(aut);
    mStateMap = new IdentityHashMap<StateProxy,Integer>(numstates);
    mFactory = factory;
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
  {
    final Collection<StateProxy> states0 = mAutomaton.getStates();
    final List<StateProxy> states = new ArrayList<StateProxy>(states0);
    final int orignumstates = states.size();
    Collections.sort(states);

    int code = 0;
    int numstates = orignumstates;
    for (final StateProxy state : states) {
      final int entry = code++;
      mStateMap.put(state, entry);
    }

    int step = 0;
    while (true) {
      final Map<StateInfo,StateInfo> infomap =
	new HashMap<StateInfo,StateInfo>(orignumstates);
      for (final StateProxy state : states) {
        final boolean initial = state.isInitial();
        final Collection<TransitionProxy> incoming =
	  mAdjacency.getEnteringTransitions(state);
        final Set<DemiTransition> demi = createDemiTransitions(incoming);
        final Set<EventProxy> elig = mAdjacency.getEligibleEvents(state);
        final Collection<EventProxy> props = state.getPropositions();
        elig.addAll(props);
        final StateInfo newinfo = new StateInfo(initial, demi, elig);
        StateInfo info = infomap.get(newinfo);
        if (info == null) {
          info = newinfo;
          infomap.put(info, info);
        }
        info.addState(state);
      }

      final Set<StateInfo> reducedstates = infomap.keySet();
      final int newnumstates = reducedstates.size();
      if (newnumstates == numstates) {
        break;
      }
      step++;
      numstates = newnumstates;

      code = 0;
      for (final StateInfo info : reducedstates) {
        final int entry = code++;
        final Collection<StateProxy> infostates = info.getStates();
	for (final StateProxy state : infostates) {
          mStateMap.put(state, entry);
        }
      }
    }

    final ComponentKind kind = mAutomaton.getKind();
    final DemiState[] demistates = new DemiState[numstates];
    for (code = 0; code < numstates; code++) {
      demistates[code] = new DemiState(code);
    }
    for (final StateProxy state : states) {
      code = mStateMap.get(state);
      demistates[code].includeState(state);
    }
    final StateProxy[] newstates = new StateProxy[numstates];
    final Collection<StateProxy> resultstates = new LinkedList<StateProxy>();
    for (code = 0; code < numstates; code++) {
      final StateProxy state = demistates[code].createState(mFactory);
      newstates[code] = state;
      resultstates.add(state);
    }
    final Collection<TransitionProxy> transitions =
      mAutomaton.getTransitions();
    final Set<TransitionProxy> resulttrans = new HashSet<TransitionProxy>();
    for (final TransitionProxy oldtrans : transitions) {
      final StateProxy oldsource = oldtrans.getSource();
      final int oldsourcecode = mStateMap.get(oldsource);
      final StateProxy newsource = newstates[oldsourcecode];
      final StateProxy oldtarget = oldtrans.getTarget();
      final int oldtargetcode = mStateMap.get(oldtarget);
      final StateProxy newtarget = newstates[oldtargetcode];
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
        mFactory.createTransitionProxy(newsource, event, newtarget);
      resulttrans.add(newtrans);
    }
    final Collection<EventProxy> events = mAutomaton.getEvents();

    System.err.println
      ("B Reduction in " + step + " steps: " +
       states.size() + " >> " + numstates + " states, " +
       transitions.size() + " >> " + resulttrans.size() + " transitions");

    return mFactory.createAutomatonProxy
      (mResultName, kind, events, resultstates, resulttrans);
  }


  private Set<DemiTransition> createDemiTransitions
    (final Collection<TransitionProxy> transitions)
  {
    final int numtrans = transitions.size();
    final Set<DemiTransition> result = new HashSet<DemiTransition>(numtrans);
    for (final TransitionProxy trans : transitions) {
      final StateProxy source = trans.getSource();
      final EventProxy event = trans.getEvent();
      final int code = mStateMap.get(source);
      final DemiTransition demi = new DemiTransition(code, event);
      result.add(demi);
    }
    return result;
  }


  //#########################################################################
  //# Local Class StateInfo
  private static class StateInfo {

    //#######################################################################
    //# Constructors
    private StateInfo(final boolean initial,
                      final Set<DemiTransition> incoming,
                      final Set<EventProxy> elig)
    {
      mIsInitial = initial;
      mIncoming = incoming;
      mElig = elig;
      mStates = new LinkedList<StateProxy>();
    }

    //#######################################################################
    //# Equals and Hashcode
    public boolean equals(final Object partner)
    {
      if (partner != null && getClass() == partner.getClass()) {
        final StateInfo info = (StateInfo) partner;
        return
          mIsInitial == info.mIsInitial &&
          mIncoming.equals(info.mIncoming) &&
          mElig.equals(info.mElig);
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return
        mIncoming.hashCode() +
        5 * mElig.hashCode() +
        (mIsInitial ? 25 : 0);
    }

    //#######################################################################
    //# Simple Access
    private Collection<StateProxy> getStates()
    {
      return mStates;
    }

    private void addState(final StateProxy state)
    {
      mStates.add(state);
    }

    //#######################################################################
    //# Data Members
    private final boolean mIsInitial;
    private final Set<DemiTransition> mIncoming;
    private final Set<EventProxy> mElig;
    private final Collection<StateProxy> mStates;

  }


  //#########################################################################
  //# Local Class DemiTransition
  private static class DemiTransition {

    //#######################################################################
    //# Constructors
    private DemiTransition(final int source, final EventProxy event)
    {
      mSource = source;
      mEvent = event;
    }

    //#######################################################################
    //# Equals and HashCode
    public boolean equals(final Object partner)
    {
      if (partner != null && partner.getClass() == getClass()) {
        final DemiTransition trans = (DemiTransition) partner;
        return mSource == trans.mSource && mEvent == trans.mEvent;
      } else {
        return false;
      }
    }

    public int hashCode()
    {
      return mEvent.hashCode() + 5 * mSource;
    }

    //#######################################################################
    //# Data Members
    private final int mSource;
    private final EventProxy mEvent;
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
      mPropositions = new LinkedList<EventProxy>();
    }

    //#######################################################################
    //# Adding Information
    private void includeState(final StateProxy state)
    {
      if (mName != null) {
        final String name = state.getName();
        if (mName.length() + name.length() > 12 || name.indexOf(":") >= 0) {
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
        return ":c" + mCode;
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
    private StringBuffer mName;
    private boolean mInitial;
    private Collection<EventProxy> mPropositions;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final String mResultName;
  private final AdjacencyMap mAdjacency;
  private final Map<StateProxy,Integer> mStateMap;
  private final ProductDESProxyFactory mFactory;

}

