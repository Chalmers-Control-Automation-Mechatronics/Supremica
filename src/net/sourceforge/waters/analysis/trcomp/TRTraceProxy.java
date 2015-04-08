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
    this(name, comment, des, null);
  }

  /**
   * Creates a new trace with a given event sequence, but without automata.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  events       Initial sequence of events, or <CODE>null</CODE>.
   */
  TRTraceProxy(final String name,
               final String comment,
               final ProductDESProxy des,
               final List<EventProxy> events)
  {
    super(name);
    mComment = comment;
    mLocation = null;
    mProductDES = des;
    if (events == null) {
      mEvents = new EventProxy[0];
    } else {
      mEvents = new EventProxy[events.size()];
      events.toArray(mEvents);
    }
    mTraceData = new HashMap<>();
    mAutomataMap = new HashMap<>();
  }

  /**
   * Creates a new trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   * @param  trace   The trace to be copied
   */
  TRTraceProxy(final TRTraceProxy trace)
  {
    this(trace, trace.getNumberOfSteps());
  }

  /**
   * Creates a new trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   * @param  trace    The trace to be copied
   * @param  numSteps The number of steps to be copied from the trace.
   *                  Only an initial segment with this number of steps
   *                  will be included in the copy.
   */
  TRTraceProxy(final TRTraceProxy trace, final int numSteps)
  {
    super(trace);
    assert numSteps <= trace.getNumberOfSteps() :
      "Not enough steps in source of copy!";
    mComment = trace.getComment();
    mLocation = null;
    mProductDES = trace.mProductDES;
    mEvents = Arrays.copyOf(trace.mEvents, numSteps - 1);
    mTraceData = new HashMap<>(trace.mTraceData.size());
    for (final Map.Entry<TRAbstractionStep,int[]> entry :
         trace.mTraceData.entrySet()) {
      final TRAbstractionStep step = entry.getKey();
      final int[] states = entry.getValue();
      mTraceData.put(step, Arrays.copyOf(states, numSteps));
    }
    mAutomataMap = new HashMap<>(trace.mAutomataMap);
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
  //# Simple Access
  void setComment(final String comment)
  {
    mComment = comment;
  }

  int getNumberOfSteps()
  {
    return mEvents.length + 1;
  }

  public Set<TRAbstractionStep> getCoveredAbstractionSteps()
  {
    return mTraceData.keySet();
  }

  StateProxy getState(final AutomatonProxy aut, final int index)
  {
    final TRAbstractionStep step = mAutomataMap.get(aut);
    final int s = getState(step, index);
    if (s < 0) {
      return null;
    } else {
      final TRAbstractionStepInput inputStep = (TRAbstractionStepInput) step;
      return inputStep.getState(s);
    }
  }

  int getState(final TRAbstractionStep step, final int index)
  {
    final int[] states = mTraceData.get(step);
    return states[index];
  }

  void setState(final TRAbstractionStep step, final int index, final int state)
  {
    final int[] states = mTraceData.get(step);
    states[index] = state;
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

  void replaceAutomaton(final TRAbstractionStep oldStep,
                        final TRAbstractionStep newStep)
  {
    final int[] states = mTraceData.remove(oldStep);
    mTraceData.put(newStep, states);
  }

  void setInputAutomaton(final TRAbstractionStepInput step)
  {
    final AutomatonProxy aut = step.getInputAutomaton();
    setInputAutomaton(aut, step);
  }

  void setInputAutomaton(final AutomatonProxy aut,
                         final TRAbstractionStep step)
  {
    mAutomataMap.put(aut, step);
  }

  void replaceInputAutomaton(final AutomatonProxy aut,
                             final TRAbstractionStep step)
  {
    final TRAbstractionStep oldStep = mAutomataMap.remove(aut);
    final int[] states = mTraceData.remove(oldStep);
    mTraceData.put(step, states);
  }

  void removeInputAutomaton(final AutomatonProxy aut)
  {
    final TRAbstractionStep step = mAutomataMap.remove(aut);
    mTraceData.remove(step);
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

  void append(final TRTraceProxy trace)
  {
    append(trace, trace.getNumberOfSteps() - 1);
  }

  void append(final TRTraceProxy trace, final int numSteps)
  {
    assert numSteps <= trace.mEvents.length : "Not enough steps to append!";
    final int totalEvents = mEvents.length + numSteps;
    final EventProxy[] newEvents = new EventProxy[totalEvents];
    System.arraycopy(mEvents, 0, newEvents, 0, mEvents.length);
    System.arraycopy(trace.mEvents, 0, newEvents, mEvents.length, numSteps);
    final int totalSteps = totalEvents + 1;
    final int firstSteps = getNumberOfSteps();
    mEvents = newEvents;
    for (final Map.Entry<TRAbstractionStep,int[]> entry : mTraceData.entrySet()) {
      final int[] firstStates = entry.getValue();
      final int lastState = firstStates[firstSteps - 1];
      final TRAbstractionStep step = entry.getKey();
      final int[] newStates = new int[totalSteps];
      System.arraycopy(firstStates, 0, newStates, 0, firstSteps);
      final int[] secondStates = trace.mTraceData.get(step);
      if (secondStates != null) {
        assert lastState == secondStates[0] :
          "End state in first trace does not match start state " +
          "in second trace for automaton " + step.getName() + "!";
        System.arraycopy(secondStates, 1, newStates, firstSteps, numSteps);
      } else {
        Arrays.fill(newStates, firstSteps, totalSteps, lastState);
      }
      entry.setValue(newStates);
    }
  }

  void widenAndAppend(final TRTraceProxy trace)
  {
    final int oldNumSteps = getNumberOfSteps();
    final int extraSteps = trace.getNumberOfSteps() - 1;
    final int newNumSteps = oldNumSteps + extraSteps;
    append(trace);
    for (final Map.Entry<TRAbstractionStep,int[]> entry :
         trace.mTraceData.entrySet()) {
      final TRAbstractionStep step = entry.getKey();
      final int[] oldStates = entry.getValue();
      final int[] newStates = new int[newNumSteps];
      Arrays.fill(newStates, 0, oldNumSteps, oldStates[0]);
      System.arraycopy(oldStates, 1, newStates, oldNumSteps, extraSteps);
      addAutomaton(step, newStates);
    }
  }


  /**
   * Cuts the trace short by removing steps at the end.
   * @param numberOfSteps The new number of steps, which should be at least 1
   *                      to allow for the initial state.
   */
  void prune(final int numberOfSteps)
  {
    final int numEvents = numberOfSteps - 1;
    mEvents = Arrays.copyOfRange(mEvents, 0, numEvents);
    for (final Map.Entry<TRAbstractionStep,int[]> entry : mTraceData.entrySet()) {
      final int[] oldStates = entry.getValue();
      final int[] newStates = Arrays.copyOfRange(oldStates, 0, numberOfSteps);
      entry.setValue(newStates);
    }
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
      final TRAutomatonProxy aut = step.getOutputAutomaton
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
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
        return inputStep.getState(s);
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
        final StateProxy state = inputStep.getState(s);
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
