//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.AbstractXMLTest;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.printer.ModuleProxyPrinter;
import net.sourceforge.waters.model.printer.ProxyPrinter;


public abstract class AbstractModuleTest
  extends AbstractXMLTest<ModuleProxy>
{

  //#########################################################################
  //# Printing Test Cases
  public void testPrint_hisc0_high()
    throws Exception
  {
    testPrint("despot", "testHISC", "hisc0_high");
  }

  public void testPrint_buffer_norm()
    throws Exception
  {
    testPrint("efa", "buffer_norm");
  }

  public void testPrint_transferline_efa()
    throws Exception
  {
    testPrint("efa", "transferline_efa");
  }

  public void testPrint_buffer_sf1()
    throws Exception
  {
    testPrint("handwritten", "buffer_sf1");
  }

  public void testPrint_cell()
    throws Exception
  {
    testPrint("handwritten", "cell");
  }

  public void testPrint_machine()
    throws Exception
  {
    testPrint("handwritten", "machine");
  }

  public void testPrint_sensoractuator1()
    throws Exception
  {
    testPrint("handwritten", "sensoractuator1");
  }

  public void testPrint_small_factory_2()
    throws Exception
  {
    testPrint("handwritten", "small_factory_2");
  }

  public void testPrint_small_factory_n()
    throws Exception
  {
    testPrint("handwritten", "small_factory_n");
  }

  public void testPrint_tictactoe()
    throws Exception
  {
    testPrint("handwritten", "tictactoe");
  }

  public void testPrint_tictactoe_incomplete()
    throws Exception
  {
    testPrint("handwritten", "tictactoe_incomplete");
  }

  public void testPrint_error7_small()
    throws Exception
  {
    testPrint("tests", "compiler", "efsm", "error7_small");
  }

  public void testPrint_colours()
    throws Exception
  {
    testPrint("tests", "compiler", "graph", "colours");
  }

  public void testPrint_nodegroup1()
    throws Exception
  {
    testPrint("tests", "compiler", "groupnode", "nodegroup1");
  }

  public void testPrint_nodegroup2()
    throws Exception
  {
    testPrint("tests", "compiler", "groupnode", "nodegroup2");
  }

  public void testPrint_array2d()
    throws Exception
  {
    testPrint("tests", "compiler", "instance", "array2d");
  }

  public void testPrint_batch_tank_vout()
    throws Exception
  {
    testPrint("tests", "efa", "batch_tank_vout");
  }

  public void testPrint_eventaliases()
    throws Exception
  {
    testPrint("tests", "nasty", "eventaliases");
  }

  public void testPrint_marked_value()
    throws Exception
  {
    testPrint("tests", "nasty", "marked_value");
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
      factory.createSimpleNodeProxy("s0", props, null, true, null, null, null);
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

    testHandcraft(module, "handwritten");
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
      factory.createSimpleNodeProxy("s0", props, null, true,
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

    testHandcraft(module, "handwritten");
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
      factory.createSimpleNodeProxy("s0", nprops, null,
                                    true, pointgeo, null, null);
    final PlainEventListProxy gprops =
      factory.createPlainEventListProxy(null);
    final Rectangle2D rect = new Rectangle(0, 0, 100, 100);
    final BoxGeometryProxy boxgeo = factory.createBoxGeometryProxy(rect);
    final List<SimpleNodeProxy> children = Collections.singletonList(node);
    final GroupNodeProxy group =
      factory.createGroupNodeProxy("g0", gprops, null, children, boxgeo);
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

    testHandcraft(module, "handwritten");
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
      factory.createSimpleNodeProxy("S0", null, null,
                                    true, pointgeo0, null, null);
    final Point2D point1 = new Point(160, 208);
    final PointGeometryProxy pointgeo1 =
      factory.createPointGeometryProxy(point1);
    final SimpleNodeProxy node1 =
      factory.createSimpleNodeProxy("S1", null, null,
                                    false, pointgeo1, null, null);
    final Rectangle2D rect = new Rectangle(80, 144, 144, 112);
    final BoxGeometryProxy boxgeo = factory.createBoxGeometryProxy(rect);
    final List<SimpleNodeProxy> children = Collections.singletonList(node1);
    final GroupNodeProxy group =
      factory.createGroupNodeProxy("G0", null, null, children, boxgeo);
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

    testHandcraft(module, "tests", "nasty");
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
  //# Marshalling Test Cases
  public void testMarshal_hisc0_high()
    throws Exception
  {
    testMarshal("despot", "testHISC", "hisc0_high");
  }

  public void testMarshal_buffer_norm()
    throws Exception
  {
    testMarshal("efa", "buffer_norm");
  }

  public void testMarshal_transferline_efa()
    throws Exception
  {
    testMarshal("efa", "transferline_efa");
  }

  public void testMarshal_buffer_sf1()
    throws Exception
  {
    testMarshal("handwritten", "buffer_sf1");
  }

  public void testMarshal_cell()
    throws Exception
  {
    testMarshal("handwritten", "cell");
  }

  public void testMarshal_machine()
    throws Exception
  {
    testMarshal("handwritten", "machine");
  }

  public void testMarshal_sensoractuator1()
    throws Exception
  {
    testMarshal("handwritten", "sensoractuator1");
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

  public void testMarshal_tictactoe_incomplete()
    throws Exception
  {
    testMarshal("handwritten", "tictactoe_incomplete");
  }

  public void testMarshal_error7_small()
    throws Exception
  {
    testMarshal("tests", "compiler", "efsm", "error7_small");
  }

  public void testMarshal_colours()
    throws Exception
  {
    testMarshal("tests", "compiler", "graph", "colours");
  }

  public void testMarshal_nodegroup1()
    throws Exception
  {
    testMarshal("tests", "compiler", "groupnode", "nodegroup1");
  }

  public void testMarshal_nodegroup2()
    throws Exception
  {
    testMarshal("tests", "compiler", "groupnode", "nodegroup2");
  }

  public void testMarshal_array2d()
    throws Exception
  {
    testMarshal("tests", "compiler", "instance", "array2d");
  }

  public void testMarshal_batch_tank_vout()
    throws Exception
  {
    testMarshal("tests", "efa", "batch_tank_vout");
  }

  public void testMarshal_eventaliases()
    throws Exception
  {
    testMarshal("tests", "nasty", "eventaliases");
  }

  public void testMarshal_marked_value()
    throws Exception
  {
    testMarshal("tests", "nasty", "marked_value");
  }


  //#########################################################################
  //# Cloning Test Cases
  public void testClone_hisc0_high()
    throws Exception
  {
    testClone("despot", "testHISC", "hisc0_high");
  }

  public void testClone_buffer_norm()
    throws Exception
  {
    testClone("efa", "buffer_norm");
  }

  public void testClone_transferline_efa()
    throws Exception
  {
    testClone("efa", "transferline_efa");
  }

  public void testClone_buffer_sf1()
    throws Exception
  {
    testClone("handwritten", "buffer_sf1");
  }

  public void testClone_cell()
    throws Exception
  {
    testClone("handwritten", "cell");
  }

  public void testClone_machine()
    throws Exception
  {
    testClone("handwritten", "machine");
  }

  public void testClone_sensoractuator1()
    throws Exception
  {
    testClone("handwritten", "sensoractuator1");
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

  public void testClone_tictactoe_incomplete()
    throws Exception
  {
    testClone("handwritten", "tictactoe_incomplete");
  }

  public void testClone_error7_small()
    throws Exception
  {
    testClone("tests", "compiler", "efsm", "error7_small");
  }

  public void testClone_colours()
    throws Exception
  {
    testClone("tests", "compiler", "graph", "colours");
  }

  public void testClone_nodegroup1()
    throws Exception
  {
    testClone("tests", "compiler", "groupnode", "nodegroup1");
  }

  public void testClone_nodegroup2()
    throws Exception
  {
    testClone("tests", "compiler", "groupnode", "nodegroup2");
  }

  public void testClone_array2d()
    throws Exception
  {
    testClone("tests", "compiler", "instance", "array2d");
  }

  public void testClone_batch_tank_vout()
    throws Exception
  {
    testClone("tests", "efa", "batch_tank_vout");
  }

  public void testClone_eventaliases()
    throws Exception
  {
    testClone("tests", "nasty", "eventaliases");
  }

  public void testClone_marked_value()
    throws Exception
  {
    testClone("tests", "nasty", "marked_value");
  }


  public void testCrossClone_hisc0_high()
    throws Exception
  {
    testCrossClone("despot", "testHISC", "hisc0_high");
  }

  public void testCrossClone_buffer_norm()
    throws Exception
  {
    testCrossClone("efa", "buffer_norm");
  }

  public void testCrossClone_transferline_efa()
    throws Exception
  {
    testCrossClone("efa", "transferline_efa");
  }

  public void testCrossClone_buffer_sf1()
    throws Exception
  {
    testCrossClone("handwritten", "buffer_sf1");
  }

  public void testCrossClone_cell()
    throws Exception
  {
    testCrossClone("handwritten", "cell");
  }

  public void testCrossClone_machine()
    throws Exception
  {
    testCrossClone("handwritten", "machine");
  }

  public void testCrossClone_sensoractuator1()
    throws Exception
  {
    testCrossClone("handwritten", "sensoractuator1");
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

  public void testCrossClone_tictactoe()
    throws Exception
  {
    testCrossClone("handwritten", "tictactoe");
  }

  public void testCrossClone_tictactoe_incomplete()
    throws Exception
  {
    testCrossClone("handwritten", "tictactoe_incomplete");
  }

  public void testCrossClone_error7_small()
    throws Exception
  {
    testCrossClone("tests", "compiler", "efsm", "error7_small");
  }

  public void testCrossClone_colours()
    throws Exception
  {
    testCrossClone("tests", "compiler", "graph", "colours");
  }

  public void testCrossClone_nodegroup1()
    throws Exception
  {
    testCrossClone("tests", "compiler", "groupnode", "nodegroup1");
  }

  public void testCrossClone_nodegroup2()
    throws Exception
  {
    testCrossClone("tests", "compiler", "groupnode", "nodegroup2");
  }

  public void testCrossClone_array2d()
    throws Exception
  {
    testCrossClone("tests", "compiler", "instance", "array2d");
  }

  public void testCrossClone_batch_tank_vout()
    throws Exception
  {
    testCrossClone("tests", "efa", "batch_tank_vout");
  }

  public void testCrossClone_eventaliases()
    throws Exception
  {
    testCrossClone("tests", "nasty", "eventaliases");
  }

  public void testCrossClone_marked_value()
    throws Exception
  {
    testCrossClone("tests", "nasty", "marked_value");
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.marshaller.AbstractXMLTest
  @Override
  protected ProxyMarshaller<ModuleProxy> getProxyMarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyUnmarshaller<ModuleProxy> getProxyUnmarshaller()
  {
    return mMarshaller;
  }

  @Override
  protected ProxyPrinter getPrinter()
  {
    return mPrinter;
  }

  @Override
  protected ModuleIntegrityChecker getIntegrityChecker()
  {
    return ModuleIntegrityChecker.getInstance();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mMarshaller = new SAXModuleMarshaller(factory, optable);
    final ModuleProxyFactory altFactory = getAlternateModuleProxyFactory();
    mAltMarshaller = new SAXModuleMarshaller(altFactory, optable);
    final PrintWriter writer = new PrintWriter(System.out);
    mPrinter = new ModuleProxyPrinter(writer);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mMarshaller = mAltMarshaller = null;
    mPrinter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getModuleProxyFactory();
  protected abstract ModuleProxyFactory getAlternateModuleProxyFactory();


  //#########################################################################
  //# Auxiliary Methods
  private void testCrossClone(final String... path)
    throws Exception
  {
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final ModuleProxyCloner cloner = factory.getCloner();
    testCrossClone(mAltMarshaller, cloner, path);
  }


  //#########################################################################
  //# Data Members
  private SAXModuleMarshaller mMarshaller;
  private SAXModuleMarshaller mAltMarshaller;
  private ProxyPrinter mPrinter;

}
