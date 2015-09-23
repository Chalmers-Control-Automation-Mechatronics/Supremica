//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
      createGraph(testlist);
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
    mChecker = ModuleHierarchyChecker.getInstance();
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
