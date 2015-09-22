//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.plain.des;

import java.net.URI;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.ImmutableOrderedSet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
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
   *                      trace, or <CODE>null</CODE>.
   * @param  des          The product DES for which this trace is
   *                      generated.
   * @param  automata     The set of automata for the new trace,
   *                      or <CODE>null</CODE> if empty.
   * @param  steps        The list of trace steps constituting the
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
    if (steps.isEmpty()) {
      throw new IllegalArgumentException
        ("Step list for trace may not be empty!");
    }
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
  @Override
  public TraceElement clone()
  {
    return (TraceElement) super.clone();
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
    return mAutomata;
  }

  @Override
  public List<TraceStepProxy> getTraceSteps()
  {
    return mTraceSteps;
  }

  @Override
  public List<EventProxy> getEvents()
  {
    if (mTraceEvents == null) {
      mTraceEvents = new TraceEventList(mTraceSteps);
    }
    return mTraceEvents;
  }


  //#########################################################################
  //# Local Class AutomataSet
  private class AutomataSet extends ImmutableOrderedSet<AutomatonProxy> {

    //#######################################################################
    //# Constructor
    AutomataSet(final Collection<? extends AutomatonProxy> automata)
    {
      super(automata);
    }

    //#######################################################################
    //# Overrides from abstract class
    //# net.sourceforge.waters.model.base.ImmutableOrderedSet
    @Override
    protected DuplicateNameException createDuplicateName(final String name)
    {
      return new DuplicateNameException
        ("Trace '" + getName() +
         "' already contains an automaton named '" + name + "'!");
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
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
    @Override
    public int size()
    {
      return mTraceSteps.size() - 1;
    }

    @Override
    public EventProxy get(final int index)
    {
      if (index >= 0) {
        final TraceStepProxy step = mTraceSteps.get(index + 1);
        return step.getEvent();
      } else {
        throw new IndexOutOfBoundsException
          ("Bad event index in trace: " + index);
      }
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}








