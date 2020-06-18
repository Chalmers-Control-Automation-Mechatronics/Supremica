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

package net.sourceforge.waters.analysis.efa.unified;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class UnifiedEFAVariableUnfolderTest
  extends AbstractAnalysisTest
{
  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    return new TestSuite(UnifiedEFAVariableUnfolderTest.class);
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Successful Test Cases using EFA
  public void testUnifiedUnfolding1()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding01");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding2()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding02");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding3()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding03");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding4()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding04");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding5()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding05");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding6()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding06");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding7()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding07");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding8()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding08");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding9()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding09");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding10()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding10");
    unfoldAndTest(module);
  }

  public void testUnifiedUnfolding11()
    throws IOException, WatersException
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unified_unfolding11");
    unfoldAndTest(module);
  }


  //#########################################################################
  //# Customisation
  void configure(final UnifiedEFACompiler compiler)
  {
    compiler.setSourceInfoEnabled(true);
  }


  //#########################################################################
  //# Utilities
  private void unfoldAndTest(final ModuleProxy module)
    throws IOException, WatersException
  {
    final Collection<EventDeclProxy> moduleEvents = module.getEventDeclList();
    final Collection<EventDeclProxy> inputEvents =
      new ArrayList<>(moduleEvents.size());
    for (final EventDeclProxy decl : moduleEvents) {
      final String name = decl.getName();
      if (name.indexOf(':') < 0 ||
          name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        inputEvents.add(decl);
      }
    }
    final Collection<Proxy> moduleComponents = module.getComponentList();
    final List<Proxy> inputComponents = new ArrayList<>(moduleComponents.size());
    SimpleComponentProxy expectedUnfolding = null;
    SimpleComponentProxy expectedUpdates = null;
    for (final Proxy proxy : moduleComponents) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(":unfolded")) {
          expectedUnfolding = comp;
        } else if (comp.getName().equals(":updates")) {
          expectedUpdates = comp;
        } else {
          inputComponents.add(comp);
        }
      } else {
        inputComponents.add(proxy);
      }
    }
    final String moduleName = module.getName();
    final ModuleProxy inputModule =
      mModuleFactory.createModuleProxy(moduleName, null, null,
                                       module.getConstantAliasList(),
                                       inputEvents,
                                       module.getEventAliasList(),
                                       inputComponents);
    final UnifiedEFACompiler compiler =
      new UnifiedEFACompiler(mDocumentManager, inputModule);
    configure(compiler);
    final UnifiedEFASystem inputSystem = compiler.compile();
    final UnifiedEFAVariableUnfolder unfolder =
      new UnifiedEFAVariableUnfolder(mModuleFactory, mOperatorTable,
                                     inputSystem.getVariableContext());
    final UnifiedEFAVariable unfoldedVariable =
      inputSystem.getVariables().remove(0);
    unfolder.setUnfoldedVariable(unfoldedVariable);
    final List<AbstractEFAEvent> events = inputSystem.getEvents();
    unfolder.setOriginalEvents(events);
    unfolder.run();
    final UnifiedEFATransitionRelation variableTR =
      unfolder.getTransitionRelation();
    final ListBufferTransitionRelation rel = variableTR.getTransitionRelation();
    rel.setName(":unfolded");
    final List<UnifiedEFATransitionRelation> trList =
      Collections.singletonList(variableTR);
    final List<AbstractEFAEvent> outputEvents =
      variableTR.getEventEncoding().getEventsIncludingTau();
    final UnifiedEFASystem outputSystem =
      new UnifiedEFASystem(moduleName, inputSystem.getVariables(), trList,
                           outputEvents, inputSystem.getVariableContext());
    final ModuleProxy outputModule = mImporter.importModule(outputSystem);
    final File outputDirectory = getOutputDirectory();
    final String outputName = outputModule.getName();
    final String ext = mModuleMarshaller.getDefaultExtension();
    final File outputFile = new File(outputDirectory, outputName + ext);
    mModuleMarshaller.marshal(outputModule, outputFile);
    for (final Proxy proxy : outputModule.getComponentList()) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(":unfolded")) {
          assertProxyEquals(mEqualityChecker, "Unexpected output automaton!",
                            comp, expectedUnfolding);
        } else if (comp.getName().equals(":updates")) {
          assertNotNull("Unexpected update in output!", expectedUpdates);
          assertProxyEquals(mEqualityChecker, "Unexpected unfolded events!",
                            comp, expectedUpdates);
          expectedUpdates = null;
        } else {
          fail("Unexpected simple component '" + comp.getName() + "' in output!");
        }
      }
    }
    assertNull("Missing update in output!", expectedUpdates);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mModuleFactory = ModuleElementFactory.getInstance();
    mOperatorTable = CompilerOperatorTable.getInstance();
    mModuleMarshaller =
      new SAXModuleMarshaller(mModuleFactory, mOperatorTable);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mImporter = new UnifiedEFASystemImporter(mModuleFactory, mOperatorTable);
    mEqualityChecker = new ModuleEqualityVisitor(true, false);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mModuleFactory = null;
    mOperatorTable = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
    mEqualityChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mModuleFactory;
  private CompilerOperatorTable mOperatorTable;
  private SAXModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private ModuleEqualityVisitor mEqualityChecker;

  private UnifiedEFASystemImporter mImporter;

}
