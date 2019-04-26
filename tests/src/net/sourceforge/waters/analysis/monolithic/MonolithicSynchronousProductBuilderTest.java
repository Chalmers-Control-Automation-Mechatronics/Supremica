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
