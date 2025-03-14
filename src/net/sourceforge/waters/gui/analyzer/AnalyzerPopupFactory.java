//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import javax.swing.JPopupMenu;

import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.WatersOptionPages;


class AnalyzerPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  AnalyzerPopupFactory(final WatersPopupActionManager master)
  {
    super(master);
  }


  //#########################################################################
  //# Shared Menu Items
  @Override
  protected void addMenuItems(final Proxy proxy)
  {
    super.addMenuItems(proxy);
    final WatersPopupActionManager master = getMaster();
    final JPopupMenu popup = getPopup();
    popup.addSeparator();
    final IDEAction synchronous = master.getAnalyzerSynchronousProductAction();
    popup.add(synchronous);
    final IDEAction synthesis = master.getAnalyzerSynthesizerAction();
    popup.add(synthesis);
    final IDEAction workbench = master.getAnalyzerWorkbenchAction();
    popup.add(workbench);
    popup.addSeparator();
    final IDEAction controllability =
      master.getAnalyzerControllabilityCheckAction();
    popup.add(controllability);
    final IDEAction conflict = master.getAnalyzerConflictCheckAction();
    popup.add(conflict);
    final IDEAction deadlock = master.getAnalyzerDeadlockCheckAction();
    popup.add(deadlock);
    final IDEAction controlLoop = master.getAnalyzerControlLoopCheckAction();
    popup.add(controlLoop);
    final IDEAction languageInclusion =
      master.getAnalyzerLanguageInclusionCheckAction();
    popup.add(languageInclusion);
    final EnumOption<ModelAnalyzerFactoryLoader> diagnosabilityOption =
      WatersOptionPages.DIAGNOSABILITY.getTopSelectorOption();
    if (diagnosabilityOption.getValue() != ModelAnalyzerFactoryLoader.Disabled) {
      final IDEAction diagnosability =
        master.getAnalyzerDiagnosabilityCheckAction();
      popup.add(diagnosability);
    }
    popup.addSeparator();
    if (proxy != null && proxy instanceof AutomatonProxy) {
      final AutomatonProxy aut = (AutomatonProxy) proxy;
      final IDEAction hide = master.getAnalyzerHideAction(aut);
      popup.add(hide);
      final IDEAction simplify = master.getAnalyzerSimplificationAction(aut);
      popup.add(simplify);
    }
    final IDEAction stateCounter = master.getAnalyzerStateCountAction();
    popup.add(stateCounter);
  }

}
