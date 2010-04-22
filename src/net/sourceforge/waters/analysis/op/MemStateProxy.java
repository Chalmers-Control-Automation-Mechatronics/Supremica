//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   MemStateProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Collection;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.printer.ProductDESProxyPrinter;


/**
 * A more efficient implementation of the {@link StateProxy} interface.
 * This class stores that state name as an integer in order to avoid
 * storing a long string when it is not needed.
 *
 * @author Simon Ware
 */

public class MemStateProxy implements StateProxy
{

  //#######################################################################
  //# Constructor
  public MemStateProxy(final int code,
                       final boolean init,
                       final Collection<EventProxy> props)
  {
    mCode = code;
    mIsInitial = init;
    mProps = props;
  }


  //#######################################################################
  //# Interface net.sourceforge.waters.model.des.StateProxy
  public String getName()
  {
    return "S:" + mCode;
  }

  public boolean isInitial()
  {
    return mIsInitial;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mProps;
  }

  public MemStateProxy clone()
  {
    return new MemStateProxy(mCode, mIsInitial, mProps);
  }

  public boolean refequals(final NamedProxy named)
  {
    if (named != null && named.getClass() == getClass()) {
      final MemStateProxy s = (MemStateProxy) named;
      return s.mCode == mCode;
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return mCode;
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
      final MemStateProxy s = (MemStateProxy) named;
      return mCode - s.mCode;
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
  public int getCode()
  {
    return mCode;
  }


  //#######################################################################
  //# Data Members
  private final int mCode;
  private final boolean mIsInitial;
  private final Collection<EventProxy> mProps;

}
