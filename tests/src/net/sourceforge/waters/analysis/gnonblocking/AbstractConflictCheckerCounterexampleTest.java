//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.gnonblocking;

import net.sourceforge.waters.analysis.modular.ProjectingLanguageInclusionChecker;
import net.sourceforge.waters.analysis.modular.Projection2;
import net.sourceforge.waters.analysis.modular.SafetyProjectionBuilder;
import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public abstract class AbstractConflictCheckerCounterexampleTest
extends AbstractConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractConflictCheckerCounterexampleTest()
  {
  }

  public AbstractConflictCheckerCounterexampleTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest
  @Override
  protected LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    final LanguageInclusionChecker checker =
      super.createLanguageInclusionChecker(des, factory);
    final SafetyProjectionBuilder projector = new Projection2(factory);
    return new ProjectingLanguageInclusionChecker(des, factory,
                                                  checker, projector);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void setConfiguration(final int islimit, final int fslimit,
                                final int itlimit, final int ftlimit)
  {
    final CompositionalGeneralisedConflictChecker checker =
        (CompositionalGeneralisedConflictChecker) getModelVerifier();
    checker.setInternalStepNodeLimit(islimit);
    checker.setFinalStepNodeLimit(fslimit);
    checker.setInternalStepTransitionLimit(itlimit);
    checker.setFinalStepTransitionLimit(ftlimit);
  }


  //#########################################################################
  //# Test Cases
  public void testFischertechnik() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "ftechnik.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testRhoneAlps() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "aip0alps.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testTbedCTCT() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_ctct.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testTbedNoderailBlock() throws Exception
  {
    setConfiguration(10000, 100000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "tbed_noderailb.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testVerriegel4B() throws Exception
  {
    setConfiguration(10000, 10000, 1000000, 1000000);
    final String group = "tests";
    final String dir = "incremental_suite";
    final String name = "verriegel4b.wmod";
    runModelVerifier(group, dir, name, false);
  }

  public void testTip3Bad() throws Exception
  {
    setConfiguration(2000, 1000000, 200000, 0);
    final String group = "tip";
    final String dir = "acsw2006";
    final String name = "tip3_bad.wmod";
    runModelVerifier(group, dir, name, false);
  }

}
