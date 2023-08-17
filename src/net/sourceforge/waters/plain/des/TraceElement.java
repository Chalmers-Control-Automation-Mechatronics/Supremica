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

package net.sourceforge.waters.plain.des;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.plain.base.Element;


/**
 * A counterexample trace for some automata of a product DES.
 * This is a simple immutable implementation of the {@link TraceProxy}
 * interface.
 *
 * @author Robi Malik
 */

public class TraceElement
  extends Element
  implements TraceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new trace.
   * @param  name         The name to be given to the new trace.
   * @param  steps        The list of trace steps constituting the
   *                      new trace. This list may not be empty, because
   *                      the first step must always represent the
   *                      initial state.
   * @param  loopIndex    Start of loop, or <CODE>-1</CODE>.
   * @throws ItemNotFoundException to indicate that one of the given
   *                      automata, events, or states cannot be found
   *                      in the product DES.
   */
  TraceElement(final String name,
               final List<? extends TraceStepProxy> steps,
               final int loopIndex)
  {
    mName = name;
    final List<TraceStepProxy> stepscopy =
      new ArrayList<TraceStepProxy>(steps);
    if (steps.isEmpty()) {
      throw new IllegalArgumentException
        ("Step list for trace may not be empty!");
    }
    mTraceSteps = Collections.unmodifiableList(stepscopy);
    mLoopIndex = loopIndex;
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
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return TraceProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitTraceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.TraceProxy
  @Override
  public String getName()
  {
    return mName;
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

  @Override
  public int getLoopIndex()
  {
    return mLoopIndex;
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
  private final String mName;
  private final List<TraceStepProxy> mTraceSteps;
  private List<EventProxy> mTraceEvents;
  private final int mLoopIndex;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -8739583251600788986L;

}
