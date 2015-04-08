//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   AbstractTRSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AbstractSynchronousProductBuilderTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.IsomorphismChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractTRSynchronousProductBuilderTest
  extends AbstractSynchronousProductBuilderTest
{

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAutomatonBuilderTest
  @Override
  protected abstract TRAbstractSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory);

  @Override
  protected TRAbstractSynchronousProductBuilder getAutomatonBuilder()
  {
    return (TRAbstractSynchronousProductBuilder) super.getAutomatonBuilder();
  }

  @Override
  protected void configureAutomatonBuilder(final ProductDESProxy des)
    throws AnalysisException
  {
    super.configureAutomatonBuilder(des);
    final TRAbstractSynchronousProductBuilder builder = getAutomatonBuilder();
    builder.setPruningDeadlocks(mPruningDeadlocks);
    final Collection<EventProxy> events = des.getEvents();
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding enc = new EventEncoding();
    EventProxy tau = null;
    for (final EventProxy event : events) {
      final byte status = getEventStatusFromAttributes(event);
      if (EventStatus.isLocalEvent(status)) {
        if (tau == null) {
          final ProductDESProxyFactory factory = getProductDESProxyFactory();
          tau = factory.createEventProxy
            (":tau", EventKind.UNCONTROLLABLE, false);
          enc.addSilentEvent(tau);
        }
        enc.addEventAlias(event, tau, translator, status);
      } else {
        enc.addEvent(event, translator, status);
      }
    }
    builder.setEventEncoding(enc);
  }

  @Override
  protected void checkResult(final ProductDESProxy des,
                             final AutomatonProxy result,
                             final AutomatonProxy expected)
    throws Exception
  {
    final TRAutomatonProxy aut = (TRAutomatonProxy) result;
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    rel.checkIntegrity();
    super.checkResult(des, result, expected);
  }


  //#########################################################################
  //# Selfloop Removal Test Cases
  public void testSyncSelfloop01() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "syncselfloop_01.wmod");
  }

  public void testSyncSelfloop02() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "syncselfloop_02.wmod");
  }

  public void testSyncSelfloop03() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "syncselfloop_03.wmod");
  }


  //#########################################################################
  //# Hiding Test Cases
  public void testHiding01() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "hiding01.wmod");
  }

  public void testHiding02() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "hiding02.wmod");
  }


  //#########################################################################
  //# Forbidden Events Test Cases
  public void testForbid2() throws Exception
  {
    try {
      mPruningDeadlocks = true;
      runAutomatonBuilder("tests", "syncprod", "forbid2.wmod");
    } finally {
      mPruningDeadlocks = false;
    }
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

  public void testUnusedProposition() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "nasty", "unused_prop2.wmod");
    final EventProxy eventA = findEvent(des, "a");
    final EventProxy eventB = findEvent(des, "b");
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final Collection<EventProxy> events = new ArrayList<>(2);
    events.add(eventA);
    events.add(eventB);
    final AutomatonProxy autA = findAutomaton(des, "A");
    final AutomatonProxy autB = findAutomaton(des, "B");
    final Collection<AutomatonProxy> automata = new ArrayList<>(2);
    automata.add(autA);
    automata.add(autB);
    final ProductDESProxy input =
      factory.createProductDESProxy("unused_prop", events, automata);
    final TRAbstractSynchronousProductBuilder builder = getAutomatonBuilder();
    builder.setModel(input);
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding enc = new EventEncoding();
    enc.addEvent(eventA, translator, EventStatus.STATUS_NONE);
    enc.addEvent(eventB, translator, EventStatus.STATUS_NONE);
    final EventProxy omega = factory.createEventProxy
      (EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION);
    enc.addProposition(omega, false);
    builder.setEventEncoding(enc);
    builder.setPruningDeadlocks(true);
    final boolean ok = builder.run();
    assertTrue(ProxyTools.getShortClassName(builder) +
               " unexpectedly returned false!", ok);
    final TRSynchronousProductResult result = builder.getAnalysisResult();
    final boolean sat = result.isSatisfied();
    assertTrue(ProxyTools.getShortClassName(builder) +
               " unexpectedly returned false!", sat);
    final AutomatonProxy computed = result.getComputedAutomaton();
    final AutomatonProxy expected = findAutomaton(des, "sync");
    final IsomorphismChecker checker = getIsomorphismChecker();
    checker.checkIsomorphism(computed, expected);
  }


  //#########################################################################
  //# Data Members
  private boolean mPruningDeadlocks = false;

}
