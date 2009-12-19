//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   GeometryToolsTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Point;
import java.awt.geom.Point2D;

import net.sourceforge.waters.model.module.GeometryTools;

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

  public static void main(String args[])
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
