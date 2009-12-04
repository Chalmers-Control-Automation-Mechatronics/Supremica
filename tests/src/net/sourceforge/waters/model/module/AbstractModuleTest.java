//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   AbstractModuleTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.module;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
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

  public void testMarshal_parManEg_I_mfb_highlevel()
    throws Exception
  {
    testMarshal("tests", "hisc", "parManEg_I_mfb_highlevel");
  }

  public void testMarshal_parManEg_I_mfb_lowlevel()
    throws Exception
  {
    testMarshal("tests", "hisc", "parManEg_I_mfb_lowlevel");
  }

  public void testMarshal_rhone_subsystem1_ld()
    throws Exception
  {
    testMarshal("tests", "hisc", "rhone_subsystem1_ld");
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

  public void testMarshal_startgeo()
    throws Exception
  {
    testMarshal("tests", "nasty", "startgeo");
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

  public void testClone_startgeo()
    throws Exception
  {
    testClone("tests", "nasty", "startgeo");
  }

  public void testClone_tictactoe()
    throws Exception
  {
    testClone("handwritten", "tictactoe");
  }


  //#########################################################################
  //# Cross-Cloning Test Cases (subject->plain and vice versa)
  public void testCrossClone_buffer_sf1()
    throws Exception
  {
    testCrossClone("handwritten", "buffer_sf1");
  }

  public void testCrossClone_colours()
    throws Exception
  {
    testCrossClone("handwritten", "colours");
  }

  public void testCrossClone_machine()
    throws Exception
  {
    testCrossClone("handwritten", "machine");
  }

  public void testCrossClone_nodegroup1()
    throws Exception
  {
    testCrossClone("handwritten", "nodegroup1");
  }

  public void testCrossClone_nodegroup2()
    throws Exception
  {
    testCrossClone("handwritten", "nodegroup2");
  }

  public void testCrossClone_parManEg_I_mfb_highlevel()
    throws Exception
  {
    testCrossClone("tests", "hisc", "parManEg_I_mfb_highlevel");
  }

  public void testCrossClone_parManEg_I_mfb_lowlevel()
    throws Exception
  {
    testCrossClone("tests", "hisc", "parManEg_I_mfb_lowlevel");
  }

  public void testCrossClone_rhone_subsystem1_ld()
    throws Exception
  {
    testCrossClone("tests", "hisc", "rhone_subsystem1_ld");
  }

  public void testCrossClone_small_factory_2()
    throws Exception
  {
    testCrossClone("handwritten", "small_factory_2");
  }

  public void testCrossClone_small_factory_n()
    throws Exception
  {
    testCrossClone("handwritten", "small_factory_n");
  }

  public void testCrossClone_startgeo()
    throws Exception
  {
    testCrossClone("tests", "nasty", "startgeo");
  }

  public void testCrossClone_tictactoe()
    throws Exception
  {
    testCrossClone("handwritten", "tictactoe");
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
      (modname, null, null, null, null, null, compList);

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

    final IdentifierProxy eventident =
      factory.createSimpleIdentifierProxy(eventname);
    final EventDeclProxy decl = factory.createEventDeclProxy
      (eventident, EventKind.CONTROLLABLE);

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
      (modname, null, null, null, eventList, null, compList);

    testHandcraft("handwritten", module);
  }


  public void testHandcraft_startgeo()
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();

    final String compname = "startgeo";
    final String modname = compname;

    final Point2D point0 = new Point(128, 80);
    final PointGeometryProxy pointgeo0 =
      factory.createPointGeometryProxy(point0);
    final SimpleNodeProxy node0 =
      factory.createSimpleNodeProxy("S0", null, true, pointgeo0, null, null);
    final Point2D point1 = new Point(160, 208);
    final PointGeometryProxy pointgeo1 =
      factory.createPointGeometryProxy(point1);
    final SimpleNodeProxy node1 =
      factory.createSimpleNodeProxy("S1", null, false, pointgeo1, null, null);
    final Rectangle2D rect = new Rectangle(80, 144, 144, 112);
    final BoxGeometryProxy boxgeo = factory.createBoxGeometryProxy(rect);
    final List<SimpleNodeProxy> children = Collections.singletonList(node1);
    final GroupNodeProxy group =
      factory.createGroupNodeProxy("G0", null, children, boxgeo);
    final Point2D start = new Point(80, 207);
    final PointGeometryProxy startgeo =
      factory.createPointGeometryProxy(start);
    final EdgeProxy edge = factory.createEdgeProxy
      (group, node0, null, null, null, startgeo, null);
    final List<NodeProxy> nodes = new LinkedList<NodeProxy>();
    nodes.add(node0);
    nodes.add(node1);
    nodes.add(group);
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
      (modname, null, null, null, null, null, compList);

    testHandcraft("tests", "nasty", module);
  }


  //#########################################################################
  //# Cross Cloning (only for modules so far)
  protected ModuleProxy testCrossClone(final String name)
    throws Exception
  {
    return testCrossClone(getWatersInputRoot(), name);
  }

  protected ModuleProxy testCrossClone(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testCrossClone(dir, name);
  }

  protected ModuleProxy testCrossClone(final String dirname1,
                                       final String dirname2,
                                       final String name)
    throws Exception
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testCrossClone(dir2, name);
  }

  protected ModuleProxy testCrossClone(final File dir, final String name)
    throws Exception
  {
    final ProxyUnmarshaller<ModuleProxy> unmarshaller = getProxyUnmarshaller();
    final String extname = name + unmarshaller.getDefaultExtension();
    final File filename = new File(dir, extname);
    return testCrossClone(filename);
  }

  protected ModuleProxy testCrossClone(final File filename)
    throws Exception
  {
    final ProxyUnmarshaller<ModuleProxy> unmarshaller =
      getAlternateProxyUnmarshaller();
    final ModuleProxyCloner cloner = getModuleProxyCloner();
    final URI uri = filename.toURI();
    final ModuleProxy module = unmarshaller.unmarshal(uri);
    final ModuleProxy cloned = (ModuleProxy) cloner.getClone(module);
    checkIntegrity(cloned);
    assertTrue("Clone differs from original!",
               cloned.equalsWithGeometry(module));
    return cloned;
  }


  //#########################################################################
  //# Simple Access
  protected ModuleProxyCloner getModuleProxyCloner()
  {
    return mCloner;
  }

  protected ProxyUnmarshaller<ModuleProxy> getAlternateProxyUnmarshaller()
  {
    return mAltMarshaller;
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

  protected ModuleIntegrityChecker getIntegrityChecker()
  {
    return ModuleIntegrityChecker.getInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxyFactory altfactory = getAlternateModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mCloner = new ModuleProxyCloner(factory);
    mMarshaller = new JAXBModuleMarshaller(factory, optable);
    mAltMarshaller = new JAXBModuleMarshaller(altfactory, optable);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ModuleProxyPrinter(writer);
  }

  protected void tearDown()
    throws Exception
  {
    mMarshaller = null;
    mAltMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getModuleProxyFactory();
  protected abstract ModuleProxyFactory getAlternateModuleProxyFactory();


  //#########################################################################
  //# Data Members
  private ModuleProxyCloner mCloner;
  private JAXBModuleMarshaller mMarshaller;
  private JAXBModuleMarshaller mAltMarshaller;
  private ProxyPrinter mPrinter;

}
