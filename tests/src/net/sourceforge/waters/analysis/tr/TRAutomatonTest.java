//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TRAutomatonTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.tr;

import java.util.Collection;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AbstractAutomatonTest;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;


public class TRAutomatonTest extends AbstractAutomatonTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(TRAutomatonTest.class);
  }

  public static void main(final String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.des.AbstractAutomatonTest
  @Override
  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return new TRAutomatonProxyFactory();
  }


  //#########################################################################
  //# Inner Class TRAutomatonProxyFactory
  private static class TRAutomatonProxyFactory
    extends ProductDESElementFactory
  {
    //#######################################################################
    //# Interface net.souceforge.waters.model.des.ProductDESProxyFactory
    @Override
    public AutomatonProxy createAutomatonProxy
      (final String name,
       final ComponentKind kind,
       final Collection<? extends EventProxy> events,
       final Collection<? extends StateProxy> states,
       final Collection<? extends TransitionProxy> transitions,
       final Map<String,String> attribs)
    {
      final AutomatonProxy aut =
        super.createAutomatonProxy(name, kind, events,
                                   states, transitions, attribs);
      try {
        return TRAutomatonProxy.createTRAutomatonProxy(aut);
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public AutomatonProxy createAutomatonProxy
      (final String name,
       final ComponentKind kind,
       final Collection<? extends EventProxy> events,
       final Collection<? extends StateProxy> states,
       final Collection<? extends TransitionProxy> transitions)
    {
      final AutomatonProxy aut =
        super.createAutomatonProxy(name, kind, events,
                                   states, transitions);
      try {
        return TRAutomatonProxy.createTRAutomatonProxy(aut);
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public AutomatonProxy createAutomatonProxy
      (final String name,
       final ComponentKind kind)
    {
      final AutomatonProxy aut = super.createAutomatonProxy(name, kind);
      try {
        return TRAutomatonProxy.createTRAutomatonProxy(aut);
      } catch (final OverflowException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 916886551935825629L;
  }

}
