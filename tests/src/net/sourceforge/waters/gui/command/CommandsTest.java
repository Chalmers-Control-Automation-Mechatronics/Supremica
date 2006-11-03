package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import java.awt.Point;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.gui.renderer.LabelProxyShape;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import junit.framework.TestSuite;
import junit.framework.TestCase;

public class CommandsTest extends TestCase
{
  public void testCommands()
  {
    GraphSubject graph1 = new GraphSubject();
    PointGeometrySubject point = new PointGeometrySubject(new Point(10, 15));
    LabelGeometrySubject label = new LabelGeometrySubject(
      new Point(LabelProxyShape.DEFAULTOFFSETX,
                LabelProxyShape.DEFAULTOFFSETY));
    SimpleNodeSubject node1 = new SimpleNodeSubject("s0", new PlainEventListSubject(),
                                                    true, point, null, label);
    point = new PointGeometrySubject(new Point(20, 15));
    label = new LabelGeometrySubject(
      new Point(LabelProxyShape.DEFAULTOFFSETX,
                LabelProxyShape.DEFAULTOFFSETY));
    SimpleNodeSubject node2 = new SimpleNodeSubject("s1", new PlainEventListSubject(),
                                                    false, point, null, label);
    point = new PointGeometrySubject(new Point(30, 15));
    label = new LabelGeometrySubject(
      new Point(LabelProxyShape.DEFAULTOFFSETX,
                LabelProxyShape.DEFAULTOFFSETY));
    SimpleNodeSubject node3 = new SimpleNodeSubject("s2", new PlainEventListSubject(),
                                                    false, point, null, label);
    Command c1 = new CreateNodeCommand(graph1, 10, 15);
    c1.execute();
    Command c2 = new CreateNodeCommand(graph1, 20, 15);
    c2.execute();
    Command c3 = new CreateNodeCommand(graph1, 30, 15);
    c3.execute();
    GraphSubject graph2 = new GraphSubject();
    graph2.getNodesModifiable().add(node1);
    graph2.getNodesModifiable().add(node2);
    graph2.getNodesModifiable().add(node3);
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
