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

package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;

import org.supremica.properties.Config;

public class GeometryAbsentException
  extends Exception
{

  //#########################################################################
  //# Constructors
  public GeometryAbsentException(final int numNodes)
  {
    mNumNodes = numNodes;
    mGroupNode = null;
  }

  public GeometryAbsentException(final GroupNodeProxy group)
  {
    mNumNodes = 0;
    mGroupNode = group;
  }


  //#########################################################################
  //# Messages
  @Override
  public String getMessage()
  {
    return getMessage(null);
  }

  public String getMessage(final SimpleComponentProxy comp)
  {
    final StringBuilder buffer = new StringBuilder();
    if (mGroupNode != null) {
      buffer.append("There is no geometry information for group node ");
      buffer.append(mGroupNode.getName());
      buffer.append(" in ");
      appendComponentName(buffer, comp);
      buffer.append('.');
    } else {
      appendComponentName(buffer, comp);
      buffer.append(" has ");
      buffer.append(mNumNodes);
      buffer.append(" states. Graphs with more than ");
      final int max = Config.DOT_MAX_NBR_OF_STATES.getValue();
      buffer.append(max);
      buffer.append(" states cannot be displayed.");
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void appendComponentName(final StringBuilder buffer,
                                   final SimpleComponentProxy comp)
  {
    if (comp != null) {
      final ComponentKind kind = comp.getKind();
      final String name = comp.getName();
      if (name.length() <= 64) {
        buffer.append(ModuleContext.getComponentKindToolTip(kind));
        buffer.append(' ');
        buffer.append(name);
      } else {
        if (buffer.length() > 0) {
          buffer.append("this ");
        } else {
          buffer.append("This ");
        }
        buffer.append(ModuleContext.getComponentKindToolTip(kind));
      }
    } else {
      if (buffer.length() > 0) {
        buffer.append("this graph");
      } else {
        buffer.append("This graph");
      }
    }
  }


  //#########################################################################
  //# Data Members
  private final int mNumNodes;
  private final GroupNodeProxy mGroupNode;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
