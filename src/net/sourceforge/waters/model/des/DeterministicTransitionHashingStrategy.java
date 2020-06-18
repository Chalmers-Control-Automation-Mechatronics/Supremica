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

package net.sourceforge.waters.model.des;

import gnu.trove.strategy.HashingStrategy;


/**
 * A GNU Trove hashing strategy for transitions in deterministic automata.
 * Considers transitions as equal if they have the same source states
 * and events, where states and events are compared by object identity.
 *
 * @author Robi Malik
 */

public class DeterministicTransitionHashingStrategy
  implements HashingStrategy<TransitionProxy>
{

  //#######################################################################
  //# Singleton Pattern
  public static HashingStrategy<TransitionProxy> getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final DeterministicTransitionHashingStrategy INSTANCE =
      new DeterministicTransitionHashingStrategy();
  }


  //#######################################################################
  //# Interface gnu.trove.TObjectHashingStrategy
  @Override
  public int computeHashCode(final TransitionProxy trans)
  {
    return trans.getSource().hashCode() + 5 * trans.getEvent().hashCode();
  }

  @Override
  public boolean equals(final TransitionProxy trans1,
                        final TransitionProxy trans2)
  {
    return trans1.getSource() == trans2.getSource() &&
           trans1.getEvent() == trans2.getEvent();
  }


  //#######################################################################
  //# Class Constants
  private static final long serialVersionUID = -1202406837488677255L;

}
