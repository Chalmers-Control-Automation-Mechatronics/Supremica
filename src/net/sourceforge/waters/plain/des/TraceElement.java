//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.des
//# CLASS:   TraceElement
//###########################################################################
//# $Id: TraceElement.java,v 1.4 2006-11-03 15:01:56 torda Exp $
//###########################################################################

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.EqualCollection;
import net.sourceforge.waters.model.base.IndexedHashSet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.plain.base.DocumentElement;


/**
 * A counterexample trace for some automata of a product DES.
 * This is a simple immutable implementation of the {@link TraceProxy}
 * interface.
 *
 * @author Robi Malik
 */

public abstract class TraceElement
  extends DocumentElement
  implements TraceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new trace.
   * @param  name         The name to be given to the new trace.
   * @param  comment      A comment describing the new trace,
   *                      or <CODE>null</CODE>.
   * @param  location     The URI to be associated with the new
   *                      document, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps consituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  TraceElement(final String name,
               final String comment,
               final URI location,
               final ProductDESProxy des,
               final Collection<? extends AutomatonProxy> automata,
               final List<? extends TraceStepProxy> steps)
  {
    super(name, comment, location);
    mProductDES = des;
    if (automata == null) {
      mAutomata = Collections.emptySet();
    } else {
      final Set<AutomatonProxy> modifiable = new AutomataSet(automata);
      mAutomata = Collections.unmodifiableSet(modifiable);
    }
    final List<TraceStepProxy> stepscopy =
      new ArrayList<TraceStepProxy>(steps);
    mTraceSteps = Collections.unmodifiableList(stepscopy);
  }

  /**
   * Creates a new trace using default values.  This constructor provides a
   * simple interface to create a trace for a deterministic product DES. It
   * creates a trace with a <CODE>null</CODE> file location, with a set of
   * automata equal to that of the product DES, and without any state
   * information in the trace steps.
   * @param  name         The name to be given to the new trace.
   * @param  des          The product DES for which the new trace is
   *                      generated.
   * @param  events       The list of events constituting the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      events cannot be found in the product DES.
   */
  TraceElement(final String name,
               final ProductDESProxy des,
               final List<? extends EventProxy> events)
  {
    super(name);
    mProductDES = des;
    final Set<AutomatonProxy> automata = des.getAutomata();
    if (des instanceof ProductDESElement) {
      mAutomata = automata;
    } else {
      final Set<AutomatonProxy> modifiable = new AutomataSet(automata);
      mAutomata = Collections.unmodifiableSet(modifiable);
    }
    final TraceStepProxy step0 = new TraceStepElement(null);
    if (events == null) {
      mTraceSteps = Collections.singletonList(step0);
    } else {
      final int numsteps = events.size() + 1;
      final List<TraceStepProxy> steps =
        new ArrayList<TraceStepProxy>(numsteps);
      steps.add(step0);
      for (final EventProxy event : events) {
        final TraceStepProxy step = new TraceStepElement(event);
        steps.add(step);
      }
      mTraceSteps = Collections.unmodifiableList(steps);
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this trace.
   */
  public TraceElement clone()
  {
    return (TraceElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.TraceProxy
  public ProductDESProxy getProductDES()
  {
    return mProductDES;
  }

  public Set<AutomatonProxy> getAutomata()
  {
    return mAutomata;
  }

  public List<TraceStepProxy> getTraceSteps()
  {
    return mTraceSteps;
  }

  public List<EventProxy> getEvents()
  {
    if (mTraceEvents == null) {
      mTraceEvents = new TraceEventList(mTraceSteps);
    }
    return mTraceEvents;
  }


  //#########################################################################
  //# Equals and Hashcode
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final TraceElement trace = (TraceElement) partner;
      return
        mProductDES == trace.mProductDES &&
        mAutomata.equals(trace.mAutomata) &&
        EqualCollection.isEqualListByContents(mTraceSteps, trace.mTraceSteps);
    } else {
      return false;
    }    
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    result *= 5;
    result += mAutomata.hashCode();
    result *= 5;
    result += EqualCollection.getListHashCodeByContents(mTraceSteps);
    return result;
  }


  //#########################################################################
  //# Local Class AutomataSet
  private class AutomataSet extends IndexedHashSet<AutomatonProxy> {

    //#######################################################################
    //# Constructor
    AutomataSet(final Collection<? extends AutomatonProxy> automata)
    {
      super(automata.size());
      insertAllUnique(automata);
    }

    //#######################################################################
    //# Overrides from abstract class HashSetProxy
    protected ItemNotFoundException createItemNotFound(final String name)
    {
      return new ItemNotFoundException
        ("Trace '" + getName() +
         "' does not contain the automaton named '" + name + "'!");
    }

    protected NameNotFoundException createNameNotFound(final String name)
    {
      return new NameNotFoundException
        ("Trace '" + getName() +
         "' does not contain an automaton named '" + name + "'!");
    }

    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Trace '" + getName() +
         "' already contains an automaton named '" + name + "'!");
    }
  
  }


  //#########################################################################
  //# Local Class TraceEventList
  private static class TraceEventList extends AbstractList<EventProxy> {

    //#######################################################################
    //# Constructor
    private TraceEventList(final List<TraceStepProxy> steps)
    {
      mTraceSteps = steps;
    }

    //#######################################################################
    //# Interface java.util.List
    public int size()
    {
      return mTraceSteps.size() - 1;
    }

    public EventProxy get(final int index)
    {
      final TraceStepProxy step = mTraceSteps.get(index + 1);
      return step.getEvent();
    }

    //#######################################################################
    //# Data Members
    private final List<TraceStepProxy> mTraceSteps;

  }


  //#########################################################################
  //# Data Members
  private final ProductDESProxy mProductDES;
  private final Set<AutomatonProxy> mAutomata;
  private final List<TraceStepProxy> mTraceSteps;
  private List<EventProxy> mTraceEvents;

}
