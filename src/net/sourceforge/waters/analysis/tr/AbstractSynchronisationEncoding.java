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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TObjectIntIterator;

import java.util.List;

import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public abstract class AbstractSynchronisationEncoding
{

  //#######################################################################
  //# Initialization
  public static AbstractSynchronisationEncoding createEncoding
    (final int[] sizes, final int numStates)
  {
     int size = 0;
    for(int i=0; i< sizes.length; i++) {
      size += AutomatonTools.log2(sizes[i]);
    }
    if (size <= IntSynchronisationEncoding.MAX_BITS) {
      return new IntSynchronisationEncoding(sizes, numStates);
    } else if (size <= LongSynchronisationEncoding.MAX_BITS){
      return new LongSynchronisationEncoding(sizes, numStates);
    } else {
      return new ArraySynchronisationEncoding(sizes, numStates);
    }
  }


  //#######################################################################
  //# Constructor
  AbstractSynchronisationEncoding(final int[] sizes, final int numStates)
  {
    mNumberOfAutomata = sizes.length;
  }

  int getNumberOfAutomata()
  {
    return mNumberOfAutomata;
  }

  //#######################################################################
  //# Access
  public abstract int getStateCode(int[] tuple);

  public abstract void addState(int[] tuple, int code);

  public abstract int getMapSize();

  public abstract List<int[]> getInverseMap();

  public abstract TObjectIntIterator<int[]> iterator();

  public abstract boolean compose(TRPartition partition);

  public int getMemoryEstimate()
  {
    return mNumberOfAutomata*getMapSize();
  }


  //#######################################################################
  //# Data members
  private final int mNumberOfAutomata;

}
