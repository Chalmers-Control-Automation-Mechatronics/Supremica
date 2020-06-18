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

package net.sourceforge.waters.gui.simulator;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import org.supremica.gui.ide.ModuleContainer;

public class AutomatonLeafNode extends DefaultMutableTreeNode
{

  //#########################################################################
  //# Constructor
  public AutomatonLeafNode(final AutomatonProxy aut,
                           final StateProxy overloadedState,
                           final int time)
  {
    super(aut.getName(), false);
    mAutomaton = aut;
    mState = overloadedState;
    mTime = time;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }

  StateProxy getOverloadedState()
  {
    return mState;
  }

  int getTime()
  {
    return mTime;
  }

  Icon getAutomatonIcon(final Simulation sim)
  {
    return getAutomatonIcon(sim, mAutomaton);
  }


  //#########################################################################
  //# Static Methods
  static Icon getAutomatonIcon(final Simulation sim, final AutomatonProxy aut)
  {
    final ModuleContainer container = sim.getModuleContainer();
    final Map<Object,SourceInfo> infomap = container.getSourceInfoMap();
    final SourceInfo info = infomap.get(aut);
    if (info == null) {
      return null;
    } else if (info.getSourceObject() instanceof VariableComponentProxy) {
      return IconAndFontLoader.ICON_VARIABLE;
    } else {
      final ComponentKind kind = aut.getKind();
      return ModuleContext.getComponentKindIcon(kind);
    }
  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final StateProxy mState;
  private final int mTime;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4785226183311677790L;

}
