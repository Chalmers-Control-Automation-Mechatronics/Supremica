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

package net.sourceforge.waters.analysis.sd;

import java.util.BitSet;

import net.sourceforge.waters.analysis.monolithic.StateTuple;

/**
 * Representation of a node in the reachability tree for SD Property iii.2
 *
 * It contains a state along with its occurrence image
 *
 * @author Mahvash Baloch
 */

class Node
{
    /** State tuple */
    private final StateTuple sTuple;

    /** Occurrence Image of the string associated with the State*/
    private final BitSet Occu;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new Node. It creates a node for the tree from given parameters
     * @param  sTuple State tuple
     * @param  events the occurrence Image of the string
     */
    public Node(final StateTuple sTuple, final BitSet events)
    {
	this.sTuple = sTuple;
	this.Occu = events;
	}

  //#########################################################################
    //# Invocation
    /**
     * returns state tuple
     * @return state tuple
     */
    public StateTuple getTuple()
    {
    return sTuple;
    }

    /**
     * returns Occurrence Image
     * @return Occurrence Image
     */
    public BitSet getOccu()
    {
    return Occu;
    }

    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((Occu == null) ? 0 : Occu.hashCode());
      result = prime * result + ((sTuple == null) ? 0 : sTuple.hashCode());
      return result;
    }

    public boolean equals(final Object obj)
    {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Node other = (Node) obj;
      if (Occu == null) {
        if (other.Occu != null)
          return false;
      } else if (!Occu.equals(other.Occu))
        return false;
      if (sTuple == null) {
        if (other.sTuple != null)
          return false;
      } else if (!sTuple.equals(other.sTuple))
        return false;
      return true;
    }


  }
