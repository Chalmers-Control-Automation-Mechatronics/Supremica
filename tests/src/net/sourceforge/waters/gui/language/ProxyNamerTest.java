//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.gui.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ProxyNamerTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(ProxyNamerTest.class);
  }

  public static void main(final String args[])
  {
    junit.textui.TestRunner.run(suite());
  }


  public ProxyNamerTest()
  {
  }

  public ProxyNamerTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mFactory = ModuleElementFactory.getInstance();
  }


  //#########################################################################
  //# Test Cases
  public void testAutomatonWithEvent() throws Exception
  {
    final IdentifierProxy aut = mFactory.createSimpleIdentifierProxy("aut");
    final IdentifierProxy e1 = mFactory.createSimpleIdentifierProxy("e1");
    final GraphProxy graph = mFactory.createGraphProxy();
    testName("Automaton",
             mFactory.createEventDeclProxy(e1, EventKind.CONTROLLABLE),
             mFactory.createSimpleComponentProxy(aut, ComponentKind.PLANT, graph));
  }

  public void testComponents() throws Exception
  {
    final IdentifierProxy aut = mFactory.createSimpleIdentifierProxy("aut");
    final IdentifierProxy inst = mFactory.createSimpleIdentifierProxy("inst");
    final IdentifierProxy var = mFactory.createSimpleIdentifierProxy("var");
    final GraphProxy graph = mFactory.createGraphProxy();
    final SimpleExpressionProxy type =
      mFactory.createSimpleIdentifierProxy("range");
    final SimpleExpressionProxy init = mFactory.createIntConstantProxy(1);
    testName("Components",
             mFactory.createSimpleComponentProxy(aut, ComponentKind.PLANT, graph),
             mFactory.createInstanceProxy(inst, "module"),
             mFactory.createVariableComponentProxy(var, type, init));
  }

  public void testEventDecl() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("event");
    testName("Event",
             mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE));
  }

  public void testEventDecls() throws Exception
  {
    final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("e1");
    final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("e2");
    testName("Events",
             mFactory.createEventDeclProxy(ident1, EventKind.CONTROLLABLE),
             mFactory.createEventDeclProxy(ident2, EventKind.CONTROLLABLE));
  }

  public void testEventDeclWithIdent() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("e1");
    testName("Event",
             mFactory.createEventDeclProxy(ident, EventKind.CONTROLLABLE),
             ident);
  }

  public void testForeachEmpty() throws Exception
  {
    final SimpleExpressionProxy range =
      mFactory.createSimpleIdentifierProxy("range");
    final ForeachProxy foreach = mFactory.createForeachProxy("i", range);
    testName("Foreach Block", foreach);
  }

  public void testForeachNested() throws Exception
  {
    final SimpleExpressionProxy range =
      mFactory.createSimpleIdentifierProxy("range");
    final ForeachProxy inner = mFactory.createForeachProxy("j", range);
    final List<ForeachProxy> list = Collections.singletonList(inner);
    final ForeachProxy outer = mFactory.createForeachProxy(list, "j", range);
    testName("Foreach Blocks", outer);
  }

  public void testForeachAutomaton() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("aut");
    final GraphProxy graph = mFactory.createGraphProxy();
    final SimpleComponentProxy comp =
      mFactory.createSimpleComponentProxy(ident, ComponentKind.SPEC, graph);
    final List<SimpleComponentProxy> list = Collections.singletonList(comp);
    final SimpleExpressionProxy range =
      mFactory.createSimpleIdentifierProxy("range");
    final ForeachProxy foreach = mFactory.createForeachProxy(list, "i", range);
    testName("Automata", foreach);
  }

  public void testForeachIdentifier() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("x");
    final List<IdentifierProxy> list = Collections.singletonList(ident);
    final SimpleExpressionProxy range =
      mFactory.createSimpleIdentifierProxy("range");
    final ForeachProxy foreach = mFactory.createForeachProxy(list, "i", range);
    testName("Labels", foreach);
  }

  public void testIdentifier() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("e1");
    testName("Label", ident);
  }

  public void testIdentifiers() throws Exception
  {
    final IdentifierProxy ident1 = mFactory.createSimpleIdentifierProxy("e1");
    final IdentifierProxy ident2 = mFactory.createSimpleIdentifierProxy("e1");
    testName("Labels", ident1, ident2);
  }

  public void testIdentifierWithGABlock() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("e1");
    final GuardActionBlockProxy block = mFactory.createGuardActionBlockProxy();
    testName("Labels", ident, block);
  }

  public void testIdentifierWithLabelBlock() throws Exception
  {
    final IdentifierProxy ident = mFactory.createSimpleIdentifierProxy("e1");
    final LabelBlockProxy block = mFactory.createLabelBlockProxy();
    testName("Labels", ident, block);
  }

  public void testSimpleAndGroupNode() throws Exception
  {
    final SimpleNodeProxy simple = mFactory.createSimpleNodeProxy("s0");
    final GroupNodeProxy group = mFactory.createGroupNodeProxy("g0");
    testName("States", simple, group);
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void testName(final String expected, final Proxy... proxies)
    throws Exception
  {
    final Collection<? extends Proxy> collection = Arrays.asList(proxies);
    final String name = ProxyNamer.getCollectionClassName(collection);
    assertEquals("Unexpected result from ProxyNamer!", expected, name);
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;

}
