//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.samples.algorithms
//# CLASS:   Bisimulation
//###########################################################################
//# $Id: Bisimulation.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.samples.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class Bisimulation {

  //#########################################################################
  //# Invocation
  public static AutomatonProxy reduce(final AutomatonProxy aut)
    throws DuplicateNameException
  {
    final String name = aut.getName();
    return reduce(aut, name);
  }

  public static AutomatonProxy reduce(final AutomatonProxy aut,
				      final String resultname)
    throws DuplicateNameException
  {
    final Bisimulation solver = new Bisimulation(aut, resultname);
    return solver.run();
  }


  //#########################################################################
  //# Constructors
  private Bisimulation(final AutomatonProxy aut, final String resultname)
  {
    final Collection states = aut.getStates();
    mAutomaton = aut;
    mResultName = resultname;
    mAdjacency = new AdjacencyMap(aut, AdjacencyMap.EXITING);
    mClassCodeMap = null;
    mStateCodeMap = null;
    mNewStateCodeMap = null;
    mNextCode = 0;
  }


  //#########################################################################
  //# Workhorse Methods
  private AutomatonProxy run()
    throws DuplicateNameException
  {
    final Collection states0 = mAutomaton.getStates();
    final List states = new ArrayList(states0);
    final int numstates = states.size();
    Collections.sort(states);
    mClassCodeMap = new HashMap(numstates);
    mNewStateCodeMap = new HashMap(numstates);

    final Iterator iter1 = states.iterator();
    while (iter1.hasNext()) {
      final StateProxy state = (StateProxy) iter1.next();
      final Set classinfo = createClass(state);
      recordClass(state, classinfo);
    }
    mStateCodeMap = mNewStateCodeMap;
    
    int prevnumcodes = 0;
    while (mNextCode > prevnumcodes && mNextCode < numstates) {
      prevnumcodes = mNextCode;
      mNextCode = 0;
      mClassCodeMap = new HashMap(numstates);
      mNewStateCodeMap = new HashMap(numstates);
      final Iterator iter2 = states.iterator();
      while (iter2.hasNext()) {
	final StateProxy state = (StateProxy) iter2.next();
	final Set classinfo = createClass(state);
	recordClass(state, classinfo);
      }
      mStateCodeMap = mNewStateCodeMap;
    }

    final ComponentKind kind = mAutomaton.getKind();
    final AutomatonProxy result = new AutomatonProxy(mResultName, kind);
    final int numclasses = mNextCode;
    final DemiState[] demistates = new DemiState[numclasses];
    for (int code = 0; code < numclasses; code++) {
      demistates[code] = new DemiState(code);
    }
    final Iterator iter3 = states.iterator();
    while (iter3.hasNext()) {
      final StateProxy state = (StateProxy) iter3.next();
      final Integer codeobj = (Integer) mStateCodeMap.get(state);
      final int code = codeobj.intValue();
      demistates[code].includeState(state);
    }
    final StateProxy[] newstates = new StateProxy[numclasses];
    for (int code = 0; code < numclasses; code++) {
      final StateProxy state = demistates[code].createState();
      newstates[code] = state;
      result.addState(state);
    }
    final Collection transitions = mAutomaton.getTransitions();
    final Set newtransitions = new HashSet();
    final Iterator iter4 = transitions.iterator();
    while (iter4.hasNext()) {
      final TransitionProxy oldtrans = (TransitionProxy) iter4.next();
      final StateProxy oldsource = oldtrans.getSource();
      final Integer oldsourcecodeobj = (Integer) mStateCodeMap.get(oldsource);
      final int oldsourcecode = oldsourcecodeobj.intValue();
      final StateProxy newsource = newstates[oldsourcecode];
      final StateProxy oldtarget = oldtrans.getTarget();
      final Integer oldtargetcodeobj = (Integer) mStateCodeMap.get(oldtarget);
      final int oldtargetcode = oldtargetcodeobj.intValue();
      final StateProxy newtarget = newstates[oldtargetcode];
      final EventProxy event = oldtrans.getEvent();
      final TransitionProxy newtrans =
	new TransitionProxy(newsource, newtarget, event);
      if (newtransitions.add(newtrans)) {
	result.addTransition(newtrans);
      }
    }

    System.err.println
      ("Bisimulation reduction: " + 
       numstates + " >> " + numclasses + " states, " +
       transitions.size() + " >> " + newtransitions.size() + " transitions");
    return result;
  }

  private Set createClass(final StateProxy state)
  {
    final Set result = new HashSet();
    int code = 0;
    if (mStateCodeMap != null) {
      final Integer codeobj = (Integer) mStateCodeMap.get(state);
      code = codeobj.intValue();
    }
 
    final Collection propositions = state.getPropositions();
    final Iterator propiter = propositions.iterator();
    while (propiter.hasNext()) {
      final EventProxy event = (EventProxy) propiter.next();
      final DemiTransition demi = new DemiTransition(event, code);
      result.add(demi);
    }

    final Collection exits = mAdjacency.getExitingTransitions(state);
    final Iterator exititer = exits.iterator();
    while (exititer.hasNext()) {
      final TransitionProxy trans = (TransitionProxy) exititer.next();
      final EventProxy event = trans.getEvent();
      if (mStateCodeMap != null) {
	final StateProxy target = trans.getTarget();
	final Integer codeobj = (Integer) mStateCodeMap.get(target);
	code = codeobj.intValue();
      }
      final DemiTransition demi = new DemiTransition(event, code);
      result.add(demi);
    }

    return result;
  }

  private void recordClass(final StateProxy state, final Set classinfo)
  {
    final Integer codeobj = (Integer) mClassCodeMap.get(classinfo);
    if (codeobj != null) {
      mNewStateCodeMap.put(state, codeobj);
    } else {
      final Integer newcodeobj = new Integer(mNextCode++);
      mClassCodeMap.put(classinfo, newcodeobj);
      mNewStateCodeMap.put(state, newcodeobj);
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
      mPropositions = new LinkedList();
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

    private StateProxy createState()
    {
      return new StateProxy(getName(), mInitial, mPropositions);
    }

    //#######################################################################
    //# Data Members
    private final int mCode;
    private StringBuffer mName;
    private boolean mInitial;
    private Collection mPropositions;
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
  private Map mClassCodeMap;
  private Map mStateCodeMap;
  private Map mNewStateCodeMap;
  private int mNextCode;

}

