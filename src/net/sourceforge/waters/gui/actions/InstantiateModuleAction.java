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

package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.CompilationInfo;
import net.sourceforge.waters.model.compiler.groupnode.GroupNodeCompiler;
import net.sourceforge.waters.model.compiler.instance.ModuleInstanceCompiler;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.VariableHelper;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


public class InstantiateModuleAction extends WatersAction
{

  protected InstantiateModuleAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Instantiate");
    putValue(Action.SHORT_DESCRIPTION, "Instantiate the current module");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
    setEnabled(false);
  }

  //#########################################################################
  //# Interface java.awt.ActionListener
  @Override
  public void actionPerformed(final ActionEvent e)
  {
    final IDE ide = getActiveModuleContainer().getIDE();
    final ModuleProxy module = getActiveModuleContainer().getModule();
    final DocumentManager manager = ide.getDocumentManager();
    try {
      final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
      final CompilationInfo compileInfo = new CompilationInfo();
      final ModuleInstanceCompiler compiler =
        new ModuleInstanceCompiler(manager, factory, compileInfo, module);
      compiler.setOptimizationEnabled(true);
      ModuleProxy instantiatedModule = compiler.compile(null);
      final GroupNodeCompiler mGroupNodeCompiler =
        new GroupNodeCompiler(factory, compileInfo, instantiatedModule);
      instantiatedModule = mGroupNodeCompiler.compile();
      final ModuleProxy supremicaEFA = buildEFAFrom(instantiatedModule);
      final DocumentContainerManager cmanager =
        ide.getDocumentContainerManager();
      cmanager.newContainer(supremicaEFA);
    } catch (final EvalException exception) {
      exception.printStackTrace();
    }
  }

  private ModuleProxy buildEFAFrom(final ModuleProxy moduleElement)
  {
    final String mName = moduleElement.getName() + "_instantiated";
    final ModuleSubject module =
      ModuleSubjectFactory.getInstance().createModuleProxy(mName, null);
    module.setComment(moduleElement.getComment());
    final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
    // collect different kinds of events ...
    final Map<String,String> eventToKindMap = new HashMap<String,String>();
    final Map<String,String> prosToKindMap = new HashMap<String,String>();
    for (final EventDeclProxy e : moduleElement.getEventDeclList()) {
      if (e.getKind() != EventKind.PROPOSITION) {
        eventToKindMap.put(e.getName(), e.getKind().name());
        exAutomata.addEvent(e.getName(), e.getKind().name());
      } else {
        prosToKindMap.put(e.getName(), e.getKind().name());
      }
    }
    for (final Proxy p : moduleElement.getComponentList()) {
      if (p instanceof SimpleComponentProxy) {
        // create an extended automaton ...
        final SimpleComponentProxy s = (SimpleComponentProxy) p;
        String exAutName = s.getName();
        if (exAutName.contains("[")) {
          exAutName = exAutName.replace('[', '_')
            .substring(0, s.getName().length() - 1);
        }
        final ExtendedAutomaton exAut =
          new ExtendedAutomaton(exAutName, s.getKind());
        // add blocked events ...
        final GraphProxy graph = s.getGraph();
        final LabelBlockProxy blockEvents = graph.getBlockedEvents();
        if (blockEvents != null) {
          for (final Proxy e : blockEvents.getEventIdentifierList()) {
            exAut.addEvent(e.toString(), eventToKindMap.get(e.toString()));
          }
        }
        // add initial, marked and forbidden states
        for (final NodeProxy n : ((SimpleComponentProxy) p).getGraph()
          .getNodes()) {
          final SimpleNodeProxy state = (SimpleNodeProxy) n;
          final boolean initial = state.isInitial();
          boolean marked = false;
          boolean forbidden = false;
          // do not know how to get marked and forbidden states ...
          final String propositions = state.getPropositions().toString();
          for (final String key : prosToKindMap.keySet()) {
            if (propositions.contains(key)) {
              if (key.equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                forbidden = true;
              if (key.equals(EventDeclProxy.DEFAULT_MARKING_NAME))
                marked = true;
            }
          }
          exAut.addState(state.getName(), marked, initial, forbidden);
        }
        // add transitions ...
        for (final EdgeProxy edge : graph.getEdges()) {
          final String source = edge.getSource().getName();
          final String target = edge.getTarget().getName();
          final List<String> eventList = new ArrayList<String>();
          // get the events in the edge ...
          for (final Proxy e : edge.getLabelBlock()
            .getEventIdentifierList()) {
            eventList.add(e.toString());
          }
          final String label = String.join(";", eventList);
          String guardIn = null;
          String actionIn = null;
          if (edge.getGuardActionBlock() != null) {
            final GuardActionBlockProxy ga = edge.getGuardActionBlock();
            if (ga.getGuards() != null && !ga.getGuards().isEmpty())
              guardIn = ga.getGuards().get(0).toString().replace("[", "_")
                .replace("]", "");
            if (ga.getActions() != null && !ga.getActions().isEmpty()) {
              final List<String> actionList = new ArrayList<String>();
              for (final BinaryExpressionProxy bi : ga.getActions()) {
                actionList
                  .add(bi.toString().replace("[", "_").replace("]", ""));
              }
              actionIn = String.join(";", actionList);
            }
          }
          exAut.addTransition(source, target, label, guardIn, actionIn);
        }
        exAutomata.addAutomaton(exAut);
      } else if (p instanceof VariableComponentProxy) {
        // add each variable component in the extended automata ...
        final VariableComponentProxy var = (VariableComponentProxy) p;
        final String name = var.getName().replace("[", "_").replace("]", "");
        exAutomata
          .addIntegerVariable(name, VariableHelper.getLowerBound(var),
                              VariableHelper.getUpperBound(var),
                              VariableHelper.getInitialIntegerValue(var),
                              VariableHelper.getMarkedIntegerValue(var));
      }
    }
    return exAutomata.getModule();
  }

  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  @Override
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.CONTAINER_SWITCH) {
      updateEnabledStatus();
    }
  }

  //#########################################################################
  //# Auxiliary Methods
  private void updateEnabledStatus()
  {
    boolean enabled = false;
    if (getActiveModuleContainer() != null) {
      final ModuleSubject module = getActiveModuleContainer().getModule();
      if (!module.getConstantAliasList().isEmpty())
        enabled = true;
      else if (!module.getEventAliasList().isEmpty())
        enabled = true;
      else {
        for (final Proxy p : module.getComponentList()) {
          if (p instanceof ForeachProxy || p instanceof InstanceProxy) {
            enabled = true;
            break;
          }
        }
      }
    }
    setEnabled(enabled);
  }

  private static final long serialVersionUID = 1L;

}
