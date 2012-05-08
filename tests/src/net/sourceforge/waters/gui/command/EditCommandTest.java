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
  //# Constructors
  public EditCommandTest()
  {
  }

  public EditCommandTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
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

  public void testNodeRename()
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
