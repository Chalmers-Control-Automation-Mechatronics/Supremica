//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class TRReducingSynchronousProductBuilderTest
  extends AbstractTRSynchronousProductBuilderTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(TRReducingSynchronousProductBuilderTest.class);
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
  protected TRReducingSynchronousProductBuilder
    createAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    return new TRReducingSynchronousProductBuilder();
  }


  //#########################################################################
  //# Reducing Test Cases
  public void testReducing01() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_01.wmod");
  }

  public void testReducing02() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_02.wmod");
  }

  public void testReducing03() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_03.wmod");
  }

  public void testReducing04() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_04.wmod");
  }

  public void testReducing05() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_05.wmod");
  }

  public void testReducing06() throws Exception
  {
    runAutomatonBuilder("tests", "syncprod", "reducing_06.wmod");
  }

}
