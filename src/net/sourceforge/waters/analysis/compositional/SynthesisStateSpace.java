//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   SynthesisStateSpace
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.map.hash.TLongIntHashMap;

import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.tr.LongSynchronisationEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class SynthesisStateSpace
{

  //#########################################################################
  //# Constructor
  public SynthesisStateSpace(final SynthesisStateMap stateMap)
  {
    mStateMap = stateMap;
  }

  public static SynthesisStateMap createStateEncodingMap
    (final AutomatonProxy automaton, final StateEncoding encoding)
  {
    return new StateEncodingMap(automaton, encoding);
  }

  public static SynthesisStateMap createPartitionMap
  (final TRPartition partition, final SynthesisStateMap parent)
  {
    return new PartitionMap(partition, parent);
  }

  public static SynthesisStateMap createSynchronisationMap
                            (final TLongIntHashMap map,
                             final LongSynchronisationEncoding encoding,
                             final List<SynthesisStateMap> parents)
  {
    return new SynchronisationMap( map, encoding, parents);
  }

  //#########################################################################
  //# Simple Access
  public boolean isSafeState(final Map<AutomatonProxy,StateProxy> tuple)
  {
    return mStateMap.getStateNumber(tuple) >= 0;
  }

  //#########################################################################
  //# Inner Class
  abstract static class SynthesisStateMap
  {
    abstract int getStateNumber(Map<AutomatonProxy,StateProxy> tuple);
  }


  //#########################################################################
  //# Inner Class
  private static class StateEncodingMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private StateEncodingMap(final AutomatonProxy automaton,
                             final StateEncoding encoding)
    {
      mAutomaton = automaton;
      mStateEncoding = encoding;
    }
    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final StateProxy state = tuple.get(mAutomaton);
      return mStateEncoding.getStateCode(state);
    }

    //#######################################################################
    //# Data Members
    private final AutomatonProxy mAutomaton;
    private final StateEncoding mStateEncoding;
  }


  //#########################################################################
  //# Inner Class
  private static class PartitionMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private PartitionMap(final TRPartition partition,
                         final SynthesisStateMap parent)
    {
      mStateToClass = partition.getStateToClass();
      mParent = parent;
    }

    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final int state = mParent.getStateNumber(tuple);
      return state < 0 ? -1 : mStateToClass[state];
    }

    //#######################################################################
    //# Data Members
    private final int[] mStateToClass;
    private final SynthesisStateMap mParent;
  }


  //#########################################################################
  //# Inner Class
  private static class SynchronisationMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private SynchronisationMap(final TLongIntHashMap map,
                               final LongSynchronisationEncoding encoding,
                               final List<SynthesisStateMap> parents)
    {
      mSynchronisationMap = map;
      mSynchronisationEncoding = encoding;
      mParents = parents;
    }

    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> fullTuple)
    {
      final int[] tuple = new int[mParents.size()];
      for (int i = 0; i < mParents.size(); i++) {
        tuple[i] = mParents.get(i).getStateNumber(fullTuple);
        if (tuple[i] < 0) {
          return -1;
        }
      }
      final long key = mSynchronisationEncoding.encode(tuple);
      return mSynchronisationMap.get(key);
    }

    //#######################################################################
    //# Data Members
    private final TLongIntHashMap mSynchronisationMap;
    private final LongSynchronisationEncoding mSynchronisationEncoding;
    private final List<SynthesisStateMap> mParents;
  }


  //#########################################################################
  //# Data Members
  private final SynthesisStateMap mStateMap;
}
