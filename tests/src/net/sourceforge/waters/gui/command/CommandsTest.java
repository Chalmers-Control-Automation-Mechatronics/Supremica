//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CommandsTest
//###########################################################################
//# $Id: CommandsTest.java,v 1.4 2007-02-14 02:01:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.awt.Point;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


public class CommandsTest extends TestCase
{
  public void testCommands()
  {
    final GraphSubject graph1 = new GraphSubject();
    final Point p1 = new Point(10, 15);
    final Point p2 = new Point(20, 15);
    final Point p3 = new Point(30, 15);
    final Command c1 = new CreateNodeCommand(graph1, p1);
    c1.execute();
    final Command c2 = new CreateNodeCommand(graph1, p2);
    c2.execute();
    final Command c3 = new CreateNodeCommand(graph1, p3);
    c3.execute();

    final GraphSubject graph2 = new GraphSubject();
    final PointGeometrySubject geo1 = new PointGeometrySubject(p1);
    final PointGeometrySubject geo2 = new PointGeometrySubject(p2);
    final PointGeometrySubject geo3 = new PointGeometrySubject(p3);
    final LabelGeometrySubject label1 =
      new LabelGeometrySubject(LabelProxyShape.DEFAULTOFFSET);
    final LabelGeometrySubject label2 =
      new LabelGeometrySubject(LabelProxyShape.DEFAULTOFFSET);
    final LabelGeometrySubject label3 =
      new LabelGeometrySubject(LabelProxyShape.DEFAULTOFFSET);
    final PointGeometrySubject initgeo =
      new PointGeometrySubject(new Point(-5, -5));
    final SimpleNodeSubject node1 =
      new SimpleNodeSubject("S0", null, true, geo1, initgeo, label1);
    final SimpleNodeSubject node2 =
      new SimpleNodeSubject("S1", null, false, geo2, null, label2);
    final SimpleNodeSubject node3 =
      new SimpleNodeSubject("S2", null, false, geo3, null, label3);
    graph2.getNodesModifiable().add(node1);
    graph2.getNodesModifiable().add(node3);
    graph2.getNodesModifiable().add(node2);

    assertTrue(graph1.equalsWithGeometry(graph2));

    c1.undo();
    graph2.getNodesModifiable().remove(node1);
    assertTrue(graph1.equalsWithGeometry(graph2));
  }
      
  public static TestSuite suite()
  {
      return new TestSuite(CommandsTest.class);
  }
  
  public static void main(String[] args) {
      junit.textui.TestRunner.run(suite());
  }
}
