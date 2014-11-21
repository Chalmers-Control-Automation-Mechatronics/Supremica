//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRTraceProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.StateEncoding;
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


/**
 * @author Robi Malik
 */

public abstract class TRTraceProxy
  extends DocumentElement
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
    super(name, comment, null);
    mProductDES = des;
    mEvents = new EventProxy[0];
    mTraceData = new HashMap<>();
    mAutomataMap = new HashMap<>();
  }


  //#########################################################################
  //# Simple Access
  int getNumberOfSteps()
  {
    return mEvents.length + 1;
  }

  int getState(final TRAbstractionStep step, final int index)
  {
    final int[] states = mTraceData.get(step);
    return states[index];
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
    return new AutomataSet();
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
  //# Auxiliary Methods
  private int getNumberOfAutomata()
  {
    int count = 0;
    for (final TRAbstractionStep step : mTraceData.keySet()) {
      if (step instanceof TRAbstractionStepInput) {
        count++;
      }
    }
    return count;
  }


  //#########################################################################
  //# Inner Class AutomataSet
  private class AutomataSet extends AbstractSet<AutomatonProxy>
  {
    //#######################################################################
    //# Interface java.util.Set<AutomatonProxy>
    @Override
    public Iterator<AutomatonProxy> iterator()
    {
      final Iterator<TRAbstractionStep> inner = mTraceData.keySet().iterator();
      return new AutomataIterator(inner);
    }

    @Override
    public int size()
    {
      return getNumberOfAutomata();
    }
  }


  //#########################################################################
  //# Inner Class AutomataSet
  private class AutomataIterator implements Iterator<AutomatonProxy>
  {
    //#######################################################################
    //# Constructor
    private AutomataIterator(final Iterator<TRAbstractionStep> inner)
    {
      mInnerIterator = inner;
    }

    //#######################################################################
    //# Interface java.util.Iterator<AutomatonProxy>
    @Override
    public boolean hasNext()
    {
      if (mNext != null) {
        return true;
      } else {
        while (mInnerIterator.hasNext()) {
          final TRAbstractionStep next = mInnerIterator.next();
          if (next instanceof TRAbstractionStepInput) {
            mNext = (TRAbstractionStepInput) next;
            return true;
          }
        }
        return false;
      }
    }

    @Override
    public AutomatonProxy next()
    {
      if (hasNext()) {
        final AutomatonProxy aut = mNext.getInputAutomaton();
        mNext = null;
        return aut;
      } else {
        throw new NoSuchElementException
          ("Attempting to read past end of TRTraceProxy automata list!");
      }
    }

    @Override
    public void remove()
    {
      mInnerIterator.remove();
    }

    //#######################################################################
    //# Data Members
    private final Iterator<TRAbstractionStep> mInnerIterator;
    private TRAbstractionStepInput mNext;
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
    public StateProxy get(final Object aut)
    {
      final TRAbstractionStepInput step = mAutomataMap.get(aut);
      assert step != null;
      assert step.getInputAutomaton() == aut;
      final int[] states = mTraceData.get(step);
      final int s = states[mIndex];
      final StateEncoding enc = step.getStateEncoding();
      return enc.getState(s);
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
      final Map.Entry<AutomatonProxy,TRAbstractionStepInput> innerEntry =
        mInnerIterator.next();
      final AutomatonProxy aut = innerEntry.getKey();
      final TRAbstractionStepInput step = innerEntry.getValue();
      assert step.getInputAutomaton() == aut;
      final int[] states = mTraceData.get(step);
      final int s = states[mIndex];
      final StateEncoding enc = step.getStateEncoding();
      final StateProxy state = enc.getState(s);
      return new AbstractMap.SimpleEntry<>(aut, state);
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        ("TRTraceProxy does not support step modification!");
    }

    //#######################################################################
    //# Data Members
    private final Iterator<Map.Entry<AutomatonProxy,TRAbstractionStepInput>>
      mInnerIterator;
    private final int mIndex;
  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mProductDES;
  private EventProxy[] mEvents;
  private final Map<TRAbstractionStep,int[]> mTraceData;
  private final Map<AutomatonProxy,TRAbstractionStepInput> mAutomataMap;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6433743484084272294L;



}
