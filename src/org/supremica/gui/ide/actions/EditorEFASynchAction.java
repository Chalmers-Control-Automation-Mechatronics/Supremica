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

import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.automata.algorithms.IISCT.EFASynchronizer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

import gnu.trove.set.hash.THashSet;

/**
 * Editor class of the Transition Projection method.
 * <p/>
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 */
public class EditorEFASynchAction
 extends IDEAction
{

  public EditorEFASynchAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "EFA Synchronization");
    putValue(Action.SHORT_DESCRIPTION, "EFA Synchronization");
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
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      final SimpleEFASystem sys = compiler.compile();

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection
       = ide.getActiveDocumentContainer().getEditorPanel()
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
      mHelper = new SimpleEFAHelper();
      final List<ComponentProxy> list = new ArrayList<>();
      if (!compList.isEmpty()) {
        final long currTime = System.currentTimeMillis();
        final EFASynchronizer synch = new EFASynchronizer(EFASynchronizer.MODE_IISCT);
        synch.init(compList);
        synch.synchronize();
        final SimpleEFAComponent result = synch.getSynchronizedEFA();
        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("----------------------------------");
        System.err.println("Time: " + elapsed + "ms (" + elapsed / 1000 + "s)");
        System.err.println("No. Synch. Locs: " + result.getNumberOfStates());
        System.err.println("No. Synch. Events: " + result.getNumberOfEvents());
        System.err.println("----------------------------------");
        System.err.println("Finish synchronizing ...");
        list.add(mHelper.getSimpleComponentProxy(result));
      }

      System.err.println("Start importing ...");
      final ModuleWindowInterface root = (ModuleWindowInterface) ide.getIDE().
       getActiveDocumentContainer().getActivePanel();
      mHelper.importToIDE(root, module, list);
      System.err.println("Finish importing ...");
    } catch (AnalysisException | EvalException | IOException | UnsupportedFlavorException ex) {
      logger.error(ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LoggerFactory.createLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;
  private SimpleEFAHelper mHelper;

}
