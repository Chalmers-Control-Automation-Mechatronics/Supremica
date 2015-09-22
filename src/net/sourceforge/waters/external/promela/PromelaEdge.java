//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.external.promela;

import java.util.List;

import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class PromelaEdge
{
  private PromelaLabel mLabel;
  private List<SimpleExpressionProxy> mGuards;
  private List<BinaryExpressionProxy> mActions;
  private PromelaNode mStart;
  private final PromelaNode mEnd;

  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label)
  {
    mStart = start;
    mEnd = end;
    mLabel = label;
  }

  /**
   * A constructor for the Promela Edge class. Takes a label block, and optional guard block, and an optional action block
   * @param start The start node the edge is connected to
   * @param end The end node the edge is connected to
   * @param label The label block for this edge
   * @param guards The guard block for this edge, or null
   * @param actions The action block for this edge, or null
   * @author Ethan Duff
   */
  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label, final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions)
  {
    this(start, end, label);
    mGuards = guards;
    mActions = actions;
  }

  public void setLabel(final PromelaLabel label)
  {
    mLabel = label;
  }

  /**
   * A method to check if this object is equal to another object
   * @param o The object to compare against
   * @return True if the other object is an equal promela edge. <br>
   * Uses value typed equality, and treats all labels as being equal
   * @author Ethan Duff
   */
  @Override
  public boolean equals(final Object o)
  {
    if(!(o instanceof PromelaEdge))
    {
      //Is not a promela edge, so is not equal
      return false;
    }
    final PromelaEdge edge = (PromelaEdge) o;
    if(!(edge.getSource() == this.getSource() && edge.getTarget() == this.getTarget()))
    {
      //Has different starting or ending node, so is not equal
      return false;
    }

    if(this.equalGuards(edge) && this.equalActions(edge))
    {
      //The guards and actions are equal, so is considered an equal edge
      return true;
    }
    else
    {
      //The guards or actions are not equal, so is not equal
      return false;
    }
  }

  /**
   * A method to check if the guards of this edge are equal to the guards of another edge
   * @param other The edge to compare against
   * @return True if the guards are equal, false otherwise
   * @author Ethan Duff
   */
  private boolean equalGuards(final PromelaEdge other)
  {
    final ModuleEqualityVisitor comparitor = new ModuleEqualityVisitor(false);

    if(other.getGuards() == null && this.getGuards() == null)
    {
      //The guard edges are both null
      return true;
    }
    else if(other.getGuards() != null && this.getGuards() != null)
    {
      if(comparitor.isEqualCollection(other.getGuards(), this.getGuards()))
      {
        //The guard edges are equal
        return true;
      }
      else
      {
        //The guard edges are not equal
        return false;
      }
    }
    else
    {
      //The guard blocks are different, so are not equal
      return false;
    }
  }

  /**
   * A method to check if the actions of this edge are equal to the actions of another edge
   * @param other The edge to compare against
   * @return True if the actions are equal, false otherwise
   * @author Ethan Duff
   */
  private boolean equalActions(final PromelaEdge other)
  {
    final ModuleEqualityVisitor comparitor = new ModuleEqualityVisitor(false);

    if(other.getActions() == null && this.getActions() == null)
    {
      //The action edges are both null, so are considered equal
      return true;
    }
    else if(other.getActions() != null && this.getActions() != null)
    {
      if(comparitor.isEqualCollection(other.getActions(), this.getActions()))
      {
        //The action blocks are the same, so are considered equal
        return true;
      }
      else
      {
        //The action blocks are different, so are not equal
        return false;
      }
    }
    else
    {
      return false;
    }
  }

  public PromelaLabel getLabelBlock()
  {
    return mLabel;
  }

  public List<SimpleExpressionProxy> getGuards()
  {
    return mGuards;
  }

  public List<BinaryExpressionProxy> getActions()
  {
    return mActions;
  }

  public void setStart(final PromelaNode start)
  {
    mStart = start;
  }
  public PromelaNode getSource()
  {
    return mStart;
  }
  public PromelaNode getTarget()
  {
    return mEnd;
  }
}








