//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import net.sourceforge.waters.analysis.options.OptionMap;

/**
 *
 * @author Benjamin Wheeler
 */
public class OptionTabbedPane extends JTabbedPane implements OptionContainer
{

  public OptionTabbedPane()
  {
    super();
    mTabbedPanes = new HashMap<>();
    optionChildren = new LinkedList<>();
  }

  @Override
  public void commitOptions()
  {
    for (final OptionContainer c : optionChildren) {
      c.commitOptions();
    }
  }

  public void populateOptions(final GUIOptionContext context,
                              final OptionMap map,
                              final String identifier)
  {
    final int catIndex = identifier.indexOf('/');
    if (catIndex != -1) {
      final String title = identifier.substring(0, catIndex);
      OptionTabbedPane pane = mTabbedPanes.get(title);
      if (pane == null) {
        pane = new OptionTabbedPane();
        addTab(title, pane);
        mTabbedPanes.put(title, pane);
        optionChildren.add(pane);
      }
      pane.populateOptions(context, map, identifier.substring(catIndex + 1));
    } else {
      if (!map.hasSubsets()) {
        final OptionListPanel pane = new OptionListPanel(context, map);
        addTab(identifier, pane);
        optionChildren.add(pane);
      } else {
        final OptionGroupPanel pane = new OptionGroupPanel(context, map);
        addTab(identifier, pane);
        optionChildren.add(pane);
      }
    }
  }

  @Override
  public void search(final SearchQuery query)
  {
    final OptionContainer selected = (OptionContainer) getSelectedComponent();
    if (selected != null) selected.search(query);
    for (final OptionContainer c : optionChildren) {
      c.search(query);
    }
  }

  @Override
  public boolean selectOption(final OptionPanel<?> panel)
  {
    for (int t=0; t<getTabCount(); t++) {
      final Component c = getComponentAt(t);
      if (c instanceof OptionContainer
        && ((OptionContainer)c).selectOption(panel)) {
        setSelectedComponent(c);
        return true;
      }
    }
    for (final OptionContainer c : optionChildren) {
      if (c.selectOption(panel)) return true;
    }
    return false;
  }

  private final Map<String, OptionTabbedPane> mTabbedPanes;
  private final List<OptionContainer> optionChildren;

  private static final long serialVersionUID = 5842441972089354096L;

}