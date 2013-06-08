//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   AbstractTransitionRelationSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.EvalException;
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

public class PartialUnfolderTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public PartialUnfolderTest()
  {
  }

  public PartialUnfolderTest(final String name)
  {
    super(name);
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
    mPartialUnfolder = new PartialUnfolder(mFactory, optable);
    mImporter = new EFSMSystemImporter(mFactory, optable);
    mPartialUnfolder.setSourceInfoEnabled(true);
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
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * empty_1.wmod.
   * </P>
   *
   * <P>
   * All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton for the abstraction rule. The automaton "after" defines the
   * expected result of abstraction. It may be missing, in which case the
   * abstraction should have no effect and return the unchanged input automaton
   * (the test expects the same object, not an identical copy).
   * </P>
   *
   * <P>
   * The names of critical events are expected to be "tau", ":alpha", and
   * ":accepting", respectively.
   * </P>
   *
   * <P>
   * After running the test, any automaton created by the rule is saved in
   * {supremica}/logs/results/analysis/op/{classname} as a .des file
   * (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void testUnfolding_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding01.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding02.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding03.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding04.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding05.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding06.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding07.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding08.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding09.wmod";
    runPartialUnfolder(group, subdir, name);
  }

  public void testUnfolding_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "efsm";
    final String name = "unfolding10.wmod";
    runPartialUnfolder(group, subdir, name);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runPartialUnfolder
    (final String group, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runPartialUnfolder(groupdir, name, bindings);
  }

  protected void runPartialUnfolder
    (final String group, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runPartialUnfolder(groupdir, subdir, name, bindings);
  }

  protected void runPartialUnfolder
    (final File groupdir, final String subdir,
     final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runPartialUnfolder(dir, name, bindings);
  }

  protected void runPartialUnfolder
    (final File dir, final String name,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final File filename = new File(dir, name);
    runPartialUnfolder(filename, bindings);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runPartialUnfolder(final String group,
                                    final String name)
  throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runPartialUnfolder(groupdir, name);
  }

  protected void runPartialUnfolder(final String group,
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
    runPartialUnfolder(dir, name);
  }

  protected void runPartialUnfolder(final File dir,
                                    final String name)
  throws Exception
  {
    final File filename = new File(dir, name);
    runPartialUnfolder(filename);
  }

  protected void runPartialUnfolder(final File filename)
  throws Exception
  {
    final List<ParameterBindingProxy> empty = null;
    runPartialUnfolder(filename, empty);
  }

  protected void runPartialUnfolder
    (final File filename,
     final List<ParameterBindingProxy> bindings)
  throws Exception
  {
    final ModuleProxy module = (ModuleProxy) mDocumentManager.load(filename);
    runPartialUnfolder(module, bindings);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runPartialUnfolder(final ModuleProxy module,
                                  final List<ParameterBindingProxy> bindings)
    throws Exception
  {
    getLogger().info("Checking " + module.getName() + " ...");
    final EFSMSystem system = createEFSMSystem(module, bindings);
    final EFSMTransitionRelation efsmTransitionRelation =
      findTR(system, BEFORE);
    final EFSMVariable unfoldedVariable = system.getVariables().get(0);
    final EFSMTransitionRelation resultTransitionRelation =
      mPartialUnfolder.unfold(efsmTransitionRelation, unfoldedVariable,
                              system);
    final List<EFSMTransitionRelation> list =
      Collections.singletonList(resultTransitionRelation);
    resultTransitionRelation.setName(RESULT);
    final EFSMVariableContext context = system.getVariableContext();
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

  private ModuleProxy createModule(final ModuleProxy module,
                                   final String componentName,
                                   final boolean required)
  {
    final List<? extends Proxy> oldComponentList = module.getComponentList();
    final List<Proxy> newComponentList =
      new ArrayList<Proxy>(oldComponentList.size());
    boolean found = false;
    for (final Proxy proxy : oldComponentList) {
      if (proxy instanceof SimpleComponentProxy) {
        final SimpleComponentProxy comp = (SimpleComponentProxy) proxy;
        if (comp.getName().equals(componentName)) {
          if (found) {
            fail("Module '" + module.getName() +
                 "' contains more than one simple component called '" +
                 componentName + "'!");
          } else {
            newComponentList.add(comp);
            found = true;
          }
        }
      } else {
        newComponentList.add(proxy);
      }
    }
    if (found) {
      return mFactory.createModuleProxy(module.getName(),
                                        module.getComment(),
                                        module.getLocation(),
                                        module.getConstantAliasList(),
                                        module.getEventDeclList(),
                                        module.getEventAliasList(),
                                        newComponentList);
    } else if (required) {
      fail("The module '" + module.getName() +
           "' does not contain any simple component called '" +
           componentName + "'!");
      return null;
    } else {
      return null;
    }
  }

  private EFSMSystem createEFSMSystem(final ModuleProxy module,
                                      final List<ParameterBindingProxy> bindings)
    throws EvalException
  {
    final ModuleProxy before = createModule(module, BEFORE, true);
    final EFSMCompiler compiler1 = new EFSMCompiler(mDocumentManager, before);
    compiler1.setSourceInfoEnabled(true);
    final EFSMSystem system = compiler1.compile(bindings);
    final ModuleProxy selfloops = createModule(module, SELFLOOPS, false);
    if (selfloops != null) {
      final EFSMVariable unfoldedVariable = system.getVariables().get(0);
      final EFSMCompiler compiler2 =
        new EFSMCompiler(mDocumentManager, selfloops);
      final EFSMSystem selfloopSystem = compiler2.compile(bindings);
      final EFSMTransitionRelation selfloopTR = findTR(selfloopSystem, SELFLOOPS);
      final EFSMEventEncoding selfloopEnc = selfloopTR.getEventEncoding();
      for (int e = EventEncoding.NONTAU; e < selfloopEnc.size(); e++) {
        final ConstraintList update = selfloopEnc.getUpdate(e);
        unfoldedVariable.addSelfloop(update);
      }
    }
    return system;
  }

  private SimpleComponentProxy findComponent(final ModuleProxy module,
                                             final String name)
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

  private EFSMTransitionRelation findTR(final EFSMSystem system,
                                        final String name)
  {
    for (final EFSMTransitionRelation tr : system.getTransitionRelations()) {
      if (tr.getName().equals(name)) {
        return tr;
      }
    }
    fail("The EFSM system '" + system.getName() +
         "' does not contain any transition relation called '" + name + "'!");
    return null;
  }

  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }


  //#########################################################################
  //# Data Members
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private ModuleProxyFactory mFactory;
  private PartialUnfolder mPartialUnfolder;
  private EFSMSystemImporter mImporter;


  //#########################################################################
  //# Class Constants
  private static final String BEFORE = "before";
  private static final String AFTER = "after";
  private static final String SELFLOOPS = "selfloops";
  private static final String RESULT = "result";

}
