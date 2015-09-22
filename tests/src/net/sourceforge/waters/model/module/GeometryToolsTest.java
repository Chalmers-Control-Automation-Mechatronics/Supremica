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

package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sourceforge.waters.subject.module.GeometryTools;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class GeometryToolsTest extends TestCase
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(GeometryToolsTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testFindClosestPointOnQuadratic__1_0()
  {
    checkFindClosestPointOnQuadratic__1(0);
  }

  public void testFindClosestPointOnQuadratic__1_00001()
  {
    checkFindClosestPointOnQuadratic__1(0.0001);
  }

  public void testFindClosestPointOnQuadratic__1_001()
  {
    checkFindClosestPointOnQuadratic__1(0.01);
  }

  public void testFindClosestPointOnQuadratic__1_1()
  {
    checkFindClosestPointOnQuadratic__1(1);
  }

  public void testFindClosestPointOnQuadratic__1_5()
  {
    checkFindClosestPointOnQuadratic__1(5);
  }

  public void testFindClosestPointOnQuadratic__1()
  {
    final Point2D p1 =
      new Point2D.Double(86.63427569872661, 85.43400149676005);
    final Point2D c =
      new Point2D.Double(119.6140600474233, 125.45226924006981);
    final Point2D p2 =
      new Point2D.Double(154.20524543405702, 166.75183512905613);
    final Point2D p =
      new Point.Double(54.26703475875043, 45.67168613808589);
    final Point2D closest1 =
      GeometryTools.findClosestPointOnQuadratic(p1, c, p2, p);
    assertEquals(p1, closest1);
    final Point2D closest2 =
      GeometryTools.findClosestPointOnQuadratic(p2, c, p1, p);
    assertEquals(p1, closest2);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkFindClosestPointOnQuadratic__1(final double d)
  {
    final Point2D p1 = new Point(100, 100);
    final Point2D c = new Point2D.Double(150 + d, 150 - d);
    final Point2D p2 = new Point(200, 200);
    final Point2D p = new Point(50, 50);
    final Point2D closest1 =
      GeometryTools.findClosestPointOnQuadratic(p1, c, p2, p);
    assertEquals(p1, closest1);
    final Point2D closest2 =
      GeometryTools.findClosestPointOnQuadratic(p2, c, p1, p);
    assertEquals(p1, closest2);
  }

}








