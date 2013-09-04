//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   EFSMSynchronizerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.efa.efsm.EFSMCompiler;
import net.sourceforge.waters.analysis.efa.efsm.EFSMSynchronizer;
import net.sourceforge.waters.analysis.efa.efsm.EFSMSystem;
import net.sourceforge.waters.analysis.efa.efsm.EFSMSystemImporter;
import net.sourceforge.waters.analysis.efa.efsm.EFSMTransitionRelation;
import net.sourceforge.waters.analysis.efa.efsm.EFSMVariableContext;
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

  /**
   *
   */
  public EFSMSynchronizerTest()
  {
    // TODO Auto-generated constructor stub
  }



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
