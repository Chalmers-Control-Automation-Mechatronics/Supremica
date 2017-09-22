//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide.actions;

import gnu.trove.set.hash.THashSet;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.supremica.automata.algorithms.IISCT.EFAPartialEvaluator;

/**
 * Editor class of the Transition Projection method.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 */
public class EditorEFAPEAction
 extends IDEAction
{

  public EditorEFAPEAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "Partial Evaluation");
    putValue(Action.SHORT_DESCRIPTION, "Partial Evaluation");
  }

  @Override
  public void actionPerformed(final ActionEvent e)
  {
    doAction();
  }

  @Override
  public void doAction()
  {
    try {
      final ModuleSubject module = ide.getActiveDocumentContainer()
       .getEditorPanel().getModuleSubject();
      if (module.getComponentList().isEmpty()) {
        return;
      }
      System.err.println("Start compiling ...");
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      compiler.setMarkingVariablEFAEnable(false);
      final SimpleEFASystem sys = compiler.compile();
      System.err.println("Finish compiling ...");

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection =
       ide.getActiveDocumentContainer().getEditorPanel()
       .getComponentsPanel().getCurrentSelection();

      final THashSet<String> components = new THashSet<>();
      for (final Proxy item : currentSelection) {
        if (item instanceof SimpleComponentSubject) {
          components.add(((SimpleComponentSubject) item).getName());
        }
      }

      if (components.isEmpty()) {
        compList.addAll(sys.getComponents());
      } else if (components.size() > 0) {
        for (final SimpleEFAComponent comp : sys.getComponents()) {
          if (components.contains(comp.getName())) {
            compList.add(comp);
          }
        }
      }
      if (!compList.isEmpty()) {
        System.err.println("Start evaluating ...");
        final SimpleEFAVariableContext context = sys.getVariableContext();
        final EFAPartialEvaluator pe = new EFAPartialEvaluator(context, sys.getEventEncoding());
        pe.init(compList);
        final boolean success = pe.evaluate();
        System.err.println("Finish evaluating ...");
        if (success) {
          final Collection<SimpleEFAComponent> residuals = pe.getResidualComponents();
          final Collection<SimpleEFAVariable> vars = pe.getEvaluatedVariables();
          for (final SimpleEFAVariable var : vars) {
            System.err.println("Evaluated: " + var.getName());
          }
          for (final SimpleEFAComponent res : residuals) {
            sys.addComponent(res);
          }
        }
      }

      System.err.println("Start importing ...");
      mHelper = new SimpleEFAHelper();
      final ModuleSubject system = (ModuleSubject) mHelper.getModuleProxy(sys);
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, system, module);
      System.err.println("Finish importing ...");
    } catch (AnalysisException | EvalException | IOException |
             UnsupportedFlavorException ex) {
      final Logger logger = LogManager.getLogger(getClass());
      final String msg = ex.getMessage() == null ? "" : ex.getMessage();
      logger.error(ProxyTools.getShortClassName(ex) + " caught in " +
                   ProxyTools.getShortClassName(this) + ": " + msg);
    }
  }

  //#########################################################################
  //# Class Constants
  private SimpleEFAHelper mHelper;
  private static final long serialVersionUID = 1008047793917621873L;

}
