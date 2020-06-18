//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import gnu.trove.iterator.TObjectIntIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder;
import net.sourceforge.waters.analysis.tr.AbstractSynchronisationEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class SynthesisStateSpace implements AutomatonProxy
{

  //#########################################################################
  //# Constructor
  public SynthesisStateSpace(final ProductDESProxyFactory factory,
                             final KindTranslator translator,
                             final ProductDESProxy des,
                             final String name)
  {
    mFactory = factory;
    mStateMaps = new LinkedList<>();
    final List<AutomatonProxy> automataList =
      new ArrayList<>(des.getAutomata().size());
    mStateEncodingMap = new HashMap<>(des.getAutomata().size());
    for (final AutomatonProxy aut : des.getAutomata()) {
      switch (translator.getComponentKind(aut)) {
      case PLANT:
      case SPEC:
        automataList.add(aut);
        break;
      default :
        break;
      }
    }
    mDES = factory.createProductDESProxy
      (des.getName(), des.getEvents(), automataList);
    if (name == null) {
      mName = Candidate.getCompositionName("sup:", automataList);
    } else {
      mName = name;
    }
  }

  //#########################################################################
  //# Initialisation
  public SynthesisStateMap createStateEncodingMap
    (final AutomatonProxy automaton, final StateEncoding encoding)
  {
    mStateEncodingMap.put(automaton, encoding);
    return new StateEncodingMap(automaton, encoding);
  }

  public SynthesisStateMap createSynchronisationMap
                            (final AbstractSynchronisationEncoding encoding,
                             final List<SynthesisStateMap> parents)
  {
    boolean merging = false;
    for (final SynthesisStateMap parent : parents) {
      if (parent.isMergibleParent()) {
        merging = true;
        break;
      }
    }
    if (!merging) {
      return new SynchronisationMap(encoding, parents);
    }
    int numParents = 0;
    for (final SynthesisStateMap parent : parents) {
      if (parent.isMergibleParent()) {
        numParents += parent.getParents().size();
      } else {
        numParents++;
      }
    }
    boolean containsBadState = false;
    int i = 0;
    final List<SynthesisStateMap> newParents = new ArrayList<>(numParents);
    final int[] sizes = new int[numParents];
    final List<List<int[]>> inverseMaps = new ArrayList<List<int[]>>();
    for (final SynthesisStateMap parent : parents) {
      if (parent.isMergibleParent()) {
        containsBadState |= parent.containsBadState();
        newParents.addAll(parent.getParents());
        for (final SynthesisStateMap grandparent :parent.getParents()) {
          sizes[i++] = grandparent.getNumberOfMergedStates();
        }
        inverseMaps.add(parent.getInverseMap());
      } else {
        newParents.add(parent);
        sizes[i++] = parent.getNumberOfMergedStates();
        inverseMaps.add(null);
      }
    }
    final AbstractSynchronisationEncoding newEncoding =
      AbstractSynchronisationEncoding.createEncoding(sizes,
                                                     encoding.getMapSize());
    final TObjectIntIterator<int[]> iter = encoding.iterator();
    tuple: while (iter.hasNext()) {
      iter.advance();
      final int[] oldTuple = iter.key();
      final int value = iter.value();
      final int[] newTuple = new int[numParents];
      i = 0;
      for (int j = 0; j < oldTuple.length; j++) {
        final List<int[]> inverseMap = inverseMaps.get(j);
        if (inverseMap == null) {
          newTuple[i++] = oldTuple[j];
        } else {
          final int[] inverse = inverseMap.get(oldTuple[j]);
          if (inverse == null) {
            continue tuple;
          }
          for (int k=0; k< inverse.length; k++) {
            newTuple[i++] = inverse[k];
          }
        }
      }
      newEncoding.addState(newTuple, value);
    }
    return new SynchronisationMap(newEncoding, newParents, containsBadState);
  }



  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public AutomatonProxy clone()
  {
    createAutomaton();
    return mAutomaton.clone();
  }

  @Override
  public String toString()
  {
    return ProxyPrinter.getPrintString(this);
  }


  //#########################################################################
  //# Interface AutomatonProxy
  @Override
  public String getName()
  {
    return mName;
  }

  @Override
  public boolean refequals(final NamedProxy partner)
  {
    return mName.equals(partner.getName());
  }

  @Override
  public int refHashCode()
  {
    return mName.hashCode();
  }

  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return AutomatonProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitAutomatonProxy(this);
  }

  @Override
  public int compareTo(final NamedProxy o)
  {
    return mName.compareTo(o.getName());
  }

  @Override
  public ComponentKind getKind()
  {
    return ComponentKind.SUPERVISOR;
  }

  @Override
  public Set<EventProxy> getEvents()
  {
    createAutomaton();
    return mAutomaton.getEvents();
  }

  @Override
  public Set<StateProxy> getStates()
  {
    createAutomaton();
    return mAutomaton.getStates();
  }

  @Override
  public Collection<TransitionProxy> getTransitions()
  {
    createAutomaton();
    return mAutomaton.getTransitions();
  }

  @Override
  public Map<String,String> getAttributes()
  {
    createAutomaton();
    return mAutomaton.getAttributes();
  }


  //#########################################################################
  //# Simple Access
  public boolean isSafeState(final Map<AutomatonProxy,StateProxy> tuple)
  {
    for (final SynthesisStateMap map : mStateMaps) {
      if (map.getStateNumber(tuple) < 0) {
        return false;
      }
    }
    return true;
  }

  public void addStateMap(final SynthesisStateMap map)
  {
    if (map.containsBadState()) {
      mStateMaps.add(map);
    } else {
      for (final SynthesisStateMap parent:map.getParents()){
        addStateMap(parent);
      }
    }
  }

  public int getNumberOfMaps()
  {
    int maps = 0;
    for (final SynthesisStateMap stateMap : mStateMaps) {
      maps += stateMap.getNumberOfMaps();
    }
    return maps;
  }

  public int getMemoryEstimate()
  {
    int mem = 0;
    for (final SynthesisStateMap stateMap : mStateMaps) {
      mem += stateMap.getMemoryEstimate();
    }
    return mem;
  }

  //#########################################################################
  //# Automaton Construction
  public AutomatonProxy createAutomaton()
  {
    if (mAutomaton == null) {
      try {
        final MonolithicSynchronousProductBuilder syncBuilder =
          new MonolithicSynchronousProductBuilder(mDES, mFactory);
        syncBuilder.setOutputName(mName);
        syncBuilder.setOutputKind(ComponentKind.SUPERVISOR);
        syncBuilder.setRemovingSelfloops(true);
        final SynthesisStateCallBack callBack = new SynthesisStateCallBack();
        syncBuilder.setStateCallback(callBack);
        syncBuilder.run();
        mAutomaton = syncBuilder.getComputedAutomaton();
      } catch(final AnalysisException exception) {
        throw exception.getRuntimeException();
      }
    }
    return mAutomaton;
  }


  //#########################################################################
  //# Inner Class
  abstract static class SynthesisStateMap
  {
    abstract Collection<SynthesisStateMap> getParents();

    abstract int getStateNumber(Map<AutomatonProxy,StateProxy> tuple);

    abstract int getNumberOfMaps();

    abstract int getMemoryEstimate();

    abstract boolean containsBadState();

    abstract boolean isMergibleParent();

    abstract int getNumberOfMergedStates();

    abstract List<int[]> getInverseMap();

    SynthesisStateMap compose(final TRPartition partition)
    {
      return new PartitionMap(partition, this);
    }
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
    Collection<SynthesisStateMap> getParents()
    {
      return Collections.emptyList();
    }

    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final StateProxy state = tuple.get(mAutomaton);
      return mStateEncoding.getStateCode(state);
    }

    @Override
    int getNumberOfMaps()
    {
      return 0;
    }

    @Override
    int getMemoryEstimate()
    {
      return 0;
    }

    @Override
    boolean containsBadState()
    {
      return false;
    }

    @Override
    boolean isMergibleParent()
    {
      return false;
    }

    @Override
    int getNumberOfMergedStates()
    {
       return mStateEncoding.getNumberOfStates();
    }

    @Override
    List<int[]> getInverseMap()
    {
      final int numStates = mStateEncoding.getNumberOfStates();
      final List<int[]> list = new ArrayList<>();
      for (int i = 0; i < numStates; i++) {
        final int[] array = new int[1];
        array[0] = i;
        list.add(array);
      }
      return list;
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
      mContainsMergedStates = partition.containsMergedStates();
      mStateToClass = partition.getStateToClass();
      mParent = parent;
      mNumberOfClasses = partition.getNumberOfClasses();
    }

    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    Collection<SynthesisStateMap> getParents()
    {
      return Collections.singletonList(mParent);
    }

    @Override
    int getStateNumber(final Map<AutomatonProxy,StateProxy> tuple)
    {
      final int state = mParent.getStateNumber(tuple);
      return state < 0 ? -1 : mStateToClass[state];
    }

    @Override
    int getNumberOfMaps()
    {
      return 1 + mParent.getNumberOfMaps();
    }

    @Override
    int getMemoryEstimate()
    {
      return mStateToClass.length + mParent.getMemoryEstimate();
    }

    @Override
    boolean containsBadState()
    {
      for (final int state: mStateToClass) {
        if (state < 0) {
          return true;
        }
      }
      return false;
    }

    @Override
    boolean isMergibleParent()
    {
      return !mContainsMergedStates;
    }

    @Override
    int getNumberOfMergedStates()
    {
      return mNumberOfClasses;
    }

    @Override
    List<int[]> getInverseMap()
    {
      final TRPartition partition =
        new TRPartition(mStateToClass, mNumberOfClasses);
      return partition.getClasses();
    }

    @Override
    SynthesisStateMap compose(final TRPartition partition)
    {
      final TRPartition parentPartition =
        new TRPartition(mStateToClass, mNumberOfClasses);
      final TRPartition newPartition =
        TRPartition.combine(parentPartition, partition);
      mStateToClass = newPartition.getStateToClass();
      mNumberOfClasses = newPartition.getNumberOfClasses();
      mContainsMergedStates |= newPartition.containsMergedStates();
      return this;
    }


    //#######################################################################
    //# Data Members
    private final SynthesisStateMap mParent;
    private int[] mStateToClass;
    private int mNumberOfClasses;
    private boolean mContainsMergedStates;
  }


  //#########################################################################
  //# Inner Class
  private static class SynchronisationMap extends SynthesisStateMap
  {
    //#######################################################################
    //# Constructor
    private SynchronisationMap(final AbstractSynchronisationEncoding encoding,
                               final List<SynthesisStateMap> parents)
    {
      this(encoding,parents,false);
    }

    private SynchronisationMap(final AbstractSynchronisationEncoding encoding,
                               final List<SynthesisStateMap> parents,
                               final boolean containsBadState)
    {
      mSynchronisationEncoding = encoding;
      mParents = parents;
      mContainsBadState = containsBadState;
    }


    //#######################################################################
    //# Override for SynthesisStateSpace
    @Override
    Collection<SynthesisStateMap> getParents()
    {
      return mParents;
    }

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
      return mSynchronisationEncoding.getStateCode(tuple);
    }

    @Override
    int getNumberOfMaps()
    {
      int maps = 0;
      for (final SynthesisStateMap parent : mParents) {
        maps += parent.getNumberOfMaps();
      }
      return 1 + maps;
    }

    @Override
    int getMemoryEstimate()
    {
      int mem = 0;
      for (final SynthesisStateMap parent :mParents) {
        mem += parent.getMemoryEstimate();
      }
      return mSynchronisationEncoding.getMemoryEstimate()+mem;
    }

    @Override
    boolean containsBadState()
    {
      return mContainsBadState;
    }

    @Override
    boolean isMergibleParent()
    {
      return !mContainsMergedStates;
    }

    @Override
    int getNumberOfMergedStates()
    {
      return mSynchronisationEncoding.getMapSize();
    }

    @Override
    List<int[]> getInverseMap()
    {
      return mSynchronisationEncoding.getInverseMap();
    }

    @Override
    SynthesisStateMap compose(final TRPartition partition)
    {
      mContainsBadState |= mSynchronisationEncoding.compose(partition);
      mContainsMergedStates |= partition.containsMergedStates();
      return this;
    }

    //#######################################################################
    //# Data Members
    private final AbstractSynchronisationEncoding mSynchronisationEncoding;
    private final List<SynthesisStateMap> mParents;
    private boolean mContainsBadState;
    private boolean mContainsMergedStates = false;


  }


  //#########################################################################
  //# Inner class
  private class SynthesisStateCallBack implements
    MonolithicSynchronousProductBuilder.StateCallback
  {
    //#######################################################################
    //# Interface MonolithicSynchronousProductBuilder.StateCallBack
    @Override
    public boolean newState(final int[] tuple) throws OverflowException
    {
      final Set<AutomatonProxy> automata = mDES.getAutomata();
      final Map<AutomatonProxy,StateProxy> map =
        new HashMap<>(automata.size());
      int i = 0;
      for (final AutomatonProxy aut : automata) {
        final StateEncoding encoding = mStateEncodingMap.get(aut);
        final int s = tuple[i];
        final StateProxy state = encoding.getState(s);
        map.put(aut, state);
        i++;
      }
      return isSafeState(map);
    }

    @Override
    public void recordStatistics(final AutomatonResult result)
    {
    }
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxyFactory mFactory;
  private final ProductDESProxy mDES;
  private final Map<AutomatonProxy,StateEncoding> mStateEncodingMap;
  private final List<SynthesisStateMap> mStateMaps;
  private final String mName;
  private AutomatonProxy mAutomaton;

}
