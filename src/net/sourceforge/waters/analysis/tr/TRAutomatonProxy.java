//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
import gnu.trove.set.hash.THashSet;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;
import net.sourceforge.waters.plain.base.AbstractNamedElement;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


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
 *     calls to retrieve the same state return the same object.
 *     When a TRAutomatonProxy object is created from an {@link AutomatonProxy}
 *     object, it also remembers the states of that automaton, and they can
 *     be retrieved by calling {@link #getOriginalState(int)}.</LI>
 * <LI>{@link TransitionProxy} objects are only created and returned on
 *     demand. If the same transition is retrieved twice, two different
 *     objects will be returned.</LI>
 * </UL>
 *
 * @see ListBufferTransitionRelation
 * @author Robi Malik
 */

public class TRAutomatonProxy
  extends AbstractNamedElement
  implements AutomatonProxy
{

  //#########################################################################
  //# Factory Methods
  /**
   * Creates a TRAutomatonProxy from an automaton if necessary.
   * This method checks whether the given automaton is a TRAutomatonProxy,
   * and if so, returns it unchanged. Otherwise, it creates a new
   * TRAutomatonProxy to match it.   *
   * @param  aut      The automaton to be converted.
   */
  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return createTRAutomatonProxy(aut, translator);
  }

  /**
   * Creates a TRAutomatonProxy from an automaton if necessary.
   * This method checks whether the given automaton is a TRAutomatonProxy,
   * and if so, returns it unchanged. Otherwise, it creates a new
   * TRAutomatonProxy to match it.
   * @param  aut      The automaton to be converted.
   * @param  translator  Kind translator that determines the {@link EventKind}
   *                  of events in the event encoding of the new automaton
   *                  if it is created.
   */
  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut, final KindTranslator translator)
    throws OverflowException
  {
    return createTRAutomatonProxy(aut, translator, 0);
  }

  /**
   * Creates a TRAutomatonProxy from an automaton if necessary.
   * This method checks whether the given automaton is a TRAutomatonProxy,
   * and if so, returns it unchanged. Otherwise, it creates a new
   * TRAutomatonProxy to match it.
   * @param  aut      The automaton to be converted.
   * @param  translator  Kind translator that determines the {@link EventKind}
   *                  of events in the event encoding of the new automaton
   *                  if it is created.
   * @param  config   Configuration flags defining which transition buffers are
   *                  needed in transition relation. Should be zero or one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   *                  If the given automaton is already a TRAutomatonProxy
   *                  and the argument is nonzero, the transition relation is
   *                  reconfigured, otherwise the configuration is used
   *                  when creating the new transition relation.
   */
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

  public static StateProxy findDumpState(final AutomatonProxy aut)
  {
    boolean hasProposition = false;
    for (final EventProxy event : aut.getEvents()) {
      if (event.getKind() == EventKind.PROPOSITION) {
        hasProposition = true;
        break;
      }
    }
    if (!hasProposition) {
      return null;
    }
    final Set<StateProxy> states = aut.getStates();
    final Set<StateProxy> candidates = new THashSet<>(states.size());
    StateProxy namedCandidate = null;
    for (final StateProxy state : states) {
      if (state.getPropositions().isEmpty()) {
        candidates.add(state);
        if (state.getName().equals(DUMP_NAME)) {
          namedCandidate = state;
        }
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    for (final TransitionProxy trans : aut.getTransitions()) {
      final StateProxy source = trans.getSource();
      if (candidates.remove(source) && candidates.isEmpty()) {
        return null;
      }
    }
    if (candidates.contains(namedCandidate)) {
      return namedCandidate;
    } else {
      for (final StateProxy state : states) {
        if (candidates.contains(state)) {
          return state;
        }
      }
      return null;
    }
  }

  public static AutomatonProxy renameAutomaton(final AutomatonProxy aut,
                                               final String name)
  {
    if (name.equals(aut.getName())) {
      return aut;
    } else if (aut instanceof TRAutomatonProxy) {
      final TRAutomatonProxy trAut = (TRAutomatonProxy) aut;
      trAut.setName(name);
      return trAut;
    } else {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      return AutomatonTools.renameAutomaton(aut, name, factory);
    }
  }


  //#########################################################################
  //# Constructors
  /**
   * Creates a TRAutomatonProxy to encapsulate a transition relation.
   * This constructor merely stores references to the given event encoding
   * and transition relation.
   * @param  eventEnc  Event encoding to be used.
   * @param  rel       Transition relation to be encapsulated.
   */
  public TRAutomatonProxy(final EventEncoding eventEnc,
                          final ListBufferTransitionRelation rel)
  {
    rel.useEventEncoding(eventEnc);
    mEventEncoding = eventEnc;
    mOriginalStates = null;
    mTransitionRelation = rel;
    provideTauEvent();
  }

  /**
   * Creates a TRAutomatonProxy to match a given automaton.
   * This constructor creates a new event encoding and transition relation
   * to reflect the contents of the given automaton. The states of the
   * automaton can be retrieved using {@link #getOriginalState(int)}.
   * @param  aut      The automaton to be replicated.
   * @param  translator  Kind translator that determines the {@link EventKind}
   *                  of events in the new event encoding.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
  public TRAutomatonProxy(final AutomatonProxy aut,
                          final KindTranslator translator,
                          final int config)
    throws OverflowException
  {
    this(aut, translator, null, config);
  }

  /**
   * Creates a TRAutomatonProxy to match a given automaton.
   * This constructor creates a new event encoding and transition relation
   * to reflect the contents of the given automaton. The states of the
   * automaton can be retrieved using {@link #getOriginalState(int)}.
   * @param  aut      The automaton to be replicated.
   * @param  translator  Kind translator that determines the {@link EventKind}
   *                  of events in the new event encoding.
   * @param  dumpState  A state of the given automaton to be used as dump state
   *                  when creating the transition relation, or <CODE>null</CODE>
   *                  to create a new unreachable dump state instead.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
  public TRAutomatonProxy(final AutomatonProxy aut,
                          final KindTranslator translator,
                          final StateProxy dumpState,
                          final int config)
    throws OverflowException
  {
    this(aut, new EventEncoding(aut, translator), dumpState, config);
  }

  /**
   * Creates a TRAutomatonProxy to match a given automaton and event encoding.
   * This constructor uses the given event encoding without copying and creates
   * a new transition relation to reflect the contents of the given automaton.
   * The states of the automaton can be retrieved using {@link
   * #getOriginalState(int)}.
   * @param  aut      The automaton to be replicated.
   * @param  eventEnc Event encoding to be used.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
  public TRAutomatonProxy(final AutomatonProxy aut,
                          final EventEncoding eventEnc,
                          final int config)
    throws OverflowException
  {
    this(aut, eventEnc, null, config);
  }

  /**
   * Creates a TRAutomatonProxy to match a given automaton and event encoding.
   * This constructor uses the given event encoding without copying and creates
   * a new transition relation to reflect the contents of the given automaton.
   * The states of the automaton can be retrieved using {@link
   * #getOriginalState(int)}.
   * @param  aut      The automaton to be replicated.
   * @param  eventEnc Event encoding to be used.
   * @param  dumpState  A state of the given automaton to be used as dump state
   *                  when creating the transition relation, or <CODE>null</CODE>
   *                  to create a new unreachable dump state instead.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
  public TRAutomatonProxy(final AutomatonProxy aut,
                          final EventEncoding eventEnc,
                          final StateProxy dumpState,
                          final int config)
    throws OverflowException
  {
    this(aut, eventEnc, new StateEncoding(aut), dumpState, config);
  }

  /**
   * Creates a TRAutomatonProxy to match a given automaton, event encoding,
   * and state encoding. This constructor uses the given event encoding without
   * copying and creates a new transition relation to reflect the contents of
   * the given automaton. The automaton's states are encoded based on the given
   * state* encoding and can be retrieved using {@link #getOriginalState(int)}.
   * @param  aut      The automaton to be replicated.
   * @param  eventEnc Event encoding to be used.
   * @param  stateEnc State encoding to provide a mapping between state numbers
   *                  and states.
   * @param  dumpState  A state in the state encoding to be used as dump state
   *                  when creating the transition relation, or <CODE>null</CODE>
   *                  to create a new unreachable dump state instead.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
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
    final int numStates = mTransitionRelation.getNumberOfStates();
    mOriginalStates = new StateProxy[numStates];
    final StateProxy[] encStates = stateEnc.getStatesArray();
    System.arraycopy(encStates, 0, mOriginalStates, 0, encStates.length);
    provideTauEvent();
  }

  /**
   * Creates a new TRAutomatonProxy by copying another.
   * This constructor creates a deep copy, with new separate event encoding
   * and transition relation.
   * @param  aut       TRAutomatonProxy object to be duplicated.
   */
  public TRAutomatonProxy(final TRAutomatonProxy aut)
  {
    mEventEncoding = new EventEncoding(aut.mEventEncoding);
    mOriginalStates = aut.mOriginalStates;
    final ListBufferTransitionRelation rel = aut.mTransitionRelation;
    final int config = rel.getConfiguration();
    mTransitionRelation =
      new ListBufferTransitionRelation(rel, mEventEncoding, config);
  }

  /**
   * Creates a new TRAutomatonProxy by copying another.
   * This constructor creates a deep copy, with new separate event encoding
   * and transition relation.
   * @param  aut      TRAutomatonProxy object to be duplicated.
   * @param  config   Configuration flags defining which transition buffers are
   *                  created in the copied transition relation. Should be one
   *                  of {@link ListBufferTransitionRelation#CONFIG_SUCCESSORS},
   *                  {@link ListBufferTransitionRelation#CONFIG_PREDECESSORS},
   *                  or {@link ListBufferTransitionRelation#CONFIG_ALL}.
   */
  public TRAutomatonProxy(final TRAutomatonProxy aut, final int config)
  {
    mEventEncoding = new EventEncoding(aut.mEventEncoding);
    mOriginalStates = aut.mOriginalStates;
    final ListBufferTransitionRelation rel = aut.mTransitionRelation;
    mTransitionRelation =
      new ListBufferTransitionRelation(rel, mEventEncoding, config);
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

  /**
   * Gets the original state associated with the given state number.
   * When a TRAutomatonProxy object is created from an {@link AutomatonProxy}
   * object, it remembers the states of that automaton, and this method can
   * be used to retrieve a state from there.
   * @param  code  Number of state to be looked up.
   * @return A state of the original automaton or <CODE>null</CODE> if not
   *         available. As the TRAutomatonProxy may have changed to differ
   *         from the original, the returned state's attributes (initial,
   *         markings, etc.) are not guaranteed to be the same as those
   *         retrieved using {@link #getStates()} or {@link #getTRState(int)}.
   */
  public StateProxy getOriginalState(final int code)
  {
    if (mOriginalStates == null) {
      return null;
    } else {
      return mOriginalStates[code];
    }
  }

  public StateProxy getOriginalState(final StateProxy state)
  {
    if (state instanceof TRState) {
      final TRState trState = (TRState) state;
      if (trState.belongsTo(this)) {
        final int code = trState.getStateIndex();
        return getOriginalState(code);
      }
    }
    return state;
  }

  /**
   * Gets the state associated with the given state number.
   * @param  code  Number of state to be looked up.
   * @return A {@link StateProxy} object that is unique to this TRAutomatonProxy
   *         and that reflects the current attributes of the requested state.
   */
  public TRState getTRState(final int code)
  {
    final TRStateList states = getTRStates();
    return states.createState(code);
  }

  /**
   * Clears all references to state objects. When a TRAutomatonProxy object is
   * created from an automaton, it remembers the states of the input automaton.
   * However, as the transition relation is modified, the original state
   * objects may longer be accurate. Then this method can be used to reset
   * all state objects and use new automatically generated ones instead.
   */
  public void resetOriginalStates()
  {
    mOriginalStates = null;
  }

  public void setName(final String name)
  {
    mTransitionRelation.setName(name);
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
    return getTRStates();
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

  private TRStateList getTRStates()
  {
    if (mStateList == null) {
      mStateList = new TRStateList();
    }
    return mStateList;
  }


  //#########################################################################
  //# Inner Class TRState
  public class TRState
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
      if (mOriginalStates != null) {
        final StateProxy state = mOriginalStates[mIndex];
        if (state != null) {
          return state.getName();
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
      return mStateList.createMarkings(pattern);
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
    public int getStateIndex()
    {
      return mIndex;
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean belongsTo(final TRAutomatonProxy tr)
    {
      return TRAutomatonProxy.this == tr;
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
        return mStateList.createState(mIndex++);
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
        final StateProxy source = mStateList.createState(s);
        final int e = mTransitionIterator.getCurrentEvent();
        final EventProxy event = mEventEncoding.getProperEvent(e);
        final int t = mTransitionIterator.getCurrentTargetState();
        final StateProxy target = mStateList.createState(t);
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
  private StateProxy[] mOriginalStates;
  private TRStateList mStateList;


  //#########################################################################
  //# Class Constants
  private static final String DUMP_NAME = ":dump";

  private static final long serialVersionUID = 8587507142812682383L;

}
