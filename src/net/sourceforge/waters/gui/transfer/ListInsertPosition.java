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

package net.sourceforge.waters.gui.transfer;

import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * A position record for subjects inserted or deleted in a list.
 * It contains a reference to a list and an index in that list.
 * This information is stored as <I>insert position</I> in a
 * {@link InsertInfo} object.
 *
 * @author Robi Malik
 */

public class ListInsertPosition {

  //#########################################################################
  //# Constructor
  public ListInsertPosition(final ListSubject<? extends ProxySubject> list,
                            final int pos)
  {
    mList = list;
    mPosition = pos;
  }


  //#######################################################################
  //# Simple Access
  public ListSubject<? extends ProxySubject> getList()
  {
    return mList;
  }

  public int getPosition()
  {
    return mPosition;
  }


  //#######################################################################
  //# Data Members
  private final ListSubject<? extends ProxySubject> mList;
  private final int mPosition;

}
