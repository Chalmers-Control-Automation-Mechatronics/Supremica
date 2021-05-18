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

import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import net.sourceforge.waters.model.options.AggregatorOptionPage;
import net.sourceforge.waters.model.options.OptionPage;


/**
 *
 * @author Benjamin Wheeler
 */

public class AggregatorOptionPagePanel
  extends JTabbedPane
  implements OptionPagePanel<AggregatorOptionPage>
{

  //#########################################################################
  //# Constructor
  AggregatorOptionPagePanel(final GUIOptionContext context,
                            final AggregatorOptionPage page)
  {
    mSubPanels = new LinkedList<>();
    for (final OptionPage subPage : page.getPages()) {
      final OptionPagePanel<?> editor =
        (OptionPagePanel<?>) subPage.createEditor(context);
      final JComponent panel = editor.asScrollableComponent();
      addTab(subPage.getTitle(), panel);
      mSubPanels.add(editor);
    }
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.gui.options.OptionPagePanel<AggregatorOptionPage>
  @Override
  public JComponent asComponent()
  {
    return this;
  }

  @Override
  public JComponent asScrollableComponent()
  {
    return this;
  }

  @Override
  public void commitOptions()
  {
    for (final OptionPagePanel<?> subPanel : mSubPanels) {
      subPanel.commitOptions();
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    final int t = getSelectedIndex();
    if (t >= 0) {
      final OptionPagePanel<?> selected = mSubPanels.get(t);
      selected.search(query);
      for (final OptionPagePanel<?> subPanel : mSubPanels) {
        if (subPanel != selected) {
          subPanel.search(query);
        }
      }
    }
  }

  @Override
  public boolean scrollToVisible(final OptionPanel<?> option)
  {
    int t = 0;
    for (final OptionPagePanel<?> subPanel : mSubPanels) {
      if (subPanel.scrollToVisible(option)) {
        setSelectedIndex(t);
        return true;
      }
      t++;
    }
    return false;
  }


  //#########################################################################
  //# Data Members
  private final List<OptionPagePanel<?>> mSubPanels;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5842441972089354096L;

}
