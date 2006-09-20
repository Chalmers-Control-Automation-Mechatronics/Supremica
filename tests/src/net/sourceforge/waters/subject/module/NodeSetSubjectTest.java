//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   NodeSetSubjectTest
//###########################################################################
//# $Id: NodeSetSubjectTest.java,v 1.1 2006-09-20 16:24:13 robi Exp $
//###########################################################################


package net.sourceforge.waters.subject.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.VisitorException;


public class NodeSetSubjectTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite() {
    return new TestSuite(NodeSetSubjectTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testFlat()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mNode3, mGroup1, mGroup2};
    final List<NodeSubject> testlist = createTestList(array);
    final GraphSubject graph = createGraph(testlist);
    checkGraph(graph, testlist);
  }

  public void testOneLevelAddFirst()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mNode3, mGroup1};
    final List<NodeSubject> testlist = createTestList(array);
    final Set<NodeSubject> children1 =
      mGroup1.getImmediateChildNodesModifiable();
    children1.add(mNode1);
    children1.add(mNode2);
    final GraphSubject graph = createGraph(testlist);
    checkGraph(graph, testlist);
  }

  public void testOneLevelAddAfter()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mNode3, mGroup1};
    final List<NodeSubject> testlist = createTestList(array);
    final GraphSubject graph = createGraph(testlist);
    final Set<NodeSubject> children1 =
      mGroup1.getImmediateChildNodesModifiable();
    children1.add(mNode1);
    children1.add(mNode2);
    checkGraph(graph, testlist);
  }

  public void testTwoLevelsAddFirst()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mNode3, mGroup1, mGroup2};
    final List<NodeSubject> testlist = createTestList(array);
    final GraphSubject graph = createGraph(testlist);
    final Set<NodeSubject> children1 =
      mGroup1.getImmediateChildNodesModifiable();
    final Set<NodeSubject> children2 =
      mGroup2.getImmediateChildNodesModifiable();
    children1.add(mNode1);
    children2.add(mNode2);
    children2.add(mGroup1);
    checkGraph(graph, testlist);
  }

  public void testTwoLevelsAddAfter()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mNode3, mGroup1, mGroup2};
    final List<NodeSubject> testlist = createTestList(array);
    final Set<NodeSubject> children1 =
      mGroup1.getImmediateChildNodesModifiable();
    final Set<NodeSubject> children2 =
      mGroup2.getImmediateChildNodesModifiable();
    children1.add(mNode1);
    children2.add(mNode2);
    children2.add(mGroup1);
    final GraphSubject graph = createGraph(testlist);
    checkGraph(graph, testlist);
  }

  public void testFlatAddForeign()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2};
    final List<NodeSubject> testlist = createTestList(array);
    final GraphSubject graph = createGraph(testlist);
    final Set<NodeSubject> nodes = graph.getNodesModifiable();
    nodes.add(mNode3);
    testlist.add(mNode3);
    checkGraph(graph, testlist);
  }

  public void testOneLevelAddForeign()
    throws VisitorException
  {
    final NodeSubject[] array = {mNode1, mNode2, mGroup1};
    final List<NodeSubject> testlist = createTestList(array);
    final GraphSubject graph = createGraph(testlist);
    final Set<NodeSubject> nodes = graph.getNodesModifiable();
    nodes.add(mNode3);
    final Set<NodeSubject> children1 =
      mGroup1.getImmediateChildNodesModifiable();
    children1.add(mNode3);
    testlist.add(mNode3);
    checkGraph(graph, testlist);
  }

  public void testOneLevelAddForeignBad()
    throws VisitorException
  {
    try {
      final NodeSubject[] array = {mNode1, mNode2, mGroup1};
      final List<NodeSubject> testlist = createTestList(array);
      final GraphSubject graph = createGraph(testlist);
      final Set<NodeSubject> children1 =
        mGroup1.getImmediateChildNodesModifiable();
      children1.add(mNode3);
      fail("Expected IllegalArgumentException not caught!");
    } catch (final IllegalArgumentException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private List<NodeSubject> createTestList(final NodeSubject[] array)
  {
    final int count = array.length;
    final List<NodeSubject> testlist = new ArrayList<NodeSubject>(count);
    for (final NodeSubject node : array) {
      testlist.add(node);
    }
    return testlist;
  }

  private GraphSubject createGraph(final List<NodeSubject> testlist)
  {
    return new GraphSubject(true, null, testlist, null);
  }

  private void checkGraph(final GraphSubject graph,
                          final List<NodeSubject> testlist)
    throws VisitorException
  {
    final Set<NodeSubject> testset = new HashSet<NodeSubject>(testlist);
    final Set<NodeSubject> nodes = graph.getNodesModifiable();
    assertEquals(testset, nodes);
    mChecker.check(graph);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  { 
    super.setUp();
    mChecker = new ModuleHierarchyChecker();
    mNode1 = new SimpleNodeSubject("s1");
    mNode2 = new SimpleNodeSubject("s2");
    mNode3 = new SimpleNodeSubject("s3");
    mGroup1 = new GroupNodeSubject("G1");
    mGroup2 = new GroupNodeSubject("G2");
  }

  protected void tearDown()
    throws Exception
  {
    mChecker = null;
    mNode1 = null;
    mNode2 = null;
    mNode3 = null;
    mGroup1 = null;
    mGroup2 = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleHierarchyChecker mChecker;

  private SimpleNodeSubject mNode1;
  private SimpleNodeSubject mNode2;
  private SimpleNodeSubject mNode3;
  private GroupNodeSubject mGroup1;
  private GroupNodeSubject mGroup2;

}
