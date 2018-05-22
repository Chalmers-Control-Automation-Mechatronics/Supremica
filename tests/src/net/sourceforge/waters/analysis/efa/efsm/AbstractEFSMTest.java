//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.efa.efsm.EFSMCompiler;
import net.sourceforge.waters.analysis.efa.efsm.EFSMEventEncoding;
import net.sourceforge.waters.analysis.efa.efsm.EFSMSystem;
import net.sourceforge.waters.analysis.efa.efsm.EFSMSystemImporter;
import net.sourceforge.waters.analysis.efa.efsm.EFSMTransitionRelation;
import net.sourceforge.waters.analysis.efa.efsm.EFSMVariable;
import net.sourceforge.waters.analysis.efa.efsm.EFSMVariableContext;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.AnalysisException;
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
 * Abstract superclass for EFSM related tests.
 * Contains common utilities to convert between modules and EFSM systems.
 *
 * @author Robi Malik
 */

abstract class AbstractEFSMTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractEFSMTest()
  {
  }

  public AbstractEFSMTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ModuleProxyFactory factory = getModuleProxyFactory();
    final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();
    mImporter = new EFSMSystemImporter(factory, optable);
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    mImporter = null;
  }


  //#########################################################################
  //# Auxiliary Methods
  ModuleProxy createModule(final ModuleProxy module,
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

  EFSMSystem createEFSMSystem(final ModuleProxy module,
                              final List<ParameterBindingProxy> bindings)
    throws EvalException, AnalysisException
  {
    final DocumentManager manager = getDocumentManager();
    final ModuleProxy before = createModule(module, BEFORE, true);
    final EFSMCompiler compiler1 = new EFSMCompiler(manager, before);
    compiler1.setSourceInfoEnabled(true);
    compiler1.setOptimizationEnabled(false);
    final EFSMSystem system = compiler1.compile(bindings);
    final ModuleProxy selfloops = createModule(module, SELFLOOPS, false);
    if (selfloops != null) {
      final EFSMVariable unfoldedVariable = system.getVariables().get(0);
      final EFSMCompiler compiler2 = new EFSMCompiler(manager, selfloops);
      compiler2.setOptimizationEnabled(false);
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

  void saveResult(final EFSMTransitionRelation efsmTR,
                  final EFSMSystem system,
                  final ModuleProxy module)
    throws Exception
  {
    final List<EFSMTransitionRelation> list =
      Collections.singletonList(efsmTR);
    efsmTR.setName(RESULT);
    final String name = module.getName();
    final EFSMVariableContext context = system.getVariableContext();
    final EFSMSystem resultSystem =
      new EFSMSystem(name, system.getVariables(), list, context);
    final ModuleProxy resultModuleProxy = mImporter.importModule(resultSystem);
    saveModule(resultModuleProxy, name);
  }

  void compareWithAfter(final EFSMTransitionRelation resultTransitionRelation,
                        final EFSMSystem system,
                        final ModuleProxy module)
  {
    final SimpleComponentProxy expected = findComponent(module, AFTER);
    compareWithAfter(resultTransitionRelation, system, module, expected);
  }

  void compareWithAfter(final EFSMTransitionRelation resultTransitionRelation,
                        final EFSMSystem system,
                        final ModuleProxy module,
                        final SimpleComponentProxy expected)
  {
    resultTransitionRelation.setName(AFTER);
    final String name = module.getName();
    final EFSMVariableContext context = system.getVariableContext();
    final List<EFSMTransitionRelation> list =
      Collections.singletonList(resultTransitionRelation);
    final EFSMSystem afterSystem =
      new EFSMSystem(name, system.getVariables(), list, context);
    final ModuleProxy afterModuleProxy = mImporter.importModule(afterSystem);
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true, false);
    final SimpleComponentProxy result = findComponent(afterModuleProxy, AFTER);
    assertProxyEquals(eq, "Unexpected result", result, expected);
  }

  SimpleComponentProxy findComponent(final ModuleProxy module,
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

  EFSMTransitionRelation findTR(final EFSMSystem system,
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
  private EFSMSystemImporter mImporter;


  //#########################################################################
  //# Class Constants
  static final String BEFORE = "before";
  static final String AFTER = "after";
  static final String SELFLOOPS = "selfloops";
  static final String RESULT = "result";

}
