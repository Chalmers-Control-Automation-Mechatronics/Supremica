//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   MemStateProxy
//###########################################################################
//# $Id$
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
