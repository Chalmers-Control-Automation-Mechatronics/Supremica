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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

public abstract class StateEncoding implements Serializable
{
  /**
   * From an array of state indices, create an encoded
   * state.
   */
  public abstract StateTuple encodeState (int[] unpacked, int depth);

  /**
   * From a state tuple, decode it into an array of state
   * indices.
   */
  public abstract int[] decodeState(StateTuple packed);

  /**
   * Decode the state for a specified automaton from the 
   * packed state tuple. The default implementation of 
   * this decodes the full state tuple and returns the 
   * appropriate element. State encodings should override
   * this method if a more efficient technique exists.
   * @param packed tuple to extract state from
   * @param automaton to get state for
   * @return state of the given automaton
   */
  public int decodeAutomatonState(StateTuple packed, int automaton)
  {
    int[] unpacked = decodeState(packed);
    return unpacked[automaton];
  }

  /**
   * Interpret the state tuple according to this encoding
   * and return a human readable string representation.
   */
  public abstract String interpret(StateTuple state);
  public abstract String interpret(int[] unpacked);

  public abstract int getEncodedLength();

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
