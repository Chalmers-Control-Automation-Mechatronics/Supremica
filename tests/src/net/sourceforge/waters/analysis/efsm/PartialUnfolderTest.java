//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   PartialUnfolderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * A test for the {@link PartialUnfolder}.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class PartialUnfolderTest
  extends AbstractAnalysisTest
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
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mPartialUnfolder = new PartialUnfolder(factory, optable);
    mPartialUnfolder.setSourceInfoEnabled(true);
    mImporter = new EFSMSystemImporter(factory, optable);
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    mPartialUnfolder = null;
    mImporter = null;
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/efsm/
   * unfolding01.wmod.
   * </P>
   *
   * <P>
   * All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton before unfolding. The automaton "after" defines the
   * expected result of partial unfolding. In addition, an automaton called
   * "selfloops" may be present, which contains additional updates to be
   * passed as selfloops to the partial unfolder.
   * </P>
   *
   * <P>
   * After running the test, a module containing the result of partial
   * unfolding is saved in {supremica}/logs/results/analysis/efsm/{classname}
   * as a .wmod file for viewing in the IDE.
   * </P>
   */
  public void testUnfolding_1() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding01");
    runPartialUnfolder(module);
  }

  public void testUnfolding_2() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding02");
    runPartialUnfolder(module);
  }

  public void testUnfolding_3() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding03");
    runPartialUnfolder(module);
  }

  public void testUnfolding_4() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding04");
    runPartialUnfolder(module);
  }

  public void testUnfolding_5() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding05");
    runPartialUnfolder(module);
  }

  public void testUnfolding_6() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding06");
    runPartialUnfolder(module);
  }

  public void testUnfolding_7() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding07");
    runPartialUnfolder(module);
  }

  public void testUnfolding_8() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding08");
    runPartialUnfolder(module);
  }

  public void testUnfolding_9() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding09");
    runPartialUnfolder(module);
  }

  public void testUnfolding_10() throws Exception
  {
    final ModuleProxy module = loadModule("tests", "efsm", "unfolding10");
    runPartialUnfolder(module);
  }

  public void testReentrant() throws Exception
  {
    testUnfolding_8();
    testUnfolding_9();
    testUnfolding_10();
    testUnfolding_9();
    testUnfolding_8();
    testUnfolding_9();
    testUnfolding_10();
    testUnfolding_9();
    testUnfolding_8();
    testUnfolding_7();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runPartialUnfolder(final ModuleProxy module)
    throws Exception
  {
    runPartialUnfolder(module, null);
  }

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
    saveModule(resultModuleProxy, module.getName());
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
      final ModuleProxyFactory factory = getModuleProxyFactory();
      return factory.createModuleProxy(module.getName(),
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
    final DocumentManager manager = getDocumentManager();
    final ModuleProxy before = createModule(module, BEFORE, true);
    final EFSMCompiler compiler1 = new EFSMCompiler(manager, before);
    compiler1.setSourceInfoEnabled(true);
    final EFSMSystem system = compiler1.compile(bindings);
    final ModuleProxy selfloops = createModule(module, SELFLOOPS, false);
    if (selfloops != null) {
      final EFSMVariable unfoldedVariable = system.getVariables().get(0);
      final EFSMCompiler compiler2 = new EFSMCompiler(manager, selfloops);
      final EFSMSystem selfloopSystem = compiler2.compile(bindings);
      final EFSMTransitionRelation selfloopTR =
        findTR(selfloopSystem, SELFLOOPS);
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
    fail("The module '" + module.getName() +
         "' does not contain any simple component called '" + name + "'!");
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
  //# Data Members
  private PartialUnfolder mPartialUnfolder;
  private EFSMSystemImporter mImporter;


  //#########################################################################
  //# Class Constants
  private static final String BEFORE = "before";
  private static final String AFTER = "after";
  private static final String SELFLOOPS = "selfloops";
  private static final String RESULT = "result";

}
