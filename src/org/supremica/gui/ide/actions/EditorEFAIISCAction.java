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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFACompiler;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAComponent;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFASystem;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.algorithms.IISCT.IISCT;
import org.supremica.automata.algorithms.IISCT.SMTSolver.Z3Solver;
import org.supremica.gui.ide.IDE;


/**
 * Editor class of the Transition Projection method.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 */

public class EditorEFAIISCAction
 extends IDEAction
{

  public EditorEFAIISCAction(final List<IDEAction> actionList)
  {
    super(actionList);

    setEditorActiveRequired(true);

    putValue(Action.NAME, "IISC");
    putValue(Action.SHORT_DESCRIPTION, "Incremental, Inductive Supervisory Control");
    putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/TranProj16.gif")));
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
      final ModuleSubject module
       = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();
      if (module.getComponentList().isEmpty()) {
        final Z3Solver z = new Z3Solver();
        z.run();
        return;
      }
      final SimpleEFACompiler compiler = new SimpleEFACompiler(module);
      final SimpleEFASystem sys = compiler.compile();

      final List<SimpleEFAComponent> compList = new ArrayList<>();
      final List<? extends Proxy> currentSelection
       = ide.getActiveDocumentContainer().getEditorPanel().getComponentsPanel().getCurrentSelection();
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
        final IISCT iisct = new IISCT(sys);
        final long currTime = System.currentTimeMillis();
        iisct.run();
        final long elapsed = System.currentTimeMillis() - currTime;
        System.err.println("time: " + elapsed + "ms");
      }
    } catch (final Exception ex) {
       logger.error(ex);
    }
  }

  //#########################################################################
  //# Class Constants
  private static final Logger logger = LogManager.getLogger(IDE.class);
  private static final long serialVersionUID = -4108158304486885027L;
}
