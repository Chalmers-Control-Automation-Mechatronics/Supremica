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

package org.supremica.automata.BDD;

import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;

import org.supremica.automata.BDD.SupremicaBDDBitVector.PSupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;

/**
 * @author jonkro
 */
public class TestPSupremicaBDDBitVector extends TestSupremicaBDDBitVector
{

  private BDDFactory factory;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    factory = BDDFactory.init("java", 10, 1000);
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    factory.done();
  }

  public void testModulo1() {
    final int dividend = 5;
    final int divisor = 4;
    final PSupremicaBDDBitVector dividendBit = new PSupremicaBDDBitVector(factory, factory.extDomain(dividend+1).varNum(), dividend);

    final SupremicaBDDBitVector remainderBit = dividendBit.divmod(divisor, false);

    final int remainder = remainderBit.val();

    assertEquals(dividend-divisor, remainder);
  }

  public void testDomains() {
    final int domainSize = 4;
    final BDDDomain domain = factory.extDomain(domainSize+1);
    final PSupremicaBDDBitVector bitVector = new PSupremicaBDDBitVector(factory, domain.varNum(), domain);

    final PSupremicaBDDBitVector constant = new PSupremicaBDDBitVector(factory, domainSize, 3);

    assertEquals("[0, 1, 2, 3, 4, 5, 6, 7]", bitVector.toString());
    assertEquals("3", constant.toString());
  }

  public void testToString() {
    final int domainSize = 4;
    final BDDDomain domain = factory.extDomain(domainSize+1);
    final PSupremicaBDDBitVector bitVector = new PSupremicaBDDBitVector(factory, domain.varNum(), domain);
    final PSupremicaBDDBitVector constant = new PSupremicaBDDBitVector(factory, 10, 1);
    final String s = bitVector.toString();
    final PSupremicaBDDBitVector newVector = constant.add(bitVector);
    final String s1 = newVector.toString();

    assertEquals("[0, 1, 2, 3, 4, 5, 6, 7]", s);
    assertEquals("[1, 2, 3, 4, 5, 6, 7, 8]", s1);
  }

  public void testSaturate() {
    final BDDDomain domain = factory.extDomain(5);
    final PSupremicaBDDBitVector bitVector = new PSupremicaBDDBitVector(factory, domain.varNum(), domain);
    final SupremicaBDDBitVector sat = bitVector.saturate(2, 5);
    final String s = sat.toString();

    assertEquals("[2, 3, 4, 5]", s);
  }

}
