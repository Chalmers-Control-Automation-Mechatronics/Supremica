//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2018 Knut Akesson, Martin Fabian, Robi Malik
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariableContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.algorithms.IISCT.EFAPartialEvaluator;
import org.supremica.automata.algorithms.IISCT.EFASynchronizer;
import org.supremica.gui.ide.IDE;


/**
 * Editor class of the Transition Projection method.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 */

public class EditorEFASynchAndEvalAction
 extends IDEAction
{

  public EditorEFASynchAndEvalAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "EFA Synch & Eval");
    putValue(Action.SHORT_DESCRIPTION, "EFA Synch & Eval");
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
      mHelper = new SimpleEFAHelper();
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      compiler.setMarkingVariablEFAEnable(false);
       final SimpleEFASystem sys = compiler.compile();

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
      } else if (components.size() > 1) {
        for (final SimpleEFAComponent comp : sys.getComponents()) {
          if (components.contains(comp.getName())) {
            compList.add(comp);
          }
        }
      }
      if (!compList.isEmpty()) {

        final long currTime = System.currentTimeMillis();
        final SimpleEFAVariableContext context = sys.getVariableContext();
        final EFAPartialEvaluator pe = new EFAPartialEvaluator(context, sys.getEventEncoding());
        final EFASynchronizer synch = new EFASynchronizer();
        SimpleEFAComponent residual = null;
        for (final Iterator<SimpleEFAComponent> it = compList.iterator(); it.hasNext();) {
          final SimpleEFAComponent com = it.next();
          synch.init(com);
          synch.addComponent(residual);
          synch.synchronize();
          final SimpleEFAComponent syn = synch.getSynchronizedEFA();
          if (!syn.equals(com)) {
            sys.disposeComponent(com);
            sys.disposeComponent(residual);
          } else {
            residual = com;
            continue;
          }
          pe.init(syn);
          if (pe.evaluate()) {
            residual = pe.getResidualComponents().iterator().next();
            sys.disposeComponent(syn);
          } else {
            residual = syn;
          }
          sys.addComponent(residual);
        }

        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("----------------------------------");
        System.err.println("Time: " + elapsed + "ms (" + elapsed / 1000 + "s)");
        System.err.println("No. Synch. Locs: " + residual.getNumberOfStates());
        System.err.println("No. Synch. Events: " + residual.getNumberOfEvents());
        System.err.println("No. Synch. Prime: " + residual.getPrimeVariables().size());
        System.err.println("No. Synch. Unprime: " + residual.getUnprimeVariables().size());
        System.err.println("----------------------------------");
      }

      System.err.println("Start importing ...");
      final ModuleSubject system = (ModuleSubject) mHelper.getModuleProxy(sys);
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, system, module);
      System.err.println("Finish importing ...");
    } catch (EvalException | IOException | OverflowException |
     UnsupportedFlavorException ex) {
      logger.error(ex);
    } catch (final AnalysisException ex) {
      java.util.logging.Logger.getLogger(EditorEFASynchAndEvalAction.class
       .getName())
       .log(Level.SEVERE, null, ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LogManager.getLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;

  private SimpleEFAHelper mHelper;

}
