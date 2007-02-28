//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   AbstractModuleTest
//###########################################################################
//# $Id: AbstractModuleTest.java,v 1.10 2007-02-28 00:03:24 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.AbstractJAXBTest;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public abstract class AbstractModuleTest extends AbstractJAXBTest<ModuleProxy>
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

  public void testMarshal_marked_value()
    throws Exception
  {
    testMarshal("tests", "nasty", "marked_value");
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

  public void testParse_marked_value()
    throws Exception
  {
    testParse("tests", "nasty", "marked_value");
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

    final PlainEventListProxy props =
      factory.createPlainEventListProxy(null);
    final SimpleNodeProxy node =
      factory.createSimpleNodeProxy("s0", props, true, null, null, null);
    final LabelBlockProxy labelblock = factory.createLabelBlockProxy();
    final EdgeProxy edge =
      factory.createEdgeProxy(node, node, labelblock, null, null, null, null);
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final List<EdgeProxy> edges = Collections.singletonList(edge);
    final GraphProxy graph =
      factory.createGraphProxy(true, null, nodes, edges);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, null, null, null, compList);

    testHandcraft("handwritten", module);
  }


  public void testHandcraft_initarrow()
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();

    final String compname = "comp";
    final String modname = "initarrow";

    final PlainEventListProxy props =
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
    final List<SimpleNodeProxy> nodes = Collections.singletonList(node);
    final GraphProxy graph =
      factory.createGraphProxy(true, null, nodes, null);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, null, null, null, compList);

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

    final PlainEventListProxy nprops =
      factory.createPlainEventListProxy(null);
    final Point2D point = new Point(50, 50);
    final PointGeometryProxy pointgeo =
      factory.createPointGeometryProxy(point);
    final SimpleNodeProxy node =
      factory.createSimpleNodeProxy("s0", nprops, true, pointgeo, null, null);
    final PlainEventListProxy gprops =
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
    final List<NodeProxy> nodes = new LinkedList<NodeProxy>();
    nodes.add(node);
    nodes.add(group);
    final List<EdgeProxy> edges = Collections.singletonList(edge);
    final GraphProxy graph =
      factory.createGraphProxy(true, null, nodes, edges);
    final IdentifierProxy compident =
      factory.createSimpleIdentifierProxy(compname);
    final SimpleComponentProxy comp =
      factory.createSimpleComponentProxy(compident, ComponentKind.SPEC, graph);

    final List<EventDeclProxy> eventList = Collections.singletonList(decl);
    final List<SimpleComponentProxy> compList =
      Collections.singletonList(comp);
    final ModuleProxy module = factory.createModuleProxy
      (modname, null, null, null, null, eventList, null, compList);

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
    mIntegrityChecker.check(module);
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
    mIntegrityChecker = ModuleIntegrityChecker.getInstance();
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
  private ModuleIntegrityChecker mIntegrityChecker;

}
