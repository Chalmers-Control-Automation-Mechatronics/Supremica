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

package net.sourceforge.waters.gui.options;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

import net.sourceforge.waters.model.options.OptionPage;
import net.sourceforge.waters.model.options.OptionPageEditor;


/**
 * Interface for GUI-based implementations of {@link OptionPageEditor}.
 *
 * @author Benjamin Wheeler
 */

interface OptionPagePanel<P extends OptionPage>
  extends OptionPageEditor<P>
{

  /**
   * Casts and returns this option page panel as a {@link JComponent}.
   */
  public JComponent asComponent();

  /**
   * Depending on the type of option page panel, creates a {@link
   * JScrollPane} containing this option page panel or simply casts this
   * option page panel as a {@link JComponent}.
   */
  public JComponent asScrollableComponent();

  /**
   * Updates the state of options within this panel to match what has
   * been entered in their entry components.
   */
  public void commitOptions();

  /**
   * Searches the panel for options matching the given query and
   * updates the query for any found matches.
   */
  public void search(SearchQuery query);

  /**
   * Selects the given option.
   * @param  panel   Option panel representing the option to be selected/
   * @return <CODE>true</CODE> if the option was found and selected
   *         within this option page panel, <CODE>false</CODE> otherwise.
   */
  public boolean selectOption(OptionPanel<?> panel);

}
