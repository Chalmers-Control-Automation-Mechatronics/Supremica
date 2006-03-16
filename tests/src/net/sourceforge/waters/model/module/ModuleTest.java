//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleTest
//###########################################################################
//# $Id: ModuleTest.java,v 1.7 2006-03-16 04:44:46 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTestCase;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class ModuleTest extends JAXBTestCase<ModuleProxy>
{

  //#########################################################################
  //# Marshalling Test Cases
  public void testMarshal_buffer_sf1()
    throws Exception
  {
    testMarshal("handwritten", "buffer_sf1");
  }

  public void testMarshal_colours()
    throws Exception
  {
    testMarshal("handwritten", "colours");
  }

  public void testMarshal_machine()
    throws Exception
  {
    testMarshal("handwritten", "machine");
  }

  public void testMarshal_nodegroup1()
    throws Exception
  {
    testMarshal("handwritten", "nodegroup1");
  }

  public void testMarshal_nodegroup2()
    throws Exception
  {
    testMarshal("handwritten", "nodegroup2");
  }

  public void testMarshal_small_factory_2()
    throws Exception
  {
    testMarshal("handwritten", "small_factory_2");
  }

  public void testMarshal_small_factory_n()
    throws Exception
  {
    testMarshal("handwritten", "small_factory_n");
  }

  public void testMarshal_tictactoe()
    throws Exception
  {
    testMarshal("handwritten", "tictactoe");
  }


  //#########################################################################
  //# Jar Test Cases
  public void testJar_buffer_sf1()
    throws Exception
  {
    testJar("handwritten", "buffer_sf1");
  }

  public void testJar_machine()
    throws Exception
  {
    testJar("handwritten", "machine");
  }

  public void testJar_small_factory_2()
    throws Exception
  {
    testJar("handwritten", "small_factory_2");
  }

  public void testJar_small_factory_n()
    throws Exception
  {
    testJar("handwritten", "small_factory_n");
  }

  public void testJar_tictactoe()
    throws Exception
  {
    testJar("handwritten", "tictactoe");
  }


  //#########################################################################
  //# Printing Test Cases
  public void testParse_buffer_sf1()
    throws Exception
  {
    testParse("handwritten", "buffer_sf1");
  }

  public void testParse_colours()
    throws Exception
  {
    testParse("handwritten", "colours");
  }

  public void testParse_machine()
    throws Exception
  {
    testParse("handwritten", "machine");
  }

  public void testParse_nodegroup1()
    throws Exception
  {
    testParse("handwritten", "nodegroup1");
  }

  public void testParse_nodegroup2()
    throws Exception
  {
    testParse("handwritten", "nodegroup2");
  }

  public void testParse_small_factory_2()
    throws Exception
  {
    testParse("handwritten", "small_factory_2");
  }

  public void testParse_small_factory_n()
    throws Exception
  {
    testParse("handwritten", "small_factory_n");
  }

  public void testParse_tictactoe()
    throws Exception
  {
    testParse("handwritten", "tictactoe");
  }


  //#########################################################################
  //# Cloning Test Cases
  public void testClone_buffer_sf1()
    throws Exception
  {
    testClone("handwritten", "buffer_sf1");
  }

  public void testClone_colours()
    throws Exception
  {
    testClone("handwritten", "colours");
  }

  public void testClone_machine()
    throws Exception
  {
    testClone("handwritten", "machine");
  }

  public void testClone_nodegroup1()
    throws Exception
  {
    testClone("handwritten", "nodegroup1");
  }

  public void testClone_nodegroup2()
    throws Exception
  {
    testClone("handwritten", "nodegroup2");
  }

  public void testClone_small_factory_2()
    throws Exception
  {
    testClone("handwritten", "small_factory_2");
  }

  public void testClone_small_factory_n()
    throws Exception
  {
    testClone("handwritten", "small_factory_n");
  }

  public void testClone_tictactoe()
    throws Exception
  {
    testClone("handwritten", "tictactoe");
  }


  //#########################################################################
  //# Handcrafting Test Cases
  public void testHandcraft_emptyedge()
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();

    final String compname = "comp";
    final String modname = "emptyedge";

    final EventListExpressionProxy props =
      factory.createPlainEventListProxy(null);
    final SimpleNodeProxy node =
      factory.createSimpleNodeProxy("s0", props, true, null, null, null);
    final LabelBlockProxy labelblock = factory.createLabelBlockProxy();
    final EdgeProxy edge = factory.createEdgeProxy(node, node, labelblock);
    final LabelBlockProxy blocked = factory.createLabelBlockProxy();
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final List<EdgeProxy> edges = Collections.singletonList(edge);
    final GraphProxy graph =
      factory.createGraphProxy(true, blocked, nodes, edges);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, null, null, compList);

    testHandcraft("handwritten", module);
  }


  public void testHandcraft_initarrow()
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();

    final String compname = "comp";
    final String modname = "initarrow";

    final EventListExpressionProxy props =
      factory.createPlainEventListProxy(null);
    final Point2D point = new Point(48, 48);
    final PointGeometryProxy pointgeo =
      factory.createPointGeometryProxy(point);
    final Point2D arrow = new Point(16, -16);
    final PointGeometryProxy arrowgeo =
      factory.createPointGeometryProxy(arrow);
    final SimpleNodeProxy node =
      factory.createSimpleNodeProxy("s0", props, true, 
                                    pointgeo, arrowgeo, null);
    final LabelBlockProxy blocked = factory.createLabelBlockProxy();
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final GraphProxy graph =
      factory.createGraphProxy(true, blocked, nodes, null);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, null, null, compList);

    testHandcraft("handwritten", module);
  }


  public void testHandcraft_pointgeo()
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();

    final String eventname = "event";
    final String compname = "comp";
    final String modname = "pointgeo";

    final EventDeclProxy decl = factory.createEventDeclProxy
      (eventname, EventKind.CONTROLLABLE);

    final EventListExpressionProxy nprops =
      factory.createPlainEventListProxy(null);
    final Point2D point = new Point(50, 50);
    final PointGeometryProxy pointgeo =
      factory.createPointGeometryProxy(point);
    final SimpleNodeProxy node =
      factory.createSimpleNodeProxy("s0", nprops, true, pointgeo, null, null);
    final EventListExpressionProxy gprops =
      factory.createPlainEventListProxy(null);
    final Rectangle2D rect = new Rectangle(0, 0, 100, 100);
    final BoxGeometryProxy boxgeo = factory.createBoxGeometryProxy(rect);
    final List<SimpleNodeProxy> children = Collections.singletonList(node);
    final GroupNodeProxy group =
      factory.createGroupNodeProxy("g0", gprops, children, boxgeo);
    final SimpleIdentifierProxy label =
      factory.createSimpleIdentifierProxy(eventname);
    final List<SimpleIdentifierProxy> labels =
      Collections.singletonList(label);
    final LabelBlockProxy labelblock =
      factory.createLabelBlockProxy(labels, null);
    final Point2D start = new Point(0, 25);
    final PointGeometryProxy startgeo =
      factory.createPointGeometryProxy(start);
    final Point2D end = new Point(50, 25);
    final PointGeometryProxy endgeo = factory.createPointGeometryProxy(end);
    final EdgeProxy edge = factory.createEdgeProxy
      (group, group, labelblock, null, null, startgeo, endgeo);
    final LabelBlockProxy blocked = factory.createLabelBlockProxy();
    final List<NodeProxy> nodes = new LinkedList<NodeProxy>();
    nodes.add(node);
    nodes.add(group);
    final List<EdgeProxy> edges = Collections.singletonList(edge);
    final GraphProxy graph =
      factory.createGraphProxy(true, blocked, nodes, edges);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<EventDeclProxy> eventList = Collections.singletonList(decl);
    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, eventList, null, compList);

    testHandcraft("handwritten", module);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  protected ProxyMarshaller<ModuleProxy> getProxyMarshaller()
  {
    return mMarshaller;
  }

  protected ProxyUnmarshaller<ModuleProxy> getProxyUnmarshaller()
  {
    return mMarshaller;
  }

  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  protected void checkIntegrity(final ModuleProxy module)
    throws Exception
  {
    checkModuleIntegrity(module);
  }


  //#########################################################################
  //# Integrity Checking
  private void checkModuleIntegrity(final ModuleProxy module)
  {
    final List<Proxy> components = module.getComponentList();
    checkComponentListIntegrity(components);
  }

  private void checkComponentListIntegrity(final List<Proxy> list)
  {
    for (final Proxy proxy : list) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        checkGraphIntegrity(comp);
      } else if (proxy instanceof InstanceProxy) {
        // O.K.
      } else if (proxy instanceof ForeachComponentProxy) {
        final ForeachComponentProxy foreach = (ForeachComponentProxy) proxy;
        final List<Proxy> body = foreach.getBody();
        checkComponentListIntegrity(body);
      } else {
        fail("Bad component type " + proxy.getClass().getName());
      } 
    }
  }

  private void checkGraphIntegrity(final SimpleComponentProxy comp)
  {
    final GraphProxy graph = comp.getGraph();
    final Set<NodeProxy> nodes = graph.getNodes();
    final Map<NodeProxy,NodeProxy> map =
      new IdentityHashMap<NodeProxy,NodeProxy>(nodes.size());
    for (final NodeProxy node : nodes) {
      if (node instanceof SimpleNodeProxy) {
        map.put(node, node);
      } else if (node instanceof GroupNodeProxy) {
        final Set<NodeProxy> children = node.getImmediateChildNodes();
        for (final NodeProxy child : children) {
          assertTrue("Bad child node '" + child.getName() +
                     "' found in group node '" + node.getName() +
                     "' of graph '" + comp.getName() + "'!",
                     map.containsKey(child));
        }
        map.put(node, node);
      } else {
        fail("Bad node type " + node.getClass().getName() +
             " in graph '" + comp.getName() + "'!");
      }
    }
    final List<EdgeProxy> edges = graph.getEdges();
    for (final EdgeProxy edge : edges) {
      final NodeProxy source = edge.getSource();
      assertTrue("Bad source node '" + source.getName() +
                 "' in graph '" + comp.getName() + "'!",
                 map.containsKey(source));
      final NodeProxy target = edge.getTarget();
      assertTrue("Bad target node '" + target.getName() +
                 "' in graph '" + comp.getName() + "'!",
                 map.containsKey(target));
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  { 
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mMarshaller = new JAXBModuleMarshaller(factory, optable);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ModuleProxyPrinter(writer);
  }

  protected void tearDown()
    throws Exception
  {
    mMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getModuleProxyFactory();


  //#########################################################################
  //# Data Members
  private JAXBModuleMarshaller mMarshaller;
  private ProxyPrinter mPrinter;

}
