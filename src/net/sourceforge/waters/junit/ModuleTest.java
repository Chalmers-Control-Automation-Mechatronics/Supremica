//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.junit
//# CLASS:   ModuleTest
//###########################################################################
//# $Id: ModuleTest.java,v 1.2 2005-02-18 01:32:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.bind.JAXBException;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.expr.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

public class ModuleTest
  extends JAXBTestCase
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(ModuleTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Marshalling Test Cases
  public void testParse_buffer_sf1()
    throws JAXBException, ModelException, IOException
  {
    testParse("buffer_sf1");
  }

  public void testParse_colours()
    throws JAXBException, ModelException, IOException
  {
    testParse("colours");
  }

  public void testParse_machine()
    throws JAXBException, ModelException, IOException
  {
    testParse("machine");
  }

  public void testParse_small_factory_2()
    throws JAXBException, ModelException, IOException
  {
    testParse("small_factory_2");
  }

  public void testParse_small_factory_n()
    throws JAXBException, ModelException, IOException
  {
    testParse("small_factory_n");
  }

  public void testParse_tictactoe()
    throws JAXBException, ModelException, IOException
  {
    testParse("tictactoe");
  }


  public void testMarshal_buffer_sf1()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("buffer_sf1");
  }

  public void testMarshal_colours()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("colours");
  }

  public void testMarshal_machine()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("machine");
  }

  public void testMarshal_small_factory_2()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("small_factory_2");
  }

  public void testMarshal_small_factory_n()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("small_factory_n");
  }

  public void testMarshal_tictactoe()
    throws JAXBException, ModelException, IOException
  {
    testMarshal("tictactoe");
  }


  //#########################################################################
  //# Handcrafting Test Cases
  public void testHandcraft_nodegroup1()
    throws JAXBException, ModelException, IOException
  {
    final ModuleProxy handcrafted = handcraft_nodegroup1();
    testHandcraft(handcrafted);
  }

  public void testHandcraft_nodegroup2()
    throws JAXBException, ModelException, IOException
  {
    final ModuleProxy handcrafted = handcraft_nodegroup2();
    testHandcraft(handcrafted);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class JAXBTestCase
  ProxyMarshaller getProxyMarshaller()
  {
    return mMarshaller;
  }

  File getInputDirectory()
  {
    return mInputDirectory;
  }

  File getOutputDirectory()
  {
    return mOutputDirectory;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  { 
    super.setUp();
    mMarshaller = new ModuleMarshaller();
    mInputDirectory = new File(getInputRoot(), "handwritten");
    mOutputDirectory = new File(getOutputRoot(), "module");
  }

  protected void tearDown()
  {
    mMarshaller = null;
    mInputDirectory = null;
    mOutputDirectory = null;
    super.tearDown();
  }


  //#########################################################################
  //# Handcrafting Modules
  private ModuleProxy handcraft_nodegroup1()
    throws ModelException
  {
    final String name  ="nodegroup1";
    final ModuleProxy module = new ModuleProxy(name, null);
    final Collection events = module.getEventDeclList();
    final Collection comps = module.getComponentList();
    final SimpleIdentifierProxy e = new SimpleIdentifierProxy("e");
    final EventDeclProxy event =
      new EventDeclProxy("e", EventKind.CONTROLLABLE);
    events.add(event);
    final SimpleIdentifierProxy nodegroup1 = new SimpleIdentifierProxy(name);
    final SimpleComponentProxy comp =
      new SimpleComponentProxy(nodegroup1, ComponentKind.SPEC);
    comps.add(comp);
    final GraphProxy graph = comp.getGraph();
    final Collection blocked = graph.getBlockedEvents();
    final Collection nodes = graph.getNodes();
    final Collection edges = graph.getEdges();
    blocked.add(e);
    final SimpleNodeProxy q0 = new SimpleNodeProxy("q0", true);
    nodes.add(q0);
    final SimpleNodeProxy q1 = new SimpleNodeProxy("q1");
    nodes.add(q1);
    final SimpleNodeProxy q2 = new SimpleNodeProxy("q2");
    nodes.add(q2);
    final GroupNodeProxy group = new GroupNodeProxy(":group", nodes);
    nodes.add(group);
    final EdgeProxy edge1 = new EdgeProxy(group, q1);
    edges.add(edge1);
    final Collection labels1 = edge1.getLabelBlock();
    labels1.add(e);
    final EdgeProxy edge2 = new EdgeProxy(q2, q2);
    edges.add(edge2);
    final Collection labels2 = edge2.getLabelBlock();
    labels2.add(e);
    return module;
  }

  private ModuleProxy handcraft_nodegroup2()
    throws ModelException
  {
    final String name  ="nodegroup2";
    final ModuleProxy module = new ModuleProxy(name, null);
    final Collection events = module.getEventDeclList();
    final Collection comps = module.getComponentList();
    final SimpleIdentifierProxy e = new SimpleIdentifierProxy("e");
    final EventDeclProxy event =
      new EventDeclProxy("e", EventKind.CONTROLLABLE);
    events.add(event);
    final SimpleIdentifierProxy nodegroup2 = new SimpleIdentifierProxy(name);
    final SimpleComponentProxy comp =
      new SimpleComponentProxy(nodegroup2, ComponentKind.SPEC);
    comps.add(comp);
    final GraphProxy graph = comp.getGraph();
    final Collection blocked = graph.getBlockedEvents();
    final Collection nodes = graph.getNodes();
    final Collection edges = graph.getEdges();
    blocked.add(e);
    final SimpleNodeProxy q0 = new SimpleNodeProxy("q0", true);
    nodes.add(q0);
    final SimpleNodeProxy q1 = new SimpleNodeProxy("q1");
    nodes.add(q1);
    final SimpleNodeProxy q2 = new SimpleNodeProxy("q2");
    nodes.add(q2);
    final Collection children = new LinkedList();
    children.add(q1);
    children.add(q0);
    final GroupNodeProxy group1 = new GroupNodeProxy(":group1", children);
    nodes.add(group1);
    children.clear();
    children.add(q2);
    children.add(q0);
    final GroupNodeProxy group2 = new GroupNodeProxy(":group2", children);
    nodes.add(group2);
    final EdgeProxy edge1 = new EdgeProxy(group1, q0);
    edges.add(edge1);
    final Collection labels1 = edge1.getLabelBlock();
    labels1.add(e);
    final EdgeProxy edge2 = new EdgeProxy(group2, q0);
    edges.add(edge2);
    final Collection labels2 = edge2.getLabelBlock();
    labels2.add(e);
    return module;
  }


  //#########################################################################
  //# Data Members
  private ProxyMarshaller mMarshaller;
  private File mInputDirectory;
  private File mOutputDirectory;

}
