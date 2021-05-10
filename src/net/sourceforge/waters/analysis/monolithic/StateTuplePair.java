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

package net.sourceforge.waters.analysis.monolithic;


/**
 * Representation of a node in the reachability tree for SD Property iii.2
 *
 * It contains a state along with its occurrence image
 *
 * @author Mahvash Baloch
 */

class StateTuplePair
{
    /**  First Statetuple */
    private final StateTuple Tuple1;

    /** Second Statetuple*/
    private final StateTuple Tuple2;


    //#########################################################################
    //# Constructor
    /**
     * Creates a new StateTuple pair.
     * @param  Tuple1 First  Statetuple
     * @param  Tuple2 Second Statetuple
     */
    public StateTuplePair(final StateTuple Tuple1, final StateTuple Tuple2)
    {
	this.Tuple1 = Tuple1;
	this.Tuple2 = Tuple2;
	}

  //#########################################################################
    //# Invocation
    /**
     * returns first state tuple
     * @return state tuple
     */
    public StateTuple getTuple1()
    {
    return Tuple1;
    }

    /**
     * returns Second Statetuple
     * @return state tuple
     */
    public StateTuple getTuple2()
    {
    return Tuple2;
    }

    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((Tuple1 == null) ? 0 : Tuple1.hashCode());
      result = prime * result + ((Tuple2 == null) ? 0 : Tuple2.hashCode());
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
      final StateTuplePair other = (StateTuplePair) obj;
      if (Tuple1 == null) {
        if (other.Tuple1 != null)
          return false;
      } else if (!Tuple1.equals(other.Tuple1))
        return false;
      if (Tuple2 == null) {
        if (other.Tuple2 != null)
          return false;
      } else if (!Tuple2.equals(other.Tuple2))
        return false;
      return true;
    }

  }
