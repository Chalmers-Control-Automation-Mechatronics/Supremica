//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditCommandTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

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
  public void testEdgeLabelReplacement()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "manwolfgoatcabbage";
    final ModuleContainer container = loadModule(dir, name);
    final GraphEditorPanel panel = openGraph(container, "Man");
    final EdgeSubject edge = findEdge(panel, "ml", "mr");
    final ModuleProxyCloner cloner = getSubjectCloner();
    final EdgeSubject target = (EdgeSubject) cloner.getClone(edge);
    final LabelBlockSubject block = target.getLabelBlock();
    final ListSubject<AbstractSubject> list = block.getEventIdentifierListModifiable();
    final ModuleSubjectFactory factory = getSubjectFactory();
    final IdentifierSubject ident =
      factory.createSimpleIdentifierProxy("change");
    list.set(1, ident);
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
    final ModuleProxyCloner cloner = getSubjectCloner();
    final EventAliasSubject complex = findEventAlias(container, "complex");
    final ExpressionSubject expr0 = complex.getExpression();
    final ExpressionSubject expr1 = (ExpressionSubject) cloner.getClone(expr0);
    final EventAliasSubject target =
      (EventAliasSubject) cloner.getClone(simple);
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
    final ModuleProxyCloner cloner = getSubjectCloner();
    final EventAliasSubject simple = findEventAlias(container, "simple");
    final ExpressionSubject expr0 = simple.getExpression();
    final ExpressionSubject expr1 = (ExpressionSubject) cloner.getClone(expr0);
    final EventAliasSubject target =
      (EventAliasSubject) cloner.getClone(complex);
    target.setExpression(expr1);
    final SelectionOwner panel =
      container.getEditorPanel().getEventAliasesPanel();
    final Command cmd = new EditCommand(complex, target, panel);
    executeCommand(cmd, container, complex, target);
  }

  public void testSimpleComponentRename()
    throws Exception
  {
    final String dir = "handwritten";
    final String name = "small_factory_2";
    final ModuleContainer container = loadModule(dir, name);
    final SimpleComponentSubject comp =
      findSimpleComponent(container, "machine1");
    final ModuleProxyCloner cloner = getSubjectCloner();
    final SimpleComponentSubject target =
      (SimpleComponentSubject) cloner.getClone(comp);
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
    final ModuleProxyCloner cloner = getSubjectCloner();
    final SimpleNodeSubject target = (SimpleNodeSubject) cloner.getClone(node);
    target.setName("change");
    final Command cmd = new EditCommand(node, target, panel);
    executeCommand(cmd, container, node, target);
  }

}