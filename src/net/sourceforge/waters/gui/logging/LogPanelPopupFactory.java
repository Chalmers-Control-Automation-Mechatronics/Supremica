//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.logging;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.gui.PopupFactory;
import net.sourceforge.waters.gui.actions.IDEAction;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


/**
 * <P>The popup factory for the {@link LogPanel}.</P>
 *
 * <P>The popup menu contains options to select and deselect, copy, and clear
 * the log, and to change verbosity and capturing options.</P>
 *
 * @author Robi Malik
 */

class LogPanelPopupFactory
  extends PopupFactory
{

  //#########################################################################
  //# Constructor
  LogPanelPopupFactory(final WatersPopupActionManager master)
  {
    super(master);
  }


  //#########################################################################
  //# Shared Menu Items
  @Override
  protected void addDefaultMenuItems()
  {
  }

  @Override
  protected void addItemSpecificMenuItems(final Proxy proxy)
  {
  }

  @Override
  protected void addCommonMenuItems()
  {
    final JPopupMenu popup = getPopup();
    final WatersPopupActionManager master = getMaster();
    final IDEAction copy = master.getCopyAction();
    popup.add(copy);
    final IDEAction select = master.getSelectAllAction();
    popup.add(select);
    final IDEAction deselect = master.getDeselectAllAction();
    popup.add(deselect);
    final IDE ide = master.getIDE();
    final LogPanel panel = ide.getLogPanel();
    if (!panel.isEmpty()) {
      final IDEAction clear = master.getLogClearAction();
      popup.add(clear);
    }
    popup.addSeparator();

    final JMenu subMenu = new JMenu("Verbosity level");
    final EnumOption<IDELogLevel> option = Config.LOG_GUI_VERBOSITY;
    for (final IDELogLevel level : IDELogLevel.getAllowedValuesForLogPanel()) {
      final String comment;
      if (level == IDELogLevel.ALL) {
        comment = "Show all messages";
      } else {
        final String name = level.toString().toLowerCase();
        comment = "Show " + name + " and more severe messages";
      }
      final IDEAction action =
        master.getConfigEnumPropertyAction(option, level, comment);
      final JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
      item.setSelected(option.getValue() == level);
      subMenu.add(item);
    }
    popup.add(subMenu);
    final IDEAction stdoutAction =
      master.getConfigBooleanPropertyAction(Config.GENERAL_REDIRECT_STDOUT,
                                            "Capture stdout");
    final JCheckBoxMenuItem stdoutItem = new JCheckBoxMenuItem(stdoutAction);
    stdoutItem.setSelected(Config.GENERAL_REDIRECT_STDOUT.getValue());
    popup.add(stdoutItem);
    final IDEAction stderrAction =
      master.getConfigBooleanPropertyAction(Config.GENERAL_REDIRECT_STDERR,
                                            "Capture stderr");
    final JCheckBoxMenuItem stderrItem = new JCheckBoxMenuItem(stderrAction);
    stderrItem.setSelected(Config.GENERAL_REDIRECT_STDERR.getValue());
    popup.add(stderrItem);
  }

}
