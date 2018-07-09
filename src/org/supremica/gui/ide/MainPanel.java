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

package org.supremica.gui.ide;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.sourceforge.waters.gui.simulator.SimulatorPanel;

import org.supremica.gui.WhiteScrollPane;


/**
 * <P>An IDE panel displayed in a {@link ModuleContainer}.</P>
 *
 * <P>The main panel consists of a {@link JSplitPane}, which is common to
 * all module views. Subclasses implement the specifics for {@link EditorPanel},
 * {@link SimulatorPanel}, and {@link SupremicaAnalyzerPanel}.</P>
 *
 * @author Knut &Aring;kesson, Robi Malik
 */

public abstract class MainPanel extends JPanel
{

  //#########################################################################
  //# Constructor
  public MainPanel(final String name)
  {
    mName = name;

    final GridBagLayout gridbag = new GridBagLayout();
    setLayout(gridbag);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;

    mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
    mSplitPane.setOneTouchExpandable(false);
    mSplitPane.setOneTouchExpandable(false);
    mSplitPane.setResizeWeight(0.0);
    gridbag.setConstraints(mSplitPane, constraints);
    add(mSplitPane);

    mEmptyRightPanel = new WhiteScrollPane();
  }


  //#########################################################################
  //# Simple Access
  @Override
  public String getName()
  {
    return mName;
  }

  protected void setLeftComponent(final JComponent newComponent)
  {
    if (newComponent != mSplitPane.getLeftComponent()) {
      final int dividerLocation = mSplitPane.getDividerLocation();
      mSplitPane.setLeftComponent(newComponent);
      mSplitPane.setDividerLocation(dividerLocation);
    }
  }

  protected boolean setRightComponent(JComponent newComponent)
  {
    if (newComponent != getRightComponent()) {
      final int dividerLocation = mSplitPane.getDividerLocation();
      if (newComponent == null) {
        newComponent = getEmptyRightPanel();
      }
      mSplitPane.setRightComponent(newComponent);
      mSplitPane.setDividerLocation(dividerLocation);
      validate();
      return true;
    } else {
      return false;
    }
  }

  public JComponent getRightComponent()
  {
    return (JComponent) mSplitPane.getRightComponent();
  }

  public JScrollPane getEmptyRightPanel()
  {
    return mEmptyRightPanel;
  }


  //#########################################################################
  //# Focus Switching
  /**
   * Activates this panel. This method is called after the panel
   * has been selected. It typically requests the focus for one of its
   * controls.
   */
  protected void activate()
  {
  }

  /**
   * Deactivates this panel. This method is called before the container
   * becomes inactive due to selection of another one.
   */
  protected void deactivate()
  {
  }


  //#########################################################################
  //# Data Members
  private final JSplitPane mSplitPane;
  private final JScrollPane mEmptyRightPanel;
  private final String mName;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5936312918878411125L;

}
