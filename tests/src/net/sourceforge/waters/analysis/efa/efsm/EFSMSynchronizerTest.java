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

package net.sourceforge.waters.analysis.efa.efsm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class EFSMSynchronizerTest extends AbstractWatersTest
{

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mFactory = ModuleElementFactory.getInstance();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller =
      new JAXBModuleMarshaller(mFactory, optable, false);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mEFSMSynchronization = new EFSMSynchronizer(mFactory);
    mImporter = new EFSMSystemImporter(mFactory, optable);
    mEFSMSynchronization.setSourceInfoEnabled(true);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mModuleMarshaller = null;
    mDocumentManager = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  public void testSynch_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch1.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch2.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch3.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch4.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch5.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch6.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testSynch_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "synch7.wmod";
    runSynchronizer(group, subdir, name);
  }

  public void testReentrant() throws Exception
  {
    testSynch_1();
    testSynch_2();
    testSynch_3();
    testSynch_4();
    testSynch_5();
    testSynch_6();
    testSynch_7();
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runSynchronizer
    (final String group, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynchronizer(groupdir, name, bindings);
  }

  protected void runSynchronizer
    (final String group, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynchronizer(groupdir, subdir, name, bindings);
  }

  protected void runSynchronizer
    (final File groupdir, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runSynchronizer(dir, name, bindings);
  }

  protected void runSynchronizer
    (final File dir, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File filename = new File(dir, name);
    runSynchronizer(filename, bindings);
  }

  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runSynchronizer(final String group, final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynchronizer(groupdir, name);
  }

  protected void runSynchronizer(final String group,
                                                 final String subdir,
                                                 final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runPartialUnfolder(groupdir, subdir, name);
  }

  protected void runPartialUnfolder(final File groupdir,
                                                 final String subdir,
                                                 final String name)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runSynchronizer(dir, name);
  }

  protected void runSynchronizer(final File dir,
                                                 final String name)
  throws Exception
  {
    final File filename = new File(dir, name);
    runSynchronizer(filename);
  }

  protected void runSynchronizer(final File filename)
  throws Exception
  {
    final List<ParameterBindingProxy> empty = null;
    runSynchronizer(filename, empty);
  }

  protected void runSynchronizer
    (final File filename,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final ModuleProxy module = (ModuleProxy) mDocumentManager.load(filename);
    runSynchronizer(module, bindings);
  }



  private void runSynchronizer(final ModuleProxy module,
                               final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    final ModuleProxy before = createModule(module, BEFORE1,BEFORE2);
    final EFSMCompiler compiler = new EFSMCompiler(mDocumentManager, before);
    compiler.setSourceInfoEnabled(true);
    compiler.setOptimizationEnabled(false);
    final EFSMSystem system = compiler.compile(bindings);
    final List<EFSMTransitionRelation> efsmTransitionRelationList =
      system.getTransitionRelations();
    final EFSMTransitionRelation efsmTR1 = efsmTransitionRelationList.get(0);
    final EFSMTransitionRelation efsmTR2 = efsmTransitionRelationList.get(1);
    final EFSMVariableContext context = system.getVariableContext();
    final EFSMTransitionRelation resultTransitionRelation =
      mEFSMSynchronization.synchronize(efsmTR1, efsmTR2);
    final List<EFSMTransitionRelation> list =
      Collections.singletonList(resultTransitionRelation);
    resultTransitionRelation.setName(RESULT);
    final EFSMSystem resultSystem =
      new EFSMSystem(module.getName(), system.getVariables(), list, context);
    final ModuleProxy resultModuleProxy = mImporter.importModule(resultSystem);
    final File outputDirectory = getOutputDirectory();
    final String ext = mModuleMarshaller.getDefaultExtension();
    final File outputFile = new File(outputDirectory, module.getName() + ext);
    mModuleMarshaller.marshal(resultModuleProxy, outputFile);
    resultTransitionRelation.setName(AFTER);
    final EFSMSystem afterSystem =
      new EFSMSystem(module.getName(), system.getVariables(), list, context);
    final ModuleProxy afterModuleProxy = mImporter.importModule(afterSystem);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, false);
    final SimpleComponentProxy result = findComponent(afterModuleProxy, AFTER);
    final SimpleComponentProxy expected = findComponent(module, AFTER);
    assertProxyEquals(eq, "Unexpected result", result, expected);
    getLogger().info("Done " + module.getName());
  }

  private SimpleComponentProxy findComponent(final ModuleProxy module, final String name)
  {
    for (final Proxy proxy : module.getComponentList()) {
      if(proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(name)) {
          return comp;
        }
      }
    }
    fail("The module '" + module.getName() + "' does not contain any simple " +
         "component called '" + name + "'!");
    return null;
  }

  private ModuleProxy createModule(final ModuleProxy module,
                                   final String componentName1,
                                   final String componentName2)
  {
    final List<? extends Proxy> oldComponentList = module.getComponentList();
    final List<Proxy> newComponentList = new ArrayList<Proxy>(oldComponentList.size());
    for (final Proxy proxy : oldComponentList) {
      if(proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(componentName1)||comp.getName().equals(componentName2)) {
          newComponentList.add(comp);
        }
      } else {
        newComponentList.add(proxy);
      }
    }
    return mFactory.createModuleProxy(module.getName(),
                                      module.getComment(),
                                      module.getLocation(),
                                      module.getConstantAliasList(),
                                      module.getEventDeclList(),
                                      module.getEventAliasList(),
                                      newComponentList);
  }
  //#########################################################################
  //# Data Members
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private ModuleProxyFactory mFactory;
  private EFSMSystemImporter mImporter;


  //#########################################################################
  //# Class Constants
  private final String BEFORE1 = "before1";
  private final String BEFORE2 = "before2";
  private final String AFTER = "after";
  private final String RESULT = "result";

  private  EFSMSynchronizer mEFSMSynchronization;
}
