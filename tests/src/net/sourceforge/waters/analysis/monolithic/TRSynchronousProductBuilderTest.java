//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   TRSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.AbstractSynchronousProductBuilderTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public class TRSynchronousProductBuilderTest
  extends AbstractSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(TRSynchronousProductBuilderTest.class);
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
  protected TRSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new TRSynchronousProductBuilder(factory);
  }

  @Override
  protected TRSynchronousProductBuilder getAutomatonBuilder()
  {
    return (TRSynchronousProductBuilder) super.getAutomatonBuilder();
  }

  @Override
  protected void configureAutomatonBuilder(final ProductDESProxy des)
    throws AnalysisException
  {
    super.configureAutomatonBuilder(des);
    final TRSynchronousProductBuilder builder = getAutomatonBuilder();
    builder.setPruningDeadlocks(mPruningDeadlocks);
    final Collection<EventProxy> events = des.getEvents();
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding enc = new EventEncoding();
    EventProxy tau = null;
    for (final EventProxy event : events) {
      final EventKind kind = event.getKind();
      final String name = event.getName();
      if (kind != EventKind.PROPOSITION &&
          name.startsWith(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        enc.addEvent(event, translator,
                     EventStatus.STATUS_FAILING |
                     EventStatus.STATUS_OUTSIDE_ALWAYS_ENABLED);
      } else if (!event.isObservable()) {
        if (tau == null) {
          final ProductDESProxyFactory factory = getProductDESProxyFactory();
          tau = factory.createEventProxy
            (":tau", EventKind.UNCONTROLLABLE, false);
          enc.addSilentEvent(tau);
        }
        enc.addEventAlias(event, tau, translator, EventStatus.STATUS_NONE);
      }
    }
    if (enc.getNumberOfProperEvents() > 1 || tau != null) {
      try {
        final EventProxy marking =
          AbstractConflictChecker.getMarkingProposition(des);
        enc.addEvent(marking, translator, EventStatus.STATUS_NONE);
      } catch (final EventNotFoundException e) {
        // No marking---never mind!
      }
      builder.setPruningForbiddenEvents(true);
      builder.setEventEncoding(enc);
    }
  }


  //#########################################################################
  //# Selfloop Removal Test Cases
  public void testSyncSelfloop() throws Exception
  {
    runAutomatonBuilder("tests", "nasty", "syncselfloop.wmod");
  }


  //#########################################################################
  //# Hiding Test Cases
  public void testHiding01() throws Exception
  {
    runAutomatonBuilder("tests", "abstraction", "hiding01.wmod");
  }


  //#########################################################################
  //# Forbidden Events Test Cases
  public void testForbid() throws Exception
  {
    runAutomatonBuilder("tests", "abstraction", "forbid2.wmod");
  }


  //#########################################################################
  //# Deadlock Pruning Test Cases
  public void testDeadlockPruning() throws Exception
  {
    try {
      mPruningDeadlocks = true;
      runAutomatonBuilder("tests", "abstraction", "deadlockPruning.wmod");
    } finally {
      mPruningDeadlocks = false;
    }
  }

  public void testTip3Pruning() throws Exception
  {
    try {
      mPruningDeadlocks = true;
      runAutomatonBuilder("tests", "nasty", "tip3pruning.wmod");
    } finally {
      mPruningDeadlocks = false;
    }
  }


  //#########################################################################
  //# Data Members
  private boolean mPruningDeadlocks = false;

}
