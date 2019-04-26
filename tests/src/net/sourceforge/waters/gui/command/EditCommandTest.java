//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.command;

import java.awt.geom.Point2D;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.GeometryTools;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.xsd.module.SplineKind;

import org.supremica.gui.ide.ModuleContainer;

import junit.framework.Test;
import junit.framework.TestSuite;


public class EditCommandTest extends AbstractCommandTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(EditCommandTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testEdgeBend()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "machine_break";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "machine");
    final EdgeSubject edge = findEdge(panel, "working", "down");
    final EdgeSubject target = ProxyTools.clone(edge);
    final Point2D point = GeometryTools.getDefaultMidPoint(target);
    point.setLocation(point.getX(), point.getY() + 20.0);
    GeometryTools.setSpecifiedMidPoint(target, point, SplineKind.INTERPOLATING);
    final Command cmd = new EditCommand(edge, target, panel);
    executeCommand(cmd, container, edge, target);
  }

  public void testEdgeLabelReplacement()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "manwolfgoatcabbage";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "Man");
    final EdgeSubject edge = findEdge(panel, "ml", "mr");
    final EdgeSubject target = ProxyTools.clone(edge);
    final LabelBlockSubject block = target.getLabelBlock();
    final ListSubject<AbstractSubject> list =
      block.getEventIdentifierListModifiable();
    final ModuleSubjectFactory factory = getSubjectFactory();
    final IdentifierSubject ident =
      factory.createSimpleIdentifierProxy("change");
    list.set(1, ident);
    final Command cmd = new EditCommand(edge, target, panel);
    executeCommand(cmd, container, edge, target);
  }

  public void testEdgeReshape()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "machine_break";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "machine");
    final EdgeSubject edge = findEdge(panel, "idle", "working");
    final EdgeSubject target = ProxyTools.clone(edge);
    final Point2D point = GeometryTools.getSinglePoint(target);
    point.setLocation(point.getX(), point.getY() - 20.0);
    GeometryTools.setSpecifiedMidPoint(target, point, SplineKind.INTERPOLATING);
    final Command cmd = new EditCommand(edge, target, panel);
    executeCommand(cmd, container, edge, target);
  }

  public void testEdgeStraighten()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "machine_break";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "machine");
    final EdgeSubject edge = findEdge(panel, "idle", "working");
    final EdgeSubject target = ProxyTools.clone(edge);
    target.setGeometry(null);
    final Command cmd = new EditCommand(edge, target, panel);
    executeCommand(cmd, container, edge, target);
  }

  public void testEventAliasMakeComplex()
    throws Exception
  {
    final String dir = "tests";
    final String subdir = "nasty";
    final String name = "eventaliases";
    final ModuleContainer container = loadModule(dir, subdir, name);
    final EventAliasSubject simple = findEventAlias(container, "simple");
    final EventAliasSubject complex = findEventAlias(container, "complex");
    final ExpressionSubject expr0 = complex.getExpression();
    final ExpressionSubject expr1 = ProxyTools.clone(expr0);
    final EventAliasSubject target = ProxyTools.clone(simple);
    target.setExpression(expr1);
    final SelectionOwner panel =
      container.getEditorPanel().getEventAliasesPanel();
    final Command cmd = new EditCommand(simple, target, panel);
    executeCommand(cmd, container, simple, target);
  }

  public void testEventAliasMakeSimple()
    throws Exception
  {
    final String dir = "tests";
    final String subdir = "nasty";
    final String name = "eventaliases";
    final ModuleContainer container = loadModule(dir, subdir, name);
    final EventAliasSubject complex = findEventAlias(container, "complex");
    final EventAliasSubject simple = findEventAlias(container, "simple");
    final ExpressionSubject expr0 = simple.getExpression();
    final ExpressionSubject expr1 = ProxyTools.clone(expr0);
    final EventAliasSubject target = ProxyTools.clone(complex);
    target.setExpression(expr1);
    final SelectionOwner panel =
      container.getEditorPanel().getEventAliasesPanel();
    final Command cmd = new EditCommand(complex, target, panel);
    executeCommand(cmd, container, complex, target);
  }

  public void testGroupNodeRename()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "winemerchant";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "problem");
    final GroupNodeSubject node = findGroupNode(panel, "NodeGroup7");
    final GroupNodeSubject target = ProxyTools.clone(node);
    target.setName("change");
    final Command cmd = new EditCommand(node, target, panel);
    executeCommand(cmd, container, node, target);
  }

  public void testSimpleComponentRename()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "small_factory_2";
    final ModuleContainer container = loadModule(dir, name);
    final SimpleComponentSubject comp =
      findSimpleComponent(container, "machine1");
    final SimpleComponentSubject target = ProxyTools.clone(comp);
    final ModuleSubjectFactory factory = getSubjectFactory();
    final SimpleIdentifierSubject ident =
      factory.createSimpleIdentifierProxy("change");
    target.setIdentifier(ident);
    final SelectionOwner panel = container.getEditorPanel().getComponentsPanel();
    final Command cmd = new EditCommand(comp, target, panel);
    executeCommand(cmd, container, comp, target);
  }

  public void testSimpleNodeRename()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "small_factory_2";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "machine1");
    final SimpleNodeSubject node = findSimpleNode(panel, "idle");
    final SimpleNodeSubject target = ProxyTools.clone(node);
    target.setName("change");
    final Command cmd = new EditCommand(node, target, panel);
    executeCommand(cmd, container, node, target);
  }

}
