//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractGeneralisedConflictCheckerTest
//###########################################################################
//# $Id: AbstractGeneralisedConflictCheckerTest.java 4768 2009-10-09 03:16:33Z robi $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.ArrayList;
import java.util.Set;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractGeneralisedConflictCheckerTest extends
    AbstractConflictCheckerTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public AbstractGeneralisedConflictCheckerTest()
  {
  }

  public AbstractGeneralisedConflictCheckerTest(final String name)
  {
    super(name);
  }

  protected void configureModelVerifier(final ProductDESProxy des)
  {
    super.configureModelVerifier(des);
    final Set<EventProxy> events = des.getEvents();
    // checks that this des does include the precondition marking
    for (final EventProxy event : events) {
      if (event.getName().equals(":alpha")
          && event.getKind().equals(EventKind.PROPOSITION)) {

        final ConflictChecker modelVer = getModelVerifier();
        modelVer.setGeneralisedPrecondition(event);
        return;
      }
    }

    fail("File does " + des.getName()
        + " not contain a proposition named :alpha.");

  }

  protected void configure(final ModuleCompiler compiler)
  {
    super.configure(compiler);
    final ArrayList<String> propositions = new ArrayList<String>(2);
    propositions.add(":alpha");
    propositions.add(EventDeclProxy.DEFAULT_MARKING_NAME);
    compiler.setEnabledPropositionNames(propositions);
  }

  // #########################################################################
  // #Test Cases --- paper (multi-coloured automata)
  public void testG1() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g1.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testG2() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g2.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testG3() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g3.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testG4() throws Exception
  {
    final String group = "tests";
    final String dir = "generalisedNonblocking";
    final String name = "g4.wmod";
    runModelVerifier(group, dir, name, false);
  }

}
