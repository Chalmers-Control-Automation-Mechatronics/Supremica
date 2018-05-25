//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.plain.base.AbstractNamedElement;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * <P>An alternative implementation of the {@link AutomatonProxy} interface
 * based on transition relations.</P>
 *
 * <P>A <CODE>TRAutomatonProxy</CODE> object encapsulates an
 * {@link EventEncoding} and a {@link ListBufferTransitionRelation}, which
 * can be accessed like an {@link AutomatonProxy}, with object creation
 * deferred to save memory and improve performance:</P>
 * <UL>
 * <LI>{@link EventProxy} objects are retrieved from the event encoding.</LI>
 * <LI>{@link StateProxy} objects are created on demand when a state is
 *     first requested. Then they are remembered to make sure that subsequent
 *     calls to retrieve the same state return the same object.</LI>
 * <LI>{@link TransitionProxy} objects are only created and returned on
 *     demand. If the same transition is retrieved twice, two different
 *     objects will be returned.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class TRAutomatonProxy
  extends AbstractNamedElement
  implements AutomatonProxy
{

  //#########################################################################
  //# Factory Methods
  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return createTRAutomatonProxy(aut, translator);
  }

  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut, final KindTranslator translator)
    throws OverflowException
  {
    return createTRAutomatonProxy(aut, translator, 0);
  }

  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut,
     final KindTranslator translator,
     final int config)
    throws OverflowException
  {
    if (aut instanceof TRAutomatonProxy) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      if (config != 0) {
        rel.reconfigure(config);
      }
      return tr;
    } else if (config != 0) {
      return new TRAutomatonProxy(aut, translator, config);
    } else {
      return new TRAutomatonProxy(aut, translator,
                                  ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
  }


  //#########################################################################
  //# Constructors
  public TRAutomatonProxy(final EventEncoding eventEnc,
                          final ListBufferTransitionRelation rel)
  {
    rel.checkEventStatusProvider(eventEnc);
    mEventEncoding = eventEnc;
    mStates = null;
    mTransitionRelation = rel;
    provideTauEvent();
  }

  public TRAutomatonProxy(final AutomatonProxy aut,
                          final KindTranslator translator,
                          final int config)
    throws OverflowException
  {
    this(aut, new EventEncoding(aut, translator), config);
  }

  public TRAutomatonProxy(final AutomatonProxy aut,
                          final EventEncoding eventEnc,
                          final int config)
    throws OverflowException
  {
    this(aut, eventEnc, null, config);
  }

  public TRAutomatonProxy(final AutomatonProxy aut,
                          final EventEncoding eventEnc,
                          final StateProxy dumpState,
                          final int config)
    throws OverflowException
  {
    this(aut, eventEnc, new StateEncoding(aut), dumpState, config);
  }

  public TRAutomatonProxy(final AutomatonProxy aut,
                          final EventEncoding eventEnc,
                          final StateEncoding stateEnc,
                          final StateProxy dumpState,
                          final int config)
    throws OverflowException
  {
    mEventEncoding = eventEnc;
    mTransitionRelation = new ListBufferTransitionRelation
      (aut, mEventEncoding, stateEnc, dumpState, config);
    mStates = null;
    final int numStates = stateEnc.getNumberOfStates();
    mStateNames = new String[numStates];
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      mStateNames[s] = state.getName();
    }
    provideTauEvent();
  }

  public TRAutomatonProxy(final TRAutomatonProxy aut)
  {
    mEventEncoding = new EventEncoding(aut.mEventEncoding);
    final ListBufferTransitionRelation rel = aut.mTransitionRelation;
    final int config = rel.getConfiguration();
    mTransitionRelation =
      new ListBufferTransitionRelation(rel, mEventEncoding, config);
    mStates = null;
    mStateNames = aut.mStateNames;
  }

  public TRAutomatonProxy(final TRAutomatonProxy aut, final int config)
  {
    mEventEncoding = new EventEncoding(aut.mEventEncoding);
    final ListBufferTransitionRelation rel = aut.mTransitionRelation;
    mTransitionRelation =
      new ListBufferTransitionRelation(rel, mEventEncoding, config);
    mStates = null;
    mStateNames = aut.mStateNames;
  }


  //#########################################################################
  //# Specific Access
  public EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  public ListBufferTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  public StateProxy getState(final int stateIndex)
  {
    if (mStates == null) {
      mStates = new TRStateList();
    }
    return mStates.createState(stateIndex);
  }

  public int getStateIndex(final StateProxy state)
  {
    final TRState trState = (TRState) state;
    return trState.getStateIndex();
  }

  /**
   * Clears all state names. When a TRAutomatonProxy object is created from
   * an automaton, it remembers the state names of the input automaton.
   * However, as the transition relation is modified, the original state
   * names may longer be accurate. Then this method can be used to reset
   * all state names and use automatically generated names based on the
   * state numbers instead.
   */
  public void resetStateNames()
  {
    mStateNames = null;
  }

  public void setKind(final ComponentKind kind)
  {
    mTransitionRelation.setKind(kind);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.AutomatonProxy
  @Override
  public String getName()
  {
    return mTransitionRelation.getName();
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
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitAutomatonProxy(this);
  }

  @Override
  public TRAutomatonProxy clone()
  {
    return new TRAutomatonProxy(this);
  }

  @Override
  public ComponentKind getKind()
  {
    return mTransitionRelation.getKind();
  }

  @Override
  public Set<EventProxy> getEvents()
  {
    return mEventEncoding.getUsedEvents();
  }

  @Override
  public Set<StateProxy> getStates()
  {
    if (mStates == null) {
      mStates = new TRStateList();
    }
    return mStates;
  }

  @Override
  public Collection<TransitionProxy> getTransitions()
  {
    return new TRTransitionCollection();
  }

  @Override
  public Map<String,String> getAttributes()
  {
    return Collections.emptyMap();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void provideTauEvent()
  {
    final String name = mTransitionRelation.getName();
    mEventEncoding.provideTauEvent(name);
  }

  private StateProxy createState(final int index)
  {
    if (mStates == null) {
      mStates = new TRStateList();
    }
    return mStates.createState(index);
  }


  //#########################################################################
  //# Inner Class TRState
  class TRState
    implements StateProxy, Cloneable
  {
    //#######################################################################
    //# Constructors
    private TRState(final int index)
    {
      mIndex = index;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.StateProxy
    @Override
    public String getName()
    {
      if (mStateNames != null) {
        if (mIndex < mStateNames.length) {
          return mStateNames[mIndex];
        } else if (mIndex != mTransitionRelation.getDumpStateIndex()) {
          mStateNames = null;
        }
      }
      return ":" + mIndex;
    }

    @Override
    public boolean isInitial()
    {
      return mTransitionRelation.isInitial(mIndex);
    }

    @Override
    public Collection<EventProxy> getPropositions()
    {
      final long pattern = mTransitionRelation.getAllMarkings(mIndex);
      return mStates.createMarkings(pattern);
    }

    @Override
    public TRState clone()
    {
      try {
        return (TRState) super.clone();
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public boolean refequals(final NamedProxy named)
    {
      if (named != null && named.getClass() == getClass()) {
        final TRState state = (TRState) named;
        return state.mIndex == mIndex;
      } else {
        return false;
      }
    }

    @Override
    public int refHashCode()
    {
      return mIndex;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desvisitor =
        (ProductDESProxyVisitor) visitor;
      return desvisitor.visitStateProxy(this);
    }

    @Override
    public Class<StateProxy> getProxyInterface()
    {
      return StateProxy.class;
    }

    @Override
    public int compareTo(final NamedProxy named)
    {
      final Class<?> clazz1 = getClass();
      final Class<?> clazz2 = named.getClass();
      if (clazz1 == clazz2) {
        final TRState state = (TRState) named;
        return mIndex - state.mIndex;
      }
      final String name1 = getName();
      final String name2 = named.getName();
      final int result = name1.compareTo(name2);
      if (result != 0) {
        return result;
      }
      final String cname1 = clazz1.getName();
      final String cname2 = clazz2.getName();
      return cname1.compareTo(cname2);
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public String toString()
    {
      return ProductDESProxyPrinter.getPrintString(this);
    }

    //#######################################################################
    //# Specific Access
    int getStateIndex()
    {
      return mIndex;
    }

    //#######################################################################
    //# Data Members
    private final int mIndex;
  }


  //#########################################################################
  //# Inner Class TRStateList
  private class TRStateList extends AbstractSet<StateProxy>
  {
    //#######################################################################
    //# Constructor
    private TRStateList()
    {
      mStates = new TRState[mTransitionRelation.getNumberOfStates()];
      mMarkings = null;
    }

    //#######################################################################
    //# Interface java.util.Set<StateProxy>
    @Override
    public Iterator<StateProxy> iterator()
    {
      return new TRStateListIterator();
    }

    @Override
    public int size()
    {
      return mTransitionRelation.getNumberOfReachableStates();
    }

    //#######################################################################
    //# Auxiliary Methods
    private TRState createState(final int index)
    {
      TRState state = mStates[index];
      if (state == null) {
        mStates[index] = state = new TRState(index);
      }
      return state;
    }

    private Collection<EventProxy> createMarkings(long pattern)
    {
      pattern &= mTransitionRelation.getUsedPropositions();
      if (pattern == 0) {
        return Collections.emptyList();
      } else if (mMarkings == null) {
        final int totalProps = mEventEncoding.getNumberOfPropositions();
        final int size = totalProps < 4 ? 1 << totalProps : 16;
        mMarkings = new TLongObjectHashMap<>(size);
      } else {
        final Collection<EventProxy> markings = mMarkings.get(pattern);
        if (markings != null) {
          return markings;
        }
      }
      final int totalProps = mEventEncoding.getNumberOfPropositions();
      int numProps;
      if (totalProps == 1) {
        numProps = 1;
      } else {
        numProps = 0;
        for (int p = 0; p < totalProps; p++) {
          if ((pattern & (1L << p)) != 0) {
            numProps++;
          }
        }
      }
      Collection<EventProxy> markings = null;
      if (numProps == 1) {
        for (int p = 0; p < totalProps; p++) {
          if ((pattern & (1L << p)) != 0) {
            final EventProxy prop = mEventEncoding.getProposition(p);
            markings = Collections.singletonList(prop);
            break;
          }
        }
      } else {
        markings = new ArrayList<>(numProps);
        for (int p = 0; p < totalProps; p++) {
          if ((pattern & (1L << p)) != 0) {
            final EventProxy prop = mEventEncoding.getProposition(p);
            markings.add(prop);
          }
        }
      }
      mMarkings.put(pattern, markings);
      return markings;
    }

    //#######################################################################
    //# Data Members
    private final TRState[] mStates;
    private TLongObjectHashMap<Collection<EventProxy>> mMarkings;
  }


  //#########################################################################
  //# Inner Class TRStateListIterator
  private class TRStateListIterator implements Iterator<StateProxy>
  {
    //#######################################################################
    //# Constructor
    private TRStateListIterator()
    {
      mIndex = 0;
    }

    //#######################################################################
    //# Interface java.util.Iterator<StateProxy>
    @Override
    public boolean hasNext()
    {
      for (; mIndex < mTransitionRelation.getNumberOfStates(); mIndex++) {
        if (mTransitionRelation.isReachable(mIndex)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public StateProxy next()
    {
      if (hasNext()) {
        return mStates.createState(mIndex++);
      } else {
        throw new NoSuchElementException
          ("Attempting to read past end of state list in TRAutomatonProxy!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("TRAutomatonProxy does not support state removal!");
    }

    //#######################################################################
    //# Data Members
    private int mIndex;
  }


  //#########################################################################
  //# Inner Class TRTransitionCollection
  private class TRTransitionCollection
    extends AbstractCollection<TransitionProxy>
  {
    //#######################################################################
    //# Interface java.util.Collection<TransitionProxy>
    @Override
    public Iterator<TransitionProxy> iterator()
    {
      return new TRTransitionCollectionIterator();
    }

    @Override
    public int size()
    {
      return mTransitionRelation.getNumberOfTransitions();
    }
  }


  //#########################################################################
  //# Inner Class TRTransitionCollectionIterator
  private class TRTransitionCollectionIterator
    implements Iterator<TransitionProxy>
  {
    //#######################################################################
    //# Constructor
    private TRTransitionCollectionIterator()
    {
      mTransitionIterator =
        mTransitionRelation.createAllTransitionsReadOnlyIterator();
      mAdvanced = false;
    }

    //#######################################################################
    //# Interface java.util.Iterator<TransitionProxy>
    @Override
    public boolean hasNext()
    {
      if (!mAdvanced) {
        mHasNext = mTransitionIterator.advance();
        mAdvanced = true;
      }
      return mHasNext;
    }

    @Override
    public TransitionProxy next()
    {
      if (hasNext()) {
        mAdvanced = false;
        final ProductDESProxyFactory factory =
          ProductDESElementFactory.getInstance();
        final int s = mTransitionIterator.getCurrentSourceState();
        final StateProxy source = createState(s);
        final int e = mTransitionIterator.getCurrentEvent();
        final EventProxy event = mEventEncoding.getProperEvent(e);
        final int t = mTransitionIterator.getCurrentTargetState();
        final StateProxy target = createState(t);
        return factory.createTransitionProxy(source, event, target);
      } else {
        throw new NoSuchElementException
          ("Attempting to read past end of transition list in TRAutomatonProxy!");
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("TRAutomatonProxy does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private final TransitionIterator mTransitionIterator;
    private boolean mAdvanced;
    private boolean mHasNext;
  }


  //#########################################################################
  //# Data Members
  private final EventEncoding mEventEncoding;
  private final ListBufferTransitionRelation mTransitionRelation;
  private TRStateList mStates;
  private String[] mStateNames;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 8587507142812682383L;

}
