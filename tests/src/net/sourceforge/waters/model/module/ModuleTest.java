//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleTest
//###########################################################################
//# $Id: ModuleTest.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################


package net.sourceforge.waters.model.module;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.IdentityHashMap;
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
