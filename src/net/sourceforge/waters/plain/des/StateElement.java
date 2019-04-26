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

package net.sourceforge.waters.plain.des;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.plain.base.NamedElement;


/**
 * A state of an automaton.
 * This is a simple immutable implementation of the {@link StateProxy}
 * interface.
 *
 * @author Robi Malik
 */

public final class StateElement
  extends NamedElement
  implements StateProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new state.
   * @param  name         The name to be given to the new state.
   * @param  initial      A flag indicating whether the new state is to be
   *                      an initial state.
   * @param  propositions The initial list of propositions for the new state,
   *                      or <CODE>null</CODE> if empty.
   *                      Each element should be of type {@link EventElement}.
   */
  StateElement(final String name,
               final boolean initial,
               final Collection<? extends EventProxy> propositions)
  {
    super(name);
    mIsInitial = initial;
    if (propositions == null) {
      mPropositions = Collections.emptyList();
    } else {
      final List<EventProxy> modifiable =
        new ArrayList<EventProxy>(propositions);
      mPropositions = Collections.unmodifiableList(modifiable);
    }
  }

  /**
   * Creates a new state using default values.
   * This constructor creates a state that is not initial
   * and has no propositions.
   * @param  name         The name to be given to the new state.
   */
  StateElement(final String name)
  {
    this(name, false, null);
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  /**
   * Returns a copy of this state.
   */
  public StateElement clone()
  {
    return (StateElement) super.clone();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.des.StateProxy
  public boolean isInitial()
  {
    return mIsInitial;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mPropositions;
  }


  //#########################################################################
  //# Equals and Hashcode
  public Class<StateProxy> getProxyInterface()
  {
    return StateProxy.class;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsInitial;
  private final Collection<EventProxy> mPropositions;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
