//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.plain.base.DocumentElement;
import net.sourceforge.waters.plain.base.Element;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * @author Robi Malik
 */

public abstract class TRTraceProxy
  extends NamedElement
  implements TraceProxy
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new empty trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   */
  TRTraceProxy(final String name,
               final String comment,
               final ProductDESProxy des)
  {
    super(name);
    mComment = comment;
    mLocation = null;
    mProductDES = des;
    mEvents = new EventProxy[0];
    mTraceData = new HashMap<>();
    mAutomataMap = new HashMap<>();
  }

  /**
   * Creates a new trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   */
  TRTraceProxy(final TRTraceProxy trace)
  {
    super(trace);
    mComment = trace.getComment();
    mLocation = null;
    mProductDES = trace.mProductDES;
    mEvents = Arrays.copyOf(trace.mEvents, trace.mEvents.length);
    mTraceData = new HashMap<>(trace.mTraceData.size());
    for (final Map.Entry<TRAbstractionStep,int[]> entry :
         trace.mTraceData.entrySet()) {
      final TRAbstractionStep step = entry.getKey();
      final int[] states = entry.getValue();
      mTraceData.put(step, Arrays.copyOf(states, states.length));
    }
    mAutomataMap = new HashMap<>(trace.mAutomataMap);
  }


  //#########################################################################
  //# Simple Access
  void setComment(final String comment)
  {
    mComment = comment;
  }

  int getNumberOfSteps()
  {
    return mEvents.length + 1;
  }

  StateProxy getState(final AutomatonProxy aut, final int index)
  {
    final TRAbstractionStep step = mAutomataMap.get(aut);
    final int s = getState(step, index);
    if (s < 0) {
      return null;
    } else {
      final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
      final StateEncoding enc = inputStep.getStateEncoding();
      assert enc.getState(s) != null;
      return enc.getState(s);
    }
  }

  int getState(final TRAbstractionStep step, final int index)
  {
    final int[] states = mTraceData.get(step);
    return states[index];
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  @Override
  public String getComment()
  {
    return mComment;
  }

  @Override
  public URI getLocation()
  {
    return mLocation;
  }

  @Override
  public File getFileLocation() throws MalformedURLException
  {
    return DocumentElement.getFileLocation(mLocation);
  }

  @Override
  public void setLocation(final URI location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.TraceProxy
  @Override
  public ProductDESProxy getProductDES()
  {
    return mProductDES;
  }

  @Override
  public Set<AutomatonProxy> getAutomata()
  {
    return mAutomataMap.keySet();
  }

  @Override
  public List<EventProxy> getEvents()
  {
    return Arrays.asList(mEvents);
  }

  @Override
  public List<TraceStepProxy> getTraceSteps()
  {
    return new TraceStepList();
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public TRTraceProxy clone()
  {
    final TRTraceProxy cloned = (TRTraceProxy) super.clone();
    cloned.mEvents = Arrays.copyOf(mEvents, mEvents.length);
    cloned.mTraceData = new HashMap<>(mTraceData.size());
    for (final Map.Entry<TRAbstractionStep,int[]> entry :
         mTraceData.entrySet()) {
      final TRAbstractionStep step = entry.getKey();
      final int[] states = entry.getValue();
      cloned.mTraceData.put(step, Arrays.copyOf(states, states.length));
    }
    cloned.mAutomataMap = new HashMap<>(mAutomataMap);
    return cloned;
  }


  //#########################################################################
  //# Trace Expansion
  public void reset(final List<EventProxy> events)
  {
    mEvents = new EventProxy[events.size()];
    events.toArray(mEvents);
    mTraceData.clear();
    mAutomataMap.clear();
  }

  void addAutomaton(final TRAbstractionStep step, final int[] states)
  {
    mTraceData.put(step, states);
  }

  void removeAutomaton(final TRAbstractionStep step)
  {
    mTraceData.remove(step);
  }

  void setInputAutomaton(final TRAbstractionStepInput step)
  {
    final AutomatonProxy aut = step.getInputAutomaton();
    mAutomataMap.put(aut, step);
  }

  void addStutteringSteps(final List<EventProxy> newEvents,
                          final List<EventProxy> nonStutterEvents)
  {
    final int newNumEvents = newEvents.size();
    final EventProxy[] newEventsArray = new EventProxy[newNumEvents];
    newEvents.toArray(newEventsArray);
    for (final Map.Entry<TRAbstractionStep,int[]> entry : mTraceData.entrySet()) {
      final int[] oldStates = entry.getValue();
      final int[] newStates = new int[newNumEvents + 1];
      int oldState = newStates[0] = oldStates[0];
      int oldIndex = 0;
      int newIndex = 0;
      final Iterator<EventProxy> nonStutterIter = nonStutterEvents.iterator();
      EventProxy nextNonStutterEvent =
        nonStutterIter.hasNext() ? nonStutterIter.next() : null;
      while (newIndex < newNumEvents) {
        while (oldIndex < mEvents.length &&
               mEvents[oldIndex] != nextNonStutterEvent) {
          oldIndex++;
          assert oldState == oldStates[oldIndex] :
            "State-change on non-stutter event detected!";
        }
        if (newEventsArray[newIndex] == nextNonStutterEvent) {
          oldIndex++;
          oldState = oldStates[oldIndex];
          nextNonStutterEvent =
            nonStutterIter.hasNext() ? nonStutterIter.next() : null;
        }
        newIndex++;
        newStates[newIndex] = oldState;
      }
      assert nextNonStutterEvent == null :
        "Failed to consume all non-stutter events!";
      entry.setValue(newStates);
    }
    mEvents = newEventsArray;
  }


  //#########################################################################
  //# Debugging
  void setUpForTraceChecking() throws AnalysisException
  {
    for (final TRAbstractionStep step : mTraceData.keySet()) {
      if (step instanceof TRAbstractionStepInput) {
        final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
        final AutomatonProxy inputAut = inputStep.getInputAutomaton();
        if (mAutomataMap.containsKey(inputAut)) {
          continue;
        }
      }
      final TRAutomatonProxy aut =
        step.getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      mAutomataMap.put(aut, step);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private int getNumberOfAutomata()
  {
    return mAutomataMap.size();
  }

  private boolean isInputAutomaton(final AutomatonProxy aut,
                                   final TRAbstractionStep step)
  {
    if (step instanceof TRAbstractionStepInput) {
      final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
      return inputStep.getInputAutomaton() == aut;
    } else {
      return false;
    }
  }


  //#########################################################################
  //# Inner Class TraceStepList
  private class TraceStepList extends AbstractList<TraceStepProxy>
  {
    //#######################################################################
    //# Interface java.util.List<TraceStepProxy>
    @Override
    public TraceStepProxy get(final int index)
    {
      if (index >= 0 && index <= mEvents.length) {
        return new TraceStep(index);
      } else {
        throw new ArrayIndexOutOfBoundsException(index);
      }
    }

    @Override
    public int size()
    {
      return getNumberOfSteps();
    }
  }


  //#########################################################################
  //# Inner Class TraceStep
  private class TraceStep
    extends Element
    implements TraceStepProxy
  {
    //#######################################################################
    //# Constructor
    private TraceStep(final int index)
    {
      mIndex = index;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.base.Proxy
    @Override
    public Class<? extends Proxy> getProxyInterface()
    {
      return TraceStepProxy.class;
    }

    @Override
    public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
    {
      final ProductDESProxyVisitor desVisitor =
        (ProductDESProxyVisitor) visitor;
      return desVisitor.visitTraceStepProxy(this);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.des.TraceStepProxy
    @Override
    public EventProxy getEvent()
    {
      return mIndex == 0 ? null : mEvents[mIndex - 1];
    }

    @Override
    public Map<AutomatonProxy,StateProxy> getStateMap()
    {
      return new TraceStepMap(mIndex);
    }

    //#######################################################################
    //# Data Members
    private final int mIndex;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 7069861297185779685L;
  }


  //#########################################################################
  //# Inner Class TraceStepMap
  private class TraceStepMap
    extends AbstractMap<AutomatonProxy,StateProxy>
  {
    //#######################################################################
    //# Constructor
    private TraceStepMap(final int index)
    {
      mIndex = index;
    }

    //#######################################################################
    //# Interface java.util.Map<AutomatonProxy,StateProxy>
    @Override
    public Set<Map.Entry<AutomatonProxy,StateProxy>> entrySet()
    {
      return new TraceStepSet(mIndex);
    }

    @Override
    public StateProxy get(final Object key)
    {
      final AutomatonProxy aut = (AutomatonProxy) key;
      final TRAbstractionStep step = mAutomataMap.get(aut);
      assert step != null;
      final int[] states = mTraceData.get(step);
      final int s = states[mIndex];
      if (s < 0) {
        return null;
      } else if (isInputAutomaton(aut, step)) {
        final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
        final StateEncoding enc = inputStep.getStateEncoding();
        assert enc.getState(s) != null;
        return enc.getState(s);
      } else {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        if (tr.getState(s) == null) {
          tr.getState(s);
        }
        return tr.getState(s);
      }
    }

    @Override
    public int size()
    {
      return getNumberOfAutomata();
    }

    //#######################################################################
    //# Data Members
    private final int mIndex;
  }


  //#########################################################################
  //# Inner Class TraceStepSet
  private class TraceStepSet
    extends AbstractSet<Map.Entry<AutomatonProxy,StateProxy>>
  {
    //#######################################################################
    //# Constructor
    private TraceStepSet(final int index)
    {
      mIndex = index;
    }

    //#######################################################################
    //# Interface java.util.Set<Map.Entry<AutomatonProxy,StateProxy>>
    @Override
    public Iterator<Map.Entry<AutomatonProxy,StateProxy>> iterator()
    {
      return new TraceStepIterator(mIndex);
    }

    @Override
    public int size()
    {
      return getNumberOfAutomata();
    }

    //#######################################################################
    //# Data Members
    private final int mIndex;
  }


  //#########################################################################
  //# Inner Class TraceStepIterator
  private class TraceStepIterator
    implements Iterator<Map.Entry<AutomatonProxy,StateProxy>>
  {
    //#######################################################################
    //# Constructor
    private TraceStepIterator(final int index)
    {
      mInnerIterator = mAutomataMap.entrySet().iterator();
      mIndex = index;
    }

    //#######################################################################
    //# Interface java.util.Iterator<Map.Entry<AutomatonProxy,StateProxy>>
    @Override
    public boolean hasNext()
    {
      return mInnerIterator.hasNext();
    }

    @Override
    public Map.Entry<AutomatonProxy,StateProxy> next()
    {
      final Map.Entry<AutomatonProxy,TRAbstractionStep> innerEntry =
        mInnerIterator.next();
      final AutomatonProxy aut = innerEntry.getKey();
      final TRAbstractionStep step = innerEntry.getValue();
      final int[] states = mTraceData.get(step);
      final int s = states[mIndex];
      if (s < 0) {
        return new AbstractMap.SimpleEntry<>(aut, null);
      } else if (isInputAutomaton(aut, step)) {
        final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
        final StateEncoding enc = inputStep.getStateEncoding();
        final StateProxy state = enc.getState(s);
        return new AbstractMap.SimpleEntry<>(aut, state);
      } else {
        final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
        final StateProxy state = tr.getState(s);
        return new AbstractMap.SimpleEntry<>(aut, state);
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("TRTraceProxy does not support step modification!");
    }

    //#######################################################################
    //# Data Members
    private final Iterator<Map.Entry<AutomatonProxy,TRAbstractionStep>>
      mInnerIterator;
    private final int mIndex;
  }


  //#########################################################################
  //# Data Members
  private URI mLocation;
  private String mComment;
  private final ProductDESProxy mProductDES;
  private EventProxy[] mEvents;
  private Map<TRAbstractionStep,int[]> mTraceData;
  private Map<AutomatonProxy,TRAbstractionStep> mAutomataMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6433743484084272294L;

}
