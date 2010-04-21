//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   StateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gnu.trove.TObjectIntHashMap;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Robi Malik
 */

public class StateEncoding
{

  //#########################################################################
  //# Constructor
  public StateEncoding()
  {
    mStates = null;
    mStateCodeMap = null;
  }

  public StateEncoding(final AutomatonProxy aut)
  {
    this(aut.getStates());
  }

  public StateEncoding(final Collection<? extends StateProxy> states)
  {
    init(states);
  }

  public StateEncoding(final StateProxy[] states)
  {
    init(states);
  }


  //#########################################################################
  //# Initialisation
  public void init(final StateProxy[] states)
  {
    final List<StateProxy> list = Arrays.asList(states);
    init(list);
  }

  public void init(final Collection<? extends StateProxy> states)
  {
    final int numStates = states.size();
    mStates = new StateProxy[numStates];
    mStateCodeMap = new TObjectIntHashMap<StateProxy>(numStates);
    int code = 0;
    for (final StateProxy state : states) {
      mStates[code] = state;
      if (state != null) {
        mStateCodeMap.put(state, code);
      }
      code++;
    }
  }


  //#########################################################################
  //# Simple Access
  public int getNumberOfStates()
  {
    return mStates.length;
  }

  public int getStateCode(final StateProxy state)
  {
    if (mStateCodeMap.containsKey(state)) {
      return mStateCodeMap.get(state);
    } else {
      return -1;
    }
  }

  public StateProxy getState(final int code)
  {
    return mStates[code];
  }

  public List<StateProxy> getStates()
  {
    return Arrays.asList(mStates);
  }


  //#########################################################################
  //# Data Members
  private StateProxy[] mStates;
  private TObjectIntHashMap<StateProxy> mStateCodeMap;

}
