//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import java.util.List;

import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.xsd.des.ConflictKind;

/**
 * @author Robi Malik
 */
public class TRConflictTraceProxy
  extends TRTraceProxy
  implements ConflictCounterExampleProxy
{

  //#########################################################################
  //# Constructors
  public TRConflictTraceProxy(final ProductDESProxy des)
  {
    this(des, null);
  }

  public TRConflictTraceProxy(final ProductDESProxy des,
                              final List<EventProxy> events)
  {
    this(des, events, ConflictKind.CONFLICT);
  }

  public TRConflictTraceProxy(final ProductDESProxy des,
                              final List<EventProxy> events,
                              final ConflictKind kind)
  {
    super(AbstractConflictChecker.getTraceName(des), null, des, events);
    mKind = kind;
  }

  /**
   * Creates a new conflict trace by copying another.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   */
  TRConflictTraceProxy(final TRConflictTraceProxy trace)
  {
    super(trace);
    mKind = trace.mKind;
  }

  /**
   * Creates a new conflict trace by copying a safety trace.
   * This copy constructor performs a deep copy of events and state lists,
   * but only shallow copy of the automata and abstraction steps.
   * @param  trace  Safety trace to be copied. To conflict trace will
   *                use all events except the last of the safety trace.
   */
  TRConflictTraceProxy(final TRSafetyTraceProxy trace)
  {
    super(trace, trace.getNumberOfSteps() - 1);
    mKind = ConflictKind.CONFLICT;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public Class<? extends Proxy> getProxyInterface()
  {
    return ConflictCounterExampleProxy.class;
  }

  @Override
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desVisitor = (ProductDESProxyVisitor) visitor;
    return desVisitor.visitConflictCounterExampleProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  @Override
  public ConflictKind getKind()
  {
    return mKind;
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public TRConflictTraceProxy clone()
  {
    return (TRConflictTraceProxy) super.clone();
  }


  //#########################################################################
  //# Data Members
  private final ConflictKind mKind;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -8991236718278268958L;

}
