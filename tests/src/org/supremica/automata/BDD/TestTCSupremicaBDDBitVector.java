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

import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.BDD.SupremicaBDDBitVector.TCSupremicaBDDBitVector;

/**
 * @author jonkro
 */
public class TestTCSupremicaBDDBitVector extends AbstractTestSupremicaBDDBitVector
{

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  public void testModulo1() {
    final int dividend = 5;
    final int divisor = 4;
    final TCSupremicaBDDBitVector dividendBit = new TCSupremicaBDDBitVector(factory, factory.extDomain(dividend+1).varNum(), dividend);

    final SupremicaBDDBitVector remainderBit = dividendBit.divmod(divisor, false);

    final int remainder = remainderBit.val();

    assertEquals(dividend-divisor, remainder);
  }

  public void testDomains() {
    final int domainSize = 4;
    final BDDDomain domain = factory.extDomain(domainSize+1);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);

    final TCSupremicaBDDBitVector constant = new TCSupremicaBDDBitVector(factory, domainSize, 3);

    final TCSupremicaBDDBitVector constantNeg = new TCSupremicaBDDBitVector(factory, domainSize, -4);

    assertEquals("[-4, -3, -2, -1, 0, 1, 2, 3]", bitVector.toString());
    assertEquals("3", constant.toString());
    assertEquals("-4", constantNeg.toString());
  }

  public void testToString() {
    final int domainSize = 4;
    final BDDDomain domain = factory.extDomain(domainSize+1);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final TCSupremicaBDDBitVector constant = new TCSupremicaBDDBitVector(factory, 10, 1);
    final String s = bitVector.toString();
    final TCSupremicaBDDBitVector newVector = constant.add(bitVector);
    final String s1 = newVector.toString();

    assertEquals("[-4, -3, -2, -1, 0, 1, 2, 3]", s);
    assertEquals("[-3, -2, -1, 0, 1, 2, 3, 4]", s1);
  }

  public void testSaturate() {
    final BDDDomain domain = factory.extDomain(9);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final SupremicaBDDBitVector sat = bitVector.saturate(2, 5);
    final String s = sat.toString();

    assertEquals("[2, 3, 4, 5]", s);
  }

  public void testSaturateNeg() {
    final BDDDomain domain = factory.extDomain(9);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final SupremicaBDDBitVector sat = bitVector.saturate(-3, 4);
    final String s = sat.toString();

    assertEquals("[-3, -2, -1, 0, 1, 2, 3, 4]", s);
  }

  public void testMinMax() {
    final BDDDomain domain = factory.extDomain(9);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final SupremicaBDDBitVector sat = bitVector.saturate(2, 5);

    final int max = sat.max();
    final int min = sat.min();

    assertEquals(5, max);
    assertEquals(2, min);
  }

  public void testLargeToString() {
    final BDDDomain domain = factory.extDomain(100*2);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final SupremicaBDDBitVector sat = bitVector.saturate(15, 100);
    final String s = sat.toString();

    assertEquals("[min=15, max=100]", s);
  }

  public void testRequiredBits() {
    final BDDDomain domain = factory.extDomain(5);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain.varNum(), 2);
    final int required = bitVector.requiredBits();

    assertEquals(3, required);
  }

  public void testRequiredBitsZero() {
    final BDDDomain domain = factory.extDomain(5);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain.varNum(), 0);
    final int required = bitVector.requiredBits();

    assertEquals(0, required);
  }

  public void testRequiredBitsLarge() {
    final BDDDomain domain = factory.extDomain(2147483647);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain.varNum(), 10000);
    final int required = bitVector.requiredBits();

    assertEquals(15, required);
  }

  public void testRequiredBitsHuge() {
    final BDDDomain domain = factory.extDomain(4294967297L);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain.varNum(), 2147483648L);
    final int required = bitVector.requiredBits();

    assertEquals(33, required);
  }

  public void testRequiredBitsVariable() {
    final BDDDomain domain = factory.extDomain(5);
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, domain);
    final int required = bitVector.requiredBits();

    assertEquals(3, required);
  }

  public void testRequiredBitsNeg() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 5, -1);
    final int required = bitVector.requiredBits();

    assertEquals(1, required);
  }

  public void testRequiredBitsNeg2() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 5, -2);
    final int required = bitVector.requiredBits();

    assertEquals(2, required);
  }

  public void testResizeLargerPos() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 2, 1);
    final SupremicaBDDBitVector resize = bitVector.resize(5);

    assertEquals(1, resize.val());
  }

  public void testResizeSmallerPos() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 5, 1);
    final SupremicaBDDBitVector resize = bitVector.resize(2);

    assertEquals(1, resize.val());
  }

  public void testResizeLargerNeg() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 2, -1);
    final SupremicaBDDBitVector resize = bitVector.resize(5);

    assertEquals(-1, resize.val());
  }

  public void testResizeSmallerNeg() {
    final TCSupremicaBDDBitVector bitVector = new TCSupremicaBDDBitVector(factory, 5, -1);
    final SupremicaBDDBitVector resize = bitVector.resize(2);

    assertEquals(-1, resize.val());
  }

}
