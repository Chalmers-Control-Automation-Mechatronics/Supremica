//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSynchronousProductBuilderTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class MonolithicSynchronousProductBuilderTest
  extends AbstractSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(MonolithicSynchronousProductBuilderTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  @Override
  protected MonolithicSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new MonolithicSynchronousProductBuilder(factory);
  }

  @Override
  protected MonolithicSynchronousProductBuilder getAutomatonBuilder()
  {
    return (MonolithicSynchronousProductBuilder) super.getAutomatonBuilder();
  }

  @Override
  protected void configureAutomatonBuilder(final ProductDESProxy des)
    throws AnalysisException
  {
    super.configureAutomatonBuilder(des);
    final MonolithicSynchronousProductBuilder builder = getAutomatonBuilder();
    builder.setPruningDeadlocks(mPruningDeadlocks);
    final Collection<EventProxy> events = des.getEvents();
    for (final EventProxy event : events) {
      final EventKind kind = event.getKind();
      final String name = event.getName();
      if (kind != EventKind.PROPOSITION &&
          name.startsWith(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        builder.addForbiddenEvent(event);
      }
    }
  }


  //#########################################################################
  //# Forbidden Events Test Cases
  public void testForbid() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "forbid1.wmod");
  }


  //#########################################################################
  //# Deadlock Pruning Test Cases
  public void testDeadlockPruning() throws Exception
  {
    try {
      mPruningDeadlocks = true;
      runAutomatonBuilder("tests", "syncprod", "deadlockPruning.wmod");
    } finally {
      mPruningDeadlocks = false;
    }
  }

  public void testTip3Pruning() throws Exception
  {
    try {
      mPruningDeadlocks = true;
      runAutomatonBuilder("tests", "syncprod", "tip3pruning.wmod");
    } finally {
      mPruningDeadlocks = false;
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mPruningDeadlocks = false;

}
