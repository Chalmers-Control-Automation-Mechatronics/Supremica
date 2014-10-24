//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TRAutomatonProxy
//###########################################################################
//# $Id$
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
 * <P>The <CODE>TRAutomatonProxy</CODE> object is to be considered as
 * <STRONG>immutable</STRONG>. While the underlying {@link EventEncoding} and
 * {@link ListBufferTransitionRelation} can be accessed for better performance,
 * any changes to these objects may result in unpredictable behaviour when
 * accessed through the <CODE>TRAutomatonProxy</CODE>.</P>
 *
 * @author Robi Malik
 */

public class TRAutomatonProxy
  extends AbstractNamedElement
  implements AutomatonProxy
{

  //#########################################################################
  //# Factory Methods
  public static TRAutomatonProxy createTRAutomatonProxy(final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return createTRAutomatonProxy(aut, translator);
  }

  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut, final KindTranslator translator)
    throws OverflowException
  {
    return createTRAutomatonProxy(aut, translator,
                                  ListBufferTransitionRelation.CONFIG_SUCCESSORS);
  }

  public static TRAutomatonProxy createTRAutomatonProxy
    (final AutomatonProxy aut,
     final KindTranslator translator,
     final int config)
    throws OverflowException
  {
    if (aut instanceof TRAutomatonProxy) {
      return (TRAutomatonProxy) aut;
    } else {
      final EventEncoding eventEnc = new EventEncoding(aut, translator);
      final StateEncoding stateEnc = new StateEncoding(aut);
      final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(aut, eventEnc, stateEnc, config);
      return new TRAutomatonProxy(eventEnc, stateEnc, rel);
    }
  }


  //#########################################################################
  //# Constructors
  public TRAutomatonProxy(final EventEncoding eventEnc,
                          final StateEncoding stateEnc,
                          final ListBufferTransitionRelation rel)
  {
    this(eventEnc, rel);
    mStates = new TRStateList(stateEnc);
  }

  public TRAutomatonProxy(final EventEncoding eventEnc,
                          final ListBufferTransitionRelation rel)
  {
    mEventEncoding = eventEnc;
    mTransitionRelation = rel;
    mStates = null;
    assert eventEnc.getNumberOfProperEvents() == rel.getNumberOfProperEvents() :
      "Unexpected number of proper events for TRAutomatonProxy!";
    for (int e = 0; e < eventEnc.getNumberOfProperEvents(); e++) {
      final byte status = rel.getProperEventStatus(e);
      eventEnc.setProperEventStatus(e, status);
    }
    assert eventEnc.getNumberOfPropositions() == rel.getNumberOfPropositions() :
      "Unexpected number of propositions for TRAutomatonProxy!";
    for (int p = 0; p < eventEnc.getNumberOfPropositions(); p++) {
      byte status = eventEnc.getPropositionStatus(p);
      if (rel.isUsedProposition(p)) {
        status &= ~EventEncoding.STATUS_UNUSED;
      } else {
        status |= EventEncoding.STATUS_UNUSED;
      }
      eventEnc.setPropositionStatus(p, status);
    }
  }

  public TRAutomatonProxy(final TRAutomatonProxy aut)
  {
    mEventEncoding = aut.mEventEncoding;
    mTransitionRelation = aut.mTransitionRelation;
    mStates = null;
    if (aut.mStates != null) {
      for (int s = 0; s < aut.mStates.capacity(); s++) {
        final StateProxy state = aut.mStates.get(s);
        if (state != null && !(state instanceof TRState)) {
          if (mStates == null) {
            mStates = new TRStateList();
          }
          final StateProxy cloned = (StateProxy) state.clone();
          mStates.set(s, cloned);
        }
      }
    }
  }


  //#########################################################################
  //# Simple Access
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
    return mStates.get(stateIndex);
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
      return "t" + mIndex;
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
      mStates = new StateProxy[mTransitionRelation.getNumberOfStates()];
      mMarkings = null;
    }

    public TRStateList(final StateEncoding stateEnc)
    {
      assert stateEnc.getNumberOfStates() == mTransitionRelation.getNumberOfStates() :
        "Unexpected number of states for TRAutomatonProxy!";
      mStates = new StateProxy[stateEnc.getNumberOfStates()];
      for (int s = 0; s < stateEnc.getNumberOfStates(); s++) {
        mStates[s] = stateEnc.getState(s);
      }
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
    //# Simple Access
    private int capacity()
    {
      return mStates.length;
    }

    private StateProxy get(final int index)
    {
      return mStates[index];
    }

    private void set(final int index, final StateProxy state)
    {
      mStates[index] = state;
    }

    //#######################################################################
    //# Auxiliary Methods
    private StateProxy createState(final int index)
    {
      StateProxy state = mStates[index];
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
    private final StateProxy[] mStates;
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 8587507142812682382L;

}
