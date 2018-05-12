//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;


/**
 * A more efficient implementation of the {@link StateProxy} interface.
 * This class stores the state name together with the initial status
 * as an integer in order to avoid storing a long string when it is not
 * needed.
 *
 * @author Simon Ware, Robi Malik
 */

public class MemStateProxy
  implements StateProxy, Cloneable
{

  //#######################################################################
  //# Constructor
  public MemStateProxy(final int code,
                       final boolean init)
  {
    this(code, init, EMPTY);
  }

  public MemStateProxy(final int code,
                       final boolean init,
                       final Collection<EventProxy> props)
  {
    mCode = init ? (code | TAG_INITIAL) : code;
    mProps = props;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.des.StateProxy
  public String getName()
  {
    return "S:" + getStateCode();
  }

  public boolean isInitial()
  {
    return (mCode & TAG_INITIAL) != 0;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mProps;
  }

  public MemStateProxy clone()
  {
    try {
      return (MemStateProxy) super.clone();
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public boolean refequals(final NamedProxy named)
  {
    if (named != null && named.getClass() == getClass()) {
      final MemStateProxy state = (MemStateProxy) named;
      return state.getStateCode() == getStateCode();
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return getStateCode();
  }

  public Object acceptVisitor(final ProxyVisitor visitor)
      throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor =
        (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }

  public Class<StateProxy> getProxyInterface()
  {
    return StateProxy.class;
  }

  public int compareTo(final NamedProxy named)
  {
    final Class<?> clazz1 = getClass();
    final Class<?> clazz2 = named.getClass();
    if (clazz1 == clazz2) {
      final MemStateProxy state = (MemStateProxy) named;
      return getStateCode() - state.getStateCode();
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
  public String toString()
  {
    return ProductDESProxyPrinter.getPrintString(this);
  }


  //#######################################################################
  //# Simple Access
  public int getStateCode()
  {
    return mCode & ~TAG_INITIAL;
  }


  //#######################################################################
  //# Data Members
  private final int mCode;
  private final Collection<EventProxy> mProps;


  //#######################################################################
  //# Class Constants
  private static final Collection<EventProxy> EMPTY = Collections.emptyList();
  private static final int TAG_INITIAL = 0x80000000;

}
