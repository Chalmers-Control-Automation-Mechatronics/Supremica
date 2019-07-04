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

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * An event in a unified EFA system.
 * This class is used for intermediate events that have been generated
 * as a result of renaming or substitution during compositional verification
 * or synthesis.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class RenamedEFAEvent extends AbstractEFAEvent
{
  //#########################################################################
  //# Constructors
  public RenamedEFAEvent(final AbstractEFAEvent original,
                         final ConstraintList update)
  {
    this(original, update, -1);
  }

  public RenamedEFAEvent(final AbstractEFAEvent original,
                         final ConstraintList update,
                         final int index)
  {
    super(update);
    mOriginalEvent = original;
    mIndex = index;
  }


  //#########################################################################
  //# Simple Access
  public AbstractEFAEvent getOriginalEvent()
  {
    return mOriginalEvent;
  }

  public int getIndex()
  {
    return mIndex;
  }

  public void setIndex(final int index)
  {
    mIndex = index;
  }

  @Override
  public String getName()
  {
    if (mIndex < 0) {
      return mOriginalEvent.getName();
    } else {
      return mOriginalEvent.getName() + ":" + mIndex;
    }
  }

  @Override
  public EventKind getKind()
  {
    return mOriginalEvent.getKind();
  }

  @Override
  public boolean isObservable()
  {
    return mOriginalEvent.isObservable();
  }


  //#########################################################################
  //# Data Members
  private final AbstractEFAEvent mOriginalEvent;
  private int mIndex;

}
